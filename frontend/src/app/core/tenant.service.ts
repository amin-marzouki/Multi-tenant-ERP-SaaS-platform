import { Injectable } from '@angular/core';

/** Works out the workspace (tenant) from the address, e.g. "acme" on acme.orbiterp.app. */
@Injectable({ providedIn: 'root' })
export class TenantService {
  /** The tenant code from the subdomain, or '' on localhost, an IP address or a bare domain. */
  subdomainTenant(): string {
    const host = window.location.hostname.toLowerCase();
    if (host === 'localhost' || host.endsWith('.localhost') || /^[\d.]+$/.test(host) || host.includes(':')) {
      return '';
    }
    const parts = host.split('.');
    return parts.length >= 3 && parts[0] !== 'www' ? parts[0] : '';
  }
}
