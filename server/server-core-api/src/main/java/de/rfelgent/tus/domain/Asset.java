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
 * An asset represents any binary meta data (video, picture, sound data...).
 *
 * @author rfelgentraeger
 */
public class Asset {

    /** referenceId of the asset */
    @NotNull
    private String referenceId;
    /** total size of the data to upload in bytes */
    @Min(1)
    private Long totalSize;
    /** date when the asset was created */
    @NotNull
    private Date creationDate;
    /** date when the asset is expired */
    private Date expirationDate;
    /** metadata of the asset */
    private Map<String, String> meta;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
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
            retVal = getMeta().entrySet().stream().map(stringStringEntry -> {
                final String key = stringStringEntry.getKey();
                final String value = stringStringEntry.getValue();
                return key + " " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
            }).collect(joining(","));
        }
        return retVal;
    }
}
