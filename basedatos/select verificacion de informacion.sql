USE avicola_upn;

-- Resumen de todos los grupos de aves activos
SELECT * FROM v_grupo_resumen;

-- Historial completo de un grupo de aves
SELECT * FROM v_historial_grupo WHERE id_grupo = 1;

-- Insumos por debajo del stock mínimo
SELECT * FROM v_alertas_stock;

-- Producción de almacén de hoy, por clasificación
SELECT * FROM v_produccion_almacen_por_clasificacion WHERE fecha_recepcion = CURDATE();

-- Vacunas pendientes o vencidas
SELECT * FROM v_vacunas_pendientes;


-- 1) Seguridad y personal
SELECT * FROM empleados;
SELECT * FROM usuarios;

-- 2) Estructura física
SELECT * FROM lotes;
SELECT * FROM galpones;
SELECT * FROM asignaciones_operario;

-- 3) Grupos de aves y control operativo diario
SELECT * FROM grupos_aves;
SELECT * FROM control_peso;
SELECT * FROM mortalidad_aves;
SELECT * FROM descarte_aves;

-- 4) Sanidad y bioseguridad
SELECT * FROM enfermedades;
SELECT * FROM vacunas;
SELECT * FROM calendario_vacunacion;
SELECT * FROM aplicaciones_sanitarias;
SELECT * FROM control_plagas;

-- 5) Alimentación e inventarios
SELECT * FROM categorias_insumo;
SELECT * FROM proveedores;
SELECT * FROM insumos;
SELECT * FROM movimientos_inventario;

-- 6) Producción de huevos (galpón -> almacén)
SELECT * FROM clasificaciones_huevo;
SELECT * FROM produccion_galpon;
SELECT * FROM produccion_galpon_detalle;
SELECT * FROM recepcion_almacen;
SELECT * FROM recepcion_almacen_detalle;