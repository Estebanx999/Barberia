package com.barberia.web;

import com.barberia.dominio.Rol;
import com.barberia.servicio.autenticacion.*;
import com.barberia.soporte.UsuarioFijoResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacionControllerTest {

    private final AutenticacionService autenticacion = mock(AutenticacionService.class);
    private final UsuarioVista juan = new UsuarioVista(7L, "Juan", "juan@correo.com", "3001112233", Rol.CLIENTE);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new AutenticacionController(autenticacion))
                .setControllerAdvice(new ManejadorErrores())
                .setCustomArgumentResolvers(new UsuarioFijoResolver(7L, Rol.CLIENTE))
                .build();
    }

    private ResultActions postJson(String ruta, String cuerpo) throws Exception {
        return mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void registroRespondeCreatedSinDevolverLaContrasena() throws Exception {
        when(autenticacion.registrarCliente("Juan", "juan@correo.com", "3001112233", "secreta123")).thenReturn(juan);

        postJson("/api/auth/registro", """
                {"nombre": "Juan", "correo": "juan@correo.com", "telefono": "3001112233", "contrasena": "secreta123"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void registroConContrasenaCortaOCorreoInvalidoRespondeBadRequest() throws Exception {
        postJson("/api/auth/registro", """
                {"nombre": "Juan", "correo": "juan@correo.com", "telefono": "300", "contrasena": "corta"}
                """).andExpect(status().isBadRequest());
        postJson("/api/auth/registro", """
                {"nombre": "Juan", "correo": "no-es-correo", "telefono": "300", "contrasena": "secreta123"}
                """).andExpect(status().isBadRequest());
        verifyNoInteractions(autenticacion);
    }

    @Test
    void registroConCorreoExistenteRespondeConflict() throws Exception {
        when(autenticacion.registrarCliente(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new CorreoYaRegistradoException());

        postJson("/api/auth/registro", """
                {"nombre": "Juan", "correo": "juan@correo.com", "telefono": "300", "contrasena": "secreta123"}
                """).andExpect(status().isConflict());
    }

    @Test
    void loginDevuelveElTokenYElUsuario() throws Exception {
        when(autenticacion.iniciarSesion("juan@correo.com", "secreta123")).thenReturn(new SesionIniciada("abc", juan));

        postJson("/api/auth/login", """
                {"correo": "juan@correo.com", "contrasena": "secreta123"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc"))
                .andExpect(jsonPath("$.usuario.nombre").value("Juan"));
    }

    @Test
    void loginConCredencialesInvalidasRespondeUnauthorized() throws Exception {
        when(autenticacion.iniciarSesion(anyString(), anyString())).thenThrow(new CredencialesInvalidasException());

        postJson("/api/auth/login", """
                {"correo": "juan@correo.com", "contrasena": "mala"}
                """).andExpect(status().isUnauthorized());
    }

    @Test
    void yoDevuelveAlUsuarioAutenticado() throws Exception {
        when(autenticacion.obtener(7L)).thenReturn(juan);
        mvc.perform(get("/api/auth/yo")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
    }
}
