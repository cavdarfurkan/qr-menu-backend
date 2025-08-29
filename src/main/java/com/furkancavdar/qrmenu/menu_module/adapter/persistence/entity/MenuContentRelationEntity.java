package com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "menu_content_relation", indexes = {
        @Index(name = "idx_rel_src", columnList = "source_item_id, field_name, position"),
        @Index(name = "idx_rel_tgt", columnList = "target_item_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_menu_content_relation_unique", columnNames = {"source_item_id", "field_name", "target_item_id"})
})
public class MenuContentRelationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Cascade(org.hibernate.annotations.CascadeType.REMOVE)
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "source_item_id", nullable = false)
    private MenuContentItemEntity sourceItem;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Cascade(org.hibernate.annotations.CascadeType.REMOVE)
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "target_item_id", nullable = false)
    private MenuContentItemEntity targetItem;

    @PositiveOrZero
    @Column(name = "position")
    private Integer position;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

}
