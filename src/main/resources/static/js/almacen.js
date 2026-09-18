const feedback = document.querySelector('#feedback');
const logoutButton = document.querySelector('#logout-button');
const clasificacionesBody = document.querySelector('#clasificaciones-body');
const recepcionesBody = document.querySelector('#recepciones-body');
const form = document.querySelector('#recepcion-form');
const submitButton = document.querySelector('#recepcion-button');
const submitSpinner = document.querySelector('#recepcion-spinner');

function showFeedback(message, tipo) {
    feedback.textContent = message;
    feedback.className = 'alert alert-' + (tipo || 'danger');
}

function hideFeedback() {
    feedback.classList.add('d-none');
}

async function getCsrf() {
    const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    if (!response.ok) throw new Error('No se pudo preparar la solicitud.');
    return response.json();
}

async function cargarClasificaciones() {
    const response = await fetch('/api/almacen/clasificaciones', { credentials: 'same-origin' });
    if (!response.ok) {
        clasificacionesBody.innerHTML = '<tr><td colspan="3" class="text-danger">No se pudieron cargar las clasificaciones.</td></tr>';
        return;
    }
    const clasificaciones = await response.json();
    if (clasificaciones.length === 0) {
        clasificacionesBody.innerHTML = '<tr><td colspan="3" class="text-secondary">Aún no hay clasificaciones registradas.</td></tr>';
        return;
    }
    clasificacionesBody.innerHTML = clasificaciones.map(c => `
        <tr data-id-clasificacion="${c.idClasificacion}">
            <td>${c.nombre}</td>
            <td><input type="number" min="0" class="form-control form-control-sm cantidad-input" value="0"></td>
            <td><input type="number" min="0" class="form-control form-control-sm cantidad-rota-input" value="0"></td>
        </tr>
    `).join('');
}

function formatearFecha(fechaIso) {
    if (!fechaIso) return '';
    return fechaIso.split('T')[0];
}

async function cargarRecepciones() {
    const response = await fetch('/api/almacen/recepciones', { credentials: 'same-origin' });
    if (!response.ok) {
        recepcionesBody.innerHTML = '<tr><td colspan="5" class="text-danger">No se pudieron cargar las recepciones.</td></tr>';
        return;
    }
    const recepciones = await response.json();
    if (recepciones.length === 0) {
        recepcionesBody.innerHTML = '<tr><td colspan="5" class="text-secondary">Aún no hay recepciones registradas.</td></tr>';
        return;
    }
    recepcionesBody.innerHTML = recepciones.map(r => `
        <tr>
            <td>${formatearFecha(r.fecha)}</td>
            <td>${r.idProduccion}</td>
            <td>${r.totalRecibido}</td>
            <td>${r.mermaTraslado}</td>
            <td>${r.porcentajeMerma.toFixed(2)}%</td>
        </tr>
    `).join('');
}

function leerDetalles() {
    const filas = clasificacionesBody.querySelectorAll('tr[data-id-clasificacion]');
    const detalles = [];
    filas.forEach(fila => {
        const cantidad = Number(fila.querySelector('.cantidad-input').value || 0);
        const cantidadRota = Number(fila.querySelector('.cantidad-rota-input').value || 0);
        if (cantidad > 0 || cantidadRota > 0) {
            detalles.push({
                idClasificacion: Number(fila.dataset.idClasificacion),
                cantidad,
                cantidadRota
            });
        }
    });
    return detalles;
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    hideFeedback();

    const detalles = leerDetalles();
    if (detalles.length === 0) {
        showFeedback('Ingresa al menos una cantidad recibida mayor a 0.', 'danger');
        return;
    }

    const payload = {
        idProduccion: Number(form.elements.namedItem('idProduccion').value),
        fecha: form.elements.namedItem('fecha').value,
        idEmpleado: Number(form.elements.namedItem('idEmpleado').value),
        observacion: form.elements.namedItem('observacion').value,
        detalles
    };

    submitButton.disabled = true;
    submitSpinner.classList.remove('d-none');
    try {
        const csrf = await getCsrf();
        const response = await fetch('/api/almacen/recepciones', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            showFeedback(error.message || 'No se pudo registrar la recepción.', 'danger');
            return;
        }
        showFeedback('Recepción registrada correctamente.', 'success');
        form.reset();
        await Promise.all([cargarClasificaciones(), cargarRecepciones()]);
    } catch (error) {
        showFeedback(error.message || 'No se pudo conectar con el servidor.', 'danger');
    } finally {
        submitButton.disabled = false;
        submitSpinner.classList.add('d-none');
    }
});

logoutButton.addEventListener('click', async () => {
    try {
        const csrf = await getCsrf();
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { [csrf.headerName]: csrf.token }
        });
    } finally {
        window.location.replace('/login');
    }
});

cargarClasificaciones();
cargarRecepciones();
