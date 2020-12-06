package lab_2.process_service.jms;

import com.google.gson.Gson;

import lab_2.process_service.entities.RollBackInsurance;
import lab_2.process_service.entities.RollBackUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import lab_2.process_service.services.RestService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static lab_2.process_service.config.ActiveMQConfig.INSURANCES_ROLLBACK_QUEUE;
import static lab_2.process_service.config.ActiveMQConfig.USER_ROLLBACK_QUEUE;


public class Receiver {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Sender sender;

    @Value("${user-service-url}")
    private String userServiceApi;

    @Value("${insurance-service-url}")
    private String insuranceServiceApi;

    RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
    private static final Logger logger = LogManager.getLogger(Receiver.class);

    @Autowired
    RestService restService = new RestService(restTemplateBuilder, userServiceApi, insuranceServiceApi);

    @JmsListener(destination = USER_ROLLBACK_QUEUE)
    public void receiveUser(Message message) throws JMSException {
        TextMessage textMessage = (TextMessage) message;
        Gson gson = new Gson();
        RollBackUser rollBackUser = gson.fromJson(textMessage.getText(), RollBackUser.class);
        boolean rollBackSuccessful = restService.rollBackUser(rollBackUser);

        if (!rollBackSuccessful) {
            if (rollBackUser.getNumberOfRollbacks() < 5) {
                rollBackUser.setNumberOfRollbacks(rollBackUser.getNumberOfRollbacks() + 1);
                sender.sendUser(gson.toJson(rollBackUser));
            } else {
                restService.changeTransactionStatus(rollBackUser.getTransactionId(), "fail");
                sender.sendToDLQ(gson.toJson(rollBackUser));
            }
        }
        System.out.println("rollBackUser" + rollBackUser.getTransactionId());
    }

    @JmsListener(destination = INSURANCES_ROLLBACK_QUEUE)
    public void receiveInsurance(Message message) throws JMSException {
        TextMessage textMessage = (TextMessage) message;
        Gson gson = new Gson();
        RollBackInsurance rollBackInsurance = gson.fromJson(textMessage.getText(), RollBackInsurance.class);

        boolean rollBackSuccessful = restService.rollBackInsurances(rollBackInsurance);
        if (!rollBackSuccessful) {
            if (rollBackInsurance.getNumberOfRollbacks() < 5) {
                rollBackInsurance.setNumberOfRollbacks(rollBackInsurance.getNumberOfRollbacks() + 1);
                sender.sendInsurances(gson.toJson(rollBackInsurance));
            } else {
                restService.changeTransactionStatus(rollBackInsurance.getTransactionId(), "fail");
                sender.sendToDLQ(gson.toJson(rollBackInsurance));
            }
        }
        System.out.println("rollBackInsurances" + rollBackInsurance.getTransactionId());
    }
}
