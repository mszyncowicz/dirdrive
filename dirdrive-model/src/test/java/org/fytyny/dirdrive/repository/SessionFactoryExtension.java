package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Directory;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.util.Arrays;

public class SessionFactoryExtension implements Extension, BeforeEachCallback, AfterEachCallback,BeforeAllCallback {
    private SessionFactory sessionFactory;
    private Transaction transaction;
    private org.hibernate.Session session;
/*
    @Override
    public Statement apply(final Statement statement, FrameworkMethod method,
                           Object test) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sessionFactory = createSessionFactory();
                createSession();
                beginTransaction();
                try {
                    statement.evaluate();
                } finally {
                    shutdown();
                }
            }
        };
    }


 */

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
    }

    private void injectEntityManagers(Object object) {
        Class<?> aClass = object.getClass();
        Arrays.stream(aClass.getDeclaredFields()).filter(lvField -> lvField.getAnnotationsByType(InjectEntityMangaer.class).length > 0)
                .forEach(lvField -> {
                    try {
                        injectInterface(object, lvField);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void injectInterface(Object object, Field lvField) throws IllegalAccessException {
        if (lvField.getAnnotationsByType(InjectEntityMangaer.class).length > 0) {
            lvField.setAccessible(true);
            Repository lvRepository = (Repository) lvField.get(object);
            injectManager(lvRepository);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        sessionFactory = createSessionFactory();
        extensionContext.getTestInstance().ifPresent(this::injectEntityManagers);
        createSession();
        beginTransaction();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
      //  shutdown();
    }

    private void shutdown() {
        try {
            try {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                session.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sessionFactory.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private SessionFactory createSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(org.fytyny.dirdrive.model.ApiKey.class)
                .addAnnotatedClass(org.fytyny.dirdrive.model.Session.class)
                .addAnnotatedClass(Directory.class);
        configuration.setProperty("hibernate.dialect",
                "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.driver_class",
                "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        return sessionFactory;
    }
    public org.hibernate.Session createSession() {
        session = sessionFactory.openSession();
        return this.session;
    }
    public void commit() {
        transaction.commit();
    }
    public void beginTransaction() {
        transaction = session.beginTransaction();
    }
    public org.hibernate.Session getSession() {
        return session;
    }

    private void injectManager(Repository repository){
        repository.setEntityManager(createSession().getEntityManagerFactory().createEntityManager());
    }
}