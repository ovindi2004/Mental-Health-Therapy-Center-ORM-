package lk.ijse.mental_health_therapy.dao.custom.impl;

import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.dao.custom.SessionDAO;
import lk.ijse.mental_health_therapy.dto.SessionDTO;
import lk.ijse.mental_health_therapy.entity.TherapySession;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SessionDAOImpl implements SessionDAO {

    @Override
    public boolean save(TherapySession session) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        Transaction tx = hs.beginTransaction();
        try {
            hs.persist(session);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            hs.close();
        }
    }


    @Override
    public boolean update(TherapySession session) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        Transaction tx = hs.beginTransaction();
        try {
            TherapySession existing = hs.get(TherapySession.class, session.getId());
            if (existing == null) return false;

            existing.setPatient(session.getPatient());
            existing.setTherapist(session.getTherapist());
            existing.setProgram(session.getProgram());
            existing.setSessionDate(session.getSessionDate());
            existing.setSessionTime(session.getSessionTime());
            existing.setStatus(session.getStatus());

            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            hs.close();
        }
    }


    @Override
    public boolean delete(Integer id) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        Transaction tx = hs.beginTransaction();
        try {
            TherapySession s = hs.get(TherapySession.class, id);
            if (s == null) return false;
            hs.remove(s);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            hs.close();
        }
    }


    @Override
    public TherapySession findById(Integer id) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        try {
            return hs.get(TherapySession.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            hs.close();
        }
    }

    @Override
    public List<TherapySession> findAll() {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        try {
            return hs.createQuery("FROM TherapySession ORDER BY sessionDate DESC", TherapySession.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            hs.close();
        }
    }


    @Override
    public List<SessionDTO> findAllJoined(String statusFilter) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        try {
            boolean all = statusFilter == null || "All".equalsIgnoreCase(statusFilter);

            String hql = "FROM TherapySession s "
                    + "LEFT JOIN FETCH s.patient "
                    + "LEFT JOIN FETCH s.therapist"
                    + (all ? "" : " WHERE s.status = :status")
                    + " ORDER BY s.sessionDate ASC , s.sessionTime";

            Query<TherapySession> q = hs.createQuery(hql, TherapySession.class);
            if (!all) q.setParameter("status", statusFilter);

            return q.list().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            hs.close();
        }
    }


    @Override
    public List<SessionDTO> search(String keyword) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        try {
            String like = "%" + keyword.toLowerCase() + "%";

            String hql = "FROM TherapySession s "
                    + "LEFT JOIN FETCH s.patient p "
                    + "LEFT JOIN FETCH s.therapist t "
                    + "WHERE LOWER(p.fullName) LIKE :kw "
                    + "   OR LOWER(t.fullName) LIKE :kw "
                    + "   OR LOWER(s.program)  LIKE :kw "
                    + "ORDER BY s.sessionDate DESC";

            return hs.createQuery(hql, TherapySession.class)
                    .setParameter("kw", like)
                    .list()
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            hs.close();
        }
    }


    @Override
    public boolean hasConflict(int therapistId, LocalDate date, String time, int excludeSessionId) {
        org.hibernate.Session hs = FactoryConfiguration.getInstance().getSession();
        try {
            Long count = hs.createQuery(
                            "SELECT COUNT(s) FROM TherapySession s "
                                    + "WHERE s.therapist.id = :tid "
                                    + "  AND s.sessionDate  = :date "
                                    + "  AND s.sessionTime  = :time "
                                    + "  AND s.status      <> 'Cancelled' "
                                    + "  AND s.id          <> :excl",
                            Long.class)
                    .setParameter("tid",  therapistId)
                    .setParameter("date", date)
                    .setParameter("time", time)
                    .setParameter("excl", excludeSessionId)
                    .uniqueResult();

            return count != null && count > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            hs.close();
        }
    }


    private SessionDTO toDTO(TherapySession s) {
        return new SessionDTO(
                s.getId(),
                s.getPatient()   != null ? s.getPatient().getId()       : 0,
                s.getPatient()   != null ? s.getPatient().getFullName() : "Unknown",
                s.getTherapist() != null ? s.getTherapist().getId()     : 0,
                s.getTherapist() != null ? s.getTherapist().getFullName() : "Unknown",
                s.getProgram(),
                s.getSessionDate(),
                s.getSessionTime(),
                s.getStatus()
        );
    }
}