package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.dto.PatientDTO;
import lk.ijse.mental_health_therapy.entity.Patient;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReceptionistDashboardController implements Initializable {


    @FXML private Label lblWelcome;
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTodaySessions;
    @FXML private Label lblPendingPayments;
    @FXML private Label lblRevenue;

    @FXML private TextField txtSearchPatient;

    @FXML private TableView<PatientDTO>            tblPatients;
    @FXML private TableColumn<PatientDTO, Integer> colId;
    @FXML private TableColumn<PatientDTO, String>  colName;
    @FXML private TableColumn<PatientDTO, String>  colPhone;
    @FXML private TableColumn<PatientDTO, String>  colEmail;
    @FXML private TableColumn<PatientDTO, String>  colProgram;
    @FXML private TableColumn<PatientDTO, String>  colAction;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadDashboardData();
        setupSearch();
    }


    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("therapyProgress"));


        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("View");
            {
                btnView.setStyle(
                        "-fx-background-color: #3A7BD5; -fx-text-fill: white;" +
                                "-fx-background-radius: 6; -fx-font-size: 11px;" +
                                "-fx-padding: 4 10; -fx-cursor: hand;"
                );
                btnView.setOnAction(e -> {
                    PatientDTO p = getTableView().getItems().get(getIndex());
                    Launcher.setRoot("PatientManagement");
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnView);
            }
        });
    }


    private void loadDashboardData() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {

            Long patientCount = session.createQuery(
                            "SELECT COUNT(p) FROM Patient p", Long.class)
                    .getSingleResult();
            lblTotalPatients.setText(String.valueOf(patientCount));


            Long todaySessions = session.createQuery(
                            "SELECT COUNT(s) FROM TherapySession s " +
                                    "WHERE s.sessionDate = :today AND s.status <> 'Cancelled'",
                            Long.class)
                    .setParameter("today", LocalDate.now())
                    .getSingleResult();
            lblTodaySessions.setText(String.valueOf(todaySessions));


            Long pendingPayments = session.createQuery(
                            "SELECT COUNT(p) FROM Payment p " +
                                    "WHERE p.paymentStatus = 'Pending'",
                            Long.class)
                    .getSingleResult();
            lblPendingPayments.setText(String.valueOf(pendingPayments));


            BigDecimal revenueToday = session.createQuery(
                            "SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p " +
                                    "WHERE p.paymentDate = :today AND p.paymentStatus = 'Paid'",
                            BigDecimal.class)
                    .setParameter("today", LocalDate.now())
                    .getSingleResult();
            lblRevenue.setText("LKR " + String.format("%,.2f", revenueToday));


            loadPatients(session, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }


    private void loadPatients(EntityManager em, String keyword) {
        List<Patient> patients;

        if (keyword == null || keyword.isBlank()) {
            patients = em.createQuery(
                            "FROM Patient ORDER BY id ASC", Patient.class)
                    .getResultList();
        } else {
            String like = "%" + keyword.toLowerCase() + "%";
            patients = em.createQuery(
                            "FROM Patient p WHERE " +
                                    "LOWER(p.fullName) LIKE :kw OR " +
                                    "LOWER(p.email)    LIKE :kw OR " +
                                    "LOWER(p.contactNumber) LIKE :kw " +
                                    "ORDER BY p.id ASC",
                            Patient.class)
                    .setParameter("kw", like)
                    .getResultList();
        }

        ObservableList<PatientDTO> data = FXCollections.observableArrayList();
        for (Patient p : patients) {
            data.add(new PatientDTO(
                    p.getId(),
                    p.getFullName(),
                    p.getDateOfBirth(),
                    p.getGender(),
                    p.getAddress(),
                    p.getContactNumber(),
                    p.getEmail(),
                    p.getTherapyProgress()
            ));
        }
        tblPatients.setItems(data);
    }


    private void setupSearch() {
        txtSearchPatient.setOnKeyReleased((KeyEvent e) -> {
            String keyword = txtSearchPatient.getText().trim();
            Session session = FactoryConfiguration.getInstance().getSession();
            try {
                loadPatients(session, keyword);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                session.close();
            }
        });
    }


    @FXML
    void showDashboard(ActionEvent event) {
        loadDashboardData();
    }

    @FXML
    void showPatientManagement(ActionEvent event) {
        Launcher.setRoot("PatientManagement");
    }

    @FXML
    void showSessionScheduling(ActionEvent event) {
        Launcher.setRoot("SessionScheduling");
    }

    @FXML
    void showPayments(ActionEvent event) {
        Launcher.setRoot("payment");
    }

    @FXML
    void showReports(ActionEvent event) {
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