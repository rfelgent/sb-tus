package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public class LocationResolverAbsolute implements LocationResolver {

    private String port = null;
    private String path = "/files/";
    private String domain = "localhost";
    private String protocol = "http";

    @Override
    public String resolve(Asset asset) {
        StringBuilder sb = new StringBuilder();
        sb.append(getProtocol()).append("://");
        sb.append(getDomain());
        if (getPort() != null) {
            sb.append(":").append(getPort());
        }
        sb.append(getPath());
        if (!getPath().endsWith("/")) {
            sb.append("/");
        }
        sb.append(asset.getReferenceId());

        return sb.toString();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
