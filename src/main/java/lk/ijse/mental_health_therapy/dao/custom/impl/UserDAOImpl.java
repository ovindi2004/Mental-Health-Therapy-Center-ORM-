package lk.ijse.mental_health_therapy.dao.custom.impl;

import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.dao.custom.UserDAO;
import lk.ijse.mental_health_therapy.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean save(User user) {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(User user) {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String username) {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, username);
            if (user != null) session.remove(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User findById(String username) {
        if (username == null || username.isEmpty()) return null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(User.class, username);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateCredentials(String oldUsername, String newUsername, String newPassword) {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, oldUsername);
            if (user == null) {
                System.out.println("DEBUG: User not found: " + oldUsername);
                return false;
            }

            if (!oldUsername.equals(newUsername)) {
                // Username change — delete old, insert new
                String role  = user.getRole();
                String email = user.getEmail();
                session.remove(user);
                session.flush();
                User newUser = new User(newUsername, newPassword, role, email);
                session.persist(newUser);
                System.out.println("DEBUG: Username changed: " + oldUsername + " → " + newUsername);
            } else {
                // Password only — HQL update
                session.createMutationQuery(
                                "UPDATE User u SET u.password = :pwd WHERE u.username = :uname"
                        )
                        .setParameter("pwd",   newPassword)
                        .setParameter("uname", oldUsername)
                        .executeUpdate();
                System.out.println("DEBUG: Password updated for: " + oldUsername);
            }

            tx.commit();
            System.out.println("DEBUG: Commit success ");
            return true;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("DEBUG: Exception → " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}