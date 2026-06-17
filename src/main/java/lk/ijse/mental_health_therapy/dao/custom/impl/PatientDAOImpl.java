package lk.ijse.mental_health_therapy.dao.custom.impl;


import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.dao.custom.PatientDAO;
import lk.ijse.mental_health_therapy.entity.Patient;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PatientDAOImpl implements PatientDAO {

    @Override
    public boolean save(Patient patient) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();

        try {

            session.persist(patient);

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
    public boolean update(Patient entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            session.merge(entity);

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
    public boolean delete(Integer patientId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx  = session.beginTransaction();
        try {
            Patient patient = session.get(Patient.class, patientId);
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
    public Patient findById(Integer id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(Patient.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Patient> findAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.createQuery("from Patient", Patient.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
}
