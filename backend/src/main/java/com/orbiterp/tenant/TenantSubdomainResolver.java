package com.orbiterp.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/** Reads the tenant code from the request host, e.g. "acme" from acme.orbiterp.app. */
@Component
public class TenantSubdomainResolver {

    private final String baseDomain;

    public TenantSubdomainResolver(@Value("${orbiterp.tenancy.base-domain:}") String baseDomain) {
        this.baseDomain = baseDomain == null ? "" : baseDomain.trim().toLowerCase(Locale.ROOT);
    }

    /** Empty when no base domain is configured or the host is not a tenant subdomain of it. */
    public Optional<String> resolve(String host) {
        if (baseDomain.isEmpty() || host == null) {
            return Optional.empty();
        }
        String name = host.toLowerCase(Locale.ROOT);
        int port = name.indexOf(':');
        if (port >= 0) {
            name = name.substring(0, port);
        }
        String suffix = "." + baseDomain;
        if (!name.endsWith(suffix)) {
            return Optional.empty();
        }
        String subdomain = name.substring(0, name.length() - suffix.length());
        if (subdomain.isEmpty() || subdomain.contains(".") || subdomain.equals("www")) {
            return Optional.empty();
        }
        return Optional.of(subdomain);
    }
}
