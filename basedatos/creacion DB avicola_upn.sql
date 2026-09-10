-- =====================================================================
-- ALCANCE ACORDADO POR EL EQUIPO 
--   - Enfoque en POSTURA (producción de huevos), NO en engorde.
--   - NO incluye Ventas / Clientes 
--   - Estructura real de la granja:
--       Granja -> LOTE (división física: "Lote A", "Lote B")
--              -> GALPÓN (cada Lote tiene 10 galpones; cada galpón
--                 tiene 1 operario a cargo y una capacidad de aves)
--              -> GRUPO DE AVES (la "camada": cuando llega un
--                 lote de gallinas con cierta cantidad, se aloja en
--                 UN galpón; el operario cría esas aves, revisa
--                 mortalidad y producción a diario)
--   - Producción de huevos en DOS momentos (para poder controlar la
--     merma en el traslado):
--       1) En el galpón: el operario clasifica lo recolectado en
--          Rojo, Pardo, Jumbo, Doble Yema, Poroso, Roto.
--       2) En almacén: se vuelve a contar/clasificar lo que llega
--          (algunos huevos se rompen en el traslado y pasan a "Roto").
--   - El veterinario necesita ver qué día le toca vacuna a cada
--     grupo de aves (calendario de vacunación), no solo el registro
--     de lo ya aplicado.
--   - Control de plagas/roedores con fecha de control.
--   - Alimento: consumo diario y semanal, por galpón, hasta que el
--     producto final llega a almacén.
--   - Roles de login: Administrador, Jefe de Granja, Veterinario,
--     Operario.
--
-- =====================================================================

DROP DATABASE IF EXISTS avicola_upn;
CREATE DATABASE avicola_upn
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE avicola_upn;

-- =====================================================================
-- 1. SEGURIDAD Y PERSONAL
-- =====================================================================

