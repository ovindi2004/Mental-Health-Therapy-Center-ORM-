package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.PatientBO;
import lk.ijse.mental_health_therapy.bo.custom.PaymentBO;
import lk.ijse.mental_health_therapy.bo.custom.SessionBO;
import lk.ijse.mental_health_therapy.bo.custom.TherapistBO;
import lk.ijse.mental_health_therapy.dto.PaymentDTO;
import lk.ijse.mental_health_therapy.dto.SessionDTO;
import lk.ijse.mental_health_therapy.dto.TherapistDTO;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController implements Initializable {


    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalSessions;
    @FXML private Label lblActivePatients;
    @FXML private Label lblActiveTherapists;


    @FXML private TabPane tabPane;


    @FXML private TableView<TherapistReportRow>      tblTherapistReport;
    @FXML private TableColumn<TherapistReportRow, String>  colTherapistName;
    @FXML private TableColumn<TherapistReportRow, Integer> colTotalSessions;
    @FXML private TableColumn<TherapistReportRow, Integer> colCompleted;
    @FXML private TableColumn<TherapistReportRow, Integer> colCancelled;
    @FXML private TableColumn<TherapistReportRow, String>  colProgram;


    @FXML private TableView<PaymentDTO>               tblFinancialReport;
    @FXML private TableColumn<PaymentDTO, String>     colFPatient;
    @FXML private TableColumn<PaymentDTO, String>     colFProgram;
    @FXML private TableColumn<PaymentDTO, BigDecimal> colFAmount;
    @FXML private TableColumn<PaymentDTO, String>     colFMethod;
    @FXML private TableColumn<PaymentDTO, LocalDate>  colFDate;
    @FXML private TableColumn<PaymentDTO, String>     colFStatus;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;


    @FXML private TableView<SessionDTO>               tblPatientHistory;
    @FXML private TableColumn<SessionDTO, String>     colHPatient;
    @FXML private TableColumn<SessionDTO, String>     colHProgram;
    @FXML private TableColumn<SessionDTO, String>     colHTherapist;
    @FXML private TableColumn<SessionDTO, LocalDate>  colHDate;
    @FXML private TableColumn<SessionDTO, String>     colHStatus;
    @FXML private TextField txtSearchHistory;


    private final PaymentBO   paymentBO   = (PaymentBO)   BOFactory.getInstance().getBO(BOFactory.BO_Types.PAYMENT);
    private final SessionBO   sessionBO   = (SessionBO)   BOFactory.getInstance().getBO(BOFactory.BO_Types.SESSION);
    private final PatientBO   patientBO   = (PatientBO)   BOFactory.getInstance().getBO(BOFactory.BO_Types.PATIENT);
    private final TherapistBO therapistBO = (TherapistBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.THERAPIST);


    private List<SessionDTO>  allSessions  = new ArrayList<>();
    private List<PaymentDTO>  allPayments  = new ArrayList<>();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTherapistTable();
        setupFinancialTable();
        setupHistoryTable();

        loadSummaryCards();
        loadTherapistReport();
        loadFinancialReport();
        loadPatientHistory();
    }


    private void setupTherapistTable() {
        colTherapistName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().therapistName));
        colTotalSessions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().totalSessions));
        colCompleted.setCellValueFactory(d ->     new javafx.beans.property.SimpleObjectProperty<>(d.getValue().completed));
        colCancelled.setCellValueFactory(d ->     new javafx.beans.property.SimpleObjectProperty<>(d.getValue().cancelled));
        colProgram.setCellValueFactory(d ->       new javafx.beans.property.SimpleStringProperty(d.getValue().assignedProgram));
    }

    private void setupFinancialTable() {
        colFPatient.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPatientName()));
        colFProgram.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getProgramName()));
        colFAmount.setCellValueFactory(d ->  new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getAmountPaid()));
        colFMethod.setCellValueFactory(d ->  new javafx.beans.property.SimpleStringProperty(d.getValue().getPaymentMethod()));
        colFDate.setCellValueFactory(d ->    new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getPaymentDate()));
        colFStatus.setCellValueFactory(d ->  new javafx.beans.property.SimpleStringProperty(d.getValue().getPaymentStatus()));
    }

    private void setupHistoryTable() {
        colHPatient.setCellValueFactory(d ->    new javafx.beans.property.SimpleStringProperty(d.getValue().getPatientName()));
        colHProgram.setCellValueFactory(d ->    new javafx.beans.property.SimpleStringProperty(d.getValue().getProgram()));
        colHTherapist.setCellValueFactory(d ->  new javafx.beans.property.SimpleStringProperty(d.getValue().getTherapistName()));
        colHDate.setCellValueFactory(d ->       new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getSessionDate()));
        colHStatus.setCellValueFactory(d ->     new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));
    }


    private void loadSummaryCards() {
        try {

            double revenue = paymentBO.getTotalCollected();
            lblTotalRevenue.setText(String.format("LKR %,.2f", revenue));


            long completedCount = sessionBO.getAllSessions("Completed").size();
            lblTotalSessions.setText(String.valueOf(completedCount));


            int patientCount = patientBO.findAllPatients().size();
            lblActivePatients.setText(String.valueOf(patientCount));


            int therapistCount = therapistBO.findAllTherapists().size();
            lblActiveTherapists.setText(String.valueOf(therapistCount));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load summary: " + e.getMessage());
        }
    }



    private void loadTherapistReport() {
        try {

            List<SessionDTO>   sessions   = sessionBO.getAllSessions("All");
            List<TherapistDTO> therapists = therapistBO.findAllTherapists();


            Map<Integer, List<SessionDTO>> byTherapist = sessions.stream()
                    .collect(Collectors.groupingBy(SessionDTO::getTherapistId));

            ObservableList<TherapistReportRow> rows = FXCollections.observableArrayList();

            for (TherapistDTO t : therapists) {
                List<SessionDTO> ts = byTherapist.getOrDefault(t.getId(), Collections.emptyList());

                long total     = ts.size();
                long completed = ts.stream().filter(s -> "Completed".equals(s.getStatus())).count();
                long cancelled = ts.stream().filter(s -> "Cancelled".equals(s.getStatus())).count();

                rows.add(new TherapistReportRow(
                        t.getFullName(),
                        (int) total,
                        (int) completed,
                        (int) cancelled,
                        t.getAssignedProgram() != null ? t.getAssignedProgram() : "—"
                ));
            }

            tblTherapistReport.setItems(rows);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load therapist report: " + e.getMessage());
        }
    }



    private void loadFinancialReport() {
        try {
            allPayments = paymentBO.getAllPayments("All");
            tblFinancialReport.setItems(FXCollections.observableArrayList(allPayments));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load financial report: " + e.getMessage());
        }
    }

    @FXML
    void filterFinancialReport(ActionEvent event) {
        LocalDate from = dpFromDate.getValue();
        LocalDate to   = dpToDate.getValue();

        if (from == null && to == null) {
            // No filter — show all
            tblFinancialReport.setItems(FXCollections.observableArrayList(allPayments));
            return;
        }

        List<PaymentDTO> filtered = allPayments.stream()
                .filter(p -> {
                    LocalDate d = p.getPaymentDate();
                    boolean afterFrom = (from == null) || !d.isBefore(from);
                    boolean beforeTo  = (to   == null) || !d.isAfter(to);
                    return afterFrom && beforeTo;
                })
                .collect(Collectors.toList());

        tblFinancialReport.setItems(FXCollections.observableArrayList(filtered));

        if (filtered.isEmpty()) {
            showInfo("No payments found for the selected date range.");
        }
    }


    private void loadPatientHistory() {
        try {
            allSessions = sessionBO.getAllSessions("All");
            tblPatientHistory.setItems(FXCollections.observableArrayList(allSessions));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load patient history: " + e.getMessage());
        }
    }

    @FXML
    void searchHistory(KeyEvent event) {
        String keyword = txtSearchHistory.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            tblPatientHistory.setItems(FXCollections.observableArrayList(allSessions));
            return;
        }

        List<SessionDTO> filtered = allSessions.stream()
                .filter(s ->
                        s.getPatientName().toLowerCase().contains(keyword)  ||
                                s.getProgram().toLowerCase().contains(keyword)       ||
                                s.getTherapistName().toLowerCase().contains(keyword) ||
                                s.getStatus().toLowerCase().contains(keyword)
                )
                .collect(Collectors.toList());

        tblPatientHistory.setItems(FXCollections.observableArrayList(filtered));
    }


    @FXML
    void exportTherapistReport(ActionEvent event) {
        ObservableList<TherapistReportRow> data = tblTherapistReport.getItems();
        if (data == null || data.isEmpty()) {
            showError("No data to export.");
            return;
        }

        File file = chooseSaveFile("therapist_report.csv");
        if (file == null) return;

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Therapist,Total Sessions,Completed,Cancelled,Assigned Program\n");
            for (TherapistReportRow r : data) {
                fw.write(String.format("%s,%d,%d,%d,%s\n",
                        escapeCsv(r.therapistName),
                        r.totalSessions,
                        r.completed,
                        r.cancelled,
                        escapeCsv(r.assignedProgram)));
            }
            showInfo("Therapist report exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    @FXML
    void exportFinancialReport(ActionEvent event) {
        ObservableList<PaymentDTO> data = tblFinancialReport.getItems();
        if (data == null || data.isEmpty()) {
            showError("No data to export.");
            return;
        }

        File file = chooseSaveFile("financial_report.csv");
        if (file == null) return;

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Patient,Program,Amount (LKR),Method,Date,Status\n");
            for (PaymentDTO p : data) {
                fw.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(p.getPatientName()),
                        escapeCsv(p.getProgramName()),
                        p.getAmountPaid().toPlainString(),
                        escapeCsv(p.getPaymentMethod()),
                        p.getPaymentDate().toString(),
                        escapeCsv(p.getPaymentStatus())));
            }
            showInfo("Financial report exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }



    @FXML
    void goBack(ActionEvent event) {
        if ("RECEPTIONIST".equals(Launcher.currentRole)) {
            Launcher.setRoot("ReceptionistDashboard");
        } else {
            Launcher.setRoot("admindashboard");
        }
    }



    private File chooseSaveFile(String defaultName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV Report");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fc.showSaveDialog(tblTherapistReport.getScene().getWindow());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }



    public static class TherapistReportRow {
        public final String therapistName;
        public final int    totalSessions;
        public final int    completed;
        public final int    cancelled;
        public final String assignedProgram;

        public TherapistReportRow(String therapistName, int totalSessions,
                                  int completed, int cancelled, String assignedProgram) {
            this.therapistName   = therapistName;
            this.totalSessions   = totalSessions;
            this.completed       = completed;
            this.cancelled       = cancelled;
            this.assignedProgram = assignedProgram;
        }
    }
}