package me.cnlm.springboot.quartz.restful.rest.facade;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
@Path("/test")
@Service
public class JobRestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Response test() {

        JSONObject json=new JSONObject();
        json.put("status",true);
        return Response.ok().entity(json).build();
    }
}
