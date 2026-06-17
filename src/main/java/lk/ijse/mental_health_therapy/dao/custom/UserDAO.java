package lk.ijse.mental_health_therapy.dao.custom;

import lk.ijse.mental_health_therapy.dao.CrudDAO;
import lk.ijse.mental_health_therapy.entity.User;

public interface UserDAO extends CrudDAO<User, String> {
    boolean updateCredentials(String oldUsername, String newUsername, String newPassword);
}