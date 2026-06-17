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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.PaymentBO;
import lk.ijse.mental_health_therapy.dto.PaymentDTO;
import lk.ijse.mental_health_therapy.entity.Patient;
import lk.ijse.mental_health_therapy.entity.TherapyProgram;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PaymentController implements Initializable {

    @FXML
    private ComboBox<String> cmbPatient;
    @FXML
    private ComboBox<String> cmbProgram;
    @FXML
    private Label lblProgramFee;
    @FXML
    private TextField txtAmountPaid;
    @FXML
    private Label lblAmountError;
    @FXML
    private ComboBox<String> cmbPaymentMethod;
    @FXML
    private DatePicker dpPaymentDate;
    @FXML
    private ComboBox<String> cmbPaymentStatus;
    @FXML
    private Label lblPaymentStatus;

    @FXML
    private Label lblTotalCollected;
    @FXML
    private Label lblPendingAmount;
    @FXML
    private Label lblTransactionCount;

    @FXML
    private ComboBox<String> cmbFilterPayment;
    @FXML
    private TableView<PaymentDTO> tblPayments;

    @FXML
    private TableColumn<PaymentDTO, Integer> colId;
    @FXML
    private TableColumn<PaymentDTO, String> colPatient;
    @FXML
    private TableColumn<PaymentDTO, String> colProgram;
    @FXML
    private TableColumn<PaymentDTO, BigDecimal> colAmount;
    @FXML
    private TableColumn<PaymentDTO, String> colMethod;
    @FXML
    private TableColumn<PaymentDTO, LocalDate> colDate;
    @FXML
    private TableColumn<PaymentDTO, String> colStatus;


    PaymentBO paymentBO = (PaymentBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.PAYMENT);


    private final Map<String, Integer> patientMap = new LinkedHashMap<>();

    private final Map<String, String> programMap = new LinkedHashMap<>();

    private int selectedPaymentId = -1;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadComboData();
        dpPaymentDate.setValue(LocalDate.now());
        refreshTable("All");
        refreshSummaryCards();
    }


    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

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
                    case "Paid" -> setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    case "Pending" -> setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
                    case "Partial" -> setStyle("-fx-text-fill: #2563B0; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });
    }

    // ── Load ComboBox data from DB via Hibernate ──────────────────────────
    private void loadComboData() {

        cmbPaymentMethod.setItems(FXCollections.observableArrayList(
                "Cash", "Card", "Bank Transfer", "Online Payment", "Cheque"));
        cmbPaymentMethod.getSelectionModel().selectFirst();

        cmbPaymentStatus.setItems(FXCollections.observableArrayList(
                "Paid", "Pending", "Partial"));
        cmbPaymentStatus.getSelectionModel().selectFirst();

        cmbFilterPayment.setItems(FXCollections.observableArrayList(
                "All", "Paid", "Pending", "Partial"));
        cmbFilterPayment.getSelectionModel().selectFirst();
        cmbFilterPayment.setOnAction(e -> refreshTable(cmbFilterPayment.getValue()));

        loadPatients();
        loadPrograms();
    }

    private void loadPatients() {
        patientMap.clear();
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<Patient> patients = session.createQuery(
                    "FROM Patient ORDER BY fullName", Patient.class).getResultList();
            ObservableList<String> names = FXCollections.observableArrayList();
            for (Patient p : patients) {
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

    private void loadPrograms() {
        programMap.clear();
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<TherapyProgram> programs = session.createQuery(
                    "FROM TherapyProgram ORDER BY programName", TherapyProgram.class).getResultList();
            ObservableList<String> items = FXCollections.observableArrayList();
            for (TherapyProgram tp : programs) {
                String display = tp.getProgramName() +
                        " (LKR " + String.format("%.2f", tp.getFee()) + ")";
                programMap.put(display, tp.getProgramId());
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
    void onPatientSelected(ActionEvent event) {

    }

    @FXML
    void onProgramSelected(ActionEvent event) {
        String selected = cmbProgram.getValue();
        if (selected == null) return;
        String programId = programMap.get(selected);
        if (programId == null) return;
        double fee = paymentBO.getProgramFee(programId);
        lblProgramFee.setText(String.format("LKR %.2f", fee));
        txtAmountPaid.setText(String.format("%.2f", fee));
    }

    @FXML
    void processPayment(ActionEvent event) {
        lblAmountError.setText("");
        lblPaymentStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
        lblPaymentStatus.setText("");

        if (cmbPatient.getValue() == null) {
            showError("Please select a patient.");
            return;
        }
        if (cmbProgram.getValue() == null) {
            showError("Please select a therapy program.");
            return;
        }
        if (txtAmountPaid.getText().isBlank()) {
            lblAmountError.setText("Amount is required.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(txtAmountPaid.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            lblAmountError.setText("Enter a valid positive amount.");
            return;
        }

        if (dpPaymentDate.getValue() == null) {
            showError("Please select a payment date.");
            return;
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setPatientId(patientMap.get(cmbPatient.getValue()));
        dto.setProgramId(programMap.get(cmbProgram.getValue()));
        dto.setAmountPaid(amount);
        dto.setPaymentMethod(cmbPaymentMethod.getValue());
        dto.setPaymentDate(dpPaymentDate.getValue());
        dto.setPaymentStatus(cmbPaymentStatus.getValue());

        try {
            boolean saved = paymentBO.savePayment(dto);
            if (saved) {
                lblPaymentStatus.setText("✔ Payment processed successfully.");
                clearForm();
                refreshTable(cmbFilterPayment.getValue());
                refreshSummaryCards();
            } else {
                showError("Payment processing failed.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    @FXML
    void onTableRowClick(MouseEvent event) {
        PaymentDTO selected = tblPayments.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selectedPaymentId = selected.getPaymentId();

        cmbPatient.setValue(selected.getPatientName());
        cmbProgram.getItems().stream()
                .filter(s -> s.startsWith(selected.getProgramName()))
                .findFirst()
                .ifPresent(cmbProgram::setValue);

        lblProgramFee.setText(String.format("LKR %.2f", selected.getProgramFee()));
        txtAmountPaid.setText(selected.getAmountPaid().toPlainString());
        cmbPaymentMethod.setValue(selected.getPaymentMethod());
        dpPaymentDate.setValue(selected.getPaymentDate());
        cmbPaymentStatus.setValue(selected.getPaymentStatus());
        lblPaymentStatus.setStyle("-fx-text-fill: #2563B0; -fx-font-size: 12px;");
        lblPaymentStatus.setText("Viewing payment #" + selectedPaymentId);
    }

    @FXML
    void printInvoice(ActionEvent event) {
        PaymentDTO selected = tblPayments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a payment record to print.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice - Payment #" + selected.getPaymentId());
        alert.setHeaderText("SERENITY Mental Health Therapy\nPayment Invoice");
        alert.setContentText(buildInvoice(selected));
        alert.showAndWait();
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
            List<PaymentDTO> list = paymentBO.getAllPayments(filter);
            tblPayments.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ex) {
            showError("Failed to load payments: " + ex.getMessage());
        }
    }

    private void refreshSummaryCards() {
        try {
            lblTotalCollected.setText(String.format("LKR %.0f", paymentBO.getTotalCollected()));
            lblPendingAmount.setText(String.format("LKR %.0f", paymentBO.getTotalPending()));
            lblTransactionCount.setText(String.valueOf(paymentBO.getTransactionCount()));
        } catch (Exception ex) {
            showError("Failed to load summary: " + ex.getMessage());
        }
    }


    private void clearForm() {
        selectedPaymentId = -1;
        cmbPatient.getSelectionModel().clearSelection();
        cmbProgram.getSelectionModel().clearSelection();
        lblProgramFee.setText("LKR 0.00");
        txtAmountPaid.clear();
        lblAmountError.setText("");
        cmbPaymentMethod.getSelectionModel().selectFirst();
        dpPaymentDate.setValue(LocalDate.now());
        cmbPaymentStatus.getSelectionModel().selectFirst();
        lblPaymentStatus.setText("");
    }

    private String buildInvoice(PaymentDTO dto) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        return String.format(
                "Invoice No  : PAY-%05d%n" +
                        "Date        : %s%n" +
                        "-----------------------------%n" +
                        "Patient     : %s%n" +
                        "Program     : %s%n" +
                        "Program Fee : LKR %.2f%n" +
                        "Amount Paid : LKR %.2f%n" +
                        "Method      : %s%n" +
                        "Status      : %s%n" +
                        "-----------------------------%n" +
                        "Thank you for choosing Serenity!",
                dto.getPaymentId(),
                dto.getPaymentDate().format(fmt),
                dto.getPatientName(),
                dto.getProgramName(),
                dto.getProgramFee(),
                dto.getAmountPaid(),
                dto.getPaymentMethod(),
                dto.getPaymentStatus());
    }

    private void showError(String msg) {
        lblPaymentStatus.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        lblPaymentStatus.setText(msg);
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}