package lk.ijse.mental_health_therapy.bo.custom;

import lk.ijse.mental_health_therapy.bo.SuperBO;
import lk.ijse.mental_health_therapy.dto.PaymentDTO;

import java.util.List;

public interface PaymentBO extends SuperBO {
    boolean          savePayment(PaymentDTO dto) throws Exception;
    boolean          updatePayment(PaymentDTO dto);
    boolean          deletePayment(int paymentId);
    List<PaymentDTO> getAllPayments(String statusFilter);
    PaymentDTO       getPaymentById(int paymentId);
    double           getProgramFee(String programId);
    double           getTotalCollected();
    double           getTotalPending();
    int              getTransactionCount();
}