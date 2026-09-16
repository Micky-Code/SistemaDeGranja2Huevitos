const logoutButton = document.querySelector('#logout-button');
const logoutError = document.querySelector('#logout-error');

logoutButton.addEventListener('click', async () => {
    logoutButton.disabled = true;
    logoutError.classList.add('d-none');
    try {
        const csrfResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
        if (!csrfResponse.ok) throw new Error();
        const csrf = await csrfResponse.json();
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { [csrf.headerName]: csrf.token }
        });
        if (!response.ok) throw new Error();
        window.location.replace('/login');
    } catch (_) {
        logoutError.textContent = 'No se pudo cerrar la sesión. Inténtalo nuevamente.';
        logoutError.classList.remove('d-none');
        logoutButton.disabled = false;
    }
});
