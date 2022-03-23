package org.fytyny.dirdrive.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/test")
public class DefaultController {

    @GET
    @Path("/say")
    public String sayHello(){
        return "Hello Wordl!";
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(){
        File file = new File("E:\\Muzyka\\Yt-Music\\K_DA - POP_STARS (ft Madison Beer, (G)I-DLE, Jaira Burns) _ Official Music Video - League of Legends-UOxkGD8qRB4.mp3");
        return Response.ok().entity(file).build();
    }
}
