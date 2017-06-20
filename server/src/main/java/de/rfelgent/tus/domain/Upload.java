package de.rfelgent.tus.domain;

import java.io.InputStream;

/**
 * @author rfelgentraeger
 */
public class Upload {

    private String assetReferenceId;
    private long offset;
    private InputStream inputStream;

    public String getAssetReferenceId() {
        return assetReferenceId;
    }

    public void setAssetReferenceId(String assetReferenceId) {
        this.assetReferenceId = assetReferenceId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
