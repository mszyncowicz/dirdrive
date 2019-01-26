package org.fytyny.dirdrive.service;

import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Stateless
public class ResponseServiceImpl implements ResponseService {

    @Override
    public Response success(Object entity) {
        return Response.ok(entity).build();
    }

    @Override
    public Response success(Object entity, Map<String, String> headers) {
        Response.ResponseBuilder resultBuilder = Response.ok(entity);
        resultBuilder = applyHeaders(resultBuilder,headers);
        return resultBuilder.build();
    }

    @Override
    public Response customResponse(Object entity, Map<String, String> headers, Integer code) {
        Response.ResponseBuilder resultBuilder = Response.ok(entity);
        resultBuilder = applyHeaders(resultBuilder,headers);
        return resultBuilder.status(code).build();
    }

    @Override
    public Response error(Object entity) {
        return Response.serverError().entity(entity).build();
    }

    @Override
    public Response error(Object entity, Integer code) {
        return Response.serverError().type(MediaType.APPLICATION_JSON_TYPE).entity(entity).status(code).build();
    }

    private Response.ResponseBuilder applyHeaders(Response.ResponseBuilder resultBuilder,Map<String, String> headers){
        for (Map.Entry<String,String> entry : headers.entrySet()){
            resultBuilder = resultBuilder.header(entry.getKey(),entry.getValue());
        }
        return resultBuilder;
    }
}
