package com.github.stuartwdouglas.kubeman;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Path("containers")
@Blocking
@Produces(MediaType.APPLICATION_JSON)
public class ContainersResource {

    public static final String DOCKER_CONTAINER_ID = "docker-container-id";
    public static final String DOCKER_CONTAINER_NAME = "docker-container-name";
    //TODO: huge hack, should just query stuff from the API
    private static Map<String, CreateRequest> containers = new HashMap<>();

    @Inject
    KubernetesClient client;


    private static String generateId() {
        Random r = new Random();
        byte[] data = new byte[50];
        r.nextBytes(data);
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encodedhash = digest.digest(data);
        return bytesToHex(encodedhash).substring(30);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @POST
    @Path("create")
    public CreateResponse create(@QueryParam("name") String name, CreateRequest request) {
        System.out.println(request);
        String id = generateId();
        if (name == null) {
            name = "auto-generated" + id;
        }
        request.name = name;
        Pod pod = new Pod();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(name);
        objectMeta.setLabels(Map.of("docker-emulation", "true", DOCKER_CONTAINER_NAME, name, DOCKER_CONTAINER_ID, id));

        PodSpec spec = new PodSpec();
        Container container = new Container();
        container.setName(name);
        container.setImage(request.Image);

        List<ContainerPort> ports = new ArrayList<>();

        container.setPorts(ports);

        container.setArgs(request.Cmd);
        if (request.Env != null) {
            container.setEnv(request.Env.stream().map(s -> {
                String[] parts = s.split("=");
                return new EnvVar(parts[0], parts[1], null);
            }).collect(Collectors.toList()));
        }
        spec.setContainers(Collections.singletonList(container));

        pod.setSpec(spec);


        pod.setMetadata(objectMeta);
        client.pods().create(pod);
        containers.put(id, request);
        return new CreateResponse(id, List.of());
    }

    @POST
    @Path("{id}/start")
    public void start(String id) {
        System.out.println("START" + id);
        var request = containers.get(id);
        PodResource<Pod> logResource = client.pods().withName(request.name);
        for (var port : request.ExposedPorts.entrySet()) {
            if (port.getKey().endsWith("/tcp")) {
                int containerPort = Integer.parseInt(port.getKey().substring(0, port.getKey().indexOf("/")));

                var forward = logResource.portForward(containerPort);
                port.setValue(Map.of("binding", "" + forward.getLocalPort()));
            }
        }
    }

    @GET
    @Path("{id}/json")
    public String inspect(String id) {
        var request = containers.get(id);
        System.out.println("INSPECT" + id);
        //huge hack
        StringBuilder sb = new StringBuilder();
        sb.append("{\"Id\":\"");
        sb.append(id);
        sb.append("\",\"Created\":\"2022-02-04T02:16:34.677378614Z\",\"Path\":\"docker-entrypoint.sh\",\"Args\":[\"postgres\",\"-c\",\"fsync=off\"],\"State\":{\"Status\":\"running\",\"Running\":true,\"Paused\":false,\"Restarting\":false,\"OOMKilled\":false,\"Dead\":false,\"Pid\":1452012,\"ExitCode\":0,\"Error\":\"\",\"StartedAt\":\"2022-02-04T02:16:34.934953937Z\",\"FinishedAt\":\"0001-01-01T00:00:00Z\"},\"Image\":\"sha256:e94a3bb612246f1f672a0d11fbd16415e2f95d308b37d38deaa8c2bd3c0116d8\",\"ResolvConfPath\":\"/var/lib/docker/containers/2f380e4741a47102150577b26abd03b477d386db971c8e7e499dc40667b4cb37/resolv.conf\",\"HostnamePath\":\"/var/lib/docker/containers/2f380e4741a47102150577b26abd03b477d386db971c8e7e499dc40667b4cb37/hostname\",\"HostsPath\":\"/var/lib/docker/containers/2f380e4741a47102150577b26abd03b477d386db971c8e7e499dc40667b4cb37/hosts\",\"LogPath\":\"/var/lib/docker/containers/2f380e4741a47102150577b26abd03b477d386db971c8e7e499dc40667b4cb37/2f380e4741a47102150577b26abd03b477d386db971c8e7e499dc40667b4cb37-json.log\",\"Name\":\"/amazing_wescoff\",\"RestartCount\":0,\"Driver\":\"overlay2\",\"Platform\":\"linux\",\"MountLabel\":\"\",\"ProcessLabel\":\"\",\"AppArmorProfile\":\"\",\"ExecIDs\":null,\"HostConfig\":{\"Binds\":[],\"ContainerIDFile\":\"\",\"LogConfig\":{\"Type\":\"json-file\",\"Config\":{}},\"NetworkMode\":\"default\"," +
                "\"PortBindings\":{\"5432/tcp\":[{\"HostIp\":\"\",\"HostPort\":\"\"}]},\"RestartPolicy\":{\"Name\":\"\",\"MaximumRetryCount\":0},\"AutoRemove\":false,\"VolumeDriver\":\"\",\"VolumesFrom\":[],\"CapAdd\":null,\"CapDrop\":null,\"CgroupnsMode\":\"host\",\"Dns\":null,\"DnsOptions\":null,\"DnsSearch\":null,\"ExtraHosts\":[],\"GroupAdd\":null,\"IpcMode\":\"shareable\",\"Cgroup\":\"\",\"Links\":null,\"OomScoreAdj\":0,\"PidMode\":\"\",\"Privileged\":false,\"PublishAllPorts\":false,\"ReadonlyRootfs\":false,\"SecurityOpt\":null,\"UTSMode\":\"\",\"UsernsMode\":\"\",\"ShmSize\":67108864,\"Runtime\":\"runc\",\"ConsoleSize\":[0,0],\"Isolation\":\"\",\"CpuShares\":0,\"Memory\":0,\"NanoCpus\":0,\"CgroupParent\":\"\",\"BlkioWeight\":0,\"BlkioWeightDevice\":null,\"BlkioDeviceReadBps\":null,\"BlkioDeviceWriteBps\":null,\"BlkioDeviceReadIOps\":null,\"BlkioDeviceWriteIOps\":null,\"CpuPeriod\":0,\"CpuQuota\":0,\"CpuRealtimePeriod\":0,\"CpuRealtimeRuntime\":0,\"CpusetCpus\":\"\",\"CpusetMems\":\"\",\"Devices\":null,\"DeviceCgroupRules\":null,\"DeviceRequests\":null,\"KernelMemory\":0,\"KernelMemoryTCP\":0,\"MemoryReservation\":0,\"MemorySwap\":0,\"MemorySwappiness\":null,\"OomKillDisable\":false,\"PidsLimit\":null,\"Ulimits\":null,\"CpuCount\":0,\"CpuPercent\":0,\"IOMaximumIOps\":0,\"IOMaximumBandwidth\":0,\"MaskedPaths\":[\"/proc/asound\",\"/proc/acpi\",\"/proc/kcore\",\"/proc/keys\",\"/proc/latency_stats\",\"/proc/timer_list\",\"/proc/timer_stats\",\"/proc/sched_debug\",\"/proc/scsi\",\"/sys/firmware\"],\"ReadonlyPaths\":[\"/proc/bus\",\"/proc/fs\",\"/proc/irq\",\"/proc/sys\",\"/proc/sysrq-trigger\"]},\"GraphDriver\":{\"Data\":{\"LowerDir\":\"/var/lib/docker/overlay2/b958fa887de4c2682221044d61e04b306fbece31582da53fdbb603159b20e335-init/diff:/var/lib/docker/overlay2/5669b53b469fc29786be30e9fd90c2a0573fa5b0af5e9ec86847f240f6ebcd88/diff:/var/lib/docker/overlay2/2efbbaba203e086992397e37e3684acafa770bf5a771cef9a9f161d3c362c671/diff:/var/lib/docker/overlay2/be97b70be1c8dc1248f68517b5821f584b3b812158faa981362a888706434b3c/diff:/var/lib/docker/overlay2/e513222fa269966fe5a28cf43892fef2c223fd2048c7af9c0cac1e61dee76877/diff:/var/lib/docker/overlay2/8c3a5a6dcc0e8aecc5dbcf94514f2317934aa8ec330a868a224df0defa7d4fe3/diff:/var/lib/docker/overlay2/f41bda7aabbb02e163d3ceaff08958b93701a784bc3dccc1c5813e0e8b59d278/diff:/var/lib/docker/overlay2/90f34a447e5d8e260fccb141e68a0f37eeb1e64647d1215248d030965d2fc596/diff:/var/lib/docker/overlay2/332002fc1d520bd39c0076ed38882a55a66aa946d129e15f036fd90ced1b99da/diff:/var/lib/docker/overlay2/8cfb9835b77e989a773f1517a756faa5fe99fce3ce26b7aa65f04b35f7995412/diff:/var/lib/docker/overlay2/fabc858b50ea6dbf33688a173380d4764be7a300b0ae345f8d7e23fc5e22433b/diff:/var/lib/docker/overlay2/06acb3f3ae212f592f0b138134eb5e6e12d06eb00ea700e1bde10dfc95544ee8/diff:/var/lib/docker/overlay2/a0c4eb14aa5b6aa0449576259a1c7b71d077edbd08a096c235f66de1b8566fea/diff:/var/lib/docker/overlay2/c9314f8688317a15c70d594fa5f465e042e58919e26e0b31a40a29389f2e1992/diff\",\"MergedDir\":\"/var/lib/docker/overlay2/b958fa887de4c2682221044d61e04b306fbece31582da53fdbb603159b20e335/merged\",\"UpperDir\":\"/var/lib/docker/overlay2/b958fa887de4c2682221044d61e04b306fbece31582da53fdbb603159b20e335/diff\",\"WorkDir\":\"/var/lib/docker/overlay2/b958fa887de4c2682221044d61e04b306fbece31582da53fdbb603159b20e335/work\"},\"Name\":\"overlay2\"},\"Mounts\":[{\"Type\":\"volume\",\"Name\":\"99a654fa8236242d3e87f888c7c3b8d1f2f20695e2a7a8b9a26a75674ef4411a\",\"Source\":\"/var/lib/docker/volumes/99a654fa8236242d3e87f888c7c3b8d1f2f20695e2a7a8b9a26a75674ef4411a/_data\",\"Destination\":\"/var/lib/postgresql/data\",\"Driver\":\"local\",\"Mode\":\"\",\"RW\":true,\"Propagation\":\"\"}],\"Config\":{\"Hostname\":\"2f380e4741a4\",\"Domainname\":\"\",\"User\":\"\",\"AttachStdin\":false,\"AttachStdout\":false,\"AttachStderr\":false,\"ExposedPorts\":{\"5432/tcp\":{}},\"Tty\":false,\"OpenStdin\":false,\"StdinOnce\":false,\"Env\":[\"POSTGRES_USER=quarkus\",\"POSTGRES_PASSWORD=quarkus\",\"POSTGRES_DB=default\",\"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/postgresql/14/bin\",\"GOSU_VERSION=1.14\",\"LANG=en_US.utf8\",\"PG_MAJOR=14\",\"PG_VERSION=14.1-1.pgdg110+1\",\"PGDATA=/var/lib/postgresql/data\"],\"Cmd\":[\"postgres\",\"-c\",\"fsync=off\"],\"Image\":\"docker.io/postgres:14.1\",\"Volumes\":{\"/var/lib/postgresql/data\":{}},\"WorkingDir\":\"\",\"Entrypoint\":[\"docker-entrypoint.sh\"],\"OnBuild\":null,\"Labels\":{\"org.testcontainers\":\"true\",\"org.testcontainers.sessionId\":\"bde12f55-4cc2-4085-affa-534f5518a691\"},\"StopSignal\":\"SIGINT\"},\"NetworkSettings\":{\"Bridge\":\"\",\"SandboxID\":\"04904cb68309d691c098225c7f733a5c6ae2c20641e166f08ccbe509caa8aca3\",\"HairpinMode\":false,\"LinkLocalIPv6Address\":\"\",\"LinkLocalIPv6PrefixLen\":0,"
        );
        sb.append("\"Ports\":{");
        boolean first = true;
        for (var port : request.ExposedPorts.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(String.format("\"%s\":[{\"HostIp\":\"0.0.0.0\",\"HostPort\":\"%s\"}]", port.getKey(), port.getValue().get("binding")));
        }
        sb.append("},");

        sb.append("\"SandboxKey\":\"/var/run/docker/netns/04904cb68309\",\"SecondaryIPAddresses\":null,\"SecondaryIPv6Addresses\":null,\"EndpointID\":\"e73c33cd54514bf4ce458d1c6c3907a2eb3ba705007142bafe6b5e4b9a6eca56\",\"Gateway\":\"172.17.0.1\",\"GlobalIPv6Address\":\"\",\"GlobalIPv6PrefixLen\":0,\"IPAddress\":\"172.17.0.4\",\"IPPrefixLen\":16,\"IPv6Gateway\":\"\",\"MacAddress\":\"02:42:ac:11:00:04\",\"Networks\":{\"bridge\":{\"IPAMConfig\":null,\"Links\":null,\"Aliases\":null,\"NetworkID\":\"09531449f7966d9b964a855c4943befc3b93cd44c857f39798e4b0689a286d76\",\"EndpointID\":\"e73c33cd54514bf4ce458d1c6c3907a2eb3ba705007142bafe6b5e4b9a6eca56\",\"Gateway\":\"172.17.0.1\",\"IPAddress\":\"172.17.0.4\",\"IPPrefixLen\":16,\"IPv6Gateway\":\"\",\"GlobalIPv6Address\":\"\",\"GlobalIPv6PrefixLen\":0,\"MacAddress\":\"02:42:ac:11:00:04\",\"DriverOpts\":null}}}}");
        return sb.toString();
    }

    @GET
    @Path("{id}/logs")
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<byte[]> logs(@PathParam("id") String id, @QueryParam("stdout") boolean stdout, @QueryParam("stderr") boolean stderr, @QueryParam("follow") boolean follow, @QueryParam("since") int since) {
        var request = containers.get(id);
        PodResource<Pod> logResource = client.pods().withName(request.name);
        if (!follow) {
            return Multi.createFrom().item(logResource.getLog().getBytes(StandardCharsets.UTF_8));
        }

        var reader = logResource.watchLog().getOutput();
        return Multi.createFrom().emitter(new Consumer<MultiEmitter<? super byte[]>>() {
            @Override
            public void accept(MultiEmitter<? super byte[]> multiEmitter) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] buffer = new byte[1000];
                            int r;
                            while ((r = reader.read(buffer)) > 0) {
                                byte[] header = new byte[8];
                                header[0] = 1;
                                header[4] = (byte) (r >> 24);
                                header[5] = (byte) (r >> 16);
                                header[6] = (byte) (r >> 8);
                                header[7] = (byte) r;
                                multiEmitter.emit(header);
                                byte[] data = new byte[r];
                                System.arraycopy(buffer, 0, data, 0, r);
                                multiEmitter.emit(data);
                                buffer = new byte[1000];
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            multiEmitter.complete();
                        }
                    }
                }).start();
            }
        });
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CreateRequest {
        String name;
        String authConfig;
        String platform;
        String Hostname;
        String Domainname;
        String User;
        Boolean AttachStdin;
        Boolean AttachStdout;
        Boolean AttachStderr;
        String PortSpecs;
        String Tty;
        String OpenStdin;
        String StdinOnce;
        List<String> Env;
        List<String> Cmd;
        String Healthcheck;
        String ArgsEscaped;
        String Entrypoint;
        String Image;
        Map<String, String> Volumes;
        String WorkingDir;
        String MacAddress;
        String OnBuild;
        String NetworkDisabled;
        Map<String, Map<String, String>> ExposedPorts;
        String StopSignal;
        String StopTimeout;
        Map<String, String> Labels;
        String NetworkingConfig;
        HostConfig HostConfig;

        @Override
        public String toString() {
            return "CreateRequest{" +
                    "name='" + name + '\'' +
                    ", authConfig='" + authConfig + '\'' +
                    ", platform='" + platform + '\'' +
                    ", hostName='" + Hostname + '\'' +
                    ", domainName='" + Domainname + '\'' +
                    ", user='" + User + '\'' +
                    ", attachStdin=" + AttachStdin +
                    ", attachStdout=" + AttachStdout +
                    ", attachStderr=" + AttachStderr +
                    ", portSpecs='" + PortSpecs + '\'' +
                    ", Tty='" + Tty + '\'' +
                    ", OpenStdin='" + OpenStdin + '\'' +
                    ", StdinOnce='" + StdinOnce + '\'' +
                    ", Env='" + Env + '\'' +
                    ", Cmd='" + Cmd + '\'' +
                    ", Healthcheck='" + Healthcheck + '\'' +
                    ", ArgsEscaped='" + ArgsEscaped + '\'' +
                    ", Entrypoint='" + Entrypoint + '\'' +
                    ", Image='" + Image + '\'' +
                    ", Volumes=" + Volumes +
                    ", WorkingDir='" + WorkingDir + '\'' +
                    ", MacAddress='" + MacAddress + '\'' +
                    ", OnBuild='" + OnBuild + '\'' +
                    ", NetworkDisabled='" + NetworkDisabled + '\'' +
                    ", ExposedPorts=" + ExposedPorts +
                    ", StopSignal='" + StopSignal + '\'' +
                    ", StopTimeout='" + StopTimeout + '\'' +
                    ", Labels=" + Labels +
                    ", NetworkingConfig='" + NetworkingConfig + '\'' +
                    ", HostConfig=" + HostConfig +
                    '}';
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class HostConfig {
        public List<String> Binds;
        public String BlkioWeight;
        public String BlkioWeightDevice;
        public String BlkioDeviceReadBps;
        public String BlkioDeviceWriteBps;
        public String BlkioDeviceReadIOps;
        public String BlkioDeviceWriteIOps;
        public String MemorySwappiness;
        public String NanoCpus;
        public String CapAdd;
        public String CapDrop;
        public String ContainerIDFile;
        public String CpuPeriod;
        public String CpuRealtimePeriod;
        public String CpuRealtimeRuntime;
        public String CpuShares;
        public String CpuQuota;
        public String CpusetCpus;
        public String CpusetMems;
        public String Devices;
        public String DeviceCgroupRules;
        public String DeviceRequests;
        public String DiskQuota;
        public String Dns;
        public String DnsOptions;
        public String DnsSearch;
        public List<String> ExtraHosts;
        public String GroupAdd;
        public String IpcMode;
        public String Cgroup;
        public List<String> Links;
        public String LogConfig;
        public String LxcConf;
        public String Memory;
        public String MemorySwap;
        public String MemoryReservation;
        public String KernelMemory;
        public String NetworkMode;
        public String OomKillDisable;
        public String Init;
        public boolean AutoRemove;
        public String OomScoreAdj;
        public Map<String, List<Map<String, String>>> PortBindings;
        public String Privileged;
        public String PublishAllPorts;
        public String ReadonlyRootfs;
        public String RestartPolicy;
        public String Ulimits;
        public String CpuCount;
        public String CpuPercent;
        public String IOMaximumIOps;
        public String IOMaximumBandwidth;
        public List<String> VolumesFrom;
        public String Mounts;
        public String PidMode;
        public String Isolation;
        public String SecurityOpt;
        public String StorageOpt;
        public String CgroupParent;
        public String VolumeDriver;
        public String ShmSize;
        public String PidsLimit;
        public String Runtime;
        public String Tmpfs;
        public String UTSMode;
        public String UsernsMode;
        public String Sysctls;
        public String ConsoleSize;

    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CreateResponse {
        public String Id;
        public List<String> Warnings;

        public CreateResponse(String id, List<String> warnings) {
            this.Id = id;
            this.Warnings = warnings;
        }

        public String getId() {
            return Id;
        }

        public List<String> getWarnings() {
            return Warnings;
        }

        public CreateResponse setId(String id) {
            this.Id = id;
            return this;
        }

        public CreateResponse setWarnings(List<String> warnings) {
            this.Warnings = warnings;
            return this;
        }
    }
}
