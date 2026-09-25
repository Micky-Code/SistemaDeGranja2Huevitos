package com.granja.dos.huevitos.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.granja.dos.huevitos.dto.EmpleadoMantenimientoRequest;
import com.granja.dos.huevitos.dto.EmpleadoMantenimientoResponse;
import com.granja.dos.huevitos.dto.EmpleadoMantenimientoResponse.AsignacionResponse;
import com.granja.dos.huevitos.dto.EmpleadoMantenimientoResponse.DocumentoResponse;
import com.granja.dos.huevitos.dto.EmpleadoMantenimientoResponse.GuardiaResponse;
import com.granja.dos.huevitos.dto.GalponResumenResponse;
import com.granja.dos.huevitos.dto.TipoDocumentoResponse;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.exception.ExceptionResponse;
import com.granja.dos.huevitos.models.personal.AsignacionGalpon;
import com.granja.dos.huevitos.models.personal.DocumentoIdentidad;
import com.granja.dos.huevitos.models.personal.Empleado;
import com.granja.dos.huevitos.models.personal.Guardia;
import com.granja.dos.huevitos.models.personal.Persona;
import com.granja.dos.huevitos.repository.AsignacionGalponRepository;
import com.granja.dos.huevitos.repository.DocumentoIdentidadRepository;
import com.granja.dos.huevitos.repository.EmpleadoRepository;
import com.granja.dos.huevitos.repository.GalponRepository;
import com.granja.dos.huevitos.repository.GuardiaRepository;
import com.granja.dos.huevitos.repository.PersonaRepository;
import com.granja.dos.huevitos.repository.TipoDocumentoRepository;

@Service
public class EmpleadoMantenimientoService {
    private final EmpleadoRepository empleados;
    private final PersonaRepository personas;
    private final DocumentoIdentidadRepository documentos;
    private final TipoDocumentoRepository tiposDocumento;
    private final GuardiaRepository guardias;
    private final AsignacionGalponRepository asignaciones;
    private final GalponRepository galpones;

