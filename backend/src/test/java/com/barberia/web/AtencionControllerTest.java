package com.barberia.web;

import com.barberia.dominio.EstadoCita;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.CitaVista;
import com.barberia.servicio.atencion.AtencionFacade;
import com.barberia.servicio.atencion.ComisionVista;
import com.barberia.servicio.atencion.CorteVista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AtencionControllerTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    private final AtencionFacade atencion = mock(AtencionFacade.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new AtencionController(atencion))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private ResultActions postJson(String ruta, String cuerpo) throws Exception {
        return mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private static CorteVista corte(boolean conCita) {
        return new CorteVista(20L, DIA, "Corte clásico", new BigDecimal("30000"), conCita,
                conCita ? 1L : null, 2L, "Carlos", 5L, "Juan",
                new ComisionVista(new BigDecimal("18000.00"), new BigDecimal("12000.00"),
                        new BigDecimal("60"), new BigDecimal("40")));
    }

    @Test
    void marcarAtendidaRespondeOk() throws Exception {
        when(atencion.marcarAtendida(1L)).thenReturn(new CitaVista(1L, DIA, LocalTime.of(10, 0),
                LocalTime.of(10, 30), EstadoCita.ATENDIDA, 2L, "Carlos", 5L, "Juan"));

        mvc.perform(patch("/api/citas/1/atendida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    @Test
    void registrarCobroRespondeCreatedConLaComision() throws Exception {
        when(atencion.registrarAtencion(eq(1L), eq("Corte clásico"), any())).thenReturn(corte(true));

        postJson("/api/citas/1/corte", """
                {"servicio": "Corte clásico", "valorCobrado": 30000}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tieneCitaPrevia").value(true))
                .andExpect(jsonPath("$.comision.montoBarbero").value(18000.00));
    }

    @Test
    void cobroConValorNoPositivoRespondeBadRequest() throws Exception {
        postJson("/api/citas/1/corte", """
                {"servicio": "Corte", "valorCobrado": 0}
                """).andExpect(status().isBadRequest());
        verifyNoInteractions(atencion);
    }

    @Test
    void cobroDeCitaNoAtendidaRespondeUnprocessableEntity() throws Exception {
        when(atencion.registrarAtencion(anyLong(), anyString(), any()))
                .thenThrow(new ReglaNegocioException("La cita debe estar marcada como atendida"));

        postJson("/api/citas/1/corte", """
                {"servicio": "Corte", "valorCobrado": 30000}
                """).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void corteSinCitaRespondeCreated() throws Exception {
        when(atencion.registrarAtencionSinCita(eq(2L), any(), eq("Corte clásico"), any())).thenReturn(corte(false));

        postJson("/api/cortes", """
                {"barberoId": 2, "clienteNombre": "Pedro", "servicio": "Corte clásico", "valorCobrado": 30000}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tieneCitaPrevia").value(false));
    }

    @Test
    void corteSinCitaSinClienteRespondeBadRequest() throws Exception {
        postJson("/api/cortes", """
                {"barberoId": 2, "servicio": "Corte", "valorCobrado": 30000}
                """).andExpect(status().isBadRequest());
        verifyNoInteractions(atencion);
    }
}
