package se.sundsvall.snailmail;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@ServiceApplication
@EnableFeignClients
@ExcludeFromJacocoGeneratedCoverageReport
@EnableScheduling
public class Application {

	public static void main(final String... args) {
		run(Application.class, args);
	}

}
