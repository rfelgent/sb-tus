package de.rfelgent.tus;

import de.rfelgent.tus.service.UploadLocationResolver;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This integration test is used to demonstrate the compatibility with the tus-java-client.
 *
 * @author rfelgentraeger
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TusClientUploadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TusClientUploadTest.class);

    @Autowired
    private UploadLocationResolver uploadLocationResolver;

    @LocalServerPort
    private String serverPort;

    @Value("classpath:prairie.jpg")
    private Resource picToUpload;

    @Before
    public void before() {
        uploadLocationResolver.setPort(serverPort);
    }

    @Test
    public void uploadAtOnce() throws IOException, ProtocolException {
        TusClient client = new TusClient();
        client.setUploadCreationURL(new URL("http://localhost:" + serverPort + "/files"));
        client.disableResuming();

        TusUpload upload = new TusUpload();
        upload.setInputStream(picToUpload.getInputStream());

        TusUploader uploader = client.createUpload(upload);
        uploader.setChunkSize(1024);     //1kb

        do {
            long totalBytes = upload.getSize();
            long bytesUploaded = uploader.getOffset();
            double progress = (double) bytesUploaded / totalBytes * 100;

            LOGGER.info("Upload at %06.2f%%.\n", progress);
        } while(uploader.uploadChunk() > -1);
    }

    @Test
    public void uploadWithResume() throws MalformedURLException {
        TusClient client = new TusClient();
        client.setUploadCreationURL(new URL("http://localhost:" + serverPort + "/files"));
    }
}
