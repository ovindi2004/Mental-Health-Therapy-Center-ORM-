package lk.ijse.mental_health_therapy.bo.custom.impl;

import lk.ijse.mental_health_therapy.bo.custom.UserBO;
import lk.ijse.mental_health_therapy.controller.ChangePasswordController;
import lk.ijse.mental_health_therapy.dao.DAOFactory;
import lk.ijse.mental_health_therapy.dao.custom.UserDAO;
import lk.ijse.mental_health_therapy.dto.UserDTO;
import lk.ijse.mental_health_therapy.entity.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserBOImpl implements UserBO {

    private final UserDAO userDAO =
            (UserDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.USER);

    @Override
    public boolean saveUser(UserDTO dto) throws Exception {
        // Password bcrypt hash karala save karanna
        String hashedPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt(10));
        dto.setPassword(hashedPassword);
        return userDAO.save(toEntity(dto));
    }

    @Override
    public boolean updateUser(UserDTO dto) {
        return userDAO.update(toEntity(dto));
    }

    @Override
    public boolean deleteUser(String username) {
        return userDAO.delete(username);
    }

    @Override
    public UserDTO getUser(String username) {
        User user = userDAO.findById(username);
        return user == null ? null : toDTO(user);
    }

    // Current password bcrypt verify
    @Override
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        User user = userDAO.findById(username);
        if (user == null) return false;
        // BCrypt.checkpw() use karanne — plain text vs stored hash compare karanna
        return BCrypt.checkpw(currentPassword, user.getPassword());
    }

    @Override
    public boolean updateCredentials(String oldUsername, String newUsername, String newPassword) {
        // New password hash karala DB eke update karanna
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        boolean dbSuccess = userDAO.updateCredentials(oldUsername, newUsername, hashedPassword);

        if (dbSuccess) {
            // Session username update
            ChangePasswordController.setLoggedInUser(
                    newUsername,
                    ChangePasswordController.getLoggedInRole()
            );
        }

        return dbSuccess;
    }

    // Username taken check
    @Override
    public boolean isUsernameTaken(String newUsername, String currentUsername) {
        if (newUsername.equals(currentUsername)) return false;
        User existing = userDAO.findById(newUsername);
        return existing != null;
    }

    // Mappers
    private User toEntity(UserDTO dto) {
        return new User(dto.getUsername(), dto.getPassword(), dto.getRole(), dto.getEmail());
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword(), user.getRole(), user.getEmail());
    }
}