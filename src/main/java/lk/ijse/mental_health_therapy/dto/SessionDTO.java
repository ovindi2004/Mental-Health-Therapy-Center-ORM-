package lk.ijse.mental_health_therapy.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SessionDTO {
    private int id;
    private int patientId;
    private String patientName;
    private int therapistId;
    private String therapistName;
    private String program;
    private LocalDate sessionDate;
    private String sessionTime;
    private String status;


}