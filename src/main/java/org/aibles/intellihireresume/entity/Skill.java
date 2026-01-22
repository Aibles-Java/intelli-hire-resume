package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aibles.intellihireresume.entity.enums.SkillType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "skills")
@Data
@EqualsAndHashCode(callSuper = true)
public class Skill extends BaseEntity {
    
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private SkillType type;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aliases", columnDefinition = "json")
    private List<String> aliases;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
}