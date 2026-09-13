document.addEventListener('DOMContentLoaded', () => {
    // 1. Marca el enlace activo en la barra de navegación
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    const navLinks = document.querySelectorAll('nav a');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // 2. Simula el guardado de datos en pantalla
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('¡Registro simulado con éxito! (Frontend independiente)');
            form.reset();
        });
    });

    // 3. Calculadora automática de huevos y bandejas en tiempo real
    const inputsHuevo = document.querySelectorAll('.grid-inputs input[type="number"]');
    if (inputsHuevo.length > 0) {
        const contenedorSuma = document.createElement('div');
        contenedorSuma.style.marginTop = '1rem';
        contenedorSuma.style.fontWeight = 'bold';
        contenedorSuma.style.color = '#2c3e50';
        contenedorSuma.innerHTML = 'Total Huevos Calculados: <span id="total-vivo">0</span> unidades (<span id="bandejas-vivo">0</span> bandejas)';
        
        document.querySelector('.grid-inputs').after(contenedorSuma);

        const totalSpan = document.getElementById('total-vivo');
        const bandejasSpan = document.getElementById('bandejas-vivo');

        const calcularTotal = () => {
            let total = 0;
            inputsHuevo.forEach(input => {
                total += parseInt(input.value) || 0;
            });
            totalSpan.textContent = total.toLocaleString();
            bandejasSpan.textContent = (total / 30).toFixed(1);
        };

        inputsHuevo.forEach(input => {
            input.addEventListener('input', calcularTotal);
        });
    }
});