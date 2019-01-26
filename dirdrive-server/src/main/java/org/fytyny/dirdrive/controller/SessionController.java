package org.fytyny.dirdrive.controller;

import org.fytyny.dirdrive.api.dto.GeneralResponseDTO;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.service.ApiKeyService;
import org.fytyny.dirdrive.service.ResponseService;
import org.fytyny.dirdrive.service.SessionService;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.transaction.*;
import javax.transaction.NotSupportedException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/session")
public class SessionController {

    @Any
    @Inject
    SessionService sessionService;

    @Any
    @Inject
    ResponseService responseService;

    @Any
    @Inject
    ApiKeyService apiKeyService;

    @Inject
    UserTransaction userTransaction;

    @GET
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSession(@HeaderParam(DirectoryController.X_API_KEY) String apiKey) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        userTransaction.begin();
        ApiKey byToken = apiKeyService.getByToken(apiKey);
        if (byToken == null){
            userTransaction.commit();
            return responseService.error(GeneralResponseDTO.authenticationFailed(),401);
        }
        Session session = sessionService.createSession(byToken);
        userTransaction.commit();
        return responseService.success(session);
    }

}
