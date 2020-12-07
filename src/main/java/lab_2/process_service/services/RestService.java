package lab_2.process_service.services;

import com.google.gson.Gson;

import lab_2.process_service.components.RestTemplateResponseErrorHandler;
import lab_2.process_service.entities.*;
import lab_2.process_service.jms.Receiver;
import lab_2.process_service.jms.Sender;
import lab_2.process_service.repositories.TransactionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RestService {
    private final RestTemplate restTemplate;

    public final String userServiceUrl;
    public final String insuranceServiceUrl;

    @Autowired
    private Sender sender;

    @Autowired
    TransactionRepository transactionRepository;
    private static final Logger logger = LogManager.getLogger(RestService.class);


    @Autowired
    public RestService(RestTemplateBuilder restTemplateBuilder, @Value("${user-service-url}") String userServiceUrl, @Value("${insurance-service-url}") String insuranceServiceUrl) {
        this.restTemplate = restTemplateBuilder
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();
        this.userServiceUrl = userServiceUrl;
        this.insuranceServiceUrl = insuranceServiceUrl;
    }


    public ResponseEntity<Insurance> createInsurance(Insurance insurance) {
        String url = this.insuranceServiceUrl + "/insurances";
        ResponseEntity<User> user = getUserById(insurance.getUserId());
        if (user.getStatusCode().is2xxSuccessful()) {
            insurance.setCost(user.getBody().getBirthDate().toEpochDay());
            HttpEntity<Object> entity = new HttpEntity<>(insurance);
            return this.restTemplate.exchange(url, HttpMethod.POST, entity, Insurance.class);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<User> createUser(User object) {
        String url = this.userServiceUrl + "/users";
        HttpEntity<Object> entity = new HttpEntity<>(object);
        return this.restTemplate.exchange(url, HttpMethod.POST, entity, User.class);
    }

    public ResponseEntity<User> updateUser(String transactionId, User user, Integer userId) {
        String url = this.userServiceUrl + "/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("transactionId", transactionId);
        HttpEntity<Object> entity = new HttpEntity<>(user, headers);
        logger.info("Process receive " + transactionId);
        ResponseEntity<User> updatedUser = this.restTemplate.exchange(url, HttpMethod.PUT, entity, User.class);
        if (updatedUser.getStatusCode().is2xxSuccessful()) {
            this.transactionRepository.save(new Transaction(transactionId, "processing", userId));
            transaction(transactionId, user, updatedUser.getBody(), userId);
            logger.info("Transaction ended successfully transactionId = " + transactionId);
        }
        return updatedUser;
    }

    public void transaction(String transactionId, User originalUser, User updatedUser, Integer userId) {
        ResponseEntity<List<Insurance>> insurancesResponse = getInsurancesByUserId(userId);
        if (insurancesResponse.getStatusCode().is2xxSuccessful()) {
            List<Insurance> insurances = insurancesResponse.getBody();
            if (updateAllInsurances(insurances, transactionId, updatedUser)) {
                changeTransactionStatus(transactionId, "finish");
            } else {
                Gson gson = new Gson();
                RollBackUser rollBackUser = new RollBackUser(originalUser, transactionId);
                RollBackInsurance rollBackInsurance = new RollBackInsurance(insurances, transactionId);
                sender.sendInsurances(gson.toJson(rollBackInsurance));
                sender.sendUser(gson.toJson(rollBackUser));
                logger.info("Start RollBack transactionId = " + transactionId);
            }
        }
    }

    public boolean updateAllInsurances(List<Insurance> insurances, String transactionId, User user) {
        for (Insurance insurance : insurances) {
            insurance.setCost(user.getBirthDate().toEpochDay());
            ResponseEntity<Insurance> insuranceResponseEntity = updateInsurance(transactionId, insurance, insurance.getId());
//          TODO DELELE ! TO TEST ROLLABACK
            if (!insuranceResponseEntity.getStatusCode().is2xxSuccessful()) {
                return false;
            }
        }
        return true;
    }

    public void changeTransactionStatus(String transactionId, String status) {
        Transaction transaction = this.transactionRepository.findTransactionByTransactionId(transactionId);
        transaction.setStatus(status);
        this.transactionRepository.save(transaction);
    }

    public boolean rollBackInsurances(RollBackInsurance rollBackInsurance) {
        for (Insurance insurance : rollBackInsurance.getInsurance()) {
            ResponseEntity<Insurance> insuranceResponseEntity = updateInsurance(rollBackInsurance.getTransactionId(), insurance, insurance.getId());
            if (insuranceResponseEntity.getStatusCode().is2xxSuccessful()) {
                return false;
            }
        }
        return true;
    }

    public boolean rollBackUser(RollBackUser rollBackUser) {
        String url = this.userServiceUrl + "/users/" + rollBackUser.getUser().getId();
        HttpHeaders headers = new HttpHeaders();
        headers.add("transactionId", rollBackUser.getTransactionId());
        HttpEntity<Object> entity = new HttpEntity<>(rollBackUser.getUser(), headers);
        ResponseEntity<User> userResponseEntity = this.restTemplate.exchange(url, HttpMethod.PUT, entity, User.class);
        return userResponseEntity.getStatusCode().is2xxSuccessful();
    }


    public ResponseEntity<Insurance> updateInsurance(String transactionId, Insurance object, Integer insuranceId) {
        String url = this.insuranceServiceUrl + "/insurances/" + insuranceId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("transactionId", transactionId);
        HttpEntity<Object> entity = new HttpEntity<>(object, headers);
        return this.restTemplate.exchange(url, HttpMethod.PUT, entity, Insurance.class);
    }

    public ResponseEntity<Transaction> getTransactionByTransactionId(String transactionId) {
        Transaction transaction = this.transactionRepository.findTransactionByTransactionId(transactionId);
        if (transaction == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        }
    }

    public ResponseEntity<List<Transaction>> getTransactionByUserId(int userId) {
        List<Transaction> transactions = this.transactionRepository.findTransactionsByUserId(userId);
        if (transactions == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        }
    }

    public ResponseEntity<List<Transaction>> getTransactions() {
        List<Transaction> transactions = this.transactionRepository.findAll();
        if (transactions == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        }
    }

    public ResponseEntity<List<User>> getUsers(String transactionId) {
        String url = this.userServiceUrl + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.add("transactionId", transactionId);
        HttpEntity<Object> entity = new HttpEntity<Object>("", headers);
        return this.restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<User>>() {
        });
    }


    public ResponseEntity<User> getUserById(Integer userId) {
        String url = this.userServiceUrl + "/users/" + userId;
        HttpEntity<Object> entity = new HttpEntity<Object>("");
        return this.restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
    }

    public ResponseEntity<Insurance> getInsuranceById(Integer insuranceId) {
        String url = this.insuranceServiceUrl + "/insurances/" + insuranceId;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>("");
        return this.restTemplate.exchange(url, HttpMethod.GET, entity, Insurance.class);
    }

    public ResponseEntity<List<Insurance>> getInsurancesByUserId(Integer userId) {
        String url = "";
        if (userId != null) {
            url = this.insuranceServiceUrl + "/insurances/?userId=" + userId;
        } else {
            url = this.insuranceServiceUrl + "/insurances";
        }
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>("");
        return this.restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Insurance>>() {
        });
    }


    public ResponseEntity<?> deleteInsurance(String insuranceId) {
        String url = this.insuranceServiceUrl + "/insurances/" + insuranceId;
        HttpEntity<Object> entity = new HttpEntity<Object>("");
        return this.restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
    }

    public ResponseEntity<?> deleteUser(String userId) {
        String url = this.userServiceUrl + "/users/" + userId;
        HttpEntity<Object> entity = new HttpEntity<Object>("");
        return this.restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
    }

}
