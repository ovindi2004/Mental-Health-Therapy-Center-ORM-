package lk.ijse.mental_health_therapy.bo.custom.impl;

import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.bo.custom.TherapyProgramBO;
import lk.ijse.mental_health_therapy.dao.DAOFactory;
import lk.ijse.mental_health_therapy.dao.custom.TherapyProgramDAO;
import lk.ijse.mental_health_therapy.dto.TherapyProgramDTO;
import lk.ijse.mental_health_therapy.entity.Therapist;
import lk.ijse.mental_health_therapy.entity.TherapyProgram;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.util.List;

public class TherapyProgramBOImpl implements TherapyProgramBO {

    private final TherapyProgramDAO therapyProgramDAO = (TherapyProgramDAO)
            DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPY_PROGRAM);
    @Override
    public boolean saveProgram(TherapyProgramDTO dto) throws Exception {
        validate(dto);
        if (therapyProgramDAO.existsById(dto.getProgramId())) {
            throw new IllegalArgumentException("Program ID \"" + dto.getProgramId() + "\" already exists.");
        }
        TherapyProgram entity = toEntity(dto);
        return therapyProgramDAO.save(entity);
    }

    @Override
    public boolean updateProgram(TherapyProgramDTO dto) throws Exception{
        validate(dto);
        TherapyProgram entity = toEntity(dto);
        return therapyProgramDAO.update(entity);
    }

    @Override
    public boolean deleteProgram(String programId) throws Exception {
        if (programId == null || programId.isBlank())
            throw new IllegalArgumentException("Program ID is required for deletion.");
        return therapyProgramDAO.delete(programId);
    }

    @Override
    public List<TherapyProgramDTO> getAllPrograms() {
        return therapyProgramDAO.findAllWithTherapist();
    }

    @Override
    public TherapyProgramDTO getProgramById(String programId) throws Exception{
        TherapyProgram entity = therapyProgramDAO.findById(programId);
        if (entity == null) return null;

        TherapyProgramDTO dto = new TherapyProgramDTO();
        dto.setProgramId(entity.getProgramId());
        dto.setProgramName(entity.getProgramName());
        dto.setDuration(entity.getDuration());
        dto.setFee(entity.getFee());
        dto.setDescription(entity.getDescription());
        if (entity.getTherapist() != null) {
            dto.setTherapistId(entity.getTherapist().getId());
            dto.setTherapistName(entity.getTherapist().getFullName());
        } else {
            dto.setTherapistId(0);
            dto.setTherapistName("Not Assigned");
        }
        return dto;
    }

    @Override
    public boolean programExists(String programId)throws Exception {

        return therapyProgramDAO.existsById(programId);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private TherapyProgram toEntity(TherapyProgramDTO dto)throws Exception {
        Therapist therapist = null;
        if (dto.getTherapistId() > 0) {
            // Fetch the managed Therapist reference from DB
            Session session = FactoryConfiguration.getInstance().getSession();
            try {
                therapist = session.find(Therapist.class, dto.getTherapistId());
            } finally {
                session.close();
            }
        }
        return new TherapyProgram(
                dto.getProgramId(),
                dto.getProgramName(),
                dto.getDuration(),
                dto.getFee(),
                dto.getDescription(),
                therapist
        );
    }

    private TherapyProgramDTO toDTO(TherapyProgram therapyProgram) throws Exception{
        return new TherapyProgramDTO(
                therapyProgram.getProgramId(),
                therapyProgram.getProgramName(),
                therapyProgram.getDuration(),
                therapyProgram.getFee(),
                therapyProgram.getDescription(),
                therapyProgram.getTherapist() != null ? therapyProgram.getTherapist().getId() : 0,
                therapyProgram.getTherapist() != null ? therapyProgram.getTherapist().getFullName() : "Not Assigned"
        );
    }

    private void validate(TherapyProgramDTO dto) {
        if (dto.getProgramId() == null || dto.getProgramId().isBlank())
            throw new IllegalArgumentException("Program ID is required.");
        if (dto.getProgramName() == null || dto.getProgramName().isBlank())
            throw new IllegalArgumentException("Program Name is required.");
        if (dto.getFee() <= 0)
            throw new IllegalArgumentException("Fee must be greater than zero.");
    }
}
