package lk.ijse.mental_health_therapy.dao.custom.impl;

import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.dao.custom.PaymentDAO;
import lk.ijse.mental_health_therapy.dto.PaymentDTO;
import lk.ijse.mental_health_therapy.entity.Payment;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    // ── save ──────────────────────────────────────────────────────────────
    @Override
    public boolean save(Payment payment) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.persist(payment);
            tx.commit();
            return payment.getPaymentId() > 0;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    // ── update ────────────────────────────────────────────────────────────
    @Override
    public boolean update(Payment payment) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            Payment existing = session.get(Payment.class, payment.getPaymentId());
            if (existing == null) return false;
            existing.setPatientId(payment.getPatientId());
            existing.setProgramId(payment.getProgramId());
            existing.setAmountPaid(payment.getAmountPaid());
            existing.setPaymentMethod(payment.getPaymentMethod());
            existing.setPaymentDate(payment.getPaymentDate());
            existing.setPaymentStatus(payment.getPaymentStatus());
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

    // ── delete ────────────────────────────────────────────────────────────
    @Override
    public boolean delete(Integer paymentId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            Payment p = session.get(Payment.class, paymentId);
            if (p == null) return false;
            session.remove(p);
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

    // ── findById ──────────────────────────────────────────────────────────
    @Override
    public Payment findById(Integer paymentId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(Payment.class, paymentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    // ── findAll (raw entities) ────────────────────────────────────────────
    @Override
    public List<Payment> findAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.createQuery(
                            "FROM Payment ORDER BY paymentDate DESC", Payment.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    // ── findAll with status filter (for TableView) ────────────────────────
    @Override
    public List<PaymentDTO> findAll(String statusFilter) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            boolean all = "All".equalsIgnoreCase(statusFilter);

            String hql = "SELECT p.paymentId, p.patientId, p.programId, " +
                    "p.amountPaid, p.paymentMethod, p.paymentDate, p.paymentStatus, " +
                    "pt.fullName, tp.programName, tp.fee " +
                    "FROM Payment p, Patient pt, TherapyProgram tp " +
                    "WHERE pt.id = p.patientId " +
                    "AND tp.programId = p.programId " +
                    (all ? "" : "AND p.paymentStatus = :status ") +
                    "ORDER BY p.paymentDate DESC";

            Query<Object[]> q = session.createQuery(hql, Object[].class);
            if (!all) q.setParameter("status", statusFilter);

            List<PaymentDTO> result = new ArrayList<>();
            for (Object[] row : q.list()) {
                result.add(new PaymentDTO(
                        (int)       row[0],   // paymentId
                        (int)       row[1],   // patientId
                        (String)    row[2],   // programId
                        (BigDecimal)row[3],   // amountPaid
                        (String)    row[4],   // paymentMethod
                        (LocalDate) row[5],   // paymentDate
                        (String)    row[6],   // paymentStatus
                        (String)    row[7],   // patientName
                        (String)    row[8],   // programName
                        (Double)    row[9]    // programFee
                ));
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    // ── findByIdJoined ────────────────────────────────────────────────────
    @Override
    public PaymentDTO findByIdJoined(int paymentId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String hql = "SELECT p.paymentId, p.patientId, p.programId, " +
                    "p.amountPaid, p.paymentMethod, p.paymentDate, p.paymentStatus, " +
                    "pt.fullName, tp.programName, tp.fee " +
                    "FROM Payment p, Patient pt, TherapyProgram tp " +
                    "WHERE pt.id = p.patientId " +
                    "AND tp.programId = p.programId " +
                    "AND p.paymentId = :id";

            Object[] row = session.createQuery(hql, Object[].class)
                    .setParameter("id", paymentId)
                    .uniqueResult();

            if (row == null) return null;

            return new PaymentDTO(
                    (int)       row[0],
                    (int)       row[1],
                    (String)    row[2],
                    (BigDecimal)row[3],
                    (String)    row[4],
                    (LocalDate) row[5],
                    (String)    row[6],
                    (String)    row[7],
                    (String)    row[8],
                    (Double)    row[9]
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    // ── getProgramFee ─────────────────────────────────────────────────────
    @Override
    public double getProgramFee(String programId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Double fee = session.createQuery(
                            "SELECT tp.fee FROM TherapyProgram tp WHERE tp.programId = :id",
                            Double.class)
                    .setParameter("id", programId)
                    .uniqueResult();
            return fee != null ? fee : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            session.close();
        }
    }

    // ── summary queries ───────────────────────────────────────────────────
    @Override
    public double getTotalCollected() { return sumByStatus("Paid"); }

    @Override
    public double getTotalPending() { return sumByStatus("Pending"); }

    private double sumByStatus(String status) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Object result = session.createQuery(
                            "SELECT SUM(p.amountPaid) FROM Payment p WHERE p.paymentStatus = :s")
                    .setParameter("s", status)
                    .uniqueResult();
            if (result == null) return 0.0;
            return ((BigDecimal) result).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            session.close();
        }
    }

    @Override
    public int getTransactionCount() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Long count = session.createQuery(
                            "SELECT COUNT(p) FROM Payment p", Long.class)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            session.close();
        }
    }
}