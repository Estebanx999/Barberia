package com.barberia.web;

import com.barberia.dominio.EstadoCita;
import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CitaControllerTest {

    private static final String SOLICITUD = """
            {"clienteId": 1, "barberoId": 2, "fecha": "2026-11-10", "horaInicio": "10:00"}
            """;

    private final AgendamientoService agendamiento = mock(AgendamientoService.class);
    private final ConsultaAgendaService consultas = mock(ConsultaAgendaService.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new CitaController(agendamiento, consultas))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private ResultActions enviar(String cuerpo) throws Exception {
        return mvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void agendarRespondeCreated() throws Exception {
        when(agendamiento.agendar(any())).thenReturn(new CitaVista(10L, LocalDate.of(2026, 11, 10),
                LocalTime.of(10, 0), LocalTime.of(10, 30), EstadoCita.AGENDADA, 2L, "Carlos", 1L, "Juan"));

        enviar(SOLICITUD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("AGENDADA"));
    }

    @Test
    void horarioOcupadoRespondeConflictConLosHorariosDisponibles() throws Exception {
        when(agendamiento.agendar(any())).thenThrow(new HorarioNoDisponibleException(
                List.of(new Franja(LocalTime.of(10, 30), LocalTime.of(11, 0)))));

        enviar(SOLICITUD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.horariosDisponibles.length()").value(1));
    }

    @Test
    void solicitudIncompletaRespondeBadRequest() throws Exception {
        enviar("""
                {"clienteId": 1, "barberoId": 2}
                """).andExpect(status().isBadRequest());
        verifyNoInteractions(agendamiento);
    }

    @Test
    void recursoInexistenteRespondeNotFound() throws Exception {
        when(agendamiento.agendar(any())).thenThrow(new RecursoNoEncontradoException("Cliente", 1L));
        enviar(SOLICITUD).andExpect(status().isNotFound());
    }

    @Test
    void reglaIncumplidaRespondeUnprocessableEntity() throws Exception {
        when(agendamiento.agendar(any())).thenThrow(new ReglaNegocioException("fuera de la jornada"));
        enviar(SOLICITUD).andExpect(status().isUnprocessableEntity());
    }
}
