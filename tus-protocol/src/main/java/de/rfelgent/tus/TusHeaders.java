package de.rfelgent.tus;

/**
 * @author rfelgentraeger
 */
public interface TusHeaders {

    String TUS_RESUMABLE = "Tus-Resumable";
    String TUS_VERSION = "Tus-Version";
    String TUS_EXTENSION = "Tus-Extension";
    String TUS_MAXSIZE = "Tus-Max-Size";
    String TUS_CHECKSUM_ALG = "Tus-Checksum-Algorithm";

    String UPLOAD_OFFSET = "Upload-Offset";
    String UPLOAD_LENGTH = "Upload-Length";
    String UPLOAD_DEFER_LENGTH = "Upload-Defer-Length";
    String UPLOAD_META = "Upload-Metadata";
    String UPLOAD_EXPIRES = "Upload-Expires";
    String UPLOAD_CHECKSUM = "Upload-Checksum";
    String UPLOAD_CONCAT = "Upload-Concat";

    String METHOD_OVERRIDE = "X-HTTP-Method-Override";

}
