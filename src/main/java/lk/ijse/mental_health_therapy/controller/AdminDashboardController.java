package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.dto.SessionDTO;
import lk.ijse.mental_health_therapy.entity.TherapySession;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {


    @FXML private Button btnDashboard;


    @FXML private Label lblTotalPatients;
    @FXML private Label lblTodaySessions;
    @FXML private Label lblPendingPayments;
    @FXML private Label lblRevenue;
    @FXML private Label lblWelcome;


    @FXML private TableView<SessionDTO>         tblPatients;
    @FXML private TableColumn<SessionDTO, String> colName;
    @FXML private TableColumn<SessionDTO, String> colPhone;
    @FXML private TableColumn<SessionDTO, String> colEmail;
    @FXML private TableColumn<SessionDTO, String> colProgram;
    @FXML private TableColumn<SessionDTO, String> colAction;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadDashboardData();
    }


    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("program"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));

        // Color-coded status column
        colAction.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setStyle(""); return;
                }
                setText(status);
                switch (status) {
                    case "Scheduled"   -> setStyle("-fx-text-fill: #2563B0; -fx-font-weight: bold;");
                    case "Completed"   -> setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    case "Cancelled"   -> setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    case "Rescheduled" -> setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
                    default            -> setStyle("");
                }
            }
        });
    }


    private void loadDashboardData() {
        Session session = FactoryConfiguration.getInstance().getSession();        try {
            //  Total Therapists
            Long therapistCount = session.createQuery(
                    "SELECT COUNT(t) FROM Therapist t", Long.class).getSingleResult();
            lblTotalPatients.setText(String.valueOf(therapistCount));

            //  Therapy Programs count
            Long programCount = session.createQuery(
                    "SELECT COUNT(p) FROM TherapyProgram p", Long.class).getSingleResult();
            lblTodaySessions.setText(String.valueOf(programCount));

            //  Total Patients
            Long patientCount = session.createQuery(
                    "SELECT COUNT(p) FROM Patient p", Long.class).getSingleResult();
            lblPendingPayments.setText(String.valueOf(patientCount));

            //  Today's Sessions
            Long todaySessions = session.createQuery(
                            "SELECT COUNT(s) FROM TherapySession s " +
                                    "WHERE s.sessionDate = :today AND s.status <> 'Cancelled'",
                            Long.class)
                    .setParameter("today", LocalDate.now())
                    .getSingleResult();
            lblRevenue.setText(String.valueOf(todaySessions));

            //  Recent Sessions table — last 10, ID descending
            List<TherapySession> recentSessions = session.createQuery(
                            "SELECT s FROM TherapySession s " +
                                    "LEFT JOIN FETCH s.patient " +
                                    "LEFT JOIN FETCH s.therapist " +
                                    "ORDER BY s.id DESC",
                            TherapySession.class)
                    .setMaxResults(10)
                    .getResultList();

            ObservableList<SessionDTO> tableData = FXCollections.observableArrayList();
            for (TherapySession s : recentSessions) {
                tableData.add(new SessionDTO(
                        s.getId(),
                        s.getPatient()   != null ? s.getPatient().getId()         : 0,
                        s.getPatient()   != null ? s.getPatient().getFullName()   : "Unknown",
                        s.getTherapist() != null ? s.getTherapist().getId()       : 0,
                        s.getTherapist() != null ? s.getTherapist().getFullName() : "Unknown",
                        s.getProgram(),
                        s.getSessionDate(),
                        s.getSessionTime(),
                        s.getStatus()
                ));
            }
            tblPatients.setItems(tableData);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }


    @FXML
    void showDashboard(ActionEvent event) {
        loadDashboardData();
    }

    @FXML
    void patientmanagement(ActionEvent event) {
        Launcher.setRoot("PatientManagement");
    }

    @FXML
    void therapistmanagement(ActionEvent event) {
        Launcher.setRoot("TherapistManagement");
    }

    @FXML
    void therapyprograms(ActionEvent event) {
        Launcher.setRoot("TherapyProgramManagement");
    }

    @FXML
    void session(ActionEvent event) {
        Launcher.setRoot("SessionScheduling");
    }




    @FXML
    void showPayments(ActionEvent event) {
        Launcher.setRoot("payment");
    }

    @FXML
    void report(ActionEvent event) {
        Launcher.setRoot("Reports");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Launcher.setRoot("login");
    }

    @FXML
    void showChangePassword(ActionEvent event) {
        Launcher.setRoot("ChangePassword");
    }

    @FXML
    void addNewPatient(ActionEvent event) {
        Launcher.setRoot("PatientManagement");
    }
}