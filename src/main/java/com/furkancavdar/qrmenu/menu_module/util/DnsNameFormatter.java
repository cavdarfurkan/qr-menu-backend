package com.furkancavdar.qrmenu.menu_module.util;

public class DnsNameFormatter {
  /**
   * Converts a single label (no dots) into an RFC-1123 compliant DNS label.
   *
   * @param input any string
   * @return valid DNS label (1-63 chars, a-z, 0-9 or hyphen, no leading/trailing hyphens)
   * @throws IllegalArgumentException if result is empty after cleaning
   */
  public static String toDnsLabel(String input) {
    if (input == null) {
      throw new IllegalArgumentException("Input cannot be null");
    }

    // 1) lowercase
    String label = input.toLowerCase();

    // 2) replace invalid chars with hyphens
    label = label.replaceAll("[^a-z0-9-]", "-");

    // 3) string loading/trailing hyphens
    label = label.replaceAll("^-+", "").replaceAll("-+$", "");

    // Replace multiple hyphens with a single one
    //        label = label.replaceAll("-{2,}", "-");

    // 4) truncate to 63 chars and strip trailing hyphens again
    if (label.length() > 63) {
      label = label.substring(0, 63).replaceAll("-+$", "");
    }

    if (label.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot convert input to a valid DNS label: \"" + input + "\"");
    }

    return label;
  }

  /**
   * Convert a full domain name (possibly with dots) into an RFC-1123 compliant name. Each label
   * between dots is cleaned independently.
   */
  public static String toDnsName(String input) {
    String[] parts = input.split("\\.");
    for (int i = 0; i < parts.length - 1; i++) {
      parts[i] = toDnsLabel(parts[i]);
    }
    return String.join(".", parts);
  }
}
