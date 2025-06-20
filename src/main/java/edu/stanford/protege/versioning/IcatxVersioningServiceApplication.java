package edu.stanford.protege.versioning;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
@Import({WebProtegeIpcApplication.class})
@EnableScheduling
public class IcatxVersioningServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IcatxVersioningServiceApplication.class, args);
	}



}
