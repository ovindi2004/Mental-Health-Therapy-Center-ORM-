package lk.ijse.mental_health_therapy.dao.custom.impl;

import lk.ijse.mental_health_therapy.entity.Therapist;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.dao.custom.TherapistDAO;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class TherapistDAOImpl implements TherapistDAO {

    @Override
    public boolean save(Therapist therapist) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        org.hibernate.Transaction tx  = session.beginTransaction();
        try {
            session.persist(therapist);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean update(Therapist entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        org.hibernate.Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(Integer id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx  = session.beginTransaction();
        try {
            Therapist patient = session.get(Therapist.class, id);
            if (patient == null) return false;
            session.remove(patient);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public Therapist findById(Integer id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(Therapist.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Therapist> findAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.createQuery("FROM Therapist", Therapist.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
}