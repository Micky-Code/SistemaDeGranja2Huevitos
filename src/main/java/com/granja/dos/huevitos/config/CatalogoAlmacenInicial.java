package com.granja.dos.huevitos.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.granja.dos.huevitos.models.almacen.ClasificacionHuevo;
import com.granja.dos.huevitos.repository.ClasificacionHuevoRepository;

/**
 * Siembra el catálogo fijo de clasificaciones de huevo (Rojo, Pardo, Jumbo,
 * Doble Yema, Poroso, Roto) la primera vez que arranca la aplicación, para
 * que el módulo de Almacén tenga contra qué clasificar sin necesitar una
 * pantalla de administración aparte.
 */
@Component
public class CatalogoAlmacenInicial implements ApplicationRunner {

    private static final String[][] CLASIFICACIONES_BASE = {
            { "ROJO", "Huevo rojo/pardo oscuro" },
            { "PARDO", "Huevo pardo claro" },
            { "JUMBO", "Huevo extra grande" },
            { "DOBLE_YEMA", "Huevo con dos yemas" },
            { "POROSO", "Cáscara porosa/defectuosa" },
            { "ROTO", "Huevo roto o quebrado" },
    };

    private final ClasificacionHuevoRepository clasificaciones;

    public CatalogoAlmacenInicial(ClasificacionHuevoRepository clasificaciones) {
        this.clasificaciones = clasificaciones;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (clasificaciones.count() > 0) {
            return;
        }
        for (String[] fila : CLASIFICACIONES_BASE) {
            clasificaciones.save(new ClasificacionHuevo(fila[0], fila[1]));
        }
    }
}
