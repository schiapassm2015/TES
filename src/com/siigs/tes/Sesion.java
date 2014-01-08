package com.siigs.tes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAfiliacion;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.RegistroCivil;
import com.siigs.tes.datos.tablas.Tutor;
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
	
	private DatosPaciente datosPaciente=null; //El paciente en turno que recibe atención
	
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
	
	public boolean tienePermiso(int id_controlador_accion){
		return ContenidoControles.ExistePermiso(id_controlador_accion, permisos);
	}

	public DatosPaciente getDatosPacienteActual(){return datosPaciente;}
	public void setDatosPacienteNuevo(DatosPaciente datos){this.datosPaciente = datos;}
	
	
	/**
	 * Esta clase mantiene toda la información conocida de un paciente
	 * @author Axel
	 *
	 */
	public static class DatosPaciente{
		public Persona persona=null;
		public Tutor tutor=null;
		public RegistroCivil registroCivil=null;
		public List<PersonaAlergia> alergias=new ArrayList<PersonaAlergia>();
		public List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		public List<ControlVacuna> vacunas = new ArrayList<ControlVacuna>();
		public List<ControlIra> iras = new ArrayList<ControlIra>();
		public List<ControlEda> edas = new ArrayList<ControlEda>();
		public List<ControlConsulta> consultas = new ArrayList<ControlConsulta>();
		public List<ControlAccionNutricional> accionesNutricionales = new ArrayList<ControlAccionNutricional>();
		public List<ControlNutricional> controlesNutricionales = new ArrayList<ControlNutricional>();
		
		public DatosPaciente(Persona p, Tutor t, RegistroCivil rc, List<PersonaAlergia> alergias, 
				List<PersonaAfiliacion> afiliaciones, List<ControlVacuna> vacunas, List<ControlIra> iras,
				List<ControlEda> edas, List<ControlConsulta> consultas,
				List<ControlAccionNutricional> accionesNutricionales, 
				List<ControlNutricional> controlesNutricionales){
			persona = p;
			tutor = t;
			registroCivil = rc;
			this.alergias = alergias;
			this.afiliaciones = afiliaciones;
			this.vacunas = vacunas;
			this.iras = iras;
			this.edas = edas;
			this.consultas = consultas;
			this.accionesNutricionales = accionesNutricionales;
			this.controlesNutricionales = controlesNutricionales;
		}
		
	}//fin clase DatosPaciente
	
}//fin Sesion
