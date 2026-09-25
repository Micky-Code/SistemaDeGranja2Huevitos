package com.granja.dos.huevitos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.granja.dos.huevitos.models.personal.DocumentoIdentidad;

public interface DocumentoIdentidadRepository extends JpaRepository<DocumentoIdentidad, Integer> {
    @EntityGraph(attributePaths = "tipoDocumento")
    Optional<DocumentoIdentidad> findFirstByPersona_IdPersonaOrderByIdDocumentoAsc(Integer idPersona);

    Optional<DocumentoIdentidad> findByTipoDocumento_IdTipoDocumentoAndNumeroDocumentoIgnoreCase(
            Integer idTipoDocumento, String numeroDocumento);
}
