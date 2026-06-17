package lk.ijse.mental_health_therapy.bo.custom.impl;

import lk.ijse.mental_health_therapy.bo.custom.PatientBO;
import lk.ijse.mental_health_therapy.dao.DAOFactory;
import lk.ijse.mental_health_therapy.dao.custom.PatientDAO;
import lk.ijse.mental_health_therapy.dto.PatientDTO;
import lk.ijse.mental_health_therapy.entity.Patient;

import java.util.ArrayList;
import java.util.List;



public class PatientBOImpl implements PatientBO {

    private final PatientDAO patientDAO =
            (PatientDAO) DAOFactory.getInstance()
                    .getDAO(DAOFactory.DAOType.PATIENT);

    @Override
    public boolean savePatient(PatientDTO dto) throws Exception {

        Patient patient = toEntity(dto);

        return patientDAO.save(patient);
    }

    @Override
    public boolean updatePatient(PatientDTO dto) throws Exception {
        Patient existingPatient = patientDAO.findById(dto.getId());
        if (existingPatient == null) {
            throw new Exception(
                    "Patient ID " + dto.getId() + " not found."
            );
        }
        return patientDAO.update(toEntity(dto));
    }

    @Override
    public boolean deletePatient(Integer patientId) throws Exception {
        Patient patient = patientDAO.findById(patientId);
        if (patient == null) {
            throw new Exception(
                    "Patient ID " + patientId + " not found."
            );
        }
        return patientDAO.delete(patientId);
    }

    @Override
    public PatientDTO findPatientById(int id) throws Exception{
        Patient patient = patientDAO.findById(id);
        if (patient == null) {
            return null;
        }
        return toDTO(patient);
    }

    @Override
    public List<PatientDTO> findAllPatients() throws Exception{
        List<Patient> patientList = patientDAO.findAll();
        List<PatientDTO> dtoList = new ArrayList<>();

        for (Patient patient : patientList) {
            dtoList.add(toDTO(patient));
        }
        return dtoList;
    }

    private PatientDTO toDTO(Patient patient) throws Exception{
        return new PatientDTO(
                patient.getId(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getAddress(),
                patient.getContactNumber(),
                patient.getEmail(),
                patient.getTherapyProgress()
        );
    }

    private Patient toEntity(PatientDTO dto) throws Exception{
        return new Patient(
                dto.getId(),
                dto.getFullName(),
                dto.getDateOfBirth(),
                dto.getGender(),
                dto.getAddress(),
                dto.getContactNumber(),
                dto.getEmail(),
                dto.getTherapyProgress()
        );
    }
}