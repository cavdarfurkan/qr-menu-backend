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

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "menu_content",
        uniqueConstraints = @UniqueConstraint(columnNames = {"menu_id", "collection_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private ThemeEntity theme;

    @Column(name = "collection_name", nullable = false)
    private String collectionName;

    @Column(name = "content_json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<JsonNode> contentJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant updatedAt;


}
