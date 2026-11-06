package com.barberia.seguridad;

import com.barberia.dominio.Rol;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Construye el UsuarioActual de cada solicitud a partir del token validado. */
@Component
public class UsuarioActualResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return UsuarioActual.class.equals(parametro.getParameterType());
    }

    @Override
    public UsuarioActual resolveArgument(MethodParameter parametro, ModelAndViewContainer modelo,
                                         NativeWebRequest solicitud, WebDataBinderFactory binderFactory) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Jwt token)) {
            throw new AuthenticationCredentialsNotFoundException("La solicitud no tiene un usuario autenticado");
        }
        return new UsuarioActual(Long.valueOf(token.getSubject()),
                Rol.valueOf(token.getClaimAsString(SeguridadConfig.CLAIM_ROL)));
    }
}
