function getCsrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (token && header) headers[header] = token;
    return headers;
}

async function cargarTiposHuevo() {
    try {
        const res = await fetch('/api/produccion/tipos-huevo', { credentials: 'same-origin' });
        if (!res.ok) return;
        const tipos = await res.json();
        
        // Cargar Tabla CRUD
        const tbody = document.querySelector('#tb-tipos-huevo');
        tbody.innerHTML = '';
        
        // Cargar Entradas en Formulario Diario
        const contenedor = document.querySelector('#contenedor-tipos-huevo');
        contenedor.innerHTML = '';

        tipos.forEach(t => {
            tbody.innerHTML += `
                <tr>
                    <td>${t.idTipo}</td>
                    <td>${t.nombre}</td>
                    <td>${t.descripcion || '-'}</td>
                </tr>`;

            contenedor.innerHTML += `
                <div class="row mb-1 align-items-center">
                    <div class="col-6"><small class="fw-semibold">${t.nombre}</small></div>
                    <div class="col-6">
                        <input type="number" class="form-control form-control-sm inp-tipo-huevo" 
                               data-tipo-id="${t.idTipo}" min="0" value="0">
                    </div>
                </div>`;
        });
    } catch (err) {
        console.error('Error al cargar tipos de huevo:', err);
    }
}

async function cargarGalpones() {
    try {
        const res = await fetch('/api/infraestructura/galpones', { credentials: 'same-origin' });
        if (!res.ok) return;
        const galpones = await res.json();
        const select = document.querySelector('#prod-galpon');
        select.innerHTML = '<option value="">Seleccione Galpón</option>';
        galpones.forEach(g => {
            select.innerHTML += <option value="${g.idGalpon}">${g.nombre}</option>;
        });
    } catch (err) {
        console.error('Error al cargar galpones:', err);
    }
}

async function cargarResumenSector() {
    try {
        const res = await fetch('/api/produccion/sectores/resumen', { credentials: 'same-origin' });
        if (!res.ok) return;
        const datos = await res.json();
        const tbody = document.querySelector('#tb-produccion-sector');
        tbody.innerHTML = '';
        datos.forEach(r => {
            tbody.innerHTML += `
                <tr>
                    <td>${r.fecha}</td>
                    <td>${r.nombreSector || r.sectorId}</td>
                    <td class="text-end"><strong>${r.totalHuevos}</strong></td>
                </tr>`;
        });
    } catch (err) {
        console.error('Error al cargar resumen sector:', err);
    }
}

// Guardar TipoHuevo
document.querySelector('#form-tipo-huevo').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        nombre: document.querySelector('#th-nombre').value,
        descripcion: document.querySelector('#th-descripcion').value
    };

    const res = await fetch('/api/produccion/tipos-huevo', {
        method: 'POST',
        credentials: 'same-origin',
        headers: getCsrfHeaders(),
        body: JSON.stringify(payload)
    });
    if (res.ok) {
        e.target.reset();
        cargarTiposHuevo();
    }
});

// Guardar ProduccionGalpon + DetalleProduccion
document.querySelector('#form-produccion').addEventListener('submit', async (e) => {
    e.preventDefault();
    const detalles = [];
    let totalHuevos = 0;

    document.querySelectorAll('.inp-tipo-huevo').forEach(inp => {
        const cantidad = parseInt(inp.value) || 0;
        if (cantidad > 0) {
            totalHuevos += cantidad;
            detalles.push({
                tipoHuevoId: parseInt(inp.dataset.tipoId),
                cantidad: cantidad
            });
        }
    });

    const payload = {
        fecha: document.querySelector('#prod-fecha').value,
        galponId: parseInt(document.querySelector('#prod-galpon').value),
        totalHuevos: totalHuevos,
        observacion: document.querySelector('#prod-observacion').value,
        detalles: detalles
    };

    const res = await fetch('/api/produccion/galpon', {
        method: 'POST',
        credentials: 'same-origin',
        headers: getCsrfHeaders(),
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        alert('Producción registrada exitosamente');
        e.target.reset();
        cargarResumenSector();
    }
});

document.addEventListener('DOMContentLoaded', () => {
    cargarTiposHuevo();
    cargarGalpones();
    cargarResumenSector();
});