    public EmpleadoMantenimientoService(EmpleadoRepository empleados, PersonaRepository personas,
            DocumentoIdentidadRepository documentos, TipoDocumentoRepository tiposDocumento,
            GuardiaRepository guardias, AsignacionGalponRepository asignaciones, GalponRepository galpones) {
        this.empleados = empleados;
        this.personas = personas;
        this.documentos = documentos;
        this.tiposDocumento = tiposDocumento;
        this.guardias = guardias;
        this.asignaciones = asignaciones;
        this.galpones = galpones;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoMantenimientoResponse> listar() {
        return empleados.findAllByOrderByIdEmpleadoAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GalponResumenResponse> listarGalpones() {
        return galpones.findAllByEstadoIgnoreCaseOrderByNombreAsc("Activo").stream()
                .map(item -> new GalponResumenResponse(item.getIdGalpon(), item.getNombre())).toList();
    }

    @Transactional(readOnly = true)
    public List<TipoDocumentoResponse> listarTiposDocumento() {
        return tiposDocumento.findAllByActivoTrueOrderByNombreAsc().stream()
                .map(item -> new TipoDocumentoResponse(item.getIdTipoDocumento(), item.getCodigo(), item.getNombre()))
                .toList();
    }

    @Transactional
    public EmpleadoMantenimientoResponse crear(EmpleadoMantenimientoRequest request) {
        Persona persona = new Persona();
        aplicarPersona(persona, request);
        personas.saveAndFlush(persona);
        guardarDocumento(persona, request);

        Empleado empleado = new Empleado();
        empleado.setPersona(persona);
        aplicarEmpleado(empleado, request);
        guardarEmpleado(empleado);
        guardarGuardia(empleado, request.guardia(), false);
        guardarAsignacion(empleado, request.asignacion());
        return toResponse(empleado);
    }

    @Transactional
    public EmpleadoMantenimientoResponse actualizar(Integer id, EmpleadoMantenimientoRequest request) {
        Empleado empleado = empleados.findById(id)
                .orElseThrow(() -> new ExceptionResponse(404, "Empleado no encontrado."));
        aplicarPersona(empleado.getPersona(), request);
        personas.saveAndFlush(empleado.getPersona());
        guardarDocumento(empleado.getPersona(), request);
        aplicarEmpleado(empleado, request);
        guardarEmpleado(empleado);
        guardarGuardia(empleado, request.guardia(), true);
        guardarAsignacion(empleado, request.asignacion());
        return toResponse(empleado);
    }

    private void aplicarPersona(Persona persona, EmpleadoMantenimientoRequest request) {
        if (request.fechaNacimiento() != null && request.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento no puede estar en el futuro.");
        }
        String correo = limpiarOpcional(request.correo());
        if (correo != null) {
            personas.findByCorreoIgnoreCase(correo)
                    .filter(actual -> !actual.getIdPersona().equals(persona.getIdPersona()))
                    .ifPresent(actual -> { throw new BadRequestException("El correo ya pertenece a otra persona."); });
        }
        persona.setNombres(request.nombres().trim());
        persona.setApellidos(request.apellidos().trim());
        persona.setFechaNacimiento(request.fechaNacimiento());
        persona.setSexo(limpiarOpcional(request.sexo()));
        persona.setCorreo(correo);
        persona.setTelefono(limpiarOpcional(request.telefono()));
        persona.setDireccion(limpiarOpcional(request.direccion()));
        persona.setEstado(request.estado());
    }

    private void guardarDocumento(Persona persona, EmpleadoMantenimientoRequest request) {
        var tipo = tiposDocumento.findById(request.idTipoDocumento())
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new BadRequestException("Seleccione un tipo de documento activo."));
        String numero = request.numeroDocumento().trim();
        DocumentoIdentidad documento = documentos
                .findFirstByPersona_IdPersonaOrderByIdDocumentoAsc(persona.getIdPersona())
                .orElseGet(DocumentoIdentidad::new);
        documentos.findByTipoDocumento_IdTipoDocumentoAndNumeroDocumentoIgnoreCase(tipo.getIdTipoDocumento(), numero)
                .filter(actual -> !actual.getIdDocumento().equals(documento.getIdDocumento()))
                .ifPresent(actual -> { throw new BadRequestException("El número de documento ya está registrado."); });
        documento.setPersona(persona);
        documento.setTipoDocumento(tipo);
        documento.setNumeroDocumento(numero);
        if (documento.getIdDocumento() == null) documento.setUsuarioCreacion(usuarioActual());
        documentos.saveAndFlush(documento);
    }

    private void aplicarEmpleado(Empleado empleado, EmpleadoMantenimientoRequest request) {
        validarPeriodo(request.fechaIngreso(), request.fechaSalida(), "empleado");
        empleado.setFechaIngreso(request.fechaIngreso());
        empleado.setFechaSalida(request.fechaSalida());
        empleado.setEstado(request.estado());
    }

    private void guardarGuardia(Empleado empleado, EmpleadoMantenimientoRequest.GuardiaRequest request,
            boolean eliminarSiNoSeEnvia) {
        var existente = guardias.findByEmpleado_IdEmpleado(empleado.getIdEmpleado());
        if (request == null) {
            if (eliminarSiNoSeEnvia) existente.ifPresent(guardias::delete);
            return;
        }
        validarPeriodo(request.fechaInicio(), request.fechaFin(), "guardia");
        Guardia guardia = existente.orElseGet(Guardia::new);
        guardia.setEmpleado(empleado);
        guardia.setTurno(request.turno());
        guardia.setFechaInicio(request.fechaInicio());
        guardia.setFechaFin(request.fechaFin());
        guardias.save(guardia);
    }

    private void guardarAsignacion(Empleado empleado, EmpleadoMantenimientoRequest.AsignacionRequest request) {
        if (request == null) return;
        validarPeriodo(request.fechaInicio(), request.fechaFin(), "asignación");
        var galpon = galpones.findById(request.idGalpon())
                .filter(item -> "Activo".equalsIgnoreCase(item.getEstado()))
                .orElseThrow(() -> new BadRequestException("Seleccione un galpón activo."));
        AsignacionGalpon asignacion;
        if (request.idAsignacion() == null) {
            asignacion = new AsignacionGalpon();
            asignacion.setEmpleado(empleado);
        } else {
            asignacion = asignaciones.findById(request.idAsignacion())
                    .filter(item -> item.getEmpleado().getIdEmpleado().equals(empleado.getIdEmpleado()))
                    .orElseThrow(() -> new BadRequestException("La asignación no pertenece al empleado."));
        }
        asignacion.setGalpon(galpon);
        asignacion.setTipoAsignacion(request.tipoAsignacion());
        asignacion.setFechaInicio(request.fechaInicio());
        asignacion.setFechaFin(request.fechaFin());
        asignaciones.save(asignacion);
    }

    private Empleado guardarEmpleado(Empleado empleado) {
        try {
            return empleados.saveAndFlush(empleado);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Los datos personales, laborales o del documento no son válidos.");
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fin, String nombre) {
        if (fin != null && fin.isBefore(inicio)) {
            throw new BadRequestException("La fecha final de " + nombre + " no puede ser anterior a la inicial.");
        }
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String usuarioActual() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private EmpleadoMantenimientoResponse toResponse(Empleado empleado) {
        Persona persona = empleado.getPersona();
        DocumentoResponse documento = documentos.findFirstByPersona_IdPersonaOrderByIdDocumentoAsc(persona.getIdPersona())
                .map(item -> new DocumentoResponse(item.getIdDocumento(), item.getTipoDocumento().getIdTipoDocumento(),
                        item.getTipoDocumento().getCodigo(), item.getTipoDocumento().getNombre(), item.getNumeroDocumento()))
                .orElse(null);
        GuardiaResponse guardia = guardias.findByEmpleado_IdEmpleado(empleado.getIdEmpleado())
                .map(item -> new GuardiaResponse(item.getIdGuardia(), item.getTurno(), item.getFechaInicio(), item.getFechaFin()))
                .orElse(null);
        List<AsignacionResponse> lista = asignaciones
                .findAllByEmpleado_IdEmpleadoOrderByFechaInicioDesc(empleado.getIdEmpleado()).stream()
                .map(item -> new AsignacionResponse(item.getIdAsignacion(), item.getGalpon().getIdGalpon(),
                        item.getGalpon().getNombre(), item.getTipoAsignacion(), item.getFechaInicio(), item.getFechaFin()))
                .toList();
        return new EmpleadoMantenimientoResponse(empleado.getIdEmpleado(), persona.getNombres(),
                persona.getApellidos(), persona.getFechaNacimiento(), persona.getSexo(), persona.getCorreo(),
                persona.getTelefono(), persona.getDireccion(), empleado.getEstado(), empleado.getFechaIngreso(),
                empleado.getFechaSalida(), documento, guardia, lista);
    }
}
