package org.fytyny.dirdrive.service;

import lombok.Setter;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.SessionRepository;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

@Stateless
public class SessionServiceImpl implements SessionService {

    @Any
    @Setter
    @Inject
    private SessionRepository sessionRepository;

    @Override
    public Session createSession(ApiKey apiKey) {
        Session session = new Session();
        session.setToken(Session.generateRandom(20));
        session.setApiKey(apiKey);
        session.setId(UUID.randomUUID());
        return sessionRepository.save(session);
    }

    @Override
    public Session getSessionByToken(String token){
        return sessionRepository.getByToken(token);
    }
}
