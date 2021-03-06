package com.github.stuartwdouglas.kubeman;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("info")
@Produces(MediaType.APPLICATION_JSON)
public class InfoResource {

    @GET
    public String info() {
        return "{\"ID\":\"KZOE:IMGJ:ODJ7:XQIJ:22GQ:WIHM:P2V2:WHII:L3LC:VFUN:HIPD:WVNA\"," +
                "\"Containers\":50," +
                "\"ContainersRunning\":1," +
                "\"ContainersPaused\":0," +
                "\"ContainersStopped\":49," +
                "\"Images\":354," +
                "\"Driver\":\"overlay2\"," +
                "\"DriverStatus\":[[\"Backing Filesystem\",\"extfs\"]," +
                "[\"Supports d_type\",\"true\"]," +
                "[\"Native Overlay Diff\",\"true\"]]," +
                "\"Plugins\":{\"Volume\":[\"local\"],\"Network\":[\"bridge\",\"host\",\"ipvlan\",\"macvlan\",\"null\",\"overlay\"],\"Authorization\":null,\"Log\":[\"awslogs\",\"fluentd\",\"gcplogs\",\"gelf\",\"journald\",\"json-file\",\"local\",\"logentries\",\"splunk\",\"syslog\"]},\"MemoryLimit\":true,\"SwapLimit\":true,\"KernelMemory\":true,\"KernelMemoryTCP\":true,\"CpuCfsPeriod\":true,\"CpuCfsQuota\":true,\"CPUShares\":true,\"CPUSet\":true,\"PidsLimit\":true,\"IPv4Forwarding\":true,\"BridgeNfIptables\":true,\"BridgeNfIp6tables\":true,\"Debug\":false,\"NFd\":33,\"OomKillDisable\":true,\"NGoroutines\":50,\"SystemTime\":\"2022-02-04T13:16:33.794493128+11:00\",\"LoggingDriver\":\"json-file\",\"CgroupDriver\":\"cgroupfs\",\"CgroupVersion\":\"1\",\"NEventsListener\":0,\"KernelVersion\":\"5.15.16-200.fc35.x86_64\",\"OperatingSystem\":\"Fedora Linux 35 (Workstation Edition)\",\"OSVersion\":\"35\",\"OSType\":\"linux\",\"Architecture\":\"x86_64\",\"IndexServerAddress\":\"https://index.docker.io/v1/\",\"RegistryConfig\":{\"AllowNondistributableArtifactsCIDRs\":[],\"AllowNondistributableArtifactsHostnames\":[],\"InsecureRegistryCIDRs\":[\"127.0.0.0/8\"],\"IndexConfigs\":{\"docker.io\":{\"Name\":\"docker.io\",\"Mirrors\":[],\"Secure\":true,\"Official\":true}},\"Mirrors\":[]},\"NCPU\":24,\"MemTotal\":67432865792,\"GenericResources\":null,\"DockerRootDir\":\"/var/lib/docker\",\"HttpProxy\":\"\",\"HttpsProxy\":\"\",\"NoProxy\":\"\",\"Name\":\"localhost.localdomain\",\"Labels\":[],\"ExperimentalBuild\":false,\"ServerVersion\":\"20.10.3\",\"Runtimes\":{\"io.containerd.runc.v2\":{\"path\":\"runc\"},\"io.containerd.runtime.v1.linux\":{\"path\":\"runc\"},\"runc\":{\"path\":\"runc\"}},\"DefaultRuntime\":\"runc\",\"Swarm\":{\"NodeID\":\"\",\"NodeAddr\":\"\",\"LocalNodeState\":\"inactive\",\"ControlAvailable\":false,\"Error\":\"\",\"RemoteManagers\":null},\"LiveRestoreEnabled\":false,\"Isolation\":\"\",\"InitBinary\":\"docker-init\",\"ContainerdCommit\":{\"ID\":\"269548fa27e0089a8b8278fc4fc781d7f65a939b\",\"Expected\":\"269548fa27e0089a8b8278fc4fc781d7f65a939b\"},\"RuncCommit\":{\"ID\":\"ff819c7e9184c13b7c2607fe6c30ae19403a7aff\",\"Expected\":\"ff819c7e9184c13b7c2607fe6c30ae19403a7aff\"},\"InitCommit\":{\"ID\":\"de40ad0\",\"Expected\":\"de40ad0\"},\"SecurityOptions\":[\"name=seccomp,profile=default\"],\"Warnings\":[\"WARNING: No blkio weight support\",\"WARNING: No blkio weight_device support\"]}";
    }
}
