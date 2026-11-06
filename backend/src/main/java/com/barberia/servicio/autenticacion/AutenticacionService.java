package com.barberia.servicio.autenticacion;

import com.barberia.dominio.Cliente;
import com.barberia.dominio.Usuario;
import com.barberia.repositorio.UsuarioRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-21: registro de clientes. HU-22, HU-23 y HU-24: inicio de sesión de dueño, barbero y cliente. */
@Service
public class AutenticacionService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final EmisorToken emisorToken;

    public AutenticacionService(UsuarioRepository usuarios, PasswordEncoder codificador, EmisorToken emisorToken) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emisorToken = emisorToken;
    }

    /** HU-21: crea la cuenta del cliente; rechaza un correo ya registrado. */
    @Transactional
    public UsuarioVista registrarCliente(String nombre, String correo, String telefono, String contrasena) {
        String correoNormalizado = Usuario.normalizarCorreo(correo);
        if (correoNormalizado == null) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (usuarios.existsByCorreo(correoNormalizado)) {
            throw new CorreoYaRegistradoException();
        }
        Cliente cliente = new Cliente(nombre, correoNormalizado, telefono);
        cliente.asignarContrasenaHash(codificador.encode(contrasena));
        return UsuarioVista.de(usuarios.save(cliente));
    }

    /** Valida las credenciales y entrega el token; el mismo mensaje de error para correo o contraseña. */
    @Transactional(readOnly = true)
    public SesionIniciada iniciarSesion(String correo, String contrasena) {
        Usuario usuario = usuarios.findByCorreo(Usuario.normalizarCorreo(correo))
                .filter(Usuario::tieneCuenta)
                .filter(encontrado -> codificador.matches(contrasena, encontrado.getContrasenaHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        return new SesionIniciada(emisorToken.emitir(usuario), UsuarioVista.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioVista obtener(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .map(UsuarioVista::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));
    }
}
