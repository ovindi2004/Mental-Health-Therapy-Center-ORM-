package lk.ijse.mental_health_therapy.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TherapyProgramDTO {
    private String programId;
    private String programName;
    private String duration;
    private double fee;
    private String description;
    private int therapistId;
    private String therapistName;

}