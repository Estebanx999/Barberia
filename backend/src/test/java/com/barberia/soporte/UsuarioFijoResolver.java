package com.barberia.soporte;

import com.barberia.dominio.Rol;
import com.barberia.seguridad.UsuarioActual;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** En pruebas de controladores, entrega siempre el mismo usuario autenticado. */
public class UsuarioFijoResolver implements HandlerMethodArgumentResolver {

    private final UsuarioActual usuario;

    public UsuarioFijoResolver(Long id, Rol rol) {
        this.usuario = new UsuarioActual(id, rol);
    }

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return UsuarioActual.class.equals(parametro.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer modelo,
                                  NativeWebRequest solicitud, WebDataBinderFactory binderFactory) {
        return usuario;
    }
}
