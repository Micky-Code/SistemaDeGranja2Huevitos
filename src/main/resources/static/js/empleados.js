const employeeForm = document.querySelector('#employee-form');
const employeePanel = document.querySelector('#employee-panel');
const employeeMessage = document.querySelector('#employees-message');
const employeeList = document.querySelector('#employees-list');
const guardToggle = document.querySelector('#include-guard');
const assignmentToggle = document.querySelector('#include-assignment');
const guardFields = document.querySelector('#guard-fields');
const assignmentFields = document.querySelector('#assignment-fields');
const barnSelect = document.querySelector('#barn');
const documentTypeSelect = document.querySelector('#document-type');
let employees = [];
let editingId = null;

function nullableValue(selector) {
    const value = document.querySelector(selector).value.trim();
    return value || null;
}

function showEmployeeMessage(text, error = false) {
    employeeMessage.textContent = text;
    employeeMessage.className = `alert ${error ? 'alert-danger' : 'alert-success'}`;
    employeeMessage.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

async function readEmployeeJson(response) {
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const validation = Array.isArray(body.errors)
            ? body.errors.map(item => item.mensaje || item.message).filter(Boolean).join(' ')
            : '';
        throw new Error(validation || body.message || body.mensaje || 'No se pudo completar la operación.');
    }
    return body;
}

function toggleSection(toggle, fields) {
    fields.hidden = !toggle.checked;
    const requiredIds = ['guard-shift', 'guard-start', 'barn', 'assignment-type'];
    for (const input of fields.querySelectorAll('input, select')) {
        input.required = toggle.checked && requiredIds.includes(input.id);
    }
    if (fields === assignmentFields) updateAssignmentDates();
}

function updateAssignmentDates() {
    const isPivot = assignmentToggle.checked && document.querySelector('#assignment-type').value === 'PIVOTE';
    const start = document.querySelector('#assignment-start');
    const end = document.querySelector('#assignment-end');
    start.disabled = !isPivot;
    end.disabled = !isPivot;
    start.required = isPivot;
    end.required = false;
}

document.querySelector('#assignment-type').addEventListener('change', updateAssignmentDates);

guardToggle.addEventListener('change', () => toggleSection(guardToggle, guardFields));
assignmentToggle.addEventListener('change', () => toggleSection(assignmentToggle, assignmentFields));

async function loadEmployees() {
    try {
        const [employeesResponse, barnsResponse, typesResponse] = await Promise.all([
            fetch('/api/empleados', { credentials: 'same-origin' }),
            fetch('/api/empleados/galpones', { credentials: 'same-origin' }),
            fetch('/api/empleados/tipos-documento', { credentials: 'same-origin' })
        ]);
        employees = await readEmployeeJson(employeesResponse);
        const barns = await readEmployeeJson(barnsResponse);
        const documentTypes = await readEmployeeJson(typesResponse);

        barnSelect.replaceChildren(new Option('Seleccionar galpón', ''));
        for (const barn of barns) barnSelect.add(new Option(barn.nombre, barn.idGalpon));
        documentTypeSelect.replaceChildren(new Option('Seleccionar tipo', ''));
        for (const type of documentTypes) {
            documentTypeSelect.add(new Option(`${type.codigo} - ${type.nombre}`, type.idTipoDocumento));
        }
        renderEmployees();
    } catch (error) {
        employeeList.innerHTML = '<tr><td colspan="7">No fue posible cargar los empleados.</td></tr>';
        showEmployeeMessage(error.message, true);
    }
}

function renderEmployees() {
    employeeList.replaceChildren();
    if (!employees.length) {
        employeeList.innerHTML = '<tr><td colspan="7" class="text-center text-secondary py-4">No hay empleados registrados.</td></tr>';
        return;
    }
    for (const employee of employees) {
        const row = employeeList.insertRow();
        const name = row.insertCell();
        name.innerHTML = `<strong>${escapeHtml(employee.nombres)} ${escapeHtml(employee.apellidos)}</strong>`;
        row.insertCell().textContent = employee.documento
            ? `${employee.documento.tipoCodigo}: ${employee.documento.numeroDocumento}` : '—';
        row.insertCell().textContent = employee.correo || employee.telefono || '—';
        row.insertCell().textContent = employee.guardia ? employee.guardia.turno : '—';
        row.insertCell().textContent = employee.asignaciones.length;
        row.insertCell().innerHTML = `<span class="badge ${employee.estado ? 'text-bg-success' : 'text-bg-secondary'}">${employee.estado ? 'Activo' : 'Inactivo'}</span>`;
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn btn-outline-success btn-sm';
        button.textContent = 'Editar';
        button.addEventListener('click', () => editEmployee(employee));
        row.insertCell().append(button);
    }
}

function escapeHtml(value) {
    const span = document.createElement('span');
    span.textContent = value || '';
    return span.innerHTML;
}

