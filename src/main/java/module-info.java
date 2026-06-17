module lk.ijse.mental_health_therapy {

    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.hibernate.orm.core;
    requires jakarta.persistence;

    requires spring.security.crypto;
    requires java.naming;
    requires java.sql;

    requires static lombok;

    requires org.slf4j;
    requires javafx.base;
    requires jbcrypt;

    opens lk.ijse.mental_health_therapy to javafx.fxml;
    opens lk.ijse.mental_health_therapy.controller to javafx.fxml;
    opens lk.ijse.mental_health_therapy.entity to org.hibernate.orm.core;
    opens lk.ijse.mental_health_therapy.dto to javafx.base;

    exports lk.ijse.mental_health_therapy;

}