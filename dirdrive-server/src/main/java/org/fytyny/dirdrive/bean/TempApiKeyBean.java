package org.fytyny.dirdrive.bean;

import org.checkerframework.checker.units.qual.A;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.service.ApiKeyService;
import org.fytyny.dirdrive.service.DirectoryService;
import org.fytyny.dirdrive.service.SessionService;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import static javax.ejb.TransactionAttributeType.REQUIRED;

@Singleton
@Startup
public class TempApiKeyBean {

    private static final String TEMP_TOKEN = "superapikey";
    private static final String TEMP_DIR = "E:\\Muzyka\\Yt-Music";
    @Any
    @Inject
    ApiKeyService apiKeyService;

    @Any
    @Inject
    DirectoryService directoryService;

    @Any
    @Inject
    SessionService sessionService;

    @Inject
    Logger logger;

    @PostConstruct
    public void init(){
        if (!apiKeyService.existByToken(TEMP_TOKEN)){
            ApiKey tempApiKey = getTempApiKey();
            Directory directory = new Directory();
            directory.setPath(TEMP_DIR);
            directory.setLabel("MUSIC");
            directory.setId(UUID.randomUUID());
            Session session = sessionService.createSession(tempApiKey);
            directoryService.addDirectoryToApiKey(directory,session);
            logger.info("Key created");
        } else {
            logger.info("Key exists");
        }
    }

    @TransactionAttribute(REQUIRED)
    private ApiKey getTempApiKey(){

        ApiKey apiKey = new ApiKey();
        apiKey.setToken(TEMP_TOKEN);
        apiKey.setId(UUID.randomUUID());
        apiKey.setDirectoryList(new ArrayList<>());
        ApiKey save = apiKeyService.save(apiKey);

        return save;
    }

}
