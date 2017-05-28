package de.rfelgent.tus.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.beans.Transient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/**
 * An asset represents any binary data (video, picture, sound data...) to upload.
 *
 * @author rfelgentraeger
 */
public class Asset {

    /** id of the asset */
    @NotNull
    private String id;
    /** total size of the data to upload in bytes */
    private Long totalSize;
    /** uploaded data in bytes */
    @NotNull
    @Min(0)
    private Long uploadedSize;
    /** date when was the asset created */
    @NotNull
    private Date creationDate = new Date();
    /** date when the asset expires and will be discarded for further use */
    private Date expirationDate;
    /** metadata of the asset */
    private Map<String, String> meta;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getUploadedSize() {
        return uploadedSize;
    }

    public void setUploadedSize(Long uploadedSize) {
        this.uploadedSize = uploadedSize;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    @Transient
    public Map<String, String> setMetaFromMetaHttpHeader(String httpHeader) {
        if (httpHeader != null && !httpHeader.isEmpty()) {
            Map<String, String> meta = new HashMap<String, String>();
            for (String pair : httpHeader.split(",")) {
                String key = pair.substring(0, pair.indexOf(" "));
                String value = pair.substring(pair.indexOf(" ") + 1);
                value = new String(Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                meta.put(key, value);
            }
            setMeta(meta);
        } else {
            setMeta(Collections.EMPTY_MAP);
        }
        return getMeta();
    }

    @Transient
    public String toMetaHttpHeader() {
        String retVal = "";
        if (getMeta() != null && !getMeta().isEmpty()) {
            retVal = getMeta().entrySet().stream().map(new Function<Map.Entry<String,String>, String>() {
                @Override
                public String apply(Map.Entry<String, String> stringStringEntry) {
                    final String key = stringStringEntry.getKey();
                    final String value = stringStringEntry.getValue();
                    return key + " " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
                }
            }).collect(joining(","));
        }
        return retVal;
    }
}
