package com.github.stuartwdouglas.kubeman;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Path("images")
@Produces(MediaType.APPLICATION_JSON)
public class ImagesResource {

    @GET
    @Path("{image}/json")
    public String result(String image) {
        return "{\"Id\":\"sha256:64f4b02dc9864d9621d248e55835408a03b3cd8239b790e610c71c29ea45a3b9\",\"RepoTags\":[\"testcontainers/ryuk:0.3.3\"],\"RepoDigests\":[\"testcontainers/ryuk@sha256:d50c04005142e165182b2e408d0c76f3cc38ca4db4af14125ce1b938820e5cef\"],\"Parent\":\"\",\"Comment\":\"buildkit.dockerfile.v0\",\"Created\":\"2021-10-14T08:34:37.190213163Z\",\"Container\":\"\",\"ContainerConfig\":{\"Hostname\":\"\",\"Domainname\":\"\",\"User\":\"\",\"AttachStdin\":false,\"AttachStdout\":false,\"AttachStderr\":false,\"Tty\":false,\"OpenStdin\":false,\"StdinOnce\":false,\"Env\":null,\"Cmd\":null,\"Image\":\"\",\"Volumes\":null,\"WorkingDir\":\"\",\"Entrypoint\":null,\"OnBuild\":null,\"Labels\":null},\"DockerVersion\":\"\",\"Author\":\"\",\"Config\":{\"Hostname\":\"\",\"Domainname\":\"\",\"User\":\"\",\"AttachStdin\":false,\"AttachStdout\":false,\"AttachStderr\":false,\"Tty\":false,\"OpenStdin\":false,\"StdinOnce\":false,\"Env\":[\"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\"],\"Cmd\":[\"/app\"],\"ArgsEscaped\":true,\"Image\":\"\",\"Volumes\":null,\"WorkingDir\":\"\",\"Entrypoint\":null,\"OnBuild\":null,\"Labels\":null},\"Architecture\":\"amd64\",\"Os\":\"linux\",\"Size\":11982543,\"VirtualSize\":11982543,\"GraphDriver\":{\"Data\":{\"LowerDir\":\"/var/lib/docker/overlay2/a593020cad75e3c4d38122333bc02c7e3e2934809073120e15859585c27d5de6/diff:/var/lib/docker/overlay2/b9354bd90cce3dc7451b3b1d655533ae74f10ed7be092de9acffca43ba1d27e9/diff\",\"MergedDir\":\"/var/lib/docker/overlay2/f9ea08b5bb4862ebba4e2ebd59e1349e821cb1f0d726f4f7feca46f7cd14c322/merged\",\"UpperDir\":\"/var/lib/docker/overlay2/f9ea08b5bb4862ebba4e2ebd59e1349e821cb1f0d726f4f7feca46f7cd14c322/diff\",\"WorkDir\":\"/var/lib/docker/overlay2/f9ea08b5bb4862ebba4e2ebd59e1349e821cb1f0d726f4f7feca46f7cd14c322/work\"},\"Name\":\"overlay2\"},\"RootFS\":{\"Type\":\"layers\",\"Layers\":[\"sha256:f1dd685eb59e7d19dd353b02c4679d9fafd21ccffe1f51960e6c3645f3ceb0cd\",\"sha256:b0d7b79bf2d669ae9417ca566db869fd39c56629970b9427bc9cd9f3e6a9fae7\",\"sha256:9abd6e27dfe5c906b1dc8dd908d4b965e9ff56904699f6776badb0386920ad14\"]},\"Metadata\":{\"LastTagTime\":\"0001-01-01T00:00:00Z\"}}";
    }

    @GET
    @Path("json")
    public List<String> list() {
        return Collections.emptyList();
    }
}
