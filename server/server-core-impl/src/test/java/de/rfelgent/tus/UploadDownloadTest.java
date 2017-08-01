package de.rfelgent.tus;

import de.rfelgent.tus.service.LocationResolver;
import de.rfelgent.tus.service.LocationResolverAbsolute;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author rfelgentraeger
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UploadDownloadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadDownloadTest.class);

    @Autowired
    private LocationResolver locationResolver;

    @LocalServerPort
    private String serverPort;

    @Value("classpath:prairie.jpg")
    private Resource picToUpload;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Before
    public void before() {
        ((LocationResolverAbsolute) locationResolver).setPort(serverPort);
    }

    @Test
    public void uploadAndDownloadImage() throws IOException, ProtocolException {
        //prepare
        TusUploader uploader = createTusUploader();

        //test
        do {
            LOGGER.info("Performing upload at offset {}", uploader.getOffset());
        } while (uploader.uploadChunk() > -1);
        uploader.finish();

        //verify the download
        String absoluteDownloadUrl = uploader.getUploadURL().toString();
        String relativeDownloadUrl = absoluteDownloadUrl.substring(absoluteDownloadUrl.lastIndexOf("/files/"));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(
                //as we are using TestRestTemplate, relative paths should work as well!
                relativeDownloadUrl,
                HttpMethod.GET, null, byte[].class, Collections.EMPTY_MAP);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse("Download is not specified by TUS protocol. Therefore, no TUS specific headers required", response.getHeaders().containsKey(TusHeaders.TUS_RESUMABLE));

        byte[] downloadedPic = response.getBody();
        assertEquals("downloaded content size must be equal of original uploaded file", picToUpload.getFile().length(), downloadedPic.length);
        assertTrue("The image content must be the same", isSameImage(picToUpload.getFile(), downloadedPic));
    }

    private boolean isSameImage(File pictureA, byte[] pictureB) throws IOException {
        BufferedImage biA = ImageIO.read(pictureA);
        DataBuffer dbA = biA.getData().getDataBuffer();
        int sizeA = dbA.getSize();
        BufferedImage biB;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(pictureB)) {
            biB = ImageIO.read(bais);
        }
        DataBuffer dbB = biB.getData().getDataBuffer();
        int sizeB = dbB.getSize();
        if (sizeA != sizeB) {
            return false;
        }
        // compare data-buffer objects //
        for (int i = 0; i < sizeA; i++) {
            if (dbA.getElem(i) != dbB.getElem(i)) {
                return false;
            }
        }
        return true;
    }

    private TusUploader createTusUploader() {
        TusClient client = new TusClient();
        try {
            client.setUploadCreationURL(new URL("http://localhost:" + serverPort + "/files"));
            client.disableResuming();

            TusUpload upload = new TusUpload();
            upload.setInputStream(picToUpload.getInputStream());
            upload.setSize(picToUpload.getFile().length());

            TusUploader uploader = client.createUpload(upload);
            uploader.setChunkSize(1024);     //1kb
            return uploader;
        } catch (Exception ioe) {
            throw new IllegalStateException("Test setup failed");
        }
    }
}
