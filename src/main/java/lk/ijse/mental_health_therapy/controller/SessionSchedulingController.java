package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.SessionBO;
import lk.ijse.mental_health_therapy.dto.SessionDTO;
import lk.ijse.mental_health_therapy.entity.Patient;
import lk.ijse.mental_health_therapy.entity.Therapist;
import lk.ijse.mental_health_therapy.entity.TherapyProgram;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SessionSchedulingController implements Initializable {


    @FXML private ComboBox<String> cmbPatient;
    @FXML private ComboBox<String> cmbProgram;
    @FXML private ComboBox<String> cmbTherapist;
    @FXML private ComboBox<String> cmbSessionTime;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private ComboBox<String> cmbFilterStatus;

    @FXML private DatePicker dpSessionDate;
    @FXML private TextField txtSearchSession;

    @FXML private TableView<SessionDTO> tblSessions;
    @FXML private TableColumn<SessionDTO, Integer> colId;
    @FXML private TableColumn<SessionDTO, String>  colPatient;
    @FXML private TableColumn<SessionDTO, String>  colProgram;
    @FXML private TableColumn<SessionDTO, String>  colTherapist;
    @FXML private TableColumn<SessionDTO, LocalDate> colDate;
    @FXML private TableColumn<SessionDTO, String>  colTime;
    @FXML private TableColumn<SessionDTO, String>  colStatus;

    @FXML private Label lblConflictError;
    @FXML private Label lblStatus;


    SessionBO sessionBO = (SessionBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.SESSION);

    private final Map<String, Integer> patientMap   = new LinkedHashMap<>();
    private final Map<String, Integer> therapistMap = new LinkedHashMap<>();
    private final Map<String, String>  programMap   = new LinkedHashMap<>();

    private int selectedSessionId = -1;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadComboData();
        dpSessionDate.setValue(LocalDate.now());
        cmbStatus.getSelectionModel().selectFirst();
        refreshTable("All");
    }


    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("program"));
        colTherapist.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("sessionTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));


        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
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


    private void loadComboData() {
        cmbSessionTime.setItems(FXCollections.observableArrayList(
                "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
                "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
        ));
        cmbSessionTime.getSelectionModel().selectFirst();

        cmbStatus.setItems(FXCollections.observableArrayList(
                "Scheduled", "Completed", "Cancelled", "Rescheduled"
        ));
        cmbStatus.getSelectionModel().selectFirst();

        cmbFilterStatus.setItems(FXCollections.observableArrayList(
                "All", "Scheduled", "Completed", "Cancelled", "Rescheduled"
        ));
        cmbFilterStatus.getSelectionModel().selectFirst();
        cmbFilterStatus.setOnAction(e -> refreshTable(cmbFilterStatus.getValue()));

        loadPatients();
        loadTherapists();
        loadPrograms();
    }

    private void loadPatients() {
        patientMap.clear();
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<Patient> list = session.createQuery(
                    "FROM Patient ORDER BY fullName", Patient.class).getResultList();
            ObservableList<String> names = FXCollections.observableArrayList();
            for (Patient p : list) {
                patientMap.put(p.getFullName(), p.getId());
                names.add(p.getFullName());
            }
            cmbPatient.setItems(names);
        } catch (Exception e) {
            showError("Failed to load patients: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void loadTherapists() {
        therapistMap.clear();
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<Therapist> list = session.createQuery(
                    "FROM Therapist ORDER BY fullName", Therapist.class).getResultList();
            ObservableList<String> names = FXCollections.observableArrayList();
            for (Therapist t : list) {
                String display = t.getFullName() + " (" + t.getSpecialization() + ")";
                therapistMap.put(display, t.getId());
                names.add(display);
            }
            cmbTherapist.setItems(names);
        } catch (Exception e) {
            showError("Failed to load therapists: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void loadPrograms() {
        programMap.clear();
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<TherapyProgram> list = session.createQuery(
                    "FROM TherapyProgram ORDER BY programName", TherapyProgram.class).getResultList();
            ObservableList<String> items = FXCollections.observableArrayList();
            for (TherapyProgram tp : list) {
                String display = tp.getProgramName() + " (" + tp.getProgramId() + ")";
                programMap.put(display, tp.getProgramName());
                items.add(display);
            }
            cmbProgram.setItems(items);
        } catch (Exception e) {
            showError("Failed to load programs: " + e.getMessage());
        } finally {
            session.close();
        }
    }


    @FXML
    void onProgramSelected(ActionEvent event) {
        String selected = cmbProgram.getValue();
        if (selected == null) return;
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String programId = selected.contains("(")
                    ? selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'))
                    : selected;
            TherapyProgram tp = session.find(TherapyProgram.class, programId);
            if (tp != null && tp.getTherapist() != null) {
                String therapistDisplay = cmbTherapist.getItems().stream()
                        .filter(s -> s.startsWith(tp.getTherapist().getFullName()))
                        .findFirst().orElse(null);
                if (therapistDisplay != null) cmbTherapist.setValue(therapistDisplay);
            }
        } catch (Exception ignored) {
        } finally {
            session.close();
        }
    }


    @FXML
    void bookSession(ActionEvent event) {
        lblConflictError.setText("");
        lblStatus.setText("");

        SessionDTO dto = buildDTO();
        if (dto == null) return;

        try {
            boolean ok = sessionBO.bookSession(dto);
            if (ok) {
                showSuccess("✔ Session booked successfully.");
                clearForm(null);
                refreshTable(cmbFilterStatus.getValue());
            } else {
                showError("Booking failed. Please try again.");
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            lblConflictError.setText("⚠ " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }


    @FXML
    void rescheduleSession(ActionEvent event) {
        lblConflictError.setText("");
        if (selectedSessionId <= 0) {
            lblConflictError.setText("⚠ Select a session from the table to reschedule.");
            return;
        }
        SessionDTO dto = buildDTO();
        if (dto == null) return;
        dto.setId(selectedSessionId);

        try {
            boolean ok = sessionBO.rescheduleSession(dto);
            if (ok) {
                showSuccess("✔ Session rescheduled successfully.");
                clearForm(null);
                refreshTable(cmbFilterStatus.getValue());
            } else {
                showError("Reschedule failed.");
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            lblConflictError.setText("⚠ " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }


    @FXML
    void cancelSession(ActionEvent event) {
        if (selectedSessionId <= 0) {
            showError("Select a session from the table to cancel.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel this session?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    boolean ok = sessionBO.cancelSession(selectedSessionId);
                    if (ok) {
                        showSuccess("✔ Session cancelled.");
                        clearForm(null);
                        refreshTable(cmbFilterStatus.getValue());
                    } else {
                        showError("Cancel failed.");
                    }
                } catch (Exception ex) {
                    showError("Error: " + ex.getMessage());
                }
            }
        });
    }


    @FXML
    void onTableRowClick(MouseEvent event) {
        SessionDTO selected = tblSessions.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selectedSessionId = selected.getId();

        cmbPatient.setValue(selected.getPatientName());

        cmbProgram.getItems().stream()
                .filter(s -> s.contains(selected.getProgram()))
                .findFirst()
                .ifPresent(cmbProgram::setValue);

        cmbTherapist.getItems().stream()
                .filter(s -> s.startsWith(selected.getTherapistName()))
                .findFirst()
                .ifPresent(cmbTherapist::setValue);

        dpSessionDate.setValue(selected.getSessionDate());
        cmbSessionTime.setValue(selected.getSessionTime());
        cmbStatus.setValue(selected.getStatus());
        lblConflictError.setText("");
        showInfo("Viewing session #" + selectedSessionId);
    }


    @FXML
    void searchSession(KeyEvent event) {
        String keyword = txtSearchSession.getText().trim();
        try {
            List<SessionDTO> results = sessionBO.searchSessions(keyword);
            // Sort search results by ID ascending
            results.sort(Comparator.comparingInt(SessionDTO::getId));
            tblSessions.setItems(FXCollections.observableArrayList(results));
            colId.setSortType(TableColumn.SortType.ASCENDING);
            tblSessions.getSortOrder().setAll(colId);
        } catch (Exception ex) {
            showError("Search error: " + ex.getMessage());
        }
    }


    @FXML
    void clearForm(ActionEvent event) {
        selectedSessionId = -1;
        cmbPatient.getSelectionModel().clearSelection();
        cmbProgram.getSelectionModel().clearSelection();
        cmbTherapist.getSelectionModel().clearSelection();
        dpSessionDate.setValue(LocalDate.now());
        cmbSessionTime.getSelectionModel().selectFirst();
        cmbStatus.getSelectionModel().selectFirst();
        lblConflictError.setText("");
        lblStatus.setText("");
        txtSearchSession.clear();
        tblSessions.getSelectionModel().clearSelection();
    }


    @FXML
    void goBack(ActionEvent event) {
        if ("RECEPTIONIST".equals(Launcher.currentRole)) {
            Launcher.setRoot("ReceptionistDashboard");
        } else {
            Launcher.setRoot("admindashboard");
        }
    }


    private void refreshTable(String filter) {
        try {
            List<SessionDTO> list = sessionBO.getAllSessions(filter);


            list.sort(Comparator.comparingInt(SessionDTO::getId));

            tblSessions.setItems(FXCollections.observableArrayList(list));

            colId.setSortType(TableColumn.SortType.ASCENDING);
            tblSessions.getSortOrder().setAll(colId);

        } catch (Exception ex) {
            showError("Failed to load sessions: " + ex.getMessage());
        }
    }

    private SessionDTO buildDTO() {
        String patientName   = cmbPatient.getValue();
        String therapistDisp = cmbTherapist.getValue();
        String programDisp   = cmbProgram.getValue();

        if (patientName == null) {
            lblConflictError.setText("⚠ Please select a patient.");
            return null;
        }
        if (programDisp == null) {
            lblConflictError.setText("⚠ Please select a therapy program.");
            return null;
        }
        if (therapistDisp == null) {
            lblConflictError.setText("⚠ Please select a therapist.");
            return null;
        }
        if (dpSessionDate.getValue() == null) {
            lblConflictError.setText("⚠ Please select a session date.");
            return null;
        }
        if (cmbSessionTime.getValue() == null) {
            lblConflictError.setText("⚠ Please select a session time.");
            return null;
        }

        SessionDTO dto = new SessionDTO();
        dto.setPatientId(patientMap.getOrDefault(patientName, 0));
        dto.setPatientName(patientName);
        dto.setTherapistId(therapistMap.getOrDefault(therapistDisp, 0));
        dto.setTherapistName(therapistDisp);
        dto.setProgram(programMap.getOrDefault(programDisp, programDisp));
        dto.setSessionDate(dpSessionDate.getValue());
        dto.setSessionTime(cmbSessionTime.getValue());
        dto.setStatus(cmbStatus.getValue() != null ? cmbStatus.getValue() : "Scheduled");
        return dto;
    }

    private void showSuccess(String msg) {
        lblStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
        lblStatus.setText(msg);
    }

    private void showError(String msg) {
        lblStatus.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        lblStatus.setText(msg);
    }

    private void showInfo(String msg) {
        lblStatus.setStyle("-fx-text-fill: #2563B0; -fx-font-size: 12px;");
        lblStatus.setText(msg);
    }
}