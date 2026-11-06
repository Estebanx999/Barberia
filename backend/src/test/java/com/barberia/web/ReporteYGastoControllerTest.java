package com.barberia.web;

import com.barberia.reporte.*;
import com.barberia.servicio.gasto.GastoService;
import com.barberia.servicio.gasto.GastoVista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReporteYGastoControllerTest {

    private static final Periodo NOVIEMBRE = new Periodo(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    private final ReporteIngresos ingresos = mock(ReporteIngresos.class);
    private final ReporteGastos gastos = mock(ReporteGastos.class);
    private final ReporteUtilidadNeta utilidad = mock(ReporteUtilidadNeta.class);
    private final GastoService gastoService = mock(GastoService.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders
                .standaloneSetup(new ReporteController(ingresos, gastos, utilidad), new GastoController(gastoService))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void reporteDeIngresosRespondeOk() throws Exception {
        when(ingresos.generar(NOVIEMBRE)).thenReturn(new ReporteIngresos.Resultado(NOVIEMBRE, 3, new BigDecimal("75000")));

        mvc.perform(get("/api/reportes/ingresos").param("desde", "2026-11-01").param("hasta", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadCortes").value(3))
                .andExpect(jsonPath("$.ingresosBrutos").value(75000));
    }

    @Test
    void reporteDeGastosIncluyeElDetalle() throws Exception {
        when(gastos.generar(NOVIEMBRE)).thenReturn(new ReporteGastos.Resultado(NOVIEMBRE, new BigDecimal("80000"),
                List.of(new ReporteGastos.GastoPorConcepto("Insumos", 2, new BigDecimal("80000")))));

        mvc.perform(get("/api/reportes/gastos").param("desde", "2026-11-01").param("hasta", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalle[0].concepto").value("Insumos"));
    }

    @Test
    void reporteDeUtilidadNetaRespondeOk() throws Exception {
        when(utilidad.generar(NOVIEMBRE)).thenReturn(new ReporteUtilidadNeta.Resultado(NOVIEMBRE,
                new BigDecimal("100000"), new BigDecimal("48000"), new BigDecimal("30000"), new BigDecimal("22000")));

        mvc.perform(get("/api/reportes/utilidad-neta").param("desde", "2026-11-01").param("hasta", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilidadNeta").value(22000));
    }

    @Test
    void periodoInvertidoRespondeBadRequest() throws Exception {
        mvc.perform(get("/api/reportes/ingresos").param("desde", "2026-11-30").param("hasta", "2026-11-01"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(ingresos);
    }

    @Test
    void registrarGastoRespondeCreated() throws Exception {
        when(gastoService.registrar(any(), any(), any())).thenReturn(
                new GastoVista(4L, "Insumos", LocalDate.of(2026, 11, 10), new BigDecimal("50000")));

        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"concepto": "Insumos", "fecha": "2026-11-10", "valor": 50000}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void gastoIncompletoRespondeBadRequest() throws Exception {
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"concepto": "", "valor": 50000}
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(gastoService);
    }
}
