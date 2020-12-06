package lab_2.process_service.repositories;

import lab_2.process_service.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Transaction findTransactionByTransactionId(String transactionId);

    List<Transaction> findAll();

    List<Transaction> findTransactionsByUserId(Integer userId);
}