function setValue(selector, value) {
    document.querySelector(selector).value = value ?? '';
}

function resetEmployeeForm() {
    editingId = null;
    employeeForm.reset();
    document.querySelector('#employee-active').checked = true;
    document.querySelector('#assignment-id').value = '';
    guardToggle.checked = false;
    assignmentToggle.checked = false;
    toggleSection(guardToggle, guardFields);
    toggleSection(assignmentToggle, assignmentFields);
    document.querySelector('#form-title').textContent = 'Nuevo empleado';
}

function editEmployee(employee) {
    resetEmployeeForm();
    editingId = employee.idEmpleado;
    setValue('#names', employee.nombres);
    setValue('#last-names', employee.apellidos);
    setValue('#birth-date', employee.fechaNacimiento);
    setValue('#sex', employee.sexo);
    setValue('#email', employee.correo);
    setValue('#phone', employee.telefono);
    setValue('#address', employee.direccion);
    setValue('#entry-date', employee.fechaIngreso);
    setValue('#exit-date', employee.fechaSalida);
    document.querySelector('#employee-active').checked = employee.estado;
    if (employee.documento) {
        setValue('#document-type', employee.documento.idTipoDocumento);
        setValue('#document-number', employee.documento.numeroDocumento);
    }
    if (employee.guardia) {
        guardToggle.checked = true;
        setValue('#guard-shift', employee.guardia.turno);
        setValue('#guard-start', employee.guardia.fechaInicio);
        setValue('#guard-end', employee.guardia.fechaFin);
    }
    const assignment = employee.asignaciones[0];
    if (assignment) {
        assignmentToggle.checked = true;
        setValue('#assignment-id', assignment.idAsignacion);
        setValue('#barn', assignment.idGalpon);
        setValue('#assignment-type', assignment.tipoAsignacion);
        setValue('#assignment-start', assignment.fechaInicio);
        setValue('#assignment-end', assignment.fechaFin);
    }
    toggleSection(guardToggle, guardFields);
    toggleSection(assignmentToggle, assignmentFields);
    document.querySelector('#form-title').textContent = 'Editar empleado';
    employeePanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function buildPayload() {
    const payload = {
        nombres: document.querySelector('#names').value.trim(),
        apellidos: document.querySelector('#last-names').value.trim(),
        fechaNacimiento: nullableValue('#birth-date'),
        sexo: nullableValue('#sex'),
        correo: nullableValue('#email'),
        telefono: nullableValue('#phone'),
        direccion: nullableValue('#address'),
        estado: document.querySelector('#employee-active').checked,
        idTipoDocumento: Number(documentTypeSelect.value),
        numeroDocumento: document.querySelector('#document-number').value.trim(),
        fechaIngreso: document.querySelector('#entry-date').value,
        fechaSalida: nullableValue('#exit-date'),
        guardia: null,
        asignacion: null
    };
    if (guardToggle.checked) {
        payload.guardia = { turno: document.querySelector('#guard-shift').value,
            fechaInicio: document.querySelector('#guard-start').value, fechaFin: nullableValue('#guard-end') };
    }
    if (assignmentToggle.checked) {
        const assignmentId = document.querySelector('#assignment-id').value;
        const isPivot = document.querySelector('#assignment-type').value === 'PIVOTE';
        payload.asignacion = { idAsignacion: assignmentId ? Number(assignmentId) : null,
            idGalpon: Number(barnSelect.value), tipoAsignacion: document.querySelector('#assignment-type').value,
            fechaInicio: isPivot ? document.querySelector('#assignment-start').value
                : nullableValue('#assignment-start') || document.querySelector('#entry-date').value,
            fechaFin: isPivot ? nullableValue('#assignment-end') : null };
    }
    return payload;
}

employeeForm.addEventListener('submit', async event => {
    event.preventDefault();
    const saveButton = document.querySelector('#save-employee');
    saveButton.disabled = true;
    try {
        const csrf = await readEmployeeJson(await fetch('/api/auth/csrf', { credentials: 'same-origin' }));
        const response = await fetch(editingId === null ? '/api/empleados' : `/api/empleados/${editingId}`, {
            method: editingId === null ? 'POST' : 'PUT', credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
            body: JSON.stringify(buildPayload())
        });
        await readEmployeeJson(response);
        resetEmployeeForm();
        showEmployeeMessage('Empleado guardado correctamente.');
        await loadEmployees();
    } catch (error) {
        showEmployeeMessage(error.message, true);
    } finally {
        saveButton.disabled = false;
    }
});

document.querySelector('#new-employee').addEventListener('click', () => {
    resetEmployeeForm();
    employeePanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
});
document.querySelector('#clear-employee').addEventListener('click', resetEmployeeForm);

resetEmployeeForm();
loadEmployees();
