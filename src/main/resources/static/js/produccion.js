let todosLosGalpones = [];
let sectorActual = null;
let galponActual = null;
let datosAnalisis = null;
let consultaIndicadores = 0;

async function consultarIndicadores(url) {
    const res = await fetch(url, { credentials: 'same-origin' });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || data.mensaje || 'No se pudieron cargar los datos de producción.');
    return data;
}

function errorIndicadores(error) {
    const mensaje = document.getElementById('produccion-mensaje');
    mensaje.textContent = error.message;
    mensaje.className = 'alert alert-danger';
}

async function cargarSectores() {
    const sectores = await consultarIndicadores('/api/infraestructura/sectores');
    const contenedor = document.getElementById('sector-list');
    contenedor.replaceChildren();
    if (!sectores.length) contenedor.textContent = 'No hay sectores registrados.';
    sectores.forEach(s => {
        const columna = document.createElement('div'); columna.className = 'col-md-4';
        const boton = document.createElement('button'); boton.type = 'button';
        boton.className = 'card p-3 shadow-sm text-center w-100';
        const titulo = document.createElement('span'); titulo.className = 'fw-bold text-success fs-5'; titulo.textContent = s.nombre;
        boton.append(titulo); boton.addEventListener('click', () => seleccionarSector(s.idSector, s.nombre));
        columna.append(boton); contenedor.append(columna);
    });
}

async function cargarGalpones() {
    todosLosGalpones = await consultarIndicadores('/api/infraestructura/galpones');
}

function seleccionarSector(id, nombre) {
    sectorActual = { id, nombre };
    document.getElementById('step-sector').classList.add('d-none');
    document.getElementById('step-galpon').classList.remove('d-none');
    mostrarGalponesDelSector(id);
}

function mostrarGalponesDelSector(idSector) {
    const galpones = todosLosGalpones.filter(g => g.sectorId === idSector);
    const contenedor = document.getElementById('galpon-list');
    contenedor.replaceChildren();
    if (!galpones.length) contenedor.textContent = 'No hay galpones registrados en este sector.';
    galpones.forEach(g => {
        const columna = document.createElement('div'); columna.className = 'col-md-4';
        const boton = document.createElement('button'); boton.type = 'button';
        const fondo = g.estado === 'Inactivo' ? 'bg-inactivo' : g.estado === 'Mantenimiento' ? 'bg-mantenimiento' : 'bg-activo';
        boton.className = `card p-3 shadow-sm galpon-card w-100 text-start ${fondo}`;
        const titulo = document.createElement('span'); titulo.className = 'fw-bold fs-5'; titulo.textContent = g.nombre;
        const estado = document.createElement('span'); estado.textContent = `Estado: ${g.estado}`;
        const capacidad = document.createElement('span'); capacidad.className = 'small text-muted'; capacidad.textContent = `Capacidad: ${g.capacidad}`;
        boton.append(titulo, estado, capacidad); boton.addEventListener('click', () => seleccionarGalpon(g.idGalpon));
        columna.append(boton); contenedor.append(columna);
    });
}

function volverSectores() {
    document.getElementById('step-galpon').classList.add('d-none');
    document.getElementById('step-sector').classList.remove('d-none');
}

async function seleccionarGalpon(idGalpon) {
    galponActual = idGalpon;
    const consulta = ++consultaIndicadores;
    datosAnalisis = null;
    document.getElementById('step-galpon').classList.add('d-none');
    document.getElementById('step-analisis').classList.remove('d-none');
    document.getElementById('analisis-resultado').classList.add('d-none');
    for (const id of ['lbl-galpon-sector', 'lbl-gallinas', 'lbl-prod-ayer', 'lbl-prom-diario']) {
        document.getElementById(id).textContent = '…';
    }
    try {
        const modo = document.getElementById('indicador-modo').value;
        const fecha = document.getElementById('indicador-fecha');
        fecha.disabled = modo === 'productivo';
        const params = new URLSearchParams();
        if (modo === 'productivo') params.set('masProductivo', 'true');
        else if (fecha.value) params.set('fecha', fecha.value);
        const data = await consultarIndicadores(`/api/produccion/registro/indicadores/${idGalpon}?${params}`);
        if (galponActual !== idGalpon || consulta !== consultaIndicadores) return;
        if (modo === 'fecha') fecha.value = data.fechaConsultada;
        document.getElementById('lbl-produccion-titulo').textContent = modo === 'productivo' ? 'Día más productivo' : 'Producción por fecha';
        document.getElementById('indicador-ayuda').textContent = data.fechaConsultada
            ? (modo === 'productivo' ? 'Mayor producción del historial: ' : 'Fecha consultada: ') + data.fechaConsultada
            : 'No hay registros de producción para este galpón.';
        datosAnalisis = data;
        document.getElementById('lbl-galpon-sector').textContent = `${data.nombreGalpon} / ${data.nombreSector}`;
        document.getElementById('lbl-gallinas').textContent = data.cantidadGallinas;
        document.getElementById('lbl-prod-ayer').textContent = data.produccionFecha;
        document.getElementById('lbl-prom-diario').textContent = data.promedioProduccionDiaria.toFixed(2);
    } catch (error) { if (consulta === consultaIndicadores && galponActual === idGalpon) { errorIndicadores(error); volverGalpones(); } }
}

function volverGalpones() {
    galponActual = null; datosAnalisis = null;
    document.getElementById('step-analisis').classList.add('d-none');
    document.getElementById('step-galpon').classList.remove('d-none');
}

function realizarAnalisis() {
    if (!datosAnalisis) return;
    document.getElementById('lbl-prom-mes').textContent = datosAnalisis.promedioProduccionMes.toFixed(2);
    const estado = document.getElementById('lbl-estado-prod');
    estado.textContent = datosAnalisis.estadoProduccion;
    estado.className = `fw-bold ${datosAnalisis.estadoProduccion === 'Óptima' ? 'text-success' : datosAnalisis.estadoProduccion === 'Regular' ? 'text-warning' : 'text-secondary'}`;
    document.getElementById('analisis-resultado').classList.remove('d-none');
}

document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('indicador-modo').addEventListener('change', () => {
        document.getElementById('indicador-fecha').disabled = document.getElementById('indicador-modo').value === 'productivo';
        if (galponActual !== null) seleccionarGalpon(galponActual);
    });
    document.getElementById('indicador-fecha').addEventListener('change', () => {
        if (galponActual !== null) seleccionarGalpon(galponActual);
    });
    try { await cargarGalpones(); await cargarSectores(); }
    catch (error) { errorIndicadores(error); }
});
