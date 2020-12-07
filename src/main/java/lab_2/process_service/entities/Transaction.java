package lab_2.process_service.entities;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String transactionId;
    private String status;
    private int userId;

    public Transaction(String transactionId, String status, int userId) {
        this.transactionId = transactionId;
        this.status = status;
        this.userId = userId;
    }

    public Transaction() {

    }


    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
