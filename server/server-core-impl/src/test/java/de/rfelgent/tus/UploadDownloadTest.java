package de.rfelgent.tus;

import de.rfelgent.tus.service.LocationResolver;
import de.rfelgent.tus.service.LocationResolverAbsolute;
import io.tus.java.client.ProtocolException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

        //test
        //create the asset
        URL assetCreationURL = new URL("http://localhost:" + serverPort + "/files");
        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.set(TusHeaders.TUS_RESUMABLE, TusVersion.SEMVERSION_1_0_0);
        createHeaders.set("Upload-Length", Long.toString(picToUpload.getFile().length()));

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                //as we are using TestRestTemplate, relative paths should work as well!
                "/files/",
                HttpMethod.POST, new HttpEntity(createHeaders), Void.class);
        assertEquals("unexpected status code ('" + responseEntity.getStatusCodeValue() + "') while creating upload", 201, responseEntity.getStatusCodeValue());
        URI absoluteUploadUri = responseEntity.getHeaders().getLocation();
        assertNotNull("missing upload URL in response for creating upload", absoluteUploadUri);
        String relativeUploadUrl = absoluteUploadUri.getPath();

        //upload the binary of the asset
        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.set(TusHeaders.TUS_RESUMABLE, TusVersion.SEMVERSION_1_0_0);
        uploadHeaders.set(TusHeaders.UPLOAD_OFFSET, Integer.toString(0));
        uploadHeaders.set("Content-Type", "application/offset+octet-stream");
        uploadHeaders.set("Expect", "100-continue");

        responseEntity = testRestTemplate.exchange(
                //as we are using TestRestTemplate, relative paths should work as well!
                relativeUploadUrl,
                HttpMethod.PATCH, new HttpEntity<>(picToUpload, uploadHeaders), Void.class);
        assertEquals("unexpected status code ('" + responseEntity.getStatusCodeValue() + "') while creating upload", 204, responseEntity.getStatusCodeValue());

        //download the binary of the asset
        String absoluteDownloadUrl = absoluteUploadUri.toURL().toString();
        String relativeDownloadUrl = absoluteDownloadUrl.substring(absoluteDownloadUrl.lastIndexOf("/files/"));
        ResponseEntity<byte[]> response = testRestTemplate.exchange(
                //as we are using TestRestTemplate, relative paths should work as well!
                relativeDownloadUrl,
                HttpMethod.GET, null, byte[].class, Collections.EMPTY_MAP);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse("Download is not specified by TUS protocol. Therefore, no TUS specific headers required", response.getHeaders().containsKey(TusHeaders.TUS_RESUMABLE));

        //verify
        byte[] downloadedPic = response.getBody();
        assertEquals("Downloaded content size must be equal of original uploaded file", picToUpload.getFile().length(), downloadedPic.length);
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
}
