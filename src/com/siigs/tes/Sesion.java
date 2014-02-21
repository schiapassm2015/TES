package com.siigs.tes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.siigs.tes.controles.ContenidoControles;
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
		private static final String TAG = DatosPaciente.class.getSimpleName();
		
		private boolean cargadoDesdeNfc=true;
		
		public Persona persona=null;
		public Tutor tutor=null;
		public RegistroCivil registroCivil=null;
		public List<PersonaAlergia> alergias=new ArrayList<PersonaAlergia>();
		public List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		public List<ControlVacuna> vacunas = new ArrayList<ControlVacuna>();
		
		public static DatosPaciente cargarDesdeBaseDatos(Context context, int _idPersona){
			try{
				Persona paciente = Persona.getPersona(context, _idPersona);
				Tutor tutor = Tutor.getTutorDePersona(context, paciente.id);
				RegistroCivil registro = RegistroCivil.getRegistro(context, paciente.id);
				List<PersonaAlergia> alergias = PersonaAlergia.getAlergiasPersona(context, paciente.id);
				List<PersonaAfiliacion> afiliaciones = PersonaAfiliacion.getAfiliacionesPersona(context, paciente.id);
				List<ControlVacuna> vacunas = ControlVacuna.getVacunasPersona(context, paciente.id);
				
				return new DatosPaciente(paciente, tutor, registro, alergias, afiliaciones, vacunas, false);
			}catch(Exception e){
				Log.d(TAG, "No se pudo cargar historial de paciente desde base de datos con _id:"+_idPersona+"\n"+e);
				return null;
			}
		}
		
		public DatosPaciente(Persona p, Tutor t, RegistroCivil rc, List<PersonaAlergia> alergias, 
				List<PersonaAfiliacion> afiliaciones, List<ControlVacuna> vacunas, boolean cargadoDesdeNfc){
			persona = p;
			tutor = t;
			registroCivil = rc;
			this.alergias = alergias;
			this.afiliaciones = afiliaciones;
			this.vacunas = vacunas;
			this.cargadoDesdeNfc = cargadoDesdeNfc;
		}
		
		public boolean fueCargadoDesdeNfc(){return cargadoDesdeNfc;}
	}//fin clase DatosPaciente
	
}//fin Sesion
