package com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.auth.adapter.persistence.entity.UserEntity;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "theme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThemeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name = "theme_location_url", nullable = false, unique = true)
    private String themeLocationUrl;

    @Column(name = "is_free")
    private Boolean isFree = false;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "theme_manifest_name", nullable = false, unique = true)),
            @AttributeOverride(name = "version", column = @Column(name = "theme_manifest_version")),
            @AttributeOverride(name = "description", column = @Column(name = "theme_manifest_description")),
            @AttributeOverride(name = "author", column = @Column(name = "theme_manifest_author")),
            @AttributeOverride(name = "createdAt", column = @Column(name = "theme_manifest_created_at")),
            @AttributeOverride(name = "schemasLocation", column = @Column(name = "theme_manifest_schemas_location", columnDefinition = "jsonb"))
    })
    private ThemeManifest themeManifest;

    @Column(name = "schemas", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, JsonNode> schemas;
}
