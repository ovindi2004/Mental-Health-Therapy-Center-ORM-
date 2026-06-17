package lk.ijse.mental_health_therapy.dao.custom;

import lk.ijse.mental_health_therapy.dao.CrudDAO;
import lk.ijse.mental_health_therapy.dto.PaymentDTO;
import lk.ijse.mental_health_therapy.entity.Payment;

import java.util.List;

public interface PaymentDAO extends CrudDAO<Payment, Integer> {

    List<PaymentDTO> findAll(String statusFilter);

    PaymentDTO findByIdJoined(int paymentId);

    double getProgramFee(String programId);

    double getTotalCollected();
    double getTotalPending();
    int    getTransactionCount();
}