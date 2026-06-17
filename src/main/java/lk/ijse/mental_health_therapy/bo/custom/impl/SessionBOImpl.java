package lk.ijse.mental_health_therapy.bo.custom.impl;

import lk.ijse.mental_health_therapy.bo.custom.SessionBO;
import lk.ijse.mental_health_therapy.dao.DAOFactory;
import lk.ijse.mental_health_therapy.dao.custom.PatientDAO;
import lk.ijse.mental_health_therapy.dao.custom.SessionDAO;
import lk.ijse.mental_health_therapy.dao.custom.TherapistDAO;
import lk.ijse.mental_health_therapy.dto.SessionDTO;
import lk.ijse.mental_health_therapy.entity.Patient;
import lk.ijse.mental_health_therapy.entity.TherapySession;
import lk.ijse.mental_health_therapy.entity.Therapist;

import java.time.LocalDate;
import java.util.List;

public class SessionBOImpl implements SessionBO {

    private final SessionDAO sessionDAO =
            (SessionDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.SESSION);

    private final PatientDAO patientDAO =
            (PatientDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PATIENT);

    private final TherapistDAO therapistDAO =
            (TherapistDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.THERAPIST);


    @Override
    public boolean bookSession(SessionDTO dto) throws Exception {
        validate(dto);


        if (sessionDAO.hasConflict(dto.getTherapistId(), dto.getSessionDate(),
                dto.getSessionTime(), 0)) {
            throw new IllegalStateException(
                    "Conflict: " + dto.getTherapistName() +
                            " already has a session at " + dto.getSessionTime() +
                            " on " + dto.getSessionDate());
        }

        TherapySession entity = toEntity(dto);
        return sessionDAO.save(entity);
    }

    @Override
    public boolean rescheduleSession(SessionDTO dto) throws Exception {
        if (dto.getId() <= 0)
            throw new IllegalArgumentException("Select a session to reschedule.");
        if (dto.getSessionDate() == null)
            throw new IllegalArgumentException("New session date is required.");
        if (dto.getSessionTime() == null || dto.getSessionTime().isBlank())
            throw new IllegalArgumentException("New session time is required.");

        // Conflict check — exclude the current session itself
        if (sessionDAO.hasConflict(dto.getTherapistId(), dto.getSessionDate(),
                dto.getSessionTime(), dto.getId())) {
            throw new IllegalStateException(
                    "Conflict: " + dto.getTherapistName() +
                            " already has a session at " + dto.getSessionTime() +
                            " on " + dto.getSessionDate());
        }

        dto.setStatus("Rescheduled");
        TherapySession entity = toEntity(dto);
        entity.setId(dto.getId());
        return sessionDAO.update(entity);
    }


    @Override
    public boolean cancelSession(int sessionId) throws Exception {
        if (sessionId <= 0)
            throw new IllegalArgumentException("Select a session to cancel.");

        TherapySession existing = sessionDAO.findById(sessionId);
        if (existing == null)
            throw new IllegalArgumentException("Session not found.");

        existing.setStatus("Cancelled");
        return sessionDAO.update(existing);
    }


    @Override
    public List<SessionDTO> getAllSessions(String statusFilter) throws Exception {
        return sessionDAO.findAllJoined(statusFilter);
    }


    @Override
    public List<SessionDTO> searchSessions(String keyword) throws Exception {
        if (keyword == null || keyword.isBlank())
            return sessionDAO.findAllJoined("All");
        return sessionDAO.search(keyword.trim());
    }


    private TherapySession toEntity(SessionDTO dto) throws Exception {
        Patient patient = patientDAO.findById(dto.getPatientId());
        if (patient == null)
            throw new IllegalArgumentException("Patient not found (ID=" + dto.getPatientId() + ").");

        Therapist therapist = therapistDAO.findById(dto.getTherapistId());
        if (therapist == null)
            throw new IllegalArgumentException("Therapist not found (ID=" + dto.getTherapistId() + ").");

        return new TherapySession(
                dto.getId(),
                patient,
                therapist,
                dto.getProgram(),
                dto.getSessionDate(),
                dto.getSessionTime(),
                dto.getStatus()
        );
    }

    private SessionDTO toDTO(TherapySession session) {
        return new SessionDTO(
                session.getId(),
                session.getPatient() != null ? session.getPatient().getId() : 0,
                session.getPatient() != null ? session.getPatient().getFullName() : "Unknown",
                session.getTherapist() != null ? session.getTherapist().getId() : 0,
                session.getTherapist() != null ? session.getTherapist().getFullName() : "Unknown",
                session.getProgram(),
                session.getSessionDate(),
                session.getSessionTime(),
                session.getStatus()
        );
    }

    private void validate(SessionDTO dto) throws Exception {
        if (dto.getPatientId() <= 0) throw new IllegalArgumentException("Patient must be selected.");
        if (dto.getTherapistId() <= 0) throw new IllegalArgumentException("Therapist must be selected.");
        if (dto.getProgram() == null || dto.getProgram().isBlank())
            throw new IllegalArgumentException("Therapy Program must be selected.");
        if (dto.getSessionDate() == null)
            throw new IllegalArgumentException("Session date is required.");
        if (dto.getSessionDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Session date cannot be in the past.");
        if (dto.getSessionTime() == null || dto.getSessionTime().isBlank())
            throw new IllegalArgumentException("Session time is required.");
    }
}