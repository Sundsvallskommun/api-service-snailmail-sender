package se.sundsvall.snailmail.integration.sftp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.sftp")
public record SftpProperties(
	String username,
	String password,
	String host,
	String defaultPath,
	int port,
	boolean allowUnknownKeys) {
}
