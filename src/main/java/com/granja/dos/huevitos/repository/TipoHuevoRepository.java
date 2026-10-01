package com.granja.dos.huevitos.repository;

import com.granja.dos.huevitos.models.produccion.TipoHuevo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoHuevoRepository extends JpaRepository<TipoHuevo, Integer> {
}
