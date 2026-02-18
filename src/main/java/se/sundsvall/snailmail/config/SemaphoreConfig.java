package se.sundsvall.snailmail.config;

import java.util.concurrent.Semaphore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@Configuration
public class SemaphoreConfig {

	private static final int MAX_PERMITS = 1; // We only want one, always

	@Bean
	@Scope(SCOPE_SINGLETON)
	public Semaphore semaphore() {
		return new Semaphore(MAX_PERMITS);
	}
}
