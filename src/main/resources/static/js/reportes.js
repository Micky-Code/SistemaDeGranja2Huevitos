const feedback = document.querySelector('#feedback');
const logoutButton = document.querySelector('#logout-button');
const generarButton = document.querySelector('#generar-button');
const generarSpinner = document.querySelector('#generar-spinner');
const reportesLista = document.querySelector('#reportes-lista');

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

function formatearFechaHora(fechaIso) {
    if (!fechaIso) return '';
    return fechaIso.replace('T', ' ').substring(0, 16);
}

function renderReporte(reporte) {
    const filas = reporte.detalles.map(d => `
        <tr><td>${d.descripcion}</td><td>${d.valor}</td></tr>
    `).join('');

    return `
        <article class="border rounded-3 p-3 mb-3">
            <div class="d-flex justify-content-between flex-wrap gap-2 mb-2">
                <div>
                    <span class="badge text-bg-light border">${reporte.tipo}</span>
                    <strong class="ms-2">${formatearFechaHora(reporte.fechaGeneracion)}</strong>
                </div>
                <span class="text-secondary small">Generado por: ${reporte.generadoPor}</span>
            </div>
            <table class="table table-sm mb-0">
                <tbody>${filas}</tbody>
            </table>
        </article>
    `;
}

async function cargarReportes() {
    const response = await fetch('/api/reportes', { credentials: 'same-origin' });
    if (!response.ok) {
        reportesLista.innerHTML = '<p class="text-danger">No se pudieron cargar los reportes.</p>';
        return;
    }
    const reportes = await response.json();
    if (reportes.length === 0) {
        reportesLista.innerHTML = '<p class="text-secondary">Aún no se ha generado ningún reporte.</p>';
        return;
    }
    reportesLista.innerHTML = reportes.map(renderReporte).join('');
}

generarButton.addEventListener('click', async () => {
    hideFeedback();
    generarButton.disabled = true;
    generarSpinner.classList.remove('d-none');
    try {
        const csrf = await getCsrf();
        const response = await fetch('/api/reportes/almacen-mermas', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { [csrf.headerName]: csrf.token }
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            showFeedback(error.message || 'No se pudo generar el reporte.', 'danger');
            return;
        }
        showFeedback('Reporte generado correctamente.', 'success');
        await cargarReportes();
    } catch (error) {
        showFeedback(error.message || 'No se pudo conectar con el servidor.', 'danger');
    } finally {
        generarButton.disabled = false;
        generarSpinner.classList.add('d-none');
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

cargarReportes();
