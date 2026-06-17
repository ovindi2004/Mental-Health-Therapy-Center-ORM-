package lk.ijse.mental_health_therapy.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapistDTO {
    private int id;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;
    private String assignedProgram;
    private String availability;



}