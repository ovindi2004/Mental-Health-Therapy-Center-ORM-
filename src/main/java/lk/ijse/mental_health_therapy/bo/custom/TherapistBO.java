package lk.ijse.mental_health_therapy.bo.custom;

import lk.ijse.mental_health_therapy.bo.SuperBO;
import lk.ijse.mental_health_therapy.dto.TherapistDTO;

import java.util.List;

public interface TherapistBO extends SuperBO {
    boolean saveTherapist(TherapistDTO dto) throws Exception;
    boolean updateTherapist(TherapistDTO dto) throws Exception;
    boolean deleteTherapist(int id);
    TherapistDTO findTherapistById(int id);
    List<TherapistDTO> findAllTherapists();
}
