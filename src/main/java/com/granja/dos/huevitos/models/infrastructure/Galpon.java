package com.granja.dos.huevitos.models.infrastructure;

import java.time.LocalDateTime;

import com.granja.dos.huevitos.models.production.SeccionGalpon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
//@Table(name = "galpon") // 
//@Getter @Setter // 
//@NoArgsConstructor
//public class Galpon {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id_galpon")
//    private Integer idGalpon;
//
//    @Column(name = "nombre", length = 50, nullable = false, unique = true)
//    private String nombre;
//
//    
//    @Enumerated(EnumType.STRING)
//    @Column(name = "seccion", nullable = false)
//    private SeccionGalpon seccion;
//
//    @Column(name = "estado", nullable = false)
//    private Boolean estado = true;
//
//    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_lote_actual")
//    private LoteAves loteActual;
//    
//    @Column(name = "creat", nullable = false, updatable = false)
//    private LocalDateTime creat = LocalDateTime.now();
//}