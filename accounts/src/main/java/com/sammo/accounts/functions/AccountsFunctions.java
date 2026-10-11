package com.sammo.accounts.functions;

import com.sammo.accounts.dto.CommunicationStatusDto;
import com.sammo.accounts.service.IAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class AccountsFunctions {

    @Bean
    public Consumer<CommunicationStatusDto> updateCommunication(IAccountsService accountsService) {
        return communicationStatus -> {
            log.info(
                    "Updating Communication status for the account number : {}, type : {} and status: {}",
                    communicationStatus.accountNumber(),
                    communicationStatus.type(),
                    communicationStatus.status()
            );

            accountsService.updateCommunicationStatus(communicationStatus);
        };
    }
}
