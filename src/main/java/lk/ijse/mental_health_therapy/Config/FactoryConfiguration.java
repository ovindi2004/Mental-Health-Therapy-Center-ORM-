package lk.ijse.mental_health_therapy.Config;

import lk.ijse.mental_health_therapy.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.mindrot.jbcrypt.BCrypt;

public class FactoryConfiguration {

    private static FactoryConfiguration instance;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {

        Configuration configuration = new Configuration();

        configuration.addAnnotatedClass(Patient.class);
        configuration.addAnnotatedClass(Therapist.class);
        configuration.addAnnotatedClass(TherapySession.class);
        configuration.addAnnotatedClass(TherapyProgram.class);
        configuration.addAnnotatedClass(Payment.class);
        configuration.addAnnotatedClass(User.class);

        ServiceRegistry serviceRegistry =
                new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

        sessionFactory =
                configuration.buildSessionFactory(serviceRegistry);

        createDefaultUsersIfNotExist();
    }


    private void createDefaultUsersIfNotExist() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();


            if (session.get(User.class, "admin") == null) {
                String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
                session.persist(new User("admin", hashedPassword, "ADMIN", "admin@mental.lk"));
            }


            if (session.get(User.class, "receptionist") == null) {
                String hashedPassword = BCrypt.hashpw("recep123", BCrypt.gensalt(10));
                session.persist(new User("receptionist", hashedPassword, "RECEPTIONIST", "recep@mental.lk"));
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized FactoryConfiguration getInstance() {
        if (instance == null) {
            instance = new FactoryConfiguration();
        }
        return instance;
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }
}