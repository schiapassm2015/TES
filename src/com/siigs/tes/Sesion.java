package com.siigs.tes;

import java.util.Calendar;
import java.util.List;

import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;

/**
 * Describe atributos de una sesión de uso en la aplicación
 * @author Axel
 *
 */
public class Sesion {

	private Usuario usuario;
	private UsuarioInvitado invitado; //Si es invitado, aquí lo indica
	private List<Permiso> permisos; //Si existe invitado, los permisos deben ser del grupo invitado
	private Calendar fechaInicio;
	
	public Sesion(Usuario usuario, UsuarioInvitado invitado, List<Permiso> permisos) {
		this.usuario = usuario;
		this.invitado = invitado;
		this.permisos = permisos;
		this.fechaInicio = Calendar.getInstance();
	}
	
	public Usuario getUsuario(){return usuario;}
	public List<Permiso> getPermisos(){return permisos;}
	public Calendar getFechaInicio(){return fechaInicio;}
	public UsuarioInvitado getUsuarioInvitado(){return invitado;}

}
