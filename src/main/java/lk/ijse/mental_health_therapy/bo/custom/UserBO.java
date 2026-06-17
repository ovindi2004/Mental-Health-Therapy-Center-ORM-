package lk.ijse.mental_health_therapy.bo.custom;

import lk.ijse.mental_health_therapy.bo.SuperBO;
import lk.ijse.mental_health_therapy.dto.UserDTO;

public interface UserBO extends SuperBO {
    boolean saveUser(UserDTO userDTO) throws Exception;
    boolean updateUser(UserDTO userDTO);
    boolean deleteUser(String username);
    UserDTO getUser(String username);
    boolean verifyCurrentPassword(String username, String currentPassword);
    boolean updateCredentials(String oldUsername, String newUsername, String newPassword);
    boolean isUsernameTaken(String newUsername, String currentUsername);
}