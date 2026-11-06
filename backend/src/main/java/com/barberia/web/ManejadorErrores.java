package com.barberia.web;

import com.barberia.servicio.AccesoDenegadoException;
import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.HorarioNoDisponibleException;
import com.barberia.servicio.autenticacion.CorreoYaRegistradoException;
import com.barberia.servicio.autenticacion.CredencialesInvalidasException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas HTTP con formato Problem Details (RFC 9457).
 * Los errores de validación de la solicitud (400) los resuelve la clase base.
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** HU-01 / HU-07: horario ocupado; incluye las franjas que siguen libres. */
    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ProblemDetail horarioNoDisponible(HorarioNoDisponibleException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setProperty("horariosDisponibles", ex.getHorariosDisponibles());
        return problema;
    }

    /** HU-21: correo ya registrado. */
    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ProblemDetail correoYaRegistrado(CorreoYaRegistradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** HU-22/23/24: credenciales inválidas. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** HU-26: el rol o el usuario no tiene acceso a esa información. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail accesoDenegado(AccesoDenegadoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /** Dos solicitudes simultáneas chocaron con una restricción única (por ejemplo, el mismo correo). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflictoDeDatos(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "La operación choca con datos ya registrados");
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaDeNegocio(ReglaNegocioException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** Operación que el estado actual de una entidad no permite. */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail estadoInvalido(IllegalStateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Invariantes del dominio que la solicitud no cumple. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail datosInvalidos(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
