package lk.ijse.mental_health_therapy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private int paymentId;
    private int patientId;
    private String programId;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String paymentStatus;


    private String patientName;
    private String programName;
    private double programFee;


}