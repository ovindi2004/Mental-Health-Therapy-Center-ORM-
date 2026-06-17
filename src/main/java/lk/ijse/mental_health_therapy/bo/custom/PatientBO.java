package lk.ijse.mental_health_therapy.bo.custom;

import lk.ijse.mental_health_therapy.bo.SuperBO;
import lk.ijse.mental_health_therapy.dto.PatientDTO;
import lk.ijse.mental_health_therapy.entity.Patient;

import java.util.ArrayList;
import java.util.List;

public interface PatientBO extends SuperBO {
    public boolean savePatient(PatientDTO dto) throws Exception;

    public boolean updatePatient(PatientDTO dto) throws Exception;

    public boolean deletePatient(Integer patientId) throws Exception;

    public PatientDTO findPatientById(int id) throws Exception;

    public List<PatientDTO> findAllPatients() throws Exception;

}
