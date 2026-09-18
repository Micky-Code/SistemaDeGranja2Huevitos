const panel = document.querySelector('#user-panel');
const form = document.querySelector('#user-form');
const message = document.querySelector('#users-message');
const list = document.querySelector('#users-list');
const roleSelect = document.querySelector('#role');
const password = document.querySelector('#password');
let users = [];
let editingId = null;

function showMessage(text, error = false) {
    message.textContent = text;
    message.className = `alert ${error ? 'alert-danger' : 'alert-success'}`;
}

async function readJson(response) {
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(body.message || body.mensaje || (response.status === 403
            ? 'No tiene permisos para administrar usuarios.' : 'No se pudo completar la operación.'));
    }
    return body;
}

async function loadData() {
    try {
        const [usersResponse, rolesResponse] = await Promise.all([
            fetch('/api/usuarios', { credentials: 'same-origin' }),
            fetch('/api/usuarios/roles', { credentials: 'same-origin' })
        ]);
        users = await readJson(usersResponse);
        const roles = await readJson(rolesResponse);
        roleSelect.replaceChildren();
        for (const role of roles) {
            const option = document.createElement('option');
            option.value = role.idRol;
            option.textContent = role.nombre;
            roleSelect.append(option);
        }
        renderUsers();
    } catch (error) {
        list.replaceChildren();
        showMessage(error.message, true);
    }
}

function renderUsers() {
    list.replaceChildren();
    if (!users.length) {
        const row = list.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 4;
        cell.textContent = 'No hay usuarios registrados.';
        return;
    }
    for (const user of users) {
        const row = list.insertRow();
        row.insertCell().textContent = user.username;
        row.insertCell().textContent = user.rol;
        row.insertCell().textContent = user.estado ? 'Activo' : 'Inactivo';
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn btn-outline-success btn-sm';
        button.textContent = 'Editar';
        button.addEventListener('click', () => editUser(user));
        row.insertCell().append(button);
    }
}

function openForm() {
    panel.hidden = false;
    panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function newUser() {
    editingId = null;
    form.reset();
    document.querySelector('#active').checked = true;
    password.required = true;
    document.querySelector('#form-title').textContent = 'Crear usuario';
    document.querySelector('#password-help').textContent = 'Mínimo 7 caracteres';
    openForm();
}

function editUser(user) {
    editingId = user.idUsuario;
    form.reset();
    document.querySelector('#username').value = user.username;
    roleSelect.value = user.idRol;
    document.querySelector('#active').checked = user.estado;
    password.required = false;
    document.querySelector('#form-title').textContent = 'Editar usuario';
    document.querySelector('#password-help').textContent = 'Déjela vacía para conservar la contraseña actual.';
    openForm();
}

document.querySelector('#new-user').addEventListener('click', newUser);
document.querySelector('#cancel-user').addEventListener('click', () => { panel.hidden = true; });
form.addEventListener('submit', async event => {
    event.preventDefault();
    const button = document.querySelector('#save-user');
    button.disabled = true;
    try {
        const csrf = await readJson(await fetch('/api/auth/csrf', { credentials: 'same-origin' }));
        const payload = {
            username: document.querySelector('#username').value.trim(),
            password: password.value,
            idRol: Number(roleSelect.value),
            estado: document.querySelector('#active').checked
        };
        const response = await fetch(editingId === null ? '/api/usuarios' : `/api/usuarios/${editingId}`, {
            method: editingId === null ? 'POST' : 'PUT',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
            body: JSON.stringify(payload)
        });
        await readJson(response);
        panel.hidden = true;
        showMessage('Usuario guardado correctamente.');
        await loadData();
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        button.disabled = false;
    }
});

loadData();
