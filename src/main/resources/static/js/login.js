const form = document.querySelector('#login-form');
const errorBox = document.querySelector('#login-error');
const button = document.querySelector('#login-button');
const spinner = document.querySelector('#login-spinner');
const buttonText = document.querySelector('#login-button-text');

function showError(message) {
    errorBox.textContent = message;
    errorBox.classList.remove('d-none');
}

async function getCsrf() {
    const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    if (!response.ok) throw new Error('No se pudo preparar el inicio de sesión. Inténtalo nuevamente.');
    return response.json();
}

fetch('/api/auth/me', { credentials: 'same-origin' })
    .then(response => { if (response.ok) window.location.replace('/menu'); })
    .catch(() => {});

form.addEventListener('submit', async event => {
    event.preventDefault();
    errorBox.classList.add('d-none');
    const username = form.elements.namedItem('username').value.trim();
    const password = form.elements.namedItem('password').value;
    if (!username || !password) {
        showError('Ingresa tu usuario y contraseña.');
        return;
    }

    button.disabled = true;
    spinner.classList.remove('d-none');
    buttonText.textContent = 'Ingresando...';
    try {
        const csrf = await getCsrf();
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) {
            showError(response.status === 401 ? 'Usuario o contraseña incorrectos.' : 'No se pudo iniciar sesión. Inténtalo nuevamente.');
            return;
        }
        window.location.assign('/menu');
    } catch (error) {
        showError(error.message || 'No se pudo conectar con el servidor.');
    } finally {
        button.disabled = false;
        spinner.classList.add('d-none');
        buttonText.textContent = 'Ingresar';
    }
});
