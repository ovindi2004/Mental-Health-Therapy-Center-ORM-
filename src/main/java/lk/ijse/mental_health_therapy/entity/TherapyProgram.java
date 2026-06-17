package lk.ijse.mental_health_therapy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Data
@Table(name = "therapy_program")
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TherapyProgram {
    @Id
    @Column(name = "program_id", length = 20)
    private String programId;

    @Column(name = "program_name", nullable = false, length = 150)
    private String programName;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "fee", nullable = false)
    private double fee;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id")
    private Therapist therapist;


}