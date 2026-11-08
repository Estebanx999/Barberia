package com.barberia.web;

import com.barberia.dominio.EstadoCita;
import com.barberia.dominio.Rol;
import com.barberia.dominio.Servicio;
import com.barberia.reporte.Periodo;
import com.barberia.servicio.AccesoDenegadoException;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.CitaVista;
import com.barberia.servicio.atencion.AtencionFacade;
import com.barberia.servicio.atencion.ComisionVista;
import com.barberia.servicio.atencion.ConsultaCortesService;
import com.barberia.servicio.atencion.CorteVista;
import com.barberia.soporte.UsuarioFijoResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AtencionControllerTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    private final AtencionFacade atencion = mock(AtencionFacade.class);
    private final ConsultaCortesService consultaCortes = mock(ConsultaCortesService.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new AtencionController(atencion, consultaCortes))
                .setControllerAdvice(new ManejadorErrores())
                .setCustomArgumentResolvers(new UsuarioFijoResolver(2L, Rol.BARBERO))
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
        when(atencion.marcarAtendida(1L, 2L)).thenReturn(new CitaVista(1L, DIA, LocalTime.of(10, 0),
                LocalTime.of(10, 30), Servicio.CORTE, "Corte de cabello", EstadoCita.ATENDIDA, 2L, "Carlos", 5L, "Juan"));

        mvc.perform(patch("/api/citas/1/atendida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    @Test
    void registrarCobroRespondeCreatedConLaComision() throws Exception {
        when(atencion.registrarAtencion(eq(1L), eq(2L), eq("Corte clásico"), any())).thenReturn(corte(true));

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
        when(atencion.registrarAtencion(anyLong(), anyLong(), anyString(), any()))
                .thenThrow(new ReglaNegocioException("La cita debe estar marcada como atendida"));

        postJson("/api/citas/1/corte", """
                {"servicio": "Corte", "valorCobrado": 30000}
                """).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void corteSinCitaRespondeCreated() throws Exception {
        when(atencion.registrarAtencionSinCita(eq(2L), any(), eq("Corte clásico"), any())).thenReturn(corte(false));

        postJson("/api/cortes", """
                {"clienteNombre": "Pedro", "servicio": "Corte clásico", "valorCobrado": 30000}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tieneCitaPrevia").value(false));
    }

    @Test
    void citaDeOtroBarberoRespondeForbidden() throws Exception {
        when(atencion.marcarAtendida(1L, 2L)).thenThrow(new AccesoDenegadoException("Solo el barbero asignado"));
        mvc.perform(patch("/api/citas/1/atendida")).andExpect(status().isForbidden());
    }

    @Test
    void misCortesUsaElBarberoAutenticado() throws Exception {
        Periodo periodo = new Periodo(DIA, DIA.plusDays(6));
        when(consultaCortes.cortesDelBarbero(2L, periodo)).thenReturn(
                new ConsultaCortesService.ResumenCortes(periodo, 1, new BigDecimal("30000"),
                        new BigDecimal("18000"), List.of(corte(true))));

        mvc.perform(get("/api/cortes/mios").param("desde", "2026-11-10").param("hasta", "2026-11-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComision").value(18000));
    }

    @Test
    void corteSinCitaSinClienteRespondeBadRequest() throws Exception {
        postJson("/api/cortes", """
                {"servicio": "Corte", "valorCobrado": 30000}
                """).andExpect(status().isBadRequest());
        verifyNoInteractions(atencion);
    }
}
