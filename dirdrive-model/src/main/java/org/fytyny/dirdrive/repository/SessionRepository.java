package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Session;

public interface SessionRepository extends Repository<Session> {
    Session getByToken(String token);
}
