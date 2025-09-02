package com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity.ThemeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "menu_content_item", indexes = {
        @Index(name = "idx_item_menu_collection", columnList = "menu_id, collection_name")
})
public class MenuContentItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private ThemeEntity theme;

    @Column(name = "collection_name", nullable = false)
    private String collectionName;

    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode data;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

}
