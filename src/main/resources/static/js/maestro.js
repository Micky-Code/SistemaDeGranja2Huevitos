(() => {
    const api = '/api/produccion/registro/tipos-huevo';
    const $ = id => document.getElementById(id);
    let editandoId = null, guardando = false;
    function mensaje(texto, error = false) {
        $('maestro-mensaje').textContent = texto;
        $('maestro-mensaje').className = `alert alert-${error ? 'danger' : 'success'}`;
    }
    async function request(url, body, method = body === undefined ? 'GET' : 'POST') {
        const headers = { Accept: 'application/json' };
        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;
            const token = document.querySelector('meta[name="_csrf"]')?.content;
            if (header && token) headers[header] = token;
        }
        const res = await fetch(url, { method,
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

    async function cargarTipos() {
        const tipos = await request(api);
        tabla('tb-tipos-huevo', tipos.map(t => [t.nombre, t.descripcion]), 3);
        const filas = $('tb-tipos-huevo').rows;
        tipos.forEach((tipo, index) => {
            const boton = document.createElement('button');
            boton.type = 'button'; boton.className = 'btn btn-outline-success btn-sm';
            boton.textContent = 'Editar'; boton.setAttribute('aria-label', `Editar ${tipo.nombre}`);
            boton.addEventListener('click', () => {
                if (guardando) return;
                editandoId = tipo.idTipo;
                $('th-nombre').value = tipo.nombre;
                $('th-descripcion').value = tipo.descripcion ?? '';
                $('th-modo').textContent = `Editando: ${tipo.nombre}`;
                $('th-modo').classList.remove('d-none');
                $('th-cancelar').classList.remove('d-none');
                $('form-tipo-huevo').querySelector('button[type="submit"]').textContent = 'Guardar cambios';
                $('th-nombre').focus();
            });
            filas[index].insertCell().append(boton);
        });
    }
    document.addEventListener('DOMContentLoaded', async () => {
        const form = $('form-tipo-huevo');
        const boton = form.querySelector('button[type="submit"]');
        function cancelarEdicion() {
            editandoId = null; form.reset();
            boton.textContent = 'Registrar tipo de huevo';
            $('th-modo').classList.add('d-none');
            $('th-cancelar').classList.add('d-none');
        }
        $('th-cancelar').addEventListener('click', cancelarEdicion);
        form.addEventListener('submit', async event => {
            event.preventDefault();
            if (guardando) return;
            guardando = true; boton.disabled = true; $('th-cancelar').disabled = true;
            const esEdicion = editandoId !== null;
            let guardado = false;
            try {
                const nombre = $('th-nombre').value.trim();
                if (!nombre) throw new Error('Ingrese el nombre del tipo de huevo.');
                await request(esEdicion ? `${api}/${editandoId}` : api,
                    { nombre, descripcion: $('th-descripcion').value.trim() }, esEdicion ? 'PUT' : 'POST');
                guardado = true;
                cancelarEdicion();
                await cargarTipos();
                mensaje(esEdicion ? 'Tipo de huevo actualizado.' : 'Tipo de huevo registrado.');
            } catch (error) {
                mensaje(guardado ? `El tipo de huevo se guardó, pero no se pudo actualizar la lista: ${error.message}` : error.message, true);
            } finally { guardando = false; boton.disabled = false; $('th-cancelar').disabled = false; }
        });
        try { await cargarTipos(); }
        catch (error) { mensaje(error.message, true); }
    });
})();