CREATE TABLE empleados (
    id_empleado     INT AUTO_INCREMENT PRIMARY KEY,
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    dni             VARCHAR(15)  NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    cargo           VARCHAR(50)  NOT NULL,          -- Ej: Operario, Veterinario, Jefe de Granja
    fecha_ingreso   DATE NOT NULL,
    estado          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Todo usuario del sistema corresponde a un empleado real (trazabilidad
-- de quién registra cada dato).
CREATE TABLE usuarios (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado     INT NOT NULL,
    usuario         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,          -- SIEMPRE con hash (bcrypt/argon2), nunca texto plano
    rol             ENUM('ADMINISTRADOR','JEFE_GRANJA','VETERINARIO','OPERARIO') NOT NULL,
    estado          BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso   DATETIME,
    creado_en       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
);

-- =====================================================================
-- 2. ESTRUCTURA FÍSICA: LOTES (A / B) Y GALPONES
-- =====================================================================

-- "Lote" aquí es la división física de la granja (Lote A, Lote B),
CREATE TABLE lotes (
    id_lote         INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(20) NOT NULL UNIQUE,     -- 'Lote A', 'Lote B'
    descripcion     VARCHAR(150)
);

CREATE TABLE galpones (
    id_galpon           INT AUTO_INCREMENT PRIMARY KEY,
    id_lote             INT NOT NULL,
    codigo_galpon       VARCHAR(20) NOT NULL UNIQUE,   -- Ej: A-01 ... A-10, B-01 ... B-10
    capacidad_maxima    INT NOT NULL,
    id_operario_actual  INT NULL,                       -- 1 operario a cargo por galpón
    estado              ENUM('OPERATIVO','MANTENIMIENTO','INACTIVO') NOT NULL DEFAULT 'OPERATIVO',

    CONSTRAINT fk_galpon_lote FOREIGN KEY (id_lote) REFERENCES lotes(id_lote),
    CONSTRAINT fk_galpon_operario FOREIGN KEY (id_operario_actual) REFERENCES empleados(id_empleado)
);

-- Historial de asignación de operarios a galpones (por si rotan personal;
-- también sirve para auditoría de "quién estuvo a cargo de qué y cuándo")
CREATE TABLE asignaciones_operario (
    id_asignacion   INT AUTO_INCREMENT PRIMARY KEY,
    id_galpon       INT NOT NULL,
    id_empleado     INT NOT NULL,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NULL,

    CONSTRAINT fk_asig_galpon FOREIGN KEY (id_galpon) REFERENCES galpones(id_galpon),
    CONSTRAINT fk_asig_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
);

-- =====================================================================
-- 3. GRUPOS DE AVES (CAMADAS) Y CONTROL OPERATIVO DIARIO
-- =====================================================================

-- Cuando llega un lote de gallinas con cierta cantidad, se aloja en UN
-- galpón: eso es un "grupo_aves". Es la unidad que se sigue día a día
-- (mortalidad, peso, producción) durante todo su ciclo de postura.
CREATE TABLE grupos_aves (
    id_grupo             INT AUTO_INCREMENT PRIMARY KEY,
    codigo_grupo         VARCHAR(20) NOT NULL UNIQUE,
    id_galpon            INT NOT NULL,
    raza                 VARCHAR(50) NOT NULL,          -- Ej: Hy-Line, Lohmann Brown
    fecha_ingreso         DATE NOT NULL,
    fecha_inicio_postura  DATE,
    cantidad_inicial      INT NOT NULL,
    cantidad_actual       INT NOT NULL,
    etapa                 ENUM('LEVANTE','PRODUCCION','DESCARTE') NOT NULL DEFAULT 'LEVANTE',
    estado                ENUM('ACTIVO','FINALIZADO') NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT fk_grupo_galpon FOREIGN KEY (id_galpon) REFERENCES galpones(id_galpon),
    CONSTRAINT chk_grupo_cantidades
        CHECK (cantidad_actual >= 0 AND cantidad_actual <= cantidad_inicial)
);

CREATE INDEX idx_grupos_estado ON grupos_aves(estado, etapa);

-- Control de peso / crecimiento (muestreos periódicos)
CREATE TABLE control_peso (
    id_control        INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo          INT NOT NULL,
    fecha             DATE NOT NULL,
    peso_promedio_gr  DECIMAL(8,2) NOT NULL,
    aves_muestreadas  INT NOT NULL,
    id_empleado       INT NOT NULL,

    CONSTRAINT fk_peso_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_peso_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    UNIQUE KEY uq_peso_grupo_fecha (id_grupo, fecha)
);

-- Mortalidad diaria: el operario la revisa todos los días; gerencia la usa
-- para explicar por qué la producción baja (menos aves = menos huevos)
CREATE TABLE mortalidad_aves (
    id_mortalidad     INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo          INT NOT NULL,
    fecha             DATE NOT NULL,
    cantidad          INT NOT NULL,
    causa             VARCHAR(150),                  -- Ej: enfermedad, calor, causa desconocida
    id_empleado       INT NOT NULL,                  -- operario que reporta

    CONSTRAINT fk_mortalidad_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_mortalidad_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    CONSTRAINT chk_mortalidad_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_mortalidad_grupo_fecha ON mortalidad_aves(id_grupo, fecha);

-- Retiro de aves vivas (fin de ciclo de postura / baja producción) —
-- distinto de mortalidad
CREATE TABLE descarte_aves (
    id_descarte       INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo          INT NOT NULL,
    fecha             DATE NOT NULL,
    cantidad          INT NOT NULL,
    motivo            VARCHAR(150),
    id_empleado       INT NOT NULL,

    CONSTRAINT fk_descarte_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_descarte_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    CONSTRAINT chk_descarte_cantidad CHECK (cantidad > 0)
);

-- =====================================================================
-- 4. MÓDULO SANITARIO Y BIOSEGURIDAD
-- =====================================================================

CREATE TABLE enfermedades (
    id_enfermedad     INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL UNIQUE,
    descripcion       VARCHAR(255)
);

-- Catálogo de vacunas (ligado al inventario para descontar stock al aplicar)
CREATE TABLE vacunas (
    id_vacuna         INT AUTO_INCREMENT PRIMARY KEY,
    id_insumo         INT NOT NULL,                  -- FK a insumos (sección 5)
    id_enfermedad     INT,
    nombre            VARCHAR(100) NOT NULL,
    via_aplicacion    VARCHAR(50),                   -- Ej: ocular, agua de bebida, inyectable

    CONSTRAINT fk_vacuna_enfermedad FOREIGN KEY (id_enfermedad) REFERENCES enfermedades(id_enfermedad)
    -- fk_vacuna_insumo se agrega en la sección 5, tras crear "insumos"
);

-- Calendario de vacunación: lo que el veterinario necesita ver ("qué día
-- le toca su vacuna a las gallinas"), independiente de si ya se aplicó.
CREATE TABLE calendario_vacunacion (
    id_programacion   INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo          INT NOT NULL,
    id_vacuna         INT NOT NULL,
    fecha_programada  DATE NOT NULL,
    estado            ENUM('PENDIENTE','APLICADA','VENCIDA') NOT NULL DEFAULT 'PENDIENTE',
    observaciones     VARCHAR(255),

    CONSTRAINT fk_calendario_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_calendario_vacuna FOREIGN KEY (id_vacuna) REFERENCES vacunas(id_vacuna)
);

CREATE INDEX idx_calendario_fecha ON calendario_vacunacion(fecha_programada, estado);

-- Registro real de lo aplicado (vacuna, tratamiento por enfermedad, o
-- desinfección general); si viene de una vacuna programada, se referencia.
CREATE TABLE aplicaciones_sanitarias (
    id_aplicacion       INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo            INT NOT NULL,
    id_programacion     INT NULL,                    -- si proviene del calendario de vacunación
    fecha               DATE NOT NULL,
    tipo                ENUM('VACUNA','TRATAMIENTO','DESINFECCION') NOT NULL,
    id_vacuna           INT NULL,
    id_enfermedad       INT NULL,
    id_insumo           INT NULL,                    -- medicamento/insumo usado
    dosis               VARCHAR(50),
    aves_atendidas      INT,
    id_empleado         INT NOT NULL,                -- veterinario que aplica
    observaciones       VARCHAR(255),

    CONSTRAINT fk_aplic_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_aplic_programacion FOREIGN KEY (id_programacion) REFERENCES calendario_vacunacion(id_programacion),
    CONSTRAINT fk_aplic_vacuna FOREIGN KEY (id_vacuna) REFERENCES vacunas(id_vacuna),
    CONSTRAINT fk_aplic_enfermedad FOREIGN KEY (id_enfermedad) REFERENCES enfermedades(id_enfermedad),
    CONSTRAINT fk_aplic_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
);

-- Control de plagas / roedores por galpón
CREATE TABLE control_plagas (
    id_control        INT AUTO_INCREMENT PRIMARY KEY,
    id_galpon         INT NOT NULL,
    fecha             DATE NOT NULL,
    tipo_plaga        ENUM('ROEDORES','INSECTOS','AVES_SILVESTRES','OTRO') NOT NULL,
    medida_aplicada   VARCHAR(255),
    proxima_fecha     DATE,
    id_empleado       INT NOT NULL,                  -- responsable del control
    observaciones     VARCHAR(255),

    CONSTRAINT fk_plagas_galpon FOREIGN KEY (id_galpon) REFERENCES galpones(id_galpon),
    CONSTRAINT fk_plagas_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
);

CREATE INDEX idx_plagas_galpon_fecha ON control_plagas(id_galpon, fecha);

-- =====================================================================
-- 5. MÓDULO DE ALIMENTACIÓN E INVENTARIOS
-- =====================================================================
-- Modelo de inventario genérico: sirve para alimento, medicamentos,
-- vacunas e insumos, con historial de movimientos y alertas de stock
-- mínimo. El consumo de alimento se registra por grupo de aves (y por
-- lo tanto, por galpón) y por día/semana según la frecuencia con la
-- que el operario lo cargue.

CREATE TABLE categorias_insumo (
    id_categoria    INT AUTO_INCREMENT PRIMARY KEY,
    nombre          ENUM('ALIMENTO','MEDICAMENTO','VACUNA','INSUMO_GENERAL') NOT NULL UNIQUE
);

CREATE TABLE proveedores (
    id_proveedor    INT AUTO_INCREMENT PRIMARY KEY,
    razon_social    VARCHAR(150) NOT NULL,
    ruc             VARCHAR(15) UNIQUE,
    telefono        VARCHAR(20),
    direccion       VARCHAR(200)
);

CREATE TABLE insumos (
    id_insumo       INT AUTO_INCREMENT PRIMARY KEY,
    id_categoria    INT NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    unidad_medida   ENUM('KG','LT','UND','DOSIS') NOT NULL,
    stock_actual    DECIMAL(12,2) NOT NULL DEFAULT 0,
    stock_minimo    DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_insumo_categoria FOREIGN KEY (id_categoria) REFERENCES categorias_insumo(id_categoria),
    CONSTRAINT chk_insumo_stock CHECK (stock_actual >= 0)
);

-- Cierra las FKs pendientes hacia "insumos"
ALTER TABLE vacunas
    ADD CONSTRAINT fk_vacuna_insumo FOREIGN KEY (id_insumo) REFERENCES insumos(id_insumo);
ALTER TABLE aplicaciones_sanitarias
    ADD CONSTRAINT fk_aplic_insumo FOREIGN KEY (id_insumo) REFERENCES insumos(id_insumo);

-- Historial único de movimientos de inventario (entradas por compra,
-- salidas por consumo de un grupo de aves, o ajustes por conteo físico)
CREATE TABLE movimientos_inventario (
    id_movimiento    INT AUTO_INCREMENT PRIMARY KEY,
    id_insumo        INT NOT NULL,
    tipo_movimiento  ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    fecha            DATE NOT NULL,
    cantidad         DECIMAL(12,2) NOT NULL,          -- siempre positiva; el signo lo da tipo_movimiento
    id_proveedor     INT NULL,                        -- solo para ENTRADA
    costo_unitario   DECIMAL(10,2) NULL,               -- solo para ENTRADA
    id_grupo         INT NULL,                        -- solo para SALIDA (consumo de un grupo/galpón)
    id_empleado      INT NOT NULL,
    observacion      VARCHAR(255),

    CONSTRAINT fk_mov_insumo FOREIGN KEY (id_insumo) REFERENCES insumos(id_insumo),
    CONSTRAINT fk_mov_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
    CONSTRAINT fk_mov_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_mov_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_mov_insumo_fecha ON movimientos_inventario(id_insumo, fecha);
CREATE INDEX idx_mov_grupo ON movimientos_inventario(id_grupo);

-- Trigger: mantiene stock_actual sincronizado automáticamente
DELIMITER $$

CREATE TRIGGER trg_movimiento_actualiza_stock
AFTER INSERT ON movimientos_inventario
FOR EACH ROW
BEGIN
    IF NEW.tipo_movimiento = 'ENTRADA' THEN
        UPDATE insumos SET stock_actual = stock_actual + NEW.cantidad
        WHERE id_insumo = NEW.id_insumo;
    ELSEIF NEW.tipo_movimiento = 'SALIDA' THEN
        UPDATE insumos SET stock_actual = stock_actual - NEW.cantidad
        WHERE id_insumo = NEW.id_insumo;
    ELSE -- AJUSTE: la "cantidad" representa el nuevo stock real contado
        UPDATE insumos SET stock_actual = NEW.cantidad
        WHERE id_insumo = NEW.id_insumo;
    END IF;
END$$

DELIMITER ;

-- =====================================================================
-- 6. MÓDULO DE PRODUCCIÓN DE HUEVOS (GALPÓN -> ALMACÉN)
-- =====================================================================

-- Catálogo de clasificación de huevos (evita repetir 6 columnas fijas
-- en cada tabla y permite agregar una clasificación nueva sin migrar)
CREATE TABLE clasificaciones_huevo (
    id_clasificacion  INT AUTO_INCREMENT PRIMARY KEY,
    nombre            ENUM('ROJO','PARDO','JUMBO','DOBLE_YEMA','POROSO','ROTO') NOT NULL UNIQUE
);

-- 6.1 Registro EN EL GALPÓN: lo que el operario recolecta y clasifica
-- el mismo día.
CREATE TABLE produccion_galpon (
    id_produccion       INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo            INT NOT NULL,
    fecha               DATE NOT NULL,
    total_recolectado   INT NOT NULL,
    id_empleado         INT NOT NULL,                -- operario
    observaciones       VARCHAR(255),

    CONSTRAINT fk_prodgalpon_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_aves(id_grupo),
    CONSTRAINT fk_prodgalpon_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    UNIQUE KEY uq_prodgalpon_grupo_fecha (id_grupo, fecha)
);

CREATE INDEX idx_prodgalpon_fecha ON produccion_galpon(fecha);

-- Detalle de clasificación en el galpón (Rojo, Pardo, Jumbo, etc.)
CREATE TABLE produccion_galpon_detalle (
    id_detalle         INT AUTO_INCREMENT PRIMARY KEY,
    id_produccion       INT NOT NULL,
    id_clasificacion    INT NOT NULL,
    cantidad             INT NOT NULL,

    CONSTRAINT fk_pgd_produccion FOREIGN KEY (id_produccion) REFERENCES produccion_galpon(id_produccion),
    CONSTRAINT fk_pgd_clasificacion FOREIGN KEY (id_clasificacion) REFERENCES clasificaciones_huevo(id_clasificacion),
    UNIQUE KEY uq_pgd_prod_clas (id_produccion, id_clasificacion),
    CONSTRAINT chk_pgd_cantidad CHECK (cantidad >= 0)
);

-- 6.2 Registro EN ALMACÉN: reconteo de lo que llega desde cada galpón.
-- Aquí se contabilizan las roturas ocurridas en el traslado.
CREATE TABLE recepcion_almacen (
    id_recepcion        INT AUTO_INCREMENT PRIMARY KEY,
    id_produccion        INT NOT NULL,                -- referencia a la producción del galpón que llega
    fecha_recepcion       DATE NOT NULL,
    cantidad_total_recibida INT NOT NULL,
    cantidad_rota_traslado  INT NOT NULL DEFAULT 0,
    id_empleado           INT NOT NULL,                -- responsable de almacén
    observaciones          VARCHAR(255),

    CONSTRAINT fk_recepcion_produccion FOREIGN KEY (id_produccion) REFERENCES produccion_galpon(id_produccion),
    CONSTRAINT fk_recepcion_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    UNIQUE KEY uq_recepcion_produccion (id_produccion)
);

-- Detalle de clasificación ya recontada en almacén (puede diferir del
-- detalle de galpón porque algunos huevos pasan a "ROTO" en el camino)
CREATE TABLE recepcion_almacen_detalle (
    id_detalle          INT AUTO_INCREMENT PRIMARY KEY,
    id_recepcion         INT NOT NULL,
    id_clasificacion     INT NOT NULL,
    cantidad              INT NOT NULL,

    CONSTRAINT fk_rad_recepcion FOREIGN KEY (id_recepcion) REFERENCES recepcion_almacen(id_recepcion),
    CONSTRAINT fk_rad_clasificacion FOREIGN KEY (id_clasificacion) REFERENCES clasificaciones_huevo(id_clasificacion),
    UNIQUE KEY uq_rad_recep_clas (id_recepcion, id_clasificacion),
    CONSTRAINT chk_rad_cantidad CHECK (cantidad >= 0)
);

-- =====================================================================
-- 7. MÓDULO ANALÍTICO (VISTAS PARA KPIs Y TRAZABILIDAD)
-- =====================================================================
-- Se implementan como VIEWS porque son datos calculados a partir de las
-- tablas operativas, no datos capturados directamente.

-- 7.1 Consumo de alimento por grupo de aves (a partir del inventario)
CREATE VIEW v_consumo_alimento_grupo AS
SELECT
    m.id_grupo,
    m.fecha,
    SUM(m.cantidad) AS kg_consumidos
FROM movimientos_inventario m
INNER JOIN insumos i ON i.id_insumo = m.id_insumo
INNER JOIN categorias_insumo c ON c.id_categoria = i.id_categoria
WHERE m.tipo_movimiento = 'SALIDA'
  AND c.nombre = 'ALIMENTO'
  AND m.id_grupo IS NOT NULL
GROUP BY m.id_grupo, m.fecha;

-- 7.2 Resumen por grupo de aves: aves vivas, % viabilidad, huevos y FCA
CREATE VIEW v_grupo_resumen AS
SELECT
    g.id_grupo,
    g.codigo_grupo,
    g.id_galpon,
    g.cantidad_inicial,
    g.cantidad_inicial
        - COALESCE((SELECT SUM(mo.cantidad) FROM mortalidad_aves mo WHERE mo.id_grupo = g.id_grupo), 0)
        - COALESCE((SELECT SUM(d.cantidad) FROM descarte_aves d WHERE d.id_grupo = g.id_grupo), 0)
        AS aves_vivas_actuales,
    ROUND(
        (g.cantidad_inicial
            - COALESCE((SELECT SUM(mo.cantidad) FROM mortalidad_aves mo WHERE mo.id_grupo = g.id_grupo), 0)
        ) / g.cantidad_inicial * 100, 2
    ) AS porcentaje_viabilidad,
    COALESCE((SELECT SUM(p.total_recolectado) FROM produccion_galpon p WHERE p.id_grupo = g.id_grupo), 0)
        AS total_huevos_producidos,
    COALESCE((SELECT SUM(ca.kg_consumidos) FROM v_consumo_alimento_grupo ca WHERE ca.id_grupo = g.id_grupo), 0)
        AS total_kg_alimento_consumido
FROM grupos_aves g;

-- 7.3 % de Postura diario (huevos producidos en galpón / aves vivas ese día)
CREATE VIEW v_porcentaje_postura_diario AS
SELECT
    p.id_grupo,
    p.fecha,
    p.total_recolectado,
    (g.cantidad_inicial
        - COALESCE((SELECT SUM(mo.cantidad) FROM mortalidad_aves mo WHERE mo.id_grupo = p.id_grupo AND mo.fecha <= p.fecha), 0)
        - COALESCE((SELECT SUM(d.cantidad) FROM descarte_aves d WHERE d.id_grupo = p.id_grupo AND d.fecha <= p.fecha), 0)
    ) AS aves_vivas_a_la_fecha,
    ROUND(
        p.total_recolectado / NULLIF((g.cantidad_inicial
            - COALESCE((SELECT SUM(mo.cantidad) FROM mortalidad_aves mo WHERE mo.id_grupo = p.id_grupo AND mo.fecha <= p.fecha), 0)
            - COALESCE((SELECT SUM(d.cantidad) FROM descarte_aves d WHERE d.id_grupo = p.id_grupo AND d.fecha <= p.fecha), 0)
        ), 0) * 100, 2
    ) AS porcentaje_postura
FROM produccion_galpon p
INNER JOIN grupos_aves g ON g.id_grupo = p.id_grupo;

-- NOTA sobre el Índice de Eficiencia Europeo (IEE):
--   IEE = (% viabilidad x % postura promedio x peso promedio del huevo en gr)
--         / (edad del lote en días x FCA) x 100
--   Los componentes ya están en v_grupo_resumen y v_porcentaje_postura_diario;
--   el cálculo final se arma en el backend combinando ambas vistas con el
--   rango de fechas del reporte (agregar peso_promedio_gr en control_peso).

-- 7.4 Alertas de stock mínimo
CREATE VIEW v_alertas_stock AS
SELECT
    i.id_insumo,
    i.nombre,
    c.nombre AS categoria,
    i.stock_actual,
    i.stock_minimo
FROM insumos i
INNER JOIN categorias_insumo c ON c.id_categoria = i.id_categoria
WHERE i.stock_actual <= i.stock_minimo
  AND i.estado = TRUE;

-- 7.5 Producción diaria en almacén por clasificación (para gerencia:
-- cuántos huevos rojos, pardo, jumbo, etc. entraron hoy, y cuánto se
-- rompió en el traslado)
CREATE VIEW v_produccion_almacen_por_clasificacion AS
SELECT
    ra.fecha_recepcion,
    pg.id_grupo,
    cl.nombre AS clasificacion,
    rad.cantidad
FROM recepcion_almacen ra
INNER JOIN produccion_galpon pg ON pg.id_produccion = ra.id_produccion
INNER JOIN recepcion_almacen_detalle rad ON rad.id_recepcion = ra.id_recepcion
INNER JOIN clasificaciones_huevo cl ON cl.id_clasificacion = rad.id_clasificacion;

-- 7.6 Vacunas pendientes/vencidas (para que el veterinario las revise)
CREATE VIEW v_vacunas_pendientes AS
SELECT
    cv.id_programacion,
    cv.id_grupo,
    g.codigo_grupo,
    v.nombre AS vacuna,
    cv.fecha_programada,
    CASE WHEN cv.fecha_programada < CURDATE() AND cv.estado = 'PENDIENTE'
         THEN 'VENCIDA' ELSE cv.estado END AS estado_real
FROM calendario_vacunacion cv
INNER JOIN grupos_aves g ON g.id_grupo = cv.id_grupo
INNER JOIN vacunas v ON v.id_vacuna = cv.id_vacuna
WHERE cv.estado <> 'APLICADA';

-- 7.7 Historial / trazabilidad unificada de un grupo de aves
CREATE VIEW v_historial_grupo AS
SELECT id_grupo, fecha, 'PRODUCCION' AS tipo_evento,
       CONCAT(total_recolectado, ' huevos recolectados') AS detalle
FROM produccion_galpon
UNION ALL
SELECT id_grupo, fecha, 'MORTALIDAD',
       CONCAT(cantidad, ' aves - ', COALESCE(causa,'sin causa registrada'))
FROM mortalidad_aves
UNION ALL
SELECT id_grupo, fecha, 'DESCARTE',
       CONCAT(cantidad, ' aves - ', COALESCE(motivo,'sin motivo registrado'))
FROM descarte_aves
UNION ALL
SELECT id_grupo, fecha, 'CONTROL_PESO',
       CONCAT(peso_promedio_gr, ' gr promedio')
FROM control_peso
UNION ALL
SELECT id_grupo, fecha, 'SANIDAD',
       CONCAT(tipo, ' - ', COALESCE(observaciones,''))
FROM aplicaciones_sanitarias
ORDER BY fecha;

-- =====================================================================
-- 8. DATOS INICIALES (SEED)
-- =====================================================================

INSERT INTO empleados (nombres, apellidos, dni, telefono, cargo, fecha_ingreso)
VALUES ('Admin', 'Sistema', '00000000', '999999999', 'Administrador', CURDATE());

INSERT INTO usuarios (id_empleado, usuario, password_hash, rol)
VALUES (1, 'admin', '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'ADMINISTRADOR');

INSERT INTO categorias_insumo (nombre) VALUES
('ALIMENTO'), ('MEDICAMENTO'), ('VACUNA'), ('INSUMO_GENERAL');

INSERT INTO clasificaciones_huevo (nombre) VALUES
('ROJO'), ('PARDO'), ('JUMBO'), ('DOBLE_YEMA'), ('POROSO'), ('ROTO');

INSERT INTO lotes (nombre, descripcion) VALUES
('Lote A', 'División física A de la granja - 10 galpones'),
('Lote B', 'División física B de la granja - 10 galpones');

INSERT INTO galpones (id_lote, codigo_galpon, capacidad_maxima) VALUES
(1, 'A-01', 5000);




-- =====================================================================
-- DATOS DE PRUEBA - avicola_upn
-- USE avicola_upn;
-- =====================================================================

-- ---------------------------------------------------------------
-- 1) EMPLEADOS (equipo del proyecto, cada uno con un cargo real)
-- ---------------------------------------------------------------
INSERT INTO empleados (nombres, apellidos, dni, telefono, cargo, fecha_ingreso) VALUES
('Miguel Angel', 'Avendaño Lizana', '70011122', '944224184', 'Jefe de Granja', '2026-01-05'),
('Edson', 'Enriquez Roque', '70022233', '944224185', 'Veterinario', '2026-01-05'),
('Jose Nicolas', 'Vasquez Guevara', '70033344', '944224186', 'Operario', '2026-01-10'),
('Juan Pablo Angel', 'Espinoza Cruz', '70044455', '944224187', 'Operario', '2026-01-10'),
('Wilder Roberto', 'Chui Tello', '70055566', '944224188', 'Operario', '2026-01-10'),
('Lisver Javier', 'Poma Pernia', '70066677', '944224189', 'Administrador', '2026-01-05');

-- id_empleado generados (según el orden de inserción, sumando el "Admin Sistema" ya creado con id 1):
--   2 = Miguel  (Jefe de Granja)
--   3 = Edson   (Veterinario)
--   4 = Jose    (Operario)
--   5 = Juan    (Operario)
--   6 = Wilder  (Operario)
--   7 = Lisver  (Administrador)

-- ---------------------------------------------------------------
-- 2) USUARIOS (login de cada empleado)
-- Contraseña de TODOS en este seed: 123456
-- (mismo hash bcrypt ya verificado que usamos para "admin")
-- ---------------------------------------------------------------
INSERT INTO usuarios (id_empleado, usuario, password_hash, rol) VALUES
(2, 'mavendano', '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'JEFE_GRANJA'),
(3, 'eenriquez', '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'VETERINARIO'),
(4, 'jvasquez',  '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'OPERARIO'),
(5, 'jespinoza', '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'OPERARIO'),
(6, 'wchui',     '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'OPERARIO'),
(7, 'lpoma',     '$2b$12$dFpNFUKImmqcm4XYrFdSTuWw27EL4pEIOwxUDA01O4qb39wyNvO52', 'ADMINISTRADOR');

-- ---------------------------------------------------------------
-- 3) GALPONES adicionales (ya existe A-01; agregamos algunos más
--    de Lote A y los primeros de Lote B) + operario a cargo
-- ---------------------------------------------------------------
INSERT INTO galpones (id_lote, codigo_galpon, capacidad_maxima, id_operario_actual) VALUES
(1, 'A-02', 5000, 4),   -- Jose a cargo
(1, 'A-03', 5000, 5),   -- Juan a cargo
(2, 'B-01', 5000, 6);   -- Wilder a cargo

UPDATE galpones SET id_operario_actual = 4 WHERE codigo_galpon = 'A-01'; -- Jose también a cargo de A-01

INSERT INTO asignaciones_operario (id_galpon, id_empleado, fecha_inicio) VALUES
(1, 4, '2026-01-10'),
(2, 4, '2026-01-10'),
(3, 5, '2026-01-10'),
(4, 6, '2026-01-10');

-- ---------------------------------------------------------------
-- 4) GRUPOS DE AVES (camadas) - una por galpón
-- ---------------------------------------------------------------
INSERT INTO grupos_aves (codigo_grupo, id_galpon, raza, fecha_ingreso, fecha_inicio_postura, cantidad_inicial, cantidad_actual, etapa) VALUES
('GRP-A01-2026', 1, 'Hy-Line Brown', '2026-01-15', '2026-02-15', 4500, 4460, 'PRODUCCION'),
('GRP-A02-2026', 2, 'Lohmann Brown', '2026-01-20', '2026-02-20', 4500, 4480, 'PRODUCCION'),
('GRP-A03-2026', 3, 'Hy-Line Brown', '2026-02-01', NULL,          4500, 4495, 'LEVANTE'),
('GRP-B01-2026', 4, 'Lohmann Brown', '2026-01-15', '2026-02-15', 4500, 4470, 'PRODUCCION');

-- ---------------------------------------------------------------
-- 5) INSUMOS + MOVIMIENTOS DE INVENTARIO (compra y consumo)
-- ---------------------------------------------------------------
INSERT INTO proveedores (razon_social, ruc, telefono, direccion) VALUES
('Molino Cañete SAC', '20512345678', '015551234', 'Av. Mariscal Benavides 450, Cañete'),
('Veterinaria AviSalud EIRL', '20598765432', '015559876', 'Jr. Lima 210, Cañete');

INSERT INTO insumos (id_categoria, nombre, unidad_medida, stock_actual, stock_minimo) VALUES
(1, 'Alimento Postura Fase 1', 'KG', 0, 500),
(1, 'Alimento Levante', 'KG', 0, 300),
(3, 'Vacuna Newcastle', 'DOSIS', 0, 200),
(2, 'Vitamina AD3E', 'LT', 0, 10);

-- Entradas (compras)
INSERT INTO movimientos_inventario (id_insumo, tipo_movimiento, fecha, cantidad, id_proveedor, costo_unitario, id_empleado) VALUES
(1, 'ENTRADA', '2026-02-01', 3000, 1, 2.80, 7),
(2, 'ENTRADA', '2026-02-01', 1500, 1, 2.50, 7),
(3, 'ENTRADA', '2026-02-01',  500, 2, 1.20, 7);

-- Salidas (consumo diario por grupo)
INSERT INTO movimientos_inventario (id_insumo, tipo_movimiento, fecha, cantidad, id_grupo, id_empleado) VALUES
(1, 'SALIDA', '2026-02-16', 480, 1, 4),
(1, 'SALIDA', '2026-02-16', 470, 2, 4),
(2, 'SALIDA', '2026-02-16', 450, 3, 5),
(1, 'SALIDA', '2026-02-16', 460, 4, 6);

-- ---------------------------------------------------------------
-- 6) MORTALIDAD Y CONTROL DE PESO
-- ---------------------------------------------------------------
INSERT INTO mortalidad_aves (id_grupo, fecha, cantidad, causa, id_empleado) VALUES
(1, '2026-02-16', 3, 'calor', 4),
(2, '2026-02-16', 2, 'causa desconocida', 4),
(4, '2026-02-16', 4, 'picaje', 6);

INSERT INTO control_peso (id_grupo, fecha, peso_promedio_gr, aves_muestreadas, id_empleado) VALUES
(3, '2026-02-16', 850.50, 50, 5);

-- ---------------------------------------------------------------
-- 7) PRODUCCIÓN EN GALPÓN + RECEPCIÓN EN ALMACÉN (con clasificación)
-- ---------------------------------------------------------------
INSERT INTO produccion_galpon (id_grupo, fecha, total_recolectado, id_empleado) VALUES
(1, '2026-02-16', 3800, 4),
(2, '2026-02-16', 3750, 4),
(4, '2026-02-16', 3700, 6);

-- Detalle por clasificación (id_clasificacion: 1=ROJO 2=PARDO 3=JUMBO 4=DOBLE_YEMA 5=POROSO 6=ROTO)
INSERT INTO produccion_galpon_detalle (id_produccion, id_clasificacion, cantidad) VALUES
(1,1,1500),(1,2,2000),(1,3,250),(1,6,50),
(2,1,1400),(2,2,2100),(2,3,220),(2,6,30),
(3,1,1450),(3,2,2050),(3,3,180),(3,6,20);

INSERT INTO recepcion_almacen (id_produccion, fecha_recepcion, cantidad_total_recibida, cantidad_rota_traslado, id_empleado) VALUES
(1, '2026-02-16', 3780, 20, 7),
(2, '2026-02-16', 3735, 15, 7),
(3, '2026-02-16', 3690, 10, 7);

INSERT INTO recepcion_almacen_detalle (id_recepcion, id_clasificacion, cantidad) VALUES
(1,1,1495),(1,2,1995),(1,3,250),(1,6,40),
(2,1,1395),(2,2,2095),(2,3,215),(2,6,30),
(3,1,1445),(3,2,2045),(3,3,175),(3,6,25);

-- ---------------------------------------------------------------
-- 8) SANIDAD: vacuna + calendario + control de plagas
-- ---------------------------------------------------------------
INSERT INTO enfermedades (nombre, descripcion) VALUES
('Newcastle', 'Enfermedad viral respiratoria y nerviosa en aves');

INSERT INTO vacunas (id_insumo, id_enfermedad, nombre, via_aplicacion) VALUES
(3, 1, 'Newcastle', 'ocular');

INSERT INTO calendario_vacunacion (id_grupo, id_vacuna, fecha_programada) VALUES
(3, 1, '2026-09-20'),   -- pendiente (a futuro respecto a hoy)
(1, 1, '2026-02-25');   -- ya vencida (quedará como VENCIDA en la vista)

INSERT INTO control_plagas (id_galpon, fecha, tipo_plaga, medida_aplicada, proxima_fecha, id_empleado) VALUES
(1, '2026-02-10', 'ROEDORES', 'Cebos rodenticidas en perímetro', '2026-03-10', 4),
(4, '2026-02-12', 'INSECTOS', 'Fumigación con piretroides', '2026-03-12', 6);

