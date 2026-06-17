package lk.ijse.mental_health_therapy.dao.custom;

import lk.ijse.mental_health_therapy.dao.CrudDAO;
import lk.ijse.mental_health_therapy.dto.SessionDTO;
import lk.ijse.mental_health_therapy.entity.TherapySession;

import java.time.LocalDate;
import java.util.List;

public interface SessionDAO extends CrudDAO<TherapySession, Integer> {

    List<SessionDTO> findAllJoined(String statusFilter);

    List<SessionDTO> search(String keyword);

    boolean hasConflict(int therapistId, LocalDate date, String time, int excludeSessionId);
}
