(() => {
    let csrfCache = null;

    async function getCsrfHeaders() {
        let token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        let header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        if (!token || !header || token.startsWith('${')) {
            if (!csrfCache) {
                try {
                    const res = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
                    if (res.ok) {
                        csrfCache = await res.json();
                    }
                } catch (e) {
                    console.error('Error fetching csrf:', e);
                }
            }
            if (csrfCache) {
                header = csrfCache.headerName;
                token = csrfCache.token;
            }
        }
        const headers = { 'Content-Type': 'application/json' };
        if (token && header) headers[header] = token;
        return headers;
    }

    let cacheSectores = [];
    let cacheGalpones = [];
    let cacheLotes = [];
    let cacheAsignaciones = [];

    function mostrarNotificacion(mensaje, esError) {
        if (esError === undefined) esError = false;
        const alertBox = document.querySelector('#infra-alert');
        if (!alertBox) return;
        alertBox.textContent = mensaje;
        alertBox.className = 'alert ' + (esError ? 'alert-danger' : 'alert-success') + ' shadow-sm rounded-3 mb-4';
        alertBox.classList.remove('d-none');
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
        setTimeout(function() { alertBox.classList.add('d-none'); }, 5000);
    }

    async function cargarSectores() {
        try {
            const res = await fetch('/api/infraestructura/sectores', { credentials: 'same-origin' });
            if (!res.ok) return;
            cacheSectores = await res.json();
            
            const selectGalponSec = document.querySelector('#gal-sector');
            const selectFiltroSec = document.querySelector('#lg-sector-filtro');
            const tbodySectores = document.querySelector('#tb-sectores');
            
            if (selectGalponSec) selectGalponSec.innerHTML = '<option value="">Seleccione Sector</option>';
            if (selectFiltroSec) selectFiltroSec.innerHTML = '<option value="">Seleccione Sector</option>';
            if (tbodySectores) tbodySectores.innerHTML = '';

            let opcionesHTML = "";
            let filasSectoresHTML = "";

            cacheSectores.forEach(function(s) {
                opcionesHTML += '<option value="' + s.idSector + '">' + s.nombre + '</option>';
                filasSectoresHTML += '<tr>' +
                    '<td>' + s.idSector + '</td>' +
                    '<td><strong>' + s.nombre + '</strong></td>' +
                    '<td>' + (s.descripcion || 'Sin descripción') + '</td>' +
                    '</tr>';
            });

            if (selectGalponSec) selectGalponSec.innerHTML += opcionesHTML; 
            if (selectFiltroSec) selectFiltroSec.innerHTML += opcionesHTML;
            if (tbodySectores) tbodySectores.innerHTML = filasSectoresHTML;
            
        } catch (err) {
            console.error('Error al cargar sectores:', err);
        }
    }

    async function cargarGalpones() {
        try {
            const res = await fetch('/api/infraestructura/galpones', { credentials: 'same-origin' });
            if (!res.ok) return;
            cacheGalpones = await res.json();
            
            const tbody = document.querySelector('#tb-galpones');
            if (tbody) tbody.innerHTML = '';

            cacheGalpones.forEach(function(g) {
                let sectorEncontrado = cacheSectores.find(function(s) { return s.idSector === g.sectorId; });
                const nombreSector = g.nombreSector || (g.sector ? g.sector.nombre : (sectorEncontrado ? sectorEncontrado.nombre : 'Sin Sector'));
                
                if (tbody) {
                    tbody.innerHTML += '<tr>' +
                        '<td>' + g.idGalpon + '</td>' +
                        '<td><strong>' + g.nombre + '</strong></td>' +
                        '<td><span class="badge bg-success">' + (g.estado || 'Activo') + '</span></td>' +
                        '<td>' + nombreSector + '</td>' +
                        '</tr>';
                }
            });
        } catch (err) {
            console.error('Error al cargar galpones:', err);
        }
    }

    async function cargarLotes() {
        try {
            const res = await fetch('/api/infraestructura/lotes', { credentials: 'same-origin' });
            if (!res.ok) return;
            cacheLotes = await res.json();
            
            const tbody = document.querySelector('#tb-lotes');
            const selectLoteLG = document.querySelector('#lg-lote');
            if (tbody) tbody.innerHTML = '';
            if (selectLoteLG) selectLoteLG.innerHTML = '<option value="">Seleccione Lote</option>';

            cacheLotes.forEach(function(l) {
                if (tbody) {
                    tbody.innerHTML += '<tr>' +
                        '<td>' + l.idLote + '</td>' +
                        '<td><strong>' + l.nombre + '</strong></td>' +
                        '<td>' + l.cantidadInicial + '</td>' +
                        '<td>' + l.cantidadActual + '</td>' +
                        '<td>' + (l.fechaIngreso || '') + '</td>' +
                        '</tr>';
                }
                if (selectLoteLG) {
                    selectLoteLG.innerHTML += '<option value="' + l.idLote + '" data-cantidad="' + l.cantidadActual + '">' + l.nombre + ' (Disponibles: ' + l.cantidadActual + ')</option>';
                }
            });
        } catch (err) {
            console.error('Error al cargar lotes:', err);
        }
    }

    async function cargarAsignaciones() {
        try {
            const res = await fetch('/api/infraestructura/lotes-galpon', { credentials: 'same-origin' });
            if (!res.ok) return;
            cacheAsignaciones = await res.json();

            const tbody = document.querySelector('#tb-lote-galpon');
            if (!tbody) return;
            tbody.innerHTML = '';

            if (!cacheAsignaciones.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">No hay asignaciones registradas.</td></tr>';
                return;
            }

            cacheAsignaciones.forEach(function(a) {
                const nombreLote = a.lote ? a.lote.nombre : ('Lote ID: ' + (a.loteId || '--'));
                const nombreGalpon = a.galpon ? a.galpon.nombre : ('Galpón ID: ' + (a.galponId || '--'));
                const fecha = a.fechaIngreso ? a.fechaIngreso.split('T')[0] : '';

                tbody.innerHTML += '<tr>' +
                    '<td>' + (a.idLoteGalpon || '—') + '</td>' +
                    '<td><strong>' + nombreLote + '</strong></td>' +
                    '<td>' + nombreGalpon + '</td>' +
                    '<td><span class="badge bg-primary fs-6">' + a.cantidadAves + ' aves</span></td>' +
                    '<td>' + fecha + '</td>' +
                    '</tr>';
            });
        } catch (err) {
            console.error('Error al cargar asignaciones:', err);
        }
    }

    async function cargarVistaInteractivaGalpones() {
        try {
            const [resSectores, resGalpones] = await Promise.all([
                fetch('/api/infraestructura/sectores', { credentials: 'same-origin' }),
                fetch('/api/infraestructura/galpones', { credentials: 'same-origin' })
            ]);

            if (!resSectores.ok || !resGalpones.ok) return;
            const sectores = await resSectores.json();
            const galpones = await resGalpones.json();

            const contenedor = document.querySelector('#contenedor-sectores-galpones');
            if (!contenedor) return;
            contenedor.innerHTML = '';

            sectores.forEach(function(sector) {
                const galponesDelSector = galpones.filter(function(g) {
                    return g.sectorId === sector.idSector || (g.sector && g.sector.idSector === sector.idSector);
                });

                let galponesCardsHTML = '';
                if (galponesDelSector.length === 0) {
                    galponesCardsHTML = '<p class="text-muted small fst-italic mb-0">No hay galpones registrados en este sector.</p>';
                } else {
                    galponesCardsHTML = '<div class="d-flex flex-wrap gap-3">';
                    galponesDelSector.forEach(function(g) {
                        galponesCardsHTML += '<div class="galpon-card-item p-3 border rounded-3 shadow-sm bg-light text-center" ' +
                             'style="width: 170px; cursor: pointer; transition: transform 0.2s ease;" ' +
                             'onclick="seleccionarGalpon(' + g.idGalpon + ')">' +
                            '<div class="fs-1 mb-1">🏠</div>' +
                            '<h6 class="fw-bold text-dark mb-1">' + g.nombre + '</h6>' +
                            '<small class="text-secondary d-block">Cap: ' + g.capacidad + '</small>' +
                            '<span class="badge bg-success mt-2">' + (g.estado || 'Activo') + '</span>' +
                        '</div>';
                    });
                    galponesCardsHTML += '</div>';
                }

                contenedor.innerHTML += '<div class="border-bottom pb-3 mb-3">' +
                    '<h6 class="fw-bold text-success fs-5 mb-2">📍 Sector: ' + sector.nombre + '</h6>' +
                    '<p class="text-muted small mb-3">' + (sector.descripcion || 'Sin descripción') + '</p>' +
                    galponesCardsHTML +
                    '</div>';
            });
        } catch (err) {
            console.error('Error en vista interactiva:', err);
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        const selectLoteElem = document.querySelector('#lg-lote');
        if (selectLoteElem) {
            selectLoteElem.addEventListener('change', function(e) {
                const selectedOption = e.target.options[e.target.selectedIndex];
                const cantidad = selectedOption.getAttribute('data-cantidad') || '';
                const cantidadInput = document.querySelector('#lg-cantidad');
                if (cantidadInput) cantidadInput.value = cantidad;
            });
        }

        const selectSectorFiltro = document.querySelector('#lg-sector-filtro');
        if (selectSectorFiltro) {
            selectSectorFiltro.addEventListener('change', function(e) {
                const sectorId = parseInt(e.target.value);
                const selectGalponDestino = document.querySelector('#lg-galpon');
                if (!selectGalponDestino) return;
                
                selectGalponDestino.innerHTML = '<option value="">Seleccione Galpón</option>';
                if (!sectorId) return;

                const galponesFiltrados = cacheGalpones.filter(function(g) {
                    return g.sectorId === sectorId || (g.sector && g.sector.idSector === sectorId);
                });
                
                if (galponesFiltrados.length === 0) {
                    selectGalponDestino.innerHTML = '<option value="">No hay galpones en este sector</option>';
                    return;
                }

                galponesFiltrados.forEach(function(g) {
                    selectGalponDestino.innerHTML += '<option value="' + g.idGalpon + '" data-capacidad="' + g.capacidad + '">' + g.nombre + ' (Capacidad: ' + g.capacidad + ')</option>';
                });
            });
        }

        const formSector = document.querySelector('#form-sector');
        if (formSector) {
            formSector.addEventListener('submit', async function(e) {
                e.preventDefault();
                const payload = {
                    nombre: document.querySelector('#sec-nombre').value.trim(),
                    descripcion: document.querySelector('#sec-descripcion').value.trim()
                };

                try {
                    const headers = await getCsrfHeaders();
                    const res = await fetch('/api/infraestructura/sectores', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: headers,
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        e.target.reset();
                        mostrarNotificacion('Sector guardado exitosamente.');
                        await cargarSectores();
                        await cargarVistaInteractivaGalpones();
                    } else {
                        mostrarNotificacion('Error al guardar el sector.', true);
                    }
                } catch (err) {
                    mostrarNotificacion('Error de conexión al servidor.', true);
                }
            });
        }

        const formGalpon = document.querySelector('#form-galpon');
        if (formGalpon) {
            formGalpon.addEventListener('submit', async function(e) {
                e.preventDefault();
                const payload = {
                    nombre: document.querySelector('#gal-nombre').value,
                    capacidad: parseInt(document.querySelector('#gal-capacidad').value),
                    sectorId: parseInt(document.querySelector('#gal-sector').value),
                    estado: 'Activo'
                };

                try {
                    const headers = await getCsrfHeaders();
                    const res = await fetch('/api/infraestructura/galpones', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: headers,
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        e.target.reset(); 
                        mostrarNotificacion('Galpón guardado exitosamente.');
                        await cargarGalpones();
                        await cargarVistaInteractivaGalpones();
                    } else {
                        mostrarNotificacion('Error al guardar el galpón.', true);
                    }
                } catch (err) {
                    mostrarNotificacion('Error de conexión.', true);
                }
            });
        }

        const formLote = document.querySelector('#form-lote');
        if (formLote) {
            formLote.addEventListener('submit', async function(e) {
                e.preventDefault();
                const cant = parseInt(document.querySelector('#lote-cant-inicial').value);
                const payload = {
                    nombre: document.querySelector('#lote-nombre').value.trim(),
                    cantidadInicial: cant,
                    cantidadActual: cant,
                    fechaIngreso: document.querySelector('#lote-fecha-ingreso').value
                };

                try {
                    const headers = await getCsrfHeaders();
                    const res = await fetch('/api/infraestructura/lotes', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: headers,
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        e.target.reset();
                        mostrarNotificacion('Lote registrado correctamente.');
                        await cargarLotes();
                    } else {
                        mostrarNotificacion('Error al guardar el lote.', true);
                    }
                } catch (err) {
                    mostrarNotificacion('Error de red al guardar lote.', true);
                }
            });
        }

        const formLoteGalpon = document.querySelector('#form-lote-galpon');
        if (formLoteGalpon) {
            formLoteGalpon.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const galponSelect = document.querySelector('#lg-galpon');
                const galponOption = galponSelect.options[galponSelect.selectedIndex];
                const capacidadMax = parseInt(galponOption.getAttribute('data-capacidad') || '999999');
                const cantidadAves = parseInt(document.querySelector('#lg-cantidad').value);

                if (cantidadAves > capacidadMax) {
                    mostrarNotificacion('La cantidad de aves (' + cantidadAves + ') excede la capacidad máxima del galpón (' + capacidadMax + ').', true);
                    return;
                }

                const payload = {
                    loteId: parseInt(document.querySelector('#lg-lote').value),
                    galponId: parseInt(document.querySelector('#lg-galpon').value),
                    cantidadAves: cantidadAves,
                    fechaIngreso: document.querySelector('#lg-fecha').value
                };

                try {
                    const headers = await getCsrfHeaders();
                    const res = await fetch('/api/infraestructura/lotes-galpon', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: headers,
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        formLoteGalpon.reset();
                        mostrarNotificacion('¡Aves asignadas al galpón exitosamente!');
                        await cargarLotes();
                        await cargarGalpones();
                        await cargarAsignaciones();
                        await cargarVistaInteractivaGalpones();
                    } else {
                        const err = await res.json().catch(function() { return {}; });
                        mostrarNotificacion(err.message || 'Error al realizar la asignación.', true);
                    }
                } catch (err) {
                    mostrarNotificacion('Error al conectar con el servidor.', true);
                }
            });
        }

        cargarSectores();
        cargarGalpones();
        cargarLotes();
        cargarAsignaciones();
        cargarVistaInteractivaGalpones();
    });

    window.seleccionarGalpon = function seleccionarGalpon(idGalpon) {
        const galpon = cacheGalpones.find(function(g) { return g.idGalpon === idGalpon; });
        if (!galpon) return;

        let sectorEncontrado = cacheSectores.find(function(s) { return s.idSector === galpon.sectorId; });
        const sectorNombre = galpon.nombreSector || (galpon.sector ? galpon.sector.nombre : (sectorEncontrado ? sectorEncontrado.nombre : 'Sin sector'));
        
        const asignacionesGalpon = cacheAsignaciones.filter(function(a) {
            return (a.galponId === idGalpon) || (a.galpon && a.galpon.idGalpon === idGalpon);
        });
        const totalAves = asignacionesGalpon.reduce(function(sum, a) { return sum + (a.cantidadAves || 0); }, 0);

        document.querySelector('#modalGalponId').textContent = galpon.idGalpon;
        document.querySelector('#modalGalponNombre').textContent = galpon.nombre;
        document.querySelector('#modalGalponEstado').textContent = galpon.estado || 'Activo';
        document.querySelector('#modalGalponSector').textContent = sectorNombre;
        document.querySelector('#modalGalponCapacidad').textContent = galpon.capacidad;
        document.querySelector('#modalGalponAves').textContent = totalAves > 0 ? (totalAves + ' aves') : 'Sin asignación activa';

        const modalElem = document.querySelector('#modalDetalleGalpon');
        if (modalElem && typeof bootstrap !== 'undefined') {
            const modal = new bootstrap.Modal(modalElem);
            modal.show();
        }
    };
})();