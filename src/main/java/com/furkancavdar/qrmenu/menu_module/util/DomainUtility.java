package com.furkancavdar.qrmenu.menu_module.util;

public class DomainUtility {

  private DomainUtility() {}

  /**
   * Extracts subdomain from a full domain by removing protocol, trailing slash, and base domain.
   *
   * @param fullDomain The full domain (e.g., "https://subdomain.menu.furkancavdar.com/")
   * @param baseDomain The base domain (e.g., "menu.furkancavdar.com")
   * @return The extracted subdomain (e.g., "subdomain")
   * @throws IllegalArgumentException if the domain doesn't contain the base domain or is invalid
   */
  public static String extractSubdomain(String fullDomain, String baseDomain) {
    if (fullDomain == null || fullDomain.trim().isEmpty()) {
      throw new IllegalArgumentException("Domain cannot be null or empty");
    }
    if (baseDomain == null || baseDomain.trim().isEmpty()) {
      throw new IllegalArgumentException("Base domain cannot be null or empty");
    }

    // Remove protocol (http:// or https://)
    String normalized = fullDomain.trim().toLowerCase();
    normalized = normalized.replaceFirst("^https?://", "");

    // Remove trailing slash
    normalized = normalized.replaceAll("/+$", "");

    // Remove base domain to extract subdomain
    String baseDomainLower = baseDomain.toLowerCase().trim();
    if (normalized.endsWith("." + baseDomainLower)) {
      normalized = normalized.substring(0, normalized.length() - baseDomainLower.length() - 1);
    } else if (normalized.equals(baseDomainLower)) {
      // If the input is just the base domain, there's no subdomain
      throw new IllegalArgumentException("Domain must include a subdomain");
    } else if (!normalized.contains(".")) {
      // If there's no dot, it's already just the subdomain
      return normalized;
    } else {
      throw new IllegalArgumentException(
          "Domain does not match base domain pattern. Expected format: subdomain."
              + baseDomainLower);
    }

    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Subdomain cannot be empty");
    }

    return normalized;
  }

  /**
   * Combines subdomain with base domain to create full domain.
   *
   * @param subdomain The subdomain (e.g., "subdomain")
   * @param baseDomain The base domain (e.g., "menu.furkancavdar.com")
   * @return The full domain (e.g., "subdomain.menu.furkancavdar.com")
   */
  public static String combineSubdomainWithBase(String subdomain, String baseDomain) {
    if (subdomain == null || subdomain.trim().isEmpty()) {
      throw new IllegalArgumentException("Subdomain cannot be null or empty");
    }
    if (baseDomain == null || baseDomain.trim().isEmpty()) {
      throw new IllegalArgumentException("Base domain cannot be null or empty");
    }

    return subdomain.trim() + "." + baseDomain.trim();
  }

  /**
   * Normalizes domain input by removing protocol, trailing slash, extracting subdomain, and
   * validating it. Ensures the subdomain is a single level (no dots).
   *
   * @param input The input domain (can be full domain or just subdomain)
   * @param baseDomain The base domain (e.g., "menu.furkancavdar.com")
   * @return The normalized and validated subdomain (single level, no dots)
   * @throws IllegalArgumentException if the domain format is invalid or contains multiple levels
   */
  public static String normalizeDomainInput(String input, String baseDomain) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Domain input cannot be null or empty");
    }
    if (baseDomain == null || baseDomain.trim().isEmpty()) {
      throw new IllegalArgumentException("Base domain cannot be null or empty");
    }

    String trimmed = input.trim();

    // Remove protocol
    trimmed = trimmed.replaceFirst("^https?://", "");

    // Remove trailing slash
    trimmed = trimmed.replaceAll("/+$", "");

    String subdomain;
    String baseDomainLower = baseDomain.toLowerCase().trim();

    // Check if input contains base domain
    if (trimmed.toLowerCase().endsWith("." + baseDomainLower)
        || trimmed.toLowerCase().equals(baseDomainLower)) {
      // Extract subdomain from full domain
      subdomain = extractSubdomain(trimmed, baseDomain);
    } else {
      // Input is already just the subdomain
      subdomain = trimmed.toLowerCase();
    }

    // Validate that subdomain is a single level (no dots)
    if (subdomain.contains(".")) {
      throw new IllegalArgumentException(
          "Subdomain must be a single level. Multiple levels (e.g., 'sub1.sub2') are not allowed. "
              + "Expected format: 'subdomain."
              + baseDomainLower
              + "' or just 'subdomain'");
    }

    // Validate subdomain using DnsNameFormatter
    return DnsNameFormatter.toDnsLabel(subdomain);
  }
}
