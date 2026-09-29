package com.granja.dos.huevitos.models.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "galpon", schema = "avicola")
@NoArgsConstructor
public class Galpon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_galpon")
    private Integer idGalpon;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "estado", length = 20)
    private String estado = "Activo";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sector", nullable = false)
    private Sector sector;

    @OneToMany(mappedBy = "galpon", fetch = FetchType.LAZY)
    private List<LoteGalpon> lotes = new ArrayList<>();
}    
    @CreationTimestamp
    private LocalDate fechaRegistro;
}
