package org.fytyny.dirdrive.service;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;

public interface ApiKeyService {

    ApiKey getApiKeyBySession(String sessionToken);

    ApiKey save(ApiKey apiKey);

    boolean existByToken(String token);

    ApiKey getByToken(String token);

    boolean containsDirectory(ApiKey apiKey, Directory directory);
}
