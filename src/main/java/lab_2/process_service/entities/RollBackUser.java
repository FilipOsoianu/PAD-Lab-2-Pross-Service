package lab_2.process_service.entities;

public class RollBackUser {
    String transactionId;
    User user;
    int numberOfRollbacks;

    public RollBackUser(User user, String transactionId) {
        this.transactionId = transactionId;
        this.user = user;
        this.numberOfRollbacks = 0;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getNumberOfRollbacks() {
        return numberOfRollbacks;
    }

    public void setNumberOfRollbacks(int numberOfRollbacks) {
        this.numberOfRollbacks = numberOfRollbacks;
    }

}
