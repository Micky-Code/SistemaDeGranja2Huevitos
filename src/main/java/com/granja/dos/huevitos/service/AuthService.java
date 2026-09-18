/**
 * 
 */
package com.granja.dos.huevitos.service;

import org.springframework.security.core.Authentication;

import com.granja.dos.huevitos.dto.LoginRequest;
import com.granja.dos.huevitos.dto.UsuarioSesionResponse;

/**
 * @author WChui
 */
public interface AuthService {
	
	public Authentication autenticar(LoginRequest request);
	
	public UsuarioSesionResponse usuarioActual(String username);

}
