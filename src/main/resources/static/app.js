const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    // 1. Navegación activa
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    const navLinks = document.querySelectorAll('nav a');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // 2. Módulo de Producción en Galpón
    const formProduccion = document.getElementById('form-produccion');
    if (formProduccion) {
        const inputsHuevos = document.querySelectorAll('.cant-huevo');
        const spanTotal = document.getElementById('total-calculado');

        inputsHuevos.forEach(input => {
            input.addEventListener('input', () => {
                let total = 0;
                inputsHuevos.forEach(i => { total += parseInt(i.value) || 0; });
                spanTotal.textContent = total.toLocaleString();
            });
        });

        formProduccion.addEventListener('submit', async (e) => {
            e.preventDefault();

            const payloadProduccion = {
                idGrupo: parseInt(document.getElementById('select-grupo').value) || 1,
                fechaRegistro: document.getElementById('input-fecha').value,
                totalRecolectado: parseInt(spanTotal.textContent.replace(/,/g, '')) || 0,
                idEmpleado: 1,
                detalles: [
                    { idClasificacion: 1, cantidad: parseInt(document.getElementById('cant-rojo').value) || 0 },
                    { idClasificacion: 2, cantidad: parseInt(document.getElementById('cant-pardo').value) || 0 },
                    { idClasificacion: 3, cantidad: parseInt(document.getElementById('cant-jumbo').value) || 0 },
                    { idClasificacion: 4, cantidad: parseInt(document.getElementById('cant-dobleyema').value) || 0 },
                    { idClasificacion: 5, cantidad: parseInt(document.getElementById('cant-poroso').value) || 0 },
                    { idClasificacion: 6, cantidad: parseInt(document.getElementById('cant-roto').value) || 0 }
                ]
            };

            try {
                const response = await fetch(API_BASE_URL + '/produccion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payloadProduccion)
                });

                if (response.ok) {
                    alert('¡Producción registrada con éxito en la Base de Datos!');
                    formProduccion.reset();
                    spanTotal.textContent = '0';
                } else {
                    alert('Error al guardar en el servidor. Revisa el backend.');
                }
            } catch (error) {
                console.warn('Backend desconectado. Datos capturados en consola:', payloadProduccion);
                alert('Modo offline: Datos capturados en consola.');
            }
        });
    }

    // 3. Módulo de Recepción en Almacén
    const formAlmacen = document.getElementById('form-almacen');
    if (formAlmacen) {
        formAlmacen.addEventListener('submit', async (e) => {
            e.preventDefault();

            const payloadAlmacen = {
                idProduccionGalpon: parseInt(document.getElementById('select-produccion').value) || 1,
                fechaRecepcion: document.getElementById('input-fecha-recepcion').value,
                cantidadTotalRecibida: parseInt(document.getElementById('cant-recibida').value) || 0,
                cantidadRotaTraslado: parseInt(document.getElementById('cant-rota-traslado').value) || 0,
                observaciones: document.getElementById('input-observaciones').value,
                idEmpleadoAlmacen: 2
            };

            try {
                const response = await fetch(API_BASE_URL + '/almacen/recepcion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payloadAlmacen)
                });

                if (response.ok) {
                    alert('¡Recepción en almacén registrada!');
                    formAlmacen.reset();
                } else {
                    alert('Error en la recepción de almacén.');
                }
            } catch (error) {
                console.warn('Backend desconectado. Datos capturados:', payloadAlmacen);
                alert('Modo offline: Datos capturados en consola.');
            }
        });
    }

    // 4. Módulo Sanitario (Mortalidad y Vacunación)
    const formMortalidad = document.getElementById('form-mortalidad');
    if (formMortalidad) {
        formMortalidad.addEventListener('submit', async (e) => {
            e.preventDefault();

            const payloadMortalidad = {
                idGrupo: parseInt(document.getElementById('select-grupo-mortalidad').value) || 1,
                fechaRegistro: document.getElementById('input-fecha-mortalidad').value,
                cantidad: parseInt(document.getElementById('cant-mortalidad').value) || 0,
                causa: document.getElementById('input-causa-mortalidad').value,
                idEmpleado: 1
            };

            try {
                await fetch(API_BASE_URL + '/sanidad/mortalidad', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payloadMortalidad)
                });
                alert('Mortalidad registrada con éxito.');
                formMortalidad.reset();
            } catch (err) {
                console.warn('Modo offline:', payloadMortalidad);
                alert('Modo offline: Datos capturados en consola.');
            }
        });
    }

    const formVacunacion = document.getElementById('form-vacunacion');
    if (formVacunacion) {
        formVacunacion.addEventListener('submit', async (e) => {
            e.preventDefault();

            const payloadVacunacion = {
                idGrupo: parseInt(document.getElementById('select-grupo-sanidad').value) || 1,
                producto: document.getElementById('input-producto-sanitario').value,
                fechaAplicacion: document.getElementById('input-fecha-aplicacion').value,
                idEmpleado: 1
            };

            try {
                await fetch(API_BASE_URL + '/sanidad/vacunacion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payloadVacunacion)
                });
                alert('Plan sanitario registrado con éxito.');
                formVacunacion.reset();
            } catch (err) {
                console.warn('Modo offline:', payloadVacunacion);
                alert('Modo offline: Datos capturados en consola.');
            }
        });
    }
});