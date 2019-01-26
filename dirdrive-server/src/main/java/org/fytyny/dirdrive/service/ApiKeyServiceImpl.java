package org.fytyny.dirdrive.service;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.ApiKeyRepository;
import org.fytyny.dirdrive.repository.SessionRepository;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Stateless
public class ApiKeyServiceImpl implements  ApiKeyService{

    @Any
    @Inject
    SessionRepository sessionRepository;

    @Any
    @Inject
    ApiKeyRepository apiKeyRepository;

    @Override
    public ApiKey getApiKeyBySession(String sessionToken) {
        Session byToken = sessionRepository.getByToken(sessionToken);
        if (byToken != null){
            return byToken.getApiKey();
        }
        return null;
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        return apiKeyRepository.save(apiKey);
    }

    @Override
    public boolean existByToken(String token) {
        return apiKeyRepository.getByToken(token) != null;
    }

    @Override
    public ApiKey getByToken(String token) {
        return apiKeyRepository.getByToken(token);
    }

    @Override
    public boolean containsDirectory(ApiKey apiKey, Directory directory) {
        return existByToken(apiKey.getToken()) && apiKey.getDirectoryList().stream().map(d -> d.getPath()).collect(Collectors.toList()).contains(directory.getPath());
    }
}
