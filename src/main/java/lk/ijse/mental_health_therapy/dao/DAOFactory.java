package lk.ijse.mental_health_therapy.dao;

import lk.ijse.mental_health_therapy.dao.custom.impl.*;

public class DAOFactory {
    private static DAOFactory instance;

    private DAOFactory() {}

    public static DAOFactory getInstance() {
        return instance == null ? instance = new DAOFactory() : instance;
    }

    public enum DAOType {
        PATIENT, THERAPIST, THERAPY_PROGRAM, SESSION, PAYMENT, USER
    }

    public SuperDAO getDAO(DAOType daoType) {
        switch (daoType) {
            case PATIENT:
                return new PatientDAOImpl();
            case THERAPIST:
                return new TherapistDAOImpl();
            case THERAPY_PROGRAM:
                return new TherapyProgramDAOImpl();
            case SESSION:
                return new SessionDAOImpl();
            case PAYMENT:
                return new PaymentDAOImpl();
            case USER:
                return new UserDAOImpl();
            default:
                return null;
        }
    }
}