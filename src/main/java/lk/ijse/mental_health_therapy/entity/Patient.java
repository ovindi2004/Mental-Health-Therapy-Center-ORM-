package lk.ijse.mental_health_therapy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "patient")
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "fullName", nullable = false)
    private String fullName;

    @Column(name = "dateOfBirth",nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender",nullable = false)
    private String gender;

    @Column(name = "address",nullable = false)
    private String address;

    @Column(name = "contactNumber",nullable = false)
    private String contactNumber;

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "therapyProgress",nullable = false,length = 50)
    private String therapyProgress;



}