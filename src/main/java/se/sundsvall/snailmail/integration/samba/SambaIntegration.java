package se.sundsvall.snailmail.integration.samba;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import se.sundsvall.snailmail.dto.SnailMailDto;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

@Component
@EnableConfigurationProperties(SambaIntegrationProperties.class)
public class SambaIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SambaIntegration.class);

	private final Gson gson = new GsonBuilder().create();
	private final CIFSContext context;
	private final String shareUrl;

	public SambaIntegration(final SambaIntegrationProperties properties) throws IOException {
		// Initialize the JCIFS context
		SingletonContext.init(properties.jcifsProperties());
		context = SingletonContext.getInstance()
				.withCredentials(new NtlmPasswordAuthenticator(properties.domain(), properties.username(), properties.password()));

		shareUrl = String.format("smb://%s:%d%s", properties.host(), properties.port(), properties.share());
	}

	public void writeBatchDataToSambaShare(SnailMailDto snailMailDto) throws IOException {
		var batchPath = shareUrl + snailMailDto.getBatchId();
		var departmentPath = batchPath + File.separator + snailMailDto.getDepartment();

		LOGGER.info("batchPath: {}", batchPath);
		LOGGER.info("departmentPath: {}", departmentPath);

		//Create folders
		createBatchFolder(batchPath);
		createDepartmentFolder(departmentPath);

		//Save the citizen data
		saveSnailMailDtoMetaData(snailMailDto, departmentPath);
	}

	/**
	 * Save citizen data including the file content.
	 * @param snailMailDto to save to disk
	 * @throws IOException if something goes wrong during communication with the samba share
	 */
	private void saveSnailMailDtoMetaData(SnailMailDto snailMailDto, String departmentPath) throws IOException {
		var citizenFile =  departmentPath + File.separator + snailMailDto.getCitizenDto().getPersonId() + ".json";

		try (var destinationFile = new SmbFile(citizenFile, context)) {
			//Will always overwrite the file if it exists, e.g. if an update comes for a user / batch
			try (var smbFileOutputStream = new SmbFileOutputStream(destinationFile)) {
				smbFileOutputStream.write(gson.toJson(snailMailDto).getBytes(StandardCharsets.UTF_8));
				smbFileOutputStream.flush();
			}
		}
	}

	private void createBatchFolder(String folder) throws SmbException, MalformedURLException {
		try (SmbFile batchFolderFile = new SmbFile(folder, context)) {
			if (!batchFolderFile.exists()) {
				LOGGER.info("Batchfolder: {}, doesn't exist, creating it.", batchFolderFile);
				batchFolderFile.mkdir();
			}
		}
	}

	private void createDepartmentFolder(String folder) throws SmbException, MalformedURLException {
		try (SmbFile departmentFolderFile = new SmbFile(folder, context)) {
			if (!departmentFolderFile.exists()) {
				LOGGER.info("Departmentfolder: {}, doesn't exist, creating it.", departmentFolderFile);
				departmentFolderFile.mkdir();
			}
		}
	}
}
