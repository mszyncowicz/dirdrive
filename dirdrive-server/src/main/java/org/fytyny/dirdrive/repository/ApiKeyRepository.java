package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.ApiKey;

public interface ApiKeyRepository extends Repository<ApiKey> {
    ApiKey getByToken(String token);
}
