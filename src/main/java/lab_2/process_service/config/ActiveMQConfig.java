package lab_2.process_service.config;


import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMQConfig {

    public static final String USER_ROLLBACK_QUEUE = "user-rollback-queue";
    public static final String INSURANCES_ROLLBACK_QUEUE = "insurance-rollback-queue";
    public static final String DEAD_LETTER_QUEUE= "dead-letter-queue";
//    public static final String UPDATE_USER_QUEUE = "update-user-queue";
//    public static final String UPDATE_INSURANCES_QUEUE = "update-insurances-queue";

}