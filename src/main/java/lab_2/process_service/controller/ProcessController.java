package lab_2.process_service.controller;


import lab_2.process_service.entities.Insurance;
import lab_2.process_service.entities.Transaction;
import lab_2.process_service.entities.User;
import lab_2.process_service.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProcessController {


    @Value("${user-service-url}")
    private String userServiceApi;

    @Value("${insurance-service-url}")
    private String insuranceServiceApi;

    RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

    @Autowired
    RestService restService = new RestService(restTemplateBuilder, userServiceApi, insuranceServiceApi);


    @PostMapping("/users")
    ResponseEntity<User> createUser(@RequestBody User newUser) {
        return restService.createUser(newUser);

    }

    @PostMapping("/insurances")
    ResponseEntity<Insurance> createInsurance(@RequestHeader() @RequestBody Insurance newInsurance) {
        return restService.createInsurance(newInsurance);

    }


    @PutMapping("/users/{userId}")
    ResponseEntity<String> updateUser(@RequestHeader() String transactionId, @RequestBody User updatedUser, @PathVariable Integer userId) {
        new Thread(() -> restService.updateUser(transactionId, updatedUser, userId)).start();
        return new ResponseEntity<>("Processing", HttpStatus.OK);
    }


    @PutMapping("/insurances/{insuranceId}")
    ResponseEntity<Insurance> updateInsurance(@RequestHeader() String transactionId, @RequestBody Insurance updatedInsurance, @PathVariable Integer insuranceId) {
        return restService.updateInsurance(transactionId, updatedInsurance, insuranceId);

    }


    @GetMapping("/users")
    ResponseEntity<List<User>> getUsers(@RequestHeader() String transactionId) {
        return restService.getUsers(transactionId);

    }

    @GetMapping("/transactions")
    ResponseEntity<List<Transaction>> getTransactions(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return restService.getTransactionByUserId(userId);
        } else {
            return restService.getTransactions();
        }
    }

    @GetMapping("/transactions/{transactionId}")
    ResponseEntity<Transaction> getTransactionById(@PathVariable String transactionId) {
        return restService.getTransactionByTransactionId(transactionId);
    }

    @GetMapping("/users/{userId}")
    ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        return restService.getUserById(userId);
    }

    @GetMapping("/insurances")
    ResponseEntity<List<Insurance>> getInsurancesByUsersId(@RequestParam(required = false) Integer userId) {
        return restService.getInsurancesByUserId(userId);
    }


    @GetMapping("/insurances/{insuranceId}")
    ResponseEntity<Insurance> getInsuranceById(@PathVariable Integer insuranceId) {
        return restService.getInsuranceById(insuranceId);
    }


    @DeleteMapping("/users/{userId}")
    ResponseEntity<?> deleteUser(@PathVariable String userId) {
        return restService.deleteUser(userId);

    }


    @DeleteMapping("/insurances/{insuranceId}")
    ResponseEntity<?> deleteInsurance(@PathVariable String insuranceId) {
        return restService.deleteInsurance(insuranceId);

    }

}
