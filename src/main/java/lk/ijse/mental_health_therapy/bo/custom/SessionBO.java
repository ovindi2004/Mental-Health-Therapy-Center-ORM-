package lk.ijse.mental_health_therapy.bo.custom;

import lk.ijse.mental_health_therapy.bo.SuperBO;
import lk.ijse.mental_health_therapy.dto.SessionDTO;

import java.util.List;

public interface SessionBO extends SuperBO {

    boolean bookSession(SessionDTO dto) throws Exception;

    boolean rescheduleSession(SessionDTO dto) throws Exception;

    boolean cancelSession(int sessionId) throws Exception;

    List<SessionDTO> getAllSessions(String statusFilter) throws Exception;

    List<SessionDTO> searchSessions(String keyword) throws Exception;
}
