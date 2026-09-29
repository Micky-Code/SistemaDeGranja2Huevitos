function getCsrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (token && header) headers[header] = token;
    return headers;
}

// Variable global para almacenar datos en memoria para los filtros en cascada
let cacheSectores = [];
let cacheGalpones = [];
let cacheLotes = [];

// 1. Cargar Sectores (Actualizado sin descripciones)
async function cargarSectores() {
    try {
        const res = await fetch('/api/infraestructura/sectores', { credentials: 'same-origin' });
        if (!res.ok) return;
        cacheSectores = await res.json();
        
        const selectGalponSec = document.querySelector('#gal-sector');
        const selectFiltroSec = document.querySelector('#lg-sector-filtro');
        const tbodySectores = document.querySelector('#tb-sectores'); // <-- Capturamos la tabla
        
        selectGalponSec.innerHTML = '<option value="">Seleccione Sector</option>';
        selectFiltroSec.innerHTML = '<option value="">Seleccione Sector</option>';
        tbodySectores.innerHTML = ''; // Limpiamos la tabla

        let opcionesHTML = "";
        let filasSectoresHTML = ""; // Memoria para la tabla

        cacheSectores.forEach(s => {
            opcionesHTML += `<option value="${s.idSector}">${s.nombre}</option>`;
            // Construimos la fila de la tabla HTML
            filasSectoresHTML += `
                <tr>
                    <td>${s.idSector}</td>
                    <td>${s.nombre}</td>
                    <td>${s.descripcion || 'Sin descripción'}</td>
                </tr>`;
        });

        // Inyectamos todo al DOM de golpe
        selectGalponSec.innerHTML += opcionesHTML; 
        selectFiltroSec.innerHTML += opcionesHTML;
        tbodySectores.innerHTML = filasSectoresHTML; // <-- Pintamos la tabla
        
    } catch (err) {
        console.error('Error al cargar sectores:', err);
    }
}

// 2. Cargar Galpones (Tabla con ID, Galpón, Estado y Sector)
async function cargarGalpones() {
    try {
        const res = await fetch('/api/infraestructura/galpones', { credentials: 'same-origin' });
        if (!res.ok) return;
        cacheGalpones = await res.json();
        
        const tbody = document.querySelector('#tb-galpones');
        tbody.innerHTML = '';

        cacheGalpones.forEach(g => {
            // Soportamos si el objeto sector viene anidado o plano
            const nombreSector = g.sector ? g.sector.nombre : (cacheSectores.find(s => s.idSector === g.sectorId)?.nombre || 'Sin Sector');
            
            tbody.innerHTML += `
                <tr>
                    <td>${g.idGalpon}</td>
                    <td><strong>${g.nombre}</strong></td>
                    <td><span class="badge bg-success">${g.estado || 'Activo'}</span></td>
                    <td>${nombreSector}</td>
                </tr>`;
        });
    } catch (err) {
        console.error('Error al cargar galpones:', err);
    }
}

// 3. Cargar Lotes y enlazar selección automática de cantidad
async function cargarLotes() {
    try {
        const res = await fetch('/api/infraestructura/lotes', { credentials: 'same-origin' });
        if (!res.ok) return;
        cacheLotes = await res.json();
        
        const tbody = document.querySelector('#tb-lotes');
        const selectLoteLG = document.querySelector('#lg-lote');
        tbody.innerHTML = '';
        selectLoteLG.innerHTML = '<option value="">Seleccione Lote</option>';

        cacheLotes.forEach(l => {
            tbody.innerHTML += `
                <tr>
                    <td>${l.idLote}</td>
                    <td>${l.nombre}</td>
                    <td>${l.cantidadInicial}</td>
                    <td>${l.cantidadActual}</td>
                    <td>${l.fechaIngreso}</td>
                </tr>`;
            selectLoteLG.innerHTML += `<option value="${l.idLote}" data-cantidad="${l.cantidadActual}">${l.nombre} (Disponibles: ${l.cantidadActual})</option>`;
        });
    } catch (err) {
        console.error('Error al cargar lotes:', err);
    }
}

