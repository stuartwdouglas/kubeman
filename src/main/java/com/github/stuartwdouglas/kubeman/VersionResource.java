package com.github.stuartwdouglas.kubeman;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionResource {

    @GET
    public String version() {
        return "{\"Platform\":{\"Name\":\"Docker Engine - Community\"}," +
                "\"Components\":[{\"Name\":\"Engine\",\"Version\":\"20.10.3\",\"Details\":{\"ApiVersion\":\"1.41\",\"Arch\":\"amd64\",\"BuildTime\":\"2021-01-29T14:32:09.000000000+00:00\",\"Experimental\":\"false\",\"GitCommit\":\"46229ca\",\"GoVersion\":\"go1.13.15\",\"KernelVersion\":\"5.15.16-200.fc35.x86_64\",\"MinAPIVersion\":\"1.12\",\"Os\":\"linux\"}},{\"Name\":\"containerd\",\"Version\":\"1.4.3\",\"Details\":{\"GitCommit\":\"269548fa27e0089a8b8278fc4fc781d7f65a939b\"}},{\"Name\":\"runc\",\"Version\":\"1.0.0-rc92\",\"Details\":{\"GitCommit\":\"ff819c7e9184c13b7c2607fe6c30ae19403a7aff\"}},{\"Name\":\"docker-init\",\"Version\":\"0.19.0\",\"Details\":{\"GitCommit\":\"de40ad0\"}}],\"Version\":\"20.10.3\",\"ApiVersion\":\"1.41\",\"MinAPIVersion\":\"1.12\",\"GitCommit\":\"46229ca\",\"GoVersion\":\"go1.13.15\",\"Os\":\"linux\",\"Arch\":\"amd64\",\"KernelVersion\":\"5.15.16-200.fc35.x86_64\",\"BuildTime\":\"2021-01-29T14:32:09.000000000+00:00\"}";
    }

}
