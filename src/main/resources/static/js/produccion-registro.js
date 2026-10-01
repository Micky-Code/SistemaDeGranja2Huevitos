(() => {
    const api = '/api/produccion/registro';
    const $ = id => document.getElementById(id);
    let tipos = [], galpones = [];

    function mensaje(texto, error = false) {
        $('produccion-mensaje').textContent = texto;
        $('produccion-mensaje').className = `alert alert-${error ? 'danger' : 'success'}`;
    }
    async function request(url, body) {
        const headers = { Accept: 'application/json' };
        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;
            const token = document.querySelector('meta[name="_csrf"]')?.content;
            if (header && token) headers[header] = token;
        }
        const res = await fetch(url, { method: body === undefined ? 'GET' : 'POST',
            credentials: 'same-origin', headers, body: body === undefined ? undefined : JSON.stringify(body) });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(data.errors?.map(e => e.message || e.mensaje).filter(Boolean).join(' ') ||
            data.message || data.mensaje || `No se pudo completar la operación (${res.status}).`);
        return data;
    }
    function tabla(id, filas, columnas) {
        const tbody = $(id);
        tbody.replaceChildren();
        if (!filas.length) {
            const tr = tbody.insertRow();
            const td = tr.insertCell(); td.colSpan = columnas; td.textContent = 'Sin registros.';
            return;
        }
        filas.forEach(fila => {
            const tr = tbody.insertRow();
            fila.forEach(valor => { tr.insertCell().textContent = valor ?? ''; });
        });
    }
    function actualizarTotal() {
        $('prod-total').textContent = [...document.querySelectorAll('.cantidad-huevo')]
            .reduce((suma, input) => suma + Number(input.value || 0), 0).toLocaleString('es-PE');
    }
    function renderTipos() {
        tabla('tb-tipos-huevo', tipos.map(t => [t.nombre, t.descripcion]), 2);
        const container = $('contenedor-tipos-huevo');
        const previos = new Map([...container.querySelectorAll('input')].map(i => [i.dataset.tipo, i.value]));
        container.replaceChildren();
        if (!tipos.length) container.textContent = 'Registre primero un tipo de huevo.';
        tipos.forEach(tipo => {
            const label = document.createElement('label');
            label.className = 'form-label'; label.htmlFor = `cantidad-${tipo.idTipo}`; label.textContent = tipo.nombre;
            const input = document.createElement('input');
            input.id = label.htmlFor; input.type = 'number'; input.min = '0'; input.max = '2147483647'; input.step = '1';
            input.required = true; input.value = previos.get(String(tipo.idTipo)) ?? '0'; input.dataset.tipo = tipo.idTipo;
            input.className = 'form-control mb-2 cantidad-huevo'; input.addEventListener('input', actualizarTotal);
            container.append(label, input);
        });
        $('guardar-produccion').disabled = !tipos.length || !galpones.length;
        actualizarTotal();
    }
    async function cargarResultados() {
        const [registros, sectores] = await Promise.all([request(api), request(`${api}/sectores`)]);
        tabla('tb-producciones', registros.map(p => [p.fecha, p.galpon, p.sector,
            p.detalles.map(d => `${d.nombre}: ${d.cantidad}`).join(', '), p.totalHuevos, p.observacion]), 6);
        tabla('tb-produccion-sector', sectores.map(p => [p.fecha, p.sector, p.totalHuevos]), 3);
    }
    function sectorSeleccionado() {
        const galpon = galpones.find(g => g.idGalpon === Number($('prod-galpon').value));
        $('prod-sector').textContent = galpon ? `Sector: ${galpon.nombreSector}` : '';
    }
    document.addEventListener('DOMContentLoaded', async () => {
        for (const opcion of ['indicadores', 'registro']) {
            $(`tab-${opcion}`).addEventListener('click', () => {
                for (const panel of ['indicadores', 'registro']) {
                    const activo = panel === opcion;
                    $(`panel-${panel}`).classList.toggle('d-none', !activo);
                    $(`tab-${panel}`).classList.toggle('active', activo);
                    $(`tab-${panel}`).setAttribute('aria-selected', String(activo));
                }
                if (opcion === 'registro') cargarResultados().catch(e => mensaje(e.message, true));
                if (opcion === 'indicadores' && galponActual) seleccionarGalpon(galponActual);
            });
        }
        const hoy = new Intl.DateTimeFormat('sv-SE', { timeZone: 'America/Lima' }).format(new Date());
        $('prod-fecha').value = hoy; $('prod-fecha').max = hoy;
        $('guardar-produccion').disabled = true;
        $('prod-galpon').addEventListener('change', sectorSeleccionado);
        $('form-tipo-huevo').addEventListener('submit', async event => {
            event.preventDefault(); const boton = event.submitter; boton.disabled = true;
            try {
                await request(`${api}/tipos-huevo`, { nombre: $('th-nombre').value.trim(), descripcion: $('th-descripcion').value.trim() });
                $('form-tipo-huevo').reset();
                tipos = await request(`${api}/tipos-huevo`); renderTipos(); mensaje('Tipo de huevo registrado.');
            } catch (e) { mensaje(e.message, true); } finally { boton.disabled = false; }
        });
        $('form-produccion').addEventListener('submit', async event => {
            event.preventDefault();
            const boton = $('guardar-produccion'); boton.disabled = true;
            let guardado = false;
            try {
                if (!tipos.length) throw new Error('Registre un tipo de huevo.');
                const detalles = [...document.querySelectorAll('.cantidad-huevo')].map(i => ({ tipoHuevoId: Number(i.dataset.tipo), cantidad: Number(i.value) }));
                await request(api, { fecha: $('prod-fecha').value, galponId: Number($('prod-galpon').value),
                    observacion: $('prod-observacion').value.trim(), detalles });
                guardado = true;
                document.querySelectorAll('.cantidad-huevo').forEach(i => { i.value = '0'; });
                $('prod-observacion').value = ''; actualizarTotal(); datosAnalisis = null;
                await cargarResultados(); mensaje('Producción registrada. El resumen del sector se actualizó.');
            } catch (e) { mensaje(guardado ? `La producción se guardó, pero no se pudo actualizar la lista: ${e.message}` : e.message, true); }
            finally { boton.disabled = !tipos.length || !galpones.length; }
        });
        try {
            [tipos, galpones] = await Promise.all([request(`${api}/tipos-huevo`), request('/api/infraestructura/galpones')]);
            galpones = galpones.filter(g => g.estado?.toLowerCase() === 'activo');
            $('prod-galpon').replaceChildren(new Option('Seleccione un galpón', ''));
            galpones.forEach(g => $('prod-galpon').add(new Option(`${g.nombre} — ${g.nombreSector}`, g.idGalpon)));
            renderTipos(); await cargarResultados();
        } catch (e) { mensaje(e.message, true); }
    });
})();
