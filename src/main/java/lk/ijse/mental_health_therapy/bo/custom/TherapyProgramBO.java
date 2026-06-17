package lk.ijse.mental_health_therapy.bo.custom;

import lk.ijse.mental_health_therapy.bo.SuperBO;
import lk.ijse.mental_health_therapy.dto.TherapyProgramDTO;

import java.util.List;

public interface TherapyProgramBO extends SuperBO {

    boolean saveProgram(TherapyProgramDTO dto) throws Exception;

    boolean updateProgram(TherapyProgramDTO dto) throws Exception;

    boolean deleteProgram(String programId) throws Exception;

    List<TherapyProgramDTO> getAllPrograms();

    TherapyProgramDTO getProgramById(String programId) throws Exception;

    boolean programExists(String programId) throws Exception;
}
