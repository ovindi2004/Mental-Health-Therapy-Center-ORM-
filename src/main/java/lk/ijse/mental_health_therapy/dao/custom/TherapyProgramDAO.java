package lk.ijse.mental_health_therapy.dao.custom;

import lk.ijse.mental_health_therapy.dao.CrudDAO;
import lk.ijse.mental_health_therapy.dto.TherapyProgramDTO;
import lk.ijse.mental_health_therapy.entity.TherapyProgram;

import java.util.List;

public interface TherapyProgramDAO extends CrudDAO<TherapyProgram, String> {


    List<TherapyProgramDTO> findAllWithTherapist();


    boolean existsById(String programId);
}
