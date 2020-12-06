package lab_2.process_service.entities;

import java.util.List;

public class RollBackInsurance {
    String transactionId;
    List<Insurance> insurance;
    int numberOfRollbacks;

    public RollBackInsurance(List<Insurance> insurance, String transactionId) {
        this.transactionId = transactionId;
        this.insurance = insurance;
        this.numberOfRollbacks = 0;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<Insurance>getInsurance() {
        return insurance;
    }

    public void setInsurance(List<Insurance> insurance) {
        this.insurance = insurance;
    }

    public int getNumberOfRollbacks() {
        return numberOfRollbacks;
    }

    public void setNumberOfRollbacks(int numberOfRollbacks) {
        this.numberOfRollbacks = numberOfRollbacks;
    }
}
