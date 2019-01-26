package org.fytyny.dirdrive.service;

import javax.ws.rs.core.Response;
import java.util.Map;

public interface ResponseService {

    Response success(Object entity);

    Response success(Object entity, Map<String,String> headers);

    Response customResponse(Object entity, Map<String,String> headers, Integer code);

    Response error(Object entity);

    Response error(Object entity, Integer code);
}
