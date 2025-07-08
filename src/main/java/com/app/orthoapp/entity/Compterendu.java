package com.app.orthoapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "compterendu")
public class Compterendu {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "date_redaction")
    private LocalDate dateRedaction;

    @Column(name = "contenu")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> contenu;

}