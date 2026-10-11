package com.message.message.functions;

import com.message.message.dto.AccountsMsgDto;
import com.message.message.dto.CommunicationStatusDto;
import com.message.message.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class MessageFunctions {

    private static final Logger log = LoggerFactory.getLogger(MessageFunctions.class);

    private final EmailService emailService;

    public MessageFunctions(EmailService emailService) {
        this.emailService = emailService;
    }

    // this is for composite emailsms case
//    @Bean
//    public Function<AccountsMsgDto, AccountsMsgDto> email() {
//        return accountsMsgDto -> {
//            log.info("Sending email with the details : {}", accountsMsgDto.toString());
//            return accountsMsgDto;
//        };
//    }
//
//    @Bean
//    public Function<AccountsMsgDto,Long> sms() {
//        return accountsMsgDto -> {
//            log.info("Sending sms with the details : {}", accountsMsgDto.toString());
//            return accountsMsgDto.accountNumber();
//        };
//    }

    // this is for separate queues
    @Bean
    public Function<AccountsMsgDto, CommunicationStatusDto> email() {
        return account -> {
            log.info("Sending email with the details : {}", account);

            try {
                emailService.sendEmail(
                        account.email(),
                        "SammoBank Account Created",
                        """
                        Hello %s,
    
                        Your SammoBank account has been successfully created.
    
                        Account Number: %d
    
                        Thank you for banking with SammoBank.
                        """.formatted(account.name(), account.accountNumber())
                );
                log.info("Email successfully sent to: {}", account.email());
                return new CommunicationStatusDto(account.accountNumber(),"EMAIL","EMAIL_SENT");
            } catch (Exception e) {
                log.error("Failed to send email to: {}", account.email(), e);
                return new CommunicationStatusDto(account.accountNumber(),"EMAIL","EMAIL_FAILED");
            }
        };
    }

    @Bean
    public Function<AccountsMsgDto, CommunicationStatusDto> sms() {
        return account -> {
            log.info("Sending sms with the details : {}", account.toString());
            return new CommunicationStatusDto(account.accountNumber(),"SMS","SMS_SENT");
        };
    }
}
