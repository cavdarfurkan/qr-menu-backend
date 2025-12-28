package com.furkancavdar.qrmenu.theme_module.adapter.persistence.storage;

import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeStoragePort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ThemeStorageAdapter implements ThemeStoragePort {

  @Value("${s3.bucket-name}")
  private String BUCKET;

  @Value("${s3.placeholder-thumbnail}")
  private String PLACEHOLDER_THUMBNAIL;

  private final S3Template s3Template;

  @Override
  public String putTheme(InputStream themeZipIs, String themeName) {
    themeName = encode(themeName);
    S3Resource resource =
        s3Template.upload(BUCKET, "%s/theme.zip".formatted(themeName), themeZipIs);

    log.info("S3ThemeStorageAdapter:putTheme");
    log.info("S3ThemeStorageAdapter:putTheme resource: {}", resource);
    return "s3://%s/%s"
        .formatted(resource.getLocation().getBucket(), resource.getLocation().getObject());
  }

  @Override
  public String putThumbnail(InputStream thumbnailIs, String themeName) {
    themeName = encode(themeName);
    try {
      InputStream optimizedIs = optimizeImage(thumbnailIs);
      S3Resource resource =
          s3Template.upload(BUCKET, "%s/preview.jpg".formatted(themeName), optimizedIs);
      return "s3://%s/%s"
          .formatted(resource.getLocation().getBucket(), resource.getLocation().getObject());
    } catch (Exception e) {
      log.error("S3ThemeStorageAdapter:putThumbnail error: {}", e.getMessage());
      log.info("S3ThemeStorageAdapter:putThumbnail fallback to placeholder thumbnail");
      return PLACEHOLDER_THUMBNAIL;
    }
  }

  @Override
  public void deleteTheme(Theme theme) {
    String themeName = encode(theme.getThemeManifest().getName());
    s3Template.deleteObject(BUCKET, "%s/theme.zip".formatted(themeName));
    s3Template.deleteObject(BUCKET, "%s/preview.jpg".formatted(themeName));
    log.info("S3ThemeStorageAdapter:deleteTheme");
  }

  private ByteArrayInputStream optimizeImage(InputStream inputStream) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    Thumbnails.of(inputStream)
        .size(200, 200)
        .keepAspectRatio(true)
        .outputFormat("jpg")
        .outputQuality(0.6)
        .toOutputStream(os);

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

    os.close();
    is.close();
    return is;
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
