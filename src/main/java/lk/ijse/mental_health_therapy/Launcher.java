package lk.ijse.mental_health_therapy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class Launcher extends Application {

    public static Stage primaryStage;
    private static boolean isFullScreen = false;

    public static String currentRole = "";

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        primaryStage.setTitle("Mental Health Therapy");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(true);

        seedDefaultUsers();


        setRoot("ReceptionistDashboard");

        primaryStage.setWidth(1200);
        primaryStage.setHeight(700);
        primaryStage.centerOnScreen();

        if (primaryStage.getScene() != null) {
            primaryStage.getScene().getRoot().setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    toggleFullScreen();
                }
            });
        }

        primaryStage.show();
    }

    private static void seedDefaultUsers() {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {

            User admin = session.get(User.class, "admin");
            if (admin == null) {
                Transaction tx = session.beginTransaction();
                session.persist(new User("admin", "admin123", "ADMIN", "admin@serenity.lk"));
                tx.commit();
                System.out.println("[Seed] Default admin user created.");
            }

            User recep = session.get(User.class, "receptionist");
            if (recep == null) {
                Transaction tx = session.beginTransaction();
                session.persist(new User("receptionist", "recep123", "RECEPTIONIST", "recep@serenity.lk"));
                tx.commit();
                System.out.println("[Seed] Default receptionist user created.");
            }

        } catch (Exception e) {
            System.err.println("[Seed] Failed to seed default users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxml) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(Launcher.class.getResource("/" + fxml + ".fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        primaryStage.setFullScreen(isFullScreen);
    }

    public static void main(String[] args) {
        launch(args);
    }
}