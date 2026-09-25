package com.granja.dos.huevitos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.granja.dos.huevitos.models.personal.TipoDocumento;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {
    List<TipoDocumento> findAllByActivoTrueOrderByNombreAsc();
}
