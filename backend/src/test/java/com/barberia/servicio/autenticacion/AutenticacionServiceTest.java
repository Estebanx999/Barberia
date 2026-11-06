package com.barberia.servicio.autenticacion;

import com.barberia.dominio.Barbero;
import com.barberia.dominio.Cliente;
import com.barberia.dominio.Rol;
import com.barberia.dominio.Usuario;
import com.barberia.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.barberia.soporte.DatosDePrueba.conId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutenticacionServiceTest {

    // Costo bajo para que las pruebas sean rápidas; en la aplicación se usa el valor por defecto.
    private final PasswordEncoder codificador = new BCryptPasswordEncoder(4);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final EmisorToken emisor = usuario -> "token-de-" + usuario.getId();

    private AutenticacionService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AutenticacionService(usuarios, codificador, emisor);
    }

    private Usuario conCuenta(Usuario usuario, String contrasena) {
        usuario.asignarContrasenaHash(codificador.encode(contrasena));
        return usuario;
    }

    // --- HU-21 ---

    @Test
    void registraAlClienteConLaContrasenaCifrada() {
        when(usuarios.existsByCorreo("juan@correo.com")).thenReturn(false);
        when(usuarios.save(any(Cliente.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0), 7));

        UsuarioVista vista = servicio.registrarCliente("Juan", " Juan@Correo.com ", "3001112233", "secreta123");

        assertEquals(7L, vista.id());
        assertEquals("juan@correo.com", vista.correo());
        assertEquals(Rol.CLIENTE, vista.rol());
        verify(usuarios).save(argThatTieneHash("secreta123"));
    }

    private Cliente argThatTieneHash(String contrasena) {
        return argThat(cliente -> cliente.getContrasenaHash() != null
                && !cliente.getContrasenaHash().equals(contrasena)
                && codificador.matches(contrasena, cliente.getContrasenaHash()));
    }

    @Test
    void noRegistraUnCorreoRepetido() {
        when(usuarios.existsByCorreo("juan@correo.com")).thenReturn(true);

        assertThrows(CorreoYaRegistradoException.class,
                () -> servicio.registrarCliente("Juan", "JUAN@correo.com", "300", "secreta123"));
        verify(usuarios, never()).save(any());
    }

    // --- HU-22, HU-23, HU-24 ---

    @Test
    void iniciaSesionConCredencialesValidas() {
        Usuario carlos = conCuenta(conId(new Barbero("Carlos", "carlos@barberia.com", null), 2), "clave1234");
        when(usuarios.findByCorreo("carlos@barberia.com")).thenReturn(Optional.of(carlos));

        SesionIniciada sesion = servicio.iniciarSesion("Carlos@Barberia.com", "clave1234");

        assertEquals("token-de-2", sesion.token());
        assertEquals(Rol.BARBERO, sesion.usuario().rol());
    }

    @Test
    void rechazaContrasenaIncorrectaOCorreoDesconocido() {
        Usuario carlos = conCuenta(conId(new Barbero("Carlos", "carlos@barberia.com", null), 2), "clave1234");
        when(usuarios.findByCorreo("carlos@barberia.com")).thenReturn(Optional.of(carlos));
        when(usuarios.findByCorreo("nadie@barberia.com")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("carlos@barberia.com", "otra"));
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("nadie@barberia.com", "clave1234"));
    }

    @Test
    void unClienteSinCuentaNoPuedeIniciarSesion() {
        // Cliente registrado por un barbero en un corte sin cita (HU-28): no tiene contraseña.
        Usuario sinCuenta = conId(new Cliente("Pedro", "pedro@correo.com", null), 9);
        when(usuarios.findByCorreo("pedro@correo.com")).thenReturn(Optional.of(sinCuenta));

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("pedro@correo.com", "x"));
    }
}
