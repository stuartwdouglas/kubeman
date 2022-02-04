package com.github.stuartwdouglas.kubeman;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Path("containers")
public class ContainersResource {

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
        Pod pod = new Pod();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(name);
        objectMeta.setLabels(Map.of("docker-emulation", "true", "docker-container-name", "name", "docker-container-id", id));

        PodSpec spec = new PodSpec();
        Container container = new Container();
        container.setName(name);
        container.setImage(request.Image);

        List<ContainerPort> ports = new ArrayList<>();

        container.setPorts(ports);
        container.setCommand(request.Cmd);
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
