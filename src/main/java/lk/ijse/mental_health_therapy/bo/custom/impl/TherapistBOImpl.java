package lk.ijse.mental_health_therapy.bo.custom.impl;

import lk.ijse.mental_health_therapy.bo.custom.TherapistBO;
import lk.ijse.mental_health_therapy.dao.DAOFactory;
import lk.ijse.mental_health_therapy.dao.custom.TherapistDAO;
import lk.ijse.mental_health_therapy.dto.PatientDTO;
import lk.ijse.mental_health_therapy.dto.TherapistDTO;
import lk.ijse.mental_health_therapy.entity.Patient;
import lk.ijse.mental_health_therapy.entity.Therapist;

import java.util.ArrayList;
import java.util.List;

public class TherapistBOImpl implements TherapistBO {

    private final TherapistDAO therapistDAO = (TherapistDAO)
            DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST);

    @Override
    public boolean saveTherapist(TherapistDTO dto) throws Exception{
        Therapist therapist = toEntity(dto);

        return therapistDAO.save(therapist);
    }

    @Override
    public boolean updateTherapist(TherapistDTO dto) throws  Exception {
        Therapist therapist = therapistDAO.findById(dto.getId());
        if (therapist == null) {
            throw new Exception(
                  "Therapist ID" + dto.getId() + "not found."
            );
        }
        return therapistDAO.update(toEntity(dto));
    }

    @Override
    public boolean deleteTherapist(int id) {
        return therapistDAO.delete(id);
    }

    @Override
    public TherapistDTO findTherapistById(int id) {
        Therapist therapist = therapistDAO.findById(id);
        if (therapist != null) {
            return new TherapistDTO(
                    therapist.getId(), therapist.getFullName(), therapist.getSpecialization(),
                    therapist.getPhone(), therapist.getEmail(),
                    therapist.getAssignedProgram(), therapist.getAvailability()
            );
        }
        return null;
    }

    @Override
    public List<TherapistDTO> findAllTherapists() {
        List<Therapist> therapists = therapistDAO.findAll();
        List<TherapistDTO> dtos = new ArrayList<>();
        for (Therapist therapist : therapists) {
            dtos.add(new TherapistDTO(
                    therapist.getId(), therapist.getFullName(), therapist.getSpecialization(),
                    therapist.getPhone(), therapist.getEmail(),
                    therapist.getAssignedProgram(), therapist.getAvailability()
            ));
        }
        return dtos;
    }

    private TherapistDTO toDTO(Therapist therapist) throws Exception{
        return new TherapistDTO(
                therapist.getId(),
                therapist.getFullName(),
                therapist.getSpecialization(),
                therapist.getPhone(),
                therapist.getEmail(),
                therapist.getAssignedProgram(),
                therapist.getAvailability()
        );

    }
    private Therapist toEntity(TherapistDTO dto) throws Exception{
        return new Therapist(
                dto.getId(),
                dto.getFullName(),
                dto.getSpecialization(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getAssignedProgram(),
                dto.getAvailability()

        );
    }
}
