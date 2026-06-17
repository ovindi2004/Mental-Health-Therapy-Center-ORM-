package lk.ijse.mental_health_therapy.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private int id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String contactNumber;
    private String email;
    private String therapyProgress;

}
