package se.sundsvall.snailmail.integration.sftp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
public class SftpConfiguration {

	private final SftpProperties sftpProperties;

	public SftpConfiguration(final SftpProperties sftpProperties) {
		this.sftpProperties = sftpProperties;
	}

	@Bean
	public DefaultSftpSessionFactory sftpSessionFactory() {
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
		factory.setHost(sftpProperties.host());
		factory.setPort(sftpProperties.port());
		factory.setUser(sftpProperties.username());
		factory.setPassword(sftpProperties.password());
		factory.setAllowUnknownKeys(sftpProperties.allowUnknownKeys());
		return factory;
	}
}
