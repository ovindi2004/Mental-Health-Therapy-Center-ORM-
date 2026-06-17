package lk.ijse.mental_health_therapy.dao.custom.impl;

import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.dao.custom.TherapyProgramDAO;
import lk.ijse.mental_health_therapy.dto.TherapyProgramDTO;
import lk.ijse.mental_health_therapy.entity.TherapyProgram;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class TherapyProgramDAOImpl implements TherapyProgramDAO {

    @Override
    public boolean save(TherapyProgram program) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.persist(program);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }


    @Override
    public boolean update(TherapyProgram program) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            TherapyProgram existing = session.get(TherapyProgram.class, program.getProgramId());
            if (existing == null) return false;

            existing.setProgramName(program.getProgramName());
            existing.setDuration(program.getDuration());
            existing.setFee(program.getFee());
            existing.setDescription(program.getDescription());
            existing.setTherapist(program.getTherapist());

            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }


    @Override
    public boolean delete(String programId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            TherapyProgram program = session.get(TherapyProgram.class, programId);
            if (program == null) return false;
            session.remove(program);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }


    @Override
    public TherapyProgram findById(String programId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(TherapyProgram.class, programId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }


    @Override
    public List<TherapyProgram> findAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.createQuery("FROM TherapyProgram ORDER BY programId", TherapyProgram.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }


    @Override
    public List<TherapyProgramDTO> findAllWithTherapist() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<TherapyProgram> programs = session
                    .createQuery("FROM TherapyProgram tp LEFT JOIN FETCH tp.therapist ORDER BY tp.programId",
                            TherapyProgram.class)
                    .list();

            return programs.stream().map(tp -> new TherapyProgramDTO(
                    tp.getProgramId(),
                    tp.getProgramName(),
                    tp.getDuration(),
                    tp.getFee(),
                    tp.getDescription(),
                    tp.getTherapist() != null ? tp.getTherapist().getId()       : 0,
                    tp.getTherapist() != null ? tp.getTherapist().getFullName() : "Not Assigned"
            )).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }


    @Override
    public boolean existsById(String programId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Long count = session.createQuery(
                            "SELECT COUNT(tp) FROM TherapyProgram tp WHERE tp.programId = :id",
                            Long.class)
                    .setParameter("id", programId)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
}