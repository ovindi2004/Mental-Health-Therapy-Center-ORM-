package lk.ijse.mental_health_therapy.bo.custom.impl;

import lk.ijse.mental_health_therapy.bo.custom.PaymentBO;
import lk.ijse.mental_health_therapy.dao.DAOFactory;
import lk.ijse.mental_health_therapy.dao.custom.PaymentDAO;
import lk.ijse.mental_health_therapy.dto.PaymentDTO;
import lk.ijse.mental_health_therapy.entity.Payment;

import java.math.BigDecimal;
import java.util.List;

public class PaymentBOImpl implements PaymentBO {

    private final PaymentDAO paymentDAO = (PaymentDAO)
            DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PAYMENT);

    @Override
    public boolean savePayment(PaymentDTO dto) throws Exception {
        validate(dto);
        return paymentDAO.save(toEntity(dto));
    }

    @Override
    public boolean updatePayment(PaymentDTO dto) {
        validate(dto);
        Payment entity = toEntity(dto);
        entity.setPaymentId(dto.getPaymentId());
        return paymentDAO.update(entity);
    }

    @Override
    public boolean deletePayment(int paymentId) {
        if (paymentId <= 0) throw new IllegalArgumentException("Invalid payment ID.");
        return paymentDAO.delete(paymentId);
    }

    @Override
    public List<PaymentDTO> getAllPayments(String statusFilter) {
        return paymentDAO.findAll(statusFilter == null ? "All" : statusFilter);
    }

    @Override
    public PaymentDTO getPaymentById(int paymentId) {
        return paymentDAO.findByIdJoined(paymentId);
    }

    @Override
    public double getProgramFee(String programId) {
        return paymentDAO.getProgramFee(programId);
    }

    @Override
    public double getTotalCollected() { return paymentDAO.getTotalCollected(); }

    @Override
    public double getTotalPending()   { return paymentDAO.getTotalPending(); }

    @Override
    public int getTransactionCount()  { return paymentDAO.getTransactionCount(); }

    // ── helpers ───────────────────────────────────────────────────────────

    private Payment toEntity(PaymentDTO dto) {
        return new Payment(
                0,                       // ← paymentId = 0 (DB auto-generate කරනවා)
                dto.getPatientId(),
                dto.getProgramId(),
                dto.getAmountPaid(),
                dto.getPaymentMethod(),
                dto.getPaymentDate(),
                dto.getPaymentStatus()
        );
    }

    private void validate(PaymentDTO dto) {
        if (dto.getPatientId() <= 0)
            throw new IllegalArgumentException("Patient must be selected.");
        if (dto.getProgramId() == null || dto.getProgramId().isBlank())
            throw new IllegalArgumentException("Therapy program must be selected.");
        if (dto.getAmountPaid() == null || dto.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        if (dto.getPaymentDate() == null)
            throw new IllegalArgumentException("Payment date is required.");
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isBlank())
            throw new IllegalArgumentException("Payment method is required.");
        if (dto.getPaymentStatus() == null || dto.getPaymentStatus().isBlank())
            throw new IllegalArgumentException("Payment status is required.");
    }
}