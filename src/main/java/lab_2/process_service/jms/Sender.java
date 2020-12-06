package lab_2.process_service.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static lab_2.process_service.config.ActiveMQConfig.*;


public class Sender {
    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendUser(String message) {
        this.jmsTemplate.send(USER_ROLLBACK_QUEUE, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }

    public void sendInsurances(String message) {
        this.jmsTemplate.send(INSURANCES_ROLLBACK_QUEUE, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }

    public void sendToDLQ(String message) {
        this.jmsTemplate.send(DEAD_LETTER_QUEUE, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }
}
