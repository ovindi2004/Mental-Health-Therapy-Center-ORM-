package lk.ijse.mental_health_therapy.bo;

import lk.ijse.mental_health_therapy.bo.custom.impl.*;

public class BOFactory {
    private static BOFactory instance;
    public BOFactory(){}
    public static BOFactory getInstance(){return instance == null ? instance = new BOFactory() : instance;}

    public enum BO_Types {
        PATIENT, THERAPIST, THERAPY_PROGRAM, SESSION, PAYMENT, USER
    }

    public SuperBO getBO(BO_Types boTypes) {
        switch (boTypes) {
            case PATIENT:
                return new PatientBOImpl();
            case THERAPIST:
                return new TherapistBOImpl();
            case THERAPY_PROGRAM:
                return new TherapyProgramBOImpl();
            case SESSION:
                return new SessionBOImpl();
            case PAYMENT:
                return new PaymentBOImpl();
            case USER:
                return new UserBOImpl();
            default:
                return null;
        }
    }
}