// Evento en cascada 1: Al seleccionar un Lote, se autocompleta la cantidad de aves
document.querySelector('#lg-lote').addEventListener('change', (e) => {
    const selectedOption = e.target.options[e.target.selectedIndex];
    const cantidad = selectedOption.getAttribute('data-cantidad') || '';
    document.querySelector('#lg-cantidad').value = cantidad;
});

// Evento en cascada 2: Al seleccionar el Sector, se cargan únicamente los galpones de ese sector
document.querySelector('#lg-sector-filtro').addEventListener('change', (e) => {
    const sectorId = parseInt(e.target.value);
    const selectGalponDestino = document.querySelector('#lg-galpon');
    
    selectGalponDestino.innerHTML = '<option value="">Seleccione Galpón</option>';
    
    if (!sectorId) return;

    const galponesFiltrados = cacheGalpones.filter(g => g.sectorId === sectorId || (g.sector && g.sector.idSector === sectorId));
    
    if (galponesFiltrados.length === 0) {
        selectGalponDestino.innerHTML = '<option value="">No hay galpones en este sector</option>';
        return;
    }

    galponesFiltrados.forEach(g => {
        selectGalponDestino.innerHTML += `<option value="${g.idGalpon}">${g.nombre} (Cap: ${g.capacidad})</option>`;
    });
});
// Guardar Lote
document.querySelector('#form-lote').addEventListener('submit', async (e) => {
    e.preventDefault();
    const cant = parseInt(document.querySelector('#lote-cant-inicial').value);
    const payload = {
        nombre: document.querySelector('#lote-nombre').value,
        cantidadInicial: cant,
        cantidadActual: cant,
        fechaIngreso: document.querySelector('#lote-fecha-ingreso').value
    };

    const res = await fetch('/api/infraestructura/lotes', {
        method: 'POST',
        credentials: 'same-origin',
        headers: getCsrfHeaders(),
        body: JSON.stringify(payload)
    });
    if (res.ok) {
        e.target.reset();
        cargarLotes();
    }
});
async function cargarVistaInteractivaGalpones() {
    try {
        // Obtenemos en paralelo sectores y galpones
        const [resSectores, resGalpones] = await Promise.all([
            fetch('/api/infraestructura/sectores', { credentials: 'same-origin' }),
            fetch('/api/infraestructura/galpones', { credentials: 'same-origin' })
        ]);

        if (!resSectores.ok || !resGalpones.ok) return;
        const sectores = await resSectores.json();
        const galpones = await resGalpones.json();

        const contenedor = document.querySelector('#contenedor-sectores-galpones');
        contenedor.innerHTML = '';

        sectores.forEach(sector => {
            // Filtramos los galpones que pertenecen a este sector
            const galponesDelSector = galpones.filter(g => g.sectorId === sector.idSector || (g.sector && g.sector.idSector === sector.idSector));

            let galponesCardsHTML = '';
            if (galponesDelSector.length === 0) {
                galponesCardsHTML = `<p class="text-muted small fst-italic">No hay galpones registrados en este sector.</p>`;
            } else {
                galponesCardsHTML = `<div class="d-flex flex-wrap gap-3">`;
                galponesDelSector.forEach(g => {
                    galponesCardsHTML += `
                        <div class="galpon-card-item p-3 border rounded-3 shadow-sm bg-light text-center" 
                             style="width: 160px; cursor: pointer; transition: all 0.2s ease;"
                             onclick="seleccionarGalpon(${g.idGalpon}, '${g.nombre}')">
                            <div class="fs-1 mb-1">🏠</div>
                            <h6 class="fw-bold text-dark mb-1">${g.nombre}</h6>
                            <small class="text-secondary d-block">Cap: ${g.capacidad}</small>
                            <span class="badge bg-success mt-2">${g.estado || 'Activo'}</span>
                        </div>`;
                });
                galponesCardsHTML += `</div>`;
            }

            contenedor.innerHTML += `
                <div class="border-bottom pb-3 mb-3">
                    <h6 class="fw-bold text-success fs-5 mb-2">📍 Sector: ${sector.nombre}</h6>
                    <p class="text-muted small mb-3">${sector.descripcion || 'Sin descripción'}</p>
                    ${galponesCardsHTML}
                </div>`;
        });
    } catch (err) {
        console.error('Error al cargar la vista interactiva de galpones:', err);
    }
}

