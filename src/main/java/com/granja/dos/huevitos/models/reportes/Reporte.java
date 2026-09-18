package com.granja.dos.huevitos.models.reportes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.granja.dos.huevitos.models.segurity.Usuario;

import jakarta.persistence.CascadeType;
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

/**
 * Cabecera genérica de un reporte generado en el sistema (módulo
 * "9. Reportes", obligatorio para todos los módulos). Un Reporte agrupa
 * varias líneas (DetalleReporte) con los valores calculados.
 */
@Entity
@Table(name = "reporte", schema = "Avicola")
@Getter
@Setter
@NoArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "parametros", length = 500)
    private String parametros;

    @Column(name = "archivo", length = 255)
    private String archivo;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReporte> detalles = new ArrayList<>();

    public Reporte(String tipo, Usuario usuario, String parametros) {
        this.tipo = tipo;
        this.usuario = usuario;
        this.parametros = parametros;
    }

    /** Agrega una línea calculada al reporte (ej. "Total recibido" -> "1500"). */
    public void agregarDetalle(String descripcion, String valor) {
        DetalleReporte detalle = new DetalleReporte(descripcion, valor);
        detalle.setReporte(this);
        this.detalles.add(detalle);
    }
}
