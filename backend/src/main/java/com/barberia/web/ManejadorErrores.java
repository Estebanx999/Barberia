package com.barberia.web;

import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.HorarioNoDisponibleException;
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

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaDeNegocio(ReglaNegocioException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** Invariantes del dominio que la solicitud no cumple. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail datosInvalidos(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
