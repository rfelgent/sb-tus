package de.rfelgent.tus.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author rfelgentraeger
 */
public class AssetStatus {
    /** so far uploaded data in bytes */
    @NotNull
    @Min(0)
    private long uploadedSize;
    /** flag indicating an upload in progress */
    private boolean uploading;
    /** flag indicating a lock status */
    private boolean locked;

    public long getUploadedSize() {
        return uploadedSize;
    }

    public void setUploadedSize(long uploadedSize) {
        this.uploadedSize = uploadedSize;
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
