package org.fytyny.dirdrive.service;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;

public interface SessionService {

    Session createSession(ApiKey apiKey);

    Session getSessionByToken(String token);
}