// Función de interacción al hacer clic en un galpón
function seleccionarGalpon(idGalpon, nombreGalpon) {
    alert(`Has seleccionado el galpón: ${nombreGalpon} (ID: ${idGalpon})`);
    // Aquí puedes redirigir a una vista de detalles o precargar el ID en formularios de producción
}


// Guardar LoteGalpon
document.querySelector('#form-lote-galpon').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        loteId: parseInt(document.querySelector('#lg-lote').value),
        galponId: parseInt(document.querySelector('#lg-galpon').value),
        cantidadAves: parseInt(document.querySelector('#lg-cantidad').value),
        fechaIngreso: document.querySelector('#lg-fecha').value
    };

    const res = await fetch('/api/infraestructura/lotes-galpon', {
        method: 'POST',
        credentials: 'same-origin',
        headers: getCsrfHeaders(),
        body: JSON.stringify(payload)
    });
    if (res.ok) {
        alert('Asignación registrada con éxito');
        e.target.reset();
        cargarLotes();
    }
});

document.addEventListener('DOMContentLoaded', () => {
    cargarSectores();
    cargarGalpones();
    cargarLotes();
});
//-----------
// Contrato de inserción para Gestión de Sectores
document.querySelector('#form-sector').addEventListener('submit', async (e) => {
    // 1. Interceptamos el evento nativo para evitar la destrucción del estado (recarga de pestaña)
    e.preventDefault();
    
    // 2. Extraemos los valores exactos del DOM basados en tu HTML
    const payload = {
        nombre: document.querySelector('#sec-nombre').value,
        descripcion: document.querySelector('#sec-descripcion').value
    };

    try {
        // 3. Transmisión asíncrona hacia el controlador Spring Boot
        const res = await fetch('/api/infraestructura/sectores', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                ...getCsrfHeaders() // Inyección del token de seguridad Thymeleaf
            },
            body: JSON.stringify(payload)
        });

        // 4. Lógica de reconciliación de la vista
        if (res.ok) {
            e.target.reset(); // Limpiamos las cajas de texto tras el éxito HTTP 201
            cargarSectores(); // Refrescamos la memoria RAM y re-renderizamos los combos desplegables
        } else {
            console.error("El servidor Spring Boot rechazó la petición. Código HTTP:", res.status);
        }
    } catch (err) {
        console.error('Fallo crítico en la capa de red al intentar guardar el sector:', err);
    }
});
// Contrato de inserción para Gestión de Galpones
document.querySelector('#form-galpon').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const payload = {
        nombre: document.querySelector('#gal-nombre').value,
        capacidad: parseInt(document.querySelector('#gal-capacidad').value),
        sectorId: parseInt(document.querySelector('#gal-sector').value),
        estado: 'Activo'
    };

    try {
        const res = await fetch('/api/infraestructura/galpones', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                ...getCsrfHeaders()
            },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            e.target.reset(); 
            cargarGalpones(); // Refresca la tabla de galpones
            cargarVistaInteractivaGalpones(); // Refresca los selectores en cascada
        } else {
            console.error("Error al guardar galpón. HTTP:", res.status);
        }
    } catch (err) {
        console.error('Fallo en la red (Galpones):', err);
    }
});