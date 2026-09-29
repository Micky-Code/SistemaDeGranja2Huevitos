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
    let cacheProduccion = [];

    function mostrarNotificacion(mensaje, esError) {
        if (esError === undefined) esError = false;
        const alertBox = document.querySelector('#prod-alert');
        if (!alertBox) return;
        alertBox.textContent = mensaje;
        alertBox.className = 'alert ' + (esError ? 'alert-danger' : 'alert-success') + ' shadow-sm rounded-3 mb-4';
        alertBox.classList.remove('d-none');
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
        setTimeout(function() { alertBox.classList.add('d-none'); }, 5000);
    }

    async function cargarSectoresYGalpones() {
        try {
            const [resSectores, resGalpones] = await Promise.all([
                fetch('/api/infraestructura/sectores', { credentials: 'same-origin' }),
                fetch('/api/infraestructura/galpones', { credentials: 'same-origin' })
            ]);

            if (resSectores.ok) cacheSectores = await resSectores.json();
            if (resGalpones.ok) cacheGalpones = await resGalpones.json();
            
            const selectSector = document.querySelector('#select-sector-prod');
            if (selectSector) {
                selectSector.innerHTML = '<option value="">-- Seleccione un Sector --</option>';
                cacheSectores.forEach(function(s) {
                    selectSector.innerHTML += '<option value="' + s.idSector + '">' + s.nombre + '</option>';
                });

                if (cacheSectores.length > 0) {
                    selectSector.value = cacheSectores[0].idSector;
                    mostrarGalponesDelSector(cacheSectores[0].idSector);
                }
            }
        } catch (err) {
            console.error('Error al cargar sectores/galpones:', err);
        }
    }

    function mostrarGalponesDelSector(sectorId) {
        const contenedor = document.querySelector('#contenedor-galpones');
        if (!contenedor) return;

        if (!sectorId) {
            contenedor.innerHTML = '<div class="col-12 text-center text-muted py-4 border rounded-3 bg-white">' +
                'Seleccione un sector arriba para visualizar sus galpones.</div>';
            return;
        }

        const idStr = String(sectorId).trim();

        const selectSector = document.querySelector('#select-sector-prod');
        const nombreSectorSeleccionado = selectSector && selectSector.options[selectSector.selectedIndex] ? selectSector.options[selectSector.selectedIndex].text.toLowerCase() : '';

        const galponesFiltrados = cacheGalpones.filter(function(g) {
            const gSectorId = g.sectorId !== undefined ? String(g.sectorId).trim() : null;
            const gNestedId = (g.sector && g.sector.idSector !== undefined) ? String(g.sector.idSector).trim() : null;
            const gSectorNombre = (g.sectorNombre || (g.sector && g.sector.nombre) || '').toLowerCase();

            return gSectorId === idStr || gNestedId === idStr || 
                   (gSectorNombre && gSectorNombre.includes(nombreSectorSeleccionado));
        });

        const galponesAMostrar = galponesFiltrados;

        if (!galponesAMostrar.length) {
            contenedor.innerHTML = '<div class="col-12 text-center text-muted py-4 border rounded-3 bg-white">' +
                'No hay galpones registrados en este sector.</div>';
            return;
        }

        let htmlCards = '';
        galponesAMostrar.forEach(function(g) {
            let estadoStr = g.estado || 'Activo';
            let badgeClass = 'bg-success';
            let iconClass = 'fa-circle-check text-success';

            if (estadoStr === 'Inactivo') {
                badgeClass = 'bg-danger';
                iconClass = 'fa-circle-xmark text-danger';
            } else if (estadoStr === 'Mantenimiento') {
                badgeClass = 'bg-warning text-dark';
                iconClass = 'fa-circle-pause text-warning';
            }

            htmlCards += '<div class="col-md-4 col-lg-3">' +
                '<div class="card p-3 shadow-sm border-0 rounded-4 h-100 bg-white">' +
                    '<div class="d-flex justify-content-between align-items-center mb-2">' +
                        '<h5 class="fw-bold mb-0 text-dark">' + g.nombre + '</h5>' +
                        '<i class="fa-solid ' + iconClass + ' fs-5"></i>' +
                    '</div>' +
                    '<p class="small text-muted mb-1">Capacidad: <strong>' + (g.capacidad || '--') + ' aves</strong></p>' +
                    '<p class="mb-3"><span class="badge ' + badgeClass + '">' + estadoStr + '</span></p>' +
                    '<button class="btn btn-outline-success btn-sm w-100 fw-bold mt-auto" ' +
                            'onclick="abrirModalRegistro(' + g.idGalpon + ', \'' + g.nombre + '\')">' +
                        '<i class="fa-solid fa-plus me-1"></i>Ingresar Producción' +
                    '</button>' +
                '</div>' +
            '</div>';
        });

        contenedor.innerHTML = htmlCards;
    }

    async function cargarHistorialProduccion() {
        try {
            const res = await fetch('/api/produccion', { credentials: 'same-origin' });
            if (!res.ok) return;
            cacheProduccion = await res.json();

            const tbody = document.querySelector('#tb-produccion-historial');
            if (!tbody) return;
            tbody.innerHTML = '';

            if (!cacheProduccion.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">No hay registros de producción.</td></tr>';
                return;
            }

            cacheProduccion.forEach(function(p) {
                let galponNombre = 'Galpón ID: ' + (p.loteGalponId || '--');
                let fechaStr = p.fecha ? p.fecha.split('T')[0] : '';
    
                let buenos = p.cantidadHuevosBuenos || 0;
                let rotos = p.cantidadHuevosRotos || 0;
                let sucios = p.cantidadHuevosSucios || 0;
                let totalHuevos = buenos + rotos + sucios;

                tbody.innerHTML += '<tr>' +
                    '<td>' + fechaStr + '</td>' +
                    '<td><strong>' + galponNombre + '</strong></td>' +
                    '<td><span class="badge bg-secondary">Registrado</span></td>' +
                    '<td><span class="badge bg-success fs-6">' + totalHuevos + ' huevos</span></td>' +
                    '<td><small class="text-muted">Buenos: ' + buenos + ' | Rotos: ' + rotos + ' | Sucios: ' + sucios + '</small></td>' +
                    '</tr>';
            });
        } catch (err) {
            console.error('Error al cargar historial de producción:', err);
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        const selectSector = document.querySelector('#select-sector-prod');
        if (selectSector) {
            selectSector.addEventListener('change', function(e) {
                mostrarGalponesDelSector(e.target.value);
            });
        }
        
        const formProd = document.querySelector('#form-produccion');
        if (formProd) {
            formProd.addEventListener('submit', async function(e) {
                e.preventDefault();

                let fechaVal = document.querySelector('#prod-fecha').value;
                if (fechaVal && fechaVal.includes('/')) {
                    const partes = fechaVal.split('/');
                    if (partes.length === 3) {
                        fechaVal = partes[2] + '-' + partes[1] + '-' + partes[0];
                    }
                }

                const payload = {
                    fecha: document.querySelector('#prod-fecha').value,
                    cantidadHuevosBuenos: parseInt(document.querySelector('#prod-pardo').value || 0) + parseInt(document.querySelector('#prod-rojo').value || 0),
                    cantidadHuevosRotos: parseInt(document.querySelector('#prod-roto').value || 0),
                    cantidadHuevosSucios: parseInt(document.querySelector('#prod-fisurado').value || 0),
                    observaciones: "Mortalidad registrada: " + document.querySelector('#prod-mortalidad').value,
                    idGalpon: parseInt(document.querySelector('#prod-galpon-id').value)
                };

                try {
                    const headers = await getCsrfHeaders();
                    const res = await fetch('/api/produccion', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: headers,
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        const modalElem = document.querySelector('#modalRegistroProduccion');
                        if (modalElem && typeof bootstrap !== 'undefined') {
                            const modal = bootstrap.Modal.getInstance(modalElem);
                            if (modal) modal.hide();
                        }

                        mostrarNotificacion('¡Registro de producción guardado exitosamente!');
                        await cargarHistorialProduccion();
                    } else {
                        const errData = await res.json().catch(function() { return {}; });
                        mostrarNotificacion(errData.message || 'Error al guardar la producción.', true);
                    }
                } catch (err) {
                    mostrarNotificacion('Error de conexión al guardar.', true);
                }
            });
        }

        cargarSectoresYGalpones();
        cargarHistorialProduccion();
    });

    window.abrirModalRegistro = function(galponId, galponNombre) {
        document.querySelector('#prod-galpon-id').value = galponId;
        document.querySelector('#modalProdGalponNombre').textContent = 'Registrar Producción - ' + galponNombre;
        document.querySelector('#prod-fecha').value = new Date().toISOString().split('T')[0];
        document.querySelector('#prod-mortalidad').value = '0';
        document.querySelector('#prod-pardo').value = '0';
        document.querySelector('#prod-rojo').value = '0';
        document.querySelector('#prod-fisurado').value = '0';
        document.querySelector('#prod-roto').value = '0';

        const modalElem = document.querySelector('#modalRegistroProduccion');
        if (modalElem && typeof bootstrap !== 'undefined') {
            const modal = new bootstrap.Modal(modalElem);
            modal.show();
        }
    };

    window.abrirModalAnalisis = function() {
        const modalElem = document.querySelector('#modalAnalisisProduccion');
        if (modalElem && typeof bootstrap !== 'undefined') {
            const modal = new bootstrap.Modal(modalElem);
            modal.show();
            const primerBoton = document.querySelector('.btn-filtro-tiempo[data-periodo="dia"]');
            window.filtrarAnalisis('dia', primerBoton);
        }
    };

    window.filtrarAnalisis = function(periodo, btnElement) {
        if (btnElement) {
            document.querySelectorAll('.btn-filtro-tiempo').forEach(function(b) {
                b.classList.remove('active');
            });
            btnElement.classList.add('active');
        }

        const ahora = new Date();
        let fechaLimite = new Date();

        if (periodo === 'semana') {
            fechaLimite.setDate(ahora.getDate() - 7);
        } else if (periodo === 'mes') {
            fechaLimite.setDate(ahora.getDate() - 30);
        } else {
            fechaLimite.setHours(0, 0, 0, 0);
        }

        const filtrados = cacheProduccion.filter(function(p) {
            if (!p.fecha) return false;
            const f = new Date(p.fecha);
            return f >= fechaLimite;
        });

        let totalPardo = 0, totalRojo = 0, totalFisurado = 0, totalRoto = 0, totalMortalidad = 0;

        filtrados.forEach(function(p) {
            totalPardo += (p.cantidadHuevosBuenos || 0);
            totalRoto += (p.cantidadHuevosRotos || 0);
            totalFisurado += (p.cantidadHuevosSucios || 0);
            totalMortalidad += 0;
        });

        const totalHuevos = totalPardo + totalRoto + totalFisurado;
        const totalMerma = totalFisurado + totalRoto;
        const porcentajeMerma = totalHuevos > 0 ? ((totalMerma / totalHuevos) * 100).toFixed(1) : '0';

        document.querySelector('#an-total-huevos').textContent = totalHuevos;
        document.querySelector('#an-total-mortalidad').textContent = totalMortalidad;
        document.querySelector('#an-porcentaje-merma').textContent = porcentajeMerma + '%';

        document.querySelector('#an-pardo').textContent = totalPardo;
        document.querySelector('#an-rojo').textContent = 0;
        document.querySelector('#an-fisurado').textContent = totalFisurado;
        document.querySelector('#an-roto').textContent = totalRoto;
    };
})();