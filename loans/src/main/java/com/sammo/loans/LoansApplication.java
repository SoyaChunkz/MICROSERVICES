package com.sammo.loans;

import com.sammo.loans.dto.LoansContactInfoDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
/*@ComponentScans({ @ComponentScan("com.sammo.loans.controller") })
@EnableJpaRepositories("com.sammo.loans.repository")
@EntityScan("com.sammo.loans.model")*/
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableConfigurationProperties(value = {LoansContactInfoDto.class})
@OpenAPIDefinition(
		info = @Info(
				title = "Loans microservice REST API Documentation",
				description = "sammobank Loans microservice REST API Documentation",
				version = "v1",
				contact = @Contact(
						name = "sammo",
						email = "tutor@sb.com",
						url = "https://www.sb.com"
				),
				license = @License(
						name = "Apache 2.0",
						url = "https://www.sb.com"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "sammobank Loans microservice REST API Documentation",
				url = ""
		)
)
public class LoansApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoansApplication.class, args);
	}
}
