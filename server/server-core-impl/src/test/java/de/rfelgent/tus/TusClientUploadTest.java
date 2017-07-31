package de.rfelgent.tus;

import de.rfelgent.tus.service.LocationResolver;
import de.rfelgent.tus.service.LocationResolverAbsolute;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
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
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * This integration test is used to demonstrate the interoperability with the official tus-java-client.
 *
 * @author rfelgentraeger
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TusClientUploadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TusClientUploadTest.class);

    @Autowired
    private LocationResolver locationResolver;

    @LocalServerPort
    private String serverPort;

    @Value("classpath:prairie.jpg")
    private Resource picToUpload;

    //preferring httpClient for "HEAD" request, as it is simpler to set up
    //@Autowired
    //private TestRestTemplate testRestTemplate;

    private HttpClient httpClient;

    @Before
    public void before() {
        ((LocationResolverAbsolute)locationResolver).setPort(serverPort);

        httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void after() {
        HttpClientUtils.closeQuietly(httpClient);
    }

    @Test
    public void uploadAtOnce_UnknownSize_ServerStoredTheWholeBinary_ButDoesNotKnowCompletedStatus() throws IOException, ProtocolException {
        //prepare
        TusUploader uploader = createTusUploader(false, false);

        //test
        do {
            LOGGER.info("Uploaded {}", uploader.getOffset());
        } while(uploader.uploadChunk() > -1);
        uploader.finish();

        //verify
        HttpHead headRequest = new HttpHead(uploader.getUploadURL().toString());
        headRequest.addHeader(TusHeaders.TUS_RESUMABLE, TusVersion.SEMVERSION_1_0_0);
        HttpResponse response = httpClient.execute(headRequest);
        EntityUtils.consumeQuietly(response.getEntity());
        assertEquals("File must be fully uploaded", picToUpload.getFile().length(), Long.parseLong(response.getFirstHeader("Upload-Offset").getValue()));
        assertNull(response.getFirstHeader("Upload-Length"));
    }

    @Test
    public void uploadAtOnce_KnownSize() throws IOException, ProtocolException {
        //prepare
        TusUploader uploader = createTusUploader(false, true);

        //test
        do {
            LOGGER.info("Uploaded {}", uploader.getOffset());
        } while(uploader.uploadChunk() > -1);
        uploader.finish();

        //verify
        HttpHead headRequest = new HttpHead(uploader.getUploadURL().toString());
        headRequest.addHeader(TusHeaders.TUS_RESUMABLE, TusVersion.SEMVERSION_1_0_0);
        HttpResponse response = httpClient.execute(headRequest);
        EntityUtils.consumeQuietly(response.getEntity());
        assertEquals("File must be fully uploaded", picToUpload.getFile().length(), Long.parseLong(response.getFirstHeader("Upload-Offset").getValue()));
        assertEquals("File must be fully uploaded", picToUpload.getFile().length(), Long.parseLong(response.getFirstHeader("Upload-Length").getValue()));
    }

    private TusUploader createTusUploader(boolean resumableUploads, boolean withKnownSize) {
        TusClient client = new TusClient();
        try {
            client.setUploadCreationURL(new URL("http://localhost:" + serverPort + "/files"));
            if (!resumableUploads) {
                client.disableResuming();
            }

            TusUpload upload = new TusUpload();
            upload.setInputStream(picToUpload.getInputStream());
            if (withKnownSize) {
                upload.setSize(picToUpload.getFile().length());
            }
            TusUploader uploader = client.createUpload(upload);
            uploader.setChunkSize(1024);     //1kb
            return uploader;
        } catch (Exception ioe) {
            throw new IllegalStateException("Test setup failed");
        }
    }
}
