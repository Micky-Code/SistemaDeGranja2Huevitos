function getCsrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = { 'Content-Type': 'application/json' };
    if (token && header) headers[header] = token;
    return headers;
}

// 1. Cargar Sectores
async function cargarSectores() {
    try {
        const res = await fetch('/api/infraestructura/sectores', { credentials: 'same-origin' });
        if (!res.ok) return;
        const sectores = await res.json();
        
        const tbody = document.querySelector('#tb-sectores');
        const select = document.querySelector('#gal-sector');
        tbody.innerHTML = '';
        select.innerHTML = '<option value="">Seleccione Sector</option>';

        sectores.forEach(s => {
            tbody.innerHTML += `
                <tr>
                    <td>${s.idSector}</td>
                    <td>${s.nombre}</td>
                    <td>${s.descripcion || '-'}</td>
                </tr>`;
            select.innerHTML += <option value="${s.idSector}">${s.nombre}</option>;
        });
    } catch (err) {
        console.error('Error al cargar sectores:', err);
    }
}

// 2. Cargar Galpones
async function cargarGalpones() {
    try {
        const res = await fetch('/api/infraestructura/galpones', { credentials: 'same-origin' });
        if (!res.ok) return;
        const galpones = await res.json();
        
        const tbody = document.querySelector('#tb-galpones');
        const selectLG = document.querySelector('#lg-galpon');
        tbody.innerHTML = '';
        selectLG.innerHTML = '<option value="">Seleccione Galpón</option>';

        galpones.forEach(g => {
            tbody.innerHTML += `
                <tr>
                    <td>${g.idGalpon}</td>
                    <td>${g.nombre}</td>
                    <td>${g.capacidad}</td>
                    <td><span class="badge bg-success">${g.estado || 'Activo'}</span></td>
                </tr>`;
            selectLG.innerHTML += <option value="${g.idGalpon}">${g.nombre}</option>;
        });
    } catch (err) {
        console.error('Error al cargar galpones:', err);
    }
}

// 3. Cargar Lotes
async function cargarLotes() {
    try {
        const res = await fetch('/api/infraestructura/lotes', { credentials: 'same-origin' });
        if (!res.ok) return;
        const lotes = await res.json();
        
        const tbody = document.querySelector('#tb-lotes');
        const selectLG = document.querySelector('#lg-lote');
        tbody.innerHTML = '';
        selectLG.innerHTML = '<option value="">Seleccione Lote</option>';

        lotes.forEach(l => {
            tbody.innerHTML += `
                <tr>
                    <td>${l.idLote}</td>
                    <td>${l.nombre}</td>
                    <td>${l.cantidadInicial}</td>
                    <td>${l.cantidadActual}</td>
                    <td>${l.fechaIngreso}</td>
                </tr>`;
            selectLG.innerHTML += <option value="${l.idLote}">${l.nombre}</option>;
        });
    } catch (err) {
        console.error('Error al cargar lotes:', err);
    }
}

// Guardar Sector
document.querySelector('#form-sector').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        nombre: document.querySelector('#sec-nombre').value,
        descripcion: document.querySelector('#sec-descripcion').value
    };

    const res = await fetch('/api/infraestructura/sectores', {
        method: 'POST',
        credentials: 'same-origin',
        headers: getCsrfHeaders(),
        body: JSON.stringify(payload)
    });
    if (res.ok) {
        e.target.reset();
        cargarSectores();
    }
});

// Guardar Galpón
document.querySelector('#form-galpon').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        nombre: document.querySelector('#gal-nombre').value,
        capacidad: parseInt(document.querySelector('#gal-capacidad').value),
        estado: 'Activo',
        sectorId: parseInt(document.querySelector('#gal-sector').value)
    };

    const res = await fetch('/api/infraestructura/galpones', {
        method: 'POST',
        credentials: 'same-origin',
        headers: getCsrfHeaders(),
        body: JSON.stringify(payload)
    });
    if (res.ok) {
        e.target.reset();
        cargarGalpones();
    }
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