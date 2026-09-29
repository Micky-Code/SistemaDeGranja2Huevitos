let todosLosGalpones = [];
let sectorActual = null;
let galponActual = null;
let datosAnalisis = null;

async function cargarSectores() {
    try {
        const res = await fetch('/api/infraestructura/sectores', { credentials: 'same-origin' });
        const sectores = await res.json();
        
        const contenedor = document.getElementById('sector-list');
        contenedor.innerHTML = '';
        
        sectores.forEach(s => {
            contenedor.innerHTML += `
                <div class="col-md-4">
                    <div class="card p-3 shadow-sm text-center" style="cursor:pointer;" onclick="seleccionarSector(${s.idSector}, '${s.nombre}')">
                        <h5 class="fw-bold text-success">${s.nombre}</h5>
                        <p class="text-muted small mb-0">${s.descripcion || 'Sector de producción'}</p>
                    </div>
                </div>
            `;
        });
    } catch (e) { console.error('Error', e); }
}

async function cargarGalpones() {
    try {
        const res = await fetch('/api/infraestructura/galpones', { credentials: 'same-origin' });
        todosLosGalpones = await res.json();
    } catch (e) { console.error('Error', e); }
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
    contenedor.innerHTML = '';

    if(galpones.length === 0) {
        contenedor.innerHTML = '<div class="alert alert-warning w-100">No hay galpones registrados en este sector.</div>';
        return;
    }

    galpones.forEach(g => {
        let claseBg = 'bg-activo';
        let icon = 'fa-check-circle text-success';
        if (g.estado === 'Inactivo') { claseBg = 'bg-inactivo'; icon = 'fa-times-circle text-danger'; }
        else if (g.estado === 'Mantenimiento') { claseBg = 'bg-mantenimiento'; icon = 'fa-wrench text-primary'; }

        contenedor.innerHTML += `
            <div class="col-md-4">
                <div class="card p-3 shadow-sm galpon-card ${claseBg}" onclick="seleccionarGalpon(${g.idGalpon})">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="fw-bold mb-0">${g.nombre}</h5>
                        <i class="fa ${icon} fs-4"></i>
                    </div>
                    <p class="mb-0 mt-2 text-dark"><strong>Estado:</strong> ${g.estado}</p>
                    <p class="small text-muted mb-0">Capacidad: ${g.capacidad}</p>
                    <button class="btn btn-sm btn-dark mt-2 w-100">Ingresar al Galpón</button>
                </div>
            </div>
        `;
    });
}

function volverSectores() {
    document.getElementById('step-galpon').classList.add('d-none');
    document.getElementById('step-sector').classList.remove('d-none');
}

async function seleccionarGalpon(idGalpon) {
    galponActual = idGalpon;
    document.getElementById('step-galpon').classList.add('d-none');
    document.getElementById('step-analisis').classList.remove('d-none');
    document.getElementById('analisis-resultado').classList.add('d-none');
    
    // Fetch data
    try {
        const res = await fetch(`/api/produccion/analisis/${idGalpon}`, { credentials: 'same-origin' });
        if(!res.ok) {
            alert('Este galpón no tiene lotes asignados o no hay datos.');
            volverGalpones();
            return;
        }
        datosAnalisis = await res.json();
        
        document.getElementById('lbl-galpon-sector').innerText = `${datosAnalisis.nombreGalpon} / ${datosAnalisis.nombreSector}`;
        document.getElementById('lbl-gallinas').innerText = datosAnalisis.cantidadGallinas;
        document.getElementById('lbl-prod-ayer').innerText = datosAnalisis.produccionAyer;
        document.getElementById('lbl-prom-diario').innerText = datosAnalisis.promedioProduccionDiaria.toFixed(2);
        
    } catch(e) {
        console.error(e);
        alert('Error obteniendo datos del galpón.');
        volverGalpones();
    }
}

function volverGalpones() {
    document.getElementById('step-analisis').classList.add('d-none');
    document.getElementById('step-galpon').classList.remove('d-none');
}

function realizarAnalisis() {
    if(!datosAnalisis) return;
    
    document.getElementById('lbl-prom-mes').innerText = datosAnalisis.promedioProduccionMes.toFixed(2);
    const lblEstado = document.getElementById('lbl-estado-prod');
    lblEstado.innerText = datosAnalisis.estadoProduccion;
    
    if (datosAnalisis.estadoProduccion === 'Óptima') {
        lblEstado.className = 'text-success fw-bold';
    } else if (datosAnalisis.estadoProduccion === 'Regular') {
        lblEstado.className = 'text-warning fw-bold';
    } else {
        lblEstado.className = 'text-danger fw-bold';
    }
    
    document.getElementById('analisis-resultado').classList.remove('d-none');
}

document.addEventListener('DOMContentLoaded', () => {
    cargarSectores();
    cargarGalpones();
});