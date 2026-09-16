package com.granja.dos.huevitos.models.infrastructure;



import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Importaciones de Jakarta y Lombok irán aquí...


@Entity
@Table(name = "sector")
@Getter @Setter
@NoArgsConstructor
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sector")
    private Integer idSector;

    // 1. TU TAREA: Define la columna 'nombre' obligando a que sea única y no nula
    // [Escribe las anotaciones @Column aquí]
    private String nombre;

    // 2. TU TAREA: Define la relación bidireccional hacia Galpon
    // Protege la memoria con FetchType.LAZY y evita tablas intermedias usando mappedBy
    // [Escribe la anotación @OneToMany aquí]
    private List<Galpon> galpones;
}