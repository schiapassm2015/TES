package com.siigs.tes.datos;


import java.util.ArrayList;

import com.siigs.tes.datos.tablas.*;
import com.siigs.tes.datos.vistas.Censo;
import com.siigs.tes.datos.vistas.EsquemasIncompletos;
import com.siigs.tes.datos.vistas.ReportesVacunas;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Axel
 * Principal Proveedor de contenido de datos de la aplicaci�n. 
 * Contiene los uri's para el acceso a las distintas funciones de datos.  
 */
public class ProveedorContenido extends ContentProvider {

	private BaseDatos basedatos;

	//CONSTANTES
	private static final String TAG = ProveedorContenido.class.getSimpleName();
	private static final String AUTHORITY = "com.siigs.tes.datos.ProveedorContenido";//ProveedorContenido.class.getName();
	
	private static final String PERSONA_PATH = Persona.NOMBRE_TABLA;
	public static final int PERSONA_TODOS = 100;
	public static final int PERSONA_ID = 110;
	public static final Uri PERSONA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PERSONA_PATH);
	
	private static final String USUARIO_PATH = Usuario.NOMBRE_TABLA;
	public static final int USUARIO_TODOS = 200;
	public static final int USUARIO_ID = 210;
	public static final Uri USUARIO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + USUARIO_PATH);
	
	private static final String USUARIO_INVITADO_PATH = UsuarioInvitado.NOMBRE_TABLA;
	public static final int USUARIO_INVITADO_TODOS = 220;
	public static final int USUARIO_INVITADO_ID = 221;
	public static final Uri USUARIO_INVITADO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + USUARIO_INVITADO_PATH);
	
	private static final String GRUPO_PATH = Grupo.NOMBRE_TABLA;
	public static final int GRUPO_TODOS = 300;
	public static final int GRUPO_ID = 310;
	public static final Uri GRUPO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + GRUPO_PATH);
	
	private static final String PERMISO_PATH = Permiso.NOMBRE_TABLA;
	public static final int PERMISO_TODOS = 400;
	public static final int PERMISO_ID = 410;
	public static final Uri PERMISO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PERMISO_PATH);
	
	private static final String BITACORA_PATH = Bitacora.NOMBRE_TABLA;
	public static final int BITACORA_TODOS = 500;
	public static final int BITACORA_ID = 510;
	public static final Uri BITACORA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + BITACORA_PATH);
	
	private static final String ERROR_SIS_PATH = ErrorSis.NOMBRE_TABLA;
	public static final int ERROR_SIS_TODOS = 600;
	public static final int ERROR_SIS_ID = 610;
	public static final Uri ERROR_SIS_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ERROR_SIS_PATH);
	
	private static final String CONTROL_VACUNA_PATH = ControlVacuna.NOMBRE_TABLA;
	public static final int CONTROL_VACUNA_TODOS = 700;
	public static final int CONTROL_VACUNA_ID = 710;
	public static final Uri CONTROL_VACUNA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CONTROL_VACUNA_PATH);
	
	private static final String VACUNA_PATH = Vacuna.NOMBRE_TABLA;
	public static final int VACUNA_TODOS = 720;
	public static final int VACUNA_ID = 721;
	public static final Uri VACUNA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + VACUNA_PATH);
	
	private static final String NACIONALIDAD_PATH = Nacionalidad.NOMBRE_TABLA;
	public static final int NACIONALIDAD_TODOS = 10;
	public static final int NACIONALIDAD_ID = 11;
	public static final Uri NACIONALIDAD_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + NACIONALIDAD_PATH);
	
	private static final String REGISTRO_CIVIL_PATH = RegistroCivil.NOMBRE_TABLA;
	public static final int REGISTRO_CIVIL_TODOS = 20;
	public static final int REGISTRO_CIVIL_ID = 21;
	public static final Uri REGISTRO_CIVIL_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + REGISTRO_CIVIL_PATH);
	
	private static final String TIPO_SANGUINEO_PATH = TipoSanguineo.NOMBRE_TABLA;
	public static final int TIPO_SANGUINEO_TODOS = 30;
	public static final int TIPO_SANGUINEO_ID = 31;
	public static final Uri TIPO_SANGUINEO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + TIPO_SANGUINEO_PATH);
	
	private static final String ANTIGUO_DOMICILIO_PATH = AntiguoDomicilio.NOMBRE_TABLA;
	public static final int ANTIGUO_DOMICILIO_TODOS = 40;
	public static final int ANTIGUO_DOMICILIO_ID = 41;
	public static final Uri ANTIGUO_DOMICILIO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ANTIGUO_DOMICILIO_PATH);
	
	private static final String ANTIGUA_UM_PATH = AntiguaUM.NOMBRE_TABLA;
	public static final int ANTIGUA_UM_TODOS = 50;
	public static final int ANTIGUA_UM_ID = 51;
	public static final Uri ANTIGUA_UM_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ANTIGUA_UM_PATH);
	
	private static final String PERSONA_AFILIACION_PATH = PersonaAfiliacion.NOMBRE_TABLA;
	public static final int PERSONA_AFILIACION_TODOS = 60;
	public static final int PERSONA_AFILIACION_ID = 61;
	public static final Uri PERSONA_AFILIACION_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PERSONA_AFILIACION_PATH);
	
	private static final String AFILIACION_PATH = Afiliacion.NOMBRE_TABLA;
	public static final int AFILIACION_TODOS = 65;
	public static final int AFILIACION_ID = 66;
	public static final Uri AFILIACION_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + AFILIACION_PATH);
	
	private static final String PERSONA_ALERGIA_PATH = PersonaAlergia.NOMBRE_TABLA;
	public static final int PERSONA_ALERGIA_TODOS = 80;
	public static final int PERSONA_ALERGIA_ID = 81;
	public static final Uri PERSONA_ALERGIA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PERSONA_ALERGIA_PATH);
	
	private static final String ALERGIA_PATH = Alergia.NOMBRE_TABLA;
	public static final int ALERGIA_TODOS = 85;
	public static final int ALERGIA_ID = 86;
	public static final Uri ALERGIA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ALERGIA_PATH);
	
	private static final String PERSONA_TUTOR_PATH = PersonaTutor.NOMBRE_TABLA;
	public static final int PERSONA_TUTOR_TODOS = 90;
	public static final int PERSONA_TUTOR_ID = 91;
	public static final Uri PERSONA_TUTOR_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PERSONA_TUTOR_PATH);
	
	private static final String TUTOR_PATH = Tutor.NOMBRE_TABLA;
	public static final int TUTOR_TODOS = 95;
	public static final int TUTOR_ID = 96;
	public static final Uri TUTOR_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + TUTOR_PATH);
	
	private static final String PENDIENTES_TARJETA_PATH = PendientesTarjeta.NOMBRE_TABLA;
	public static final int PENDIENTES_TARJETA_TODOS = 1200;
	public static final int PENDIENTES_TARJETA_ID = 1201;
	public static final Uri PENDIENTES_TARJETA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PENDIENTES_TARJETA_PATH);
	
	private static final String NOTIFICACION_PATH = Notificacion.NOMBRE_TABLA;
	public static final int NOTIFICACION_TODOS = 1300;
	public static final int NOTIFICACION_ID = 1301;
	public static final Uri NOTIFICACION_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + NOTIFICACION_PATH);
	
	private static final String ARBOL_SEGMENTACION_PATH = ArbolSegmentacion.NOMBRE_TABLA;
	public static final int ARBOL_SEGMENTACION_TODOS = 1400;
	public static final int ARBOL_SEGMENTACION_ID = 1401;
	public static final Uri ARBOL_SEGMENTACION_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ARBOL_SEGMENTACION_PATH);
	
	private static final String OPERADORA_CELULAR_PATH = OperadoraCelular.NOMBRE_TABLA;
	public static final int OPERADORA_CELULAR_TODOS = 1500;
	public static final int OPERADORA_CELULAR_ID = 1501;
	public static final Uri OPERADORA_CELULAR_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + OPERADORA_CELULAR_PATH);
	
	private static final String REGLA_VACUNA_PATH = ReglaVacuna.NOMBRE_TABLA;
	public static final int REGLA_VACUNA_TODOS = 1600;
	public static final int REGLA_VACUNA_ID = 1601;
	public static final Uri REGLA_VACUNA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + REGLA_VACUNA_PATH);
	
	private static final String VIA_VACUNA_PATH = ViaVacuna.NOMBRE_TABLA;
	public static final int VIA_VACUNA_TODOS = 1610;
	public static final int VIA_VACUNA_ID = 1620;
	public static final Uri VIA_VACUNA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + VIA_VACUNA_PATH);
	
	private static final String ESQUEMA_INCOMPLETO_PATH = EsquemaIncompleto.NOMBRE_TABLA;
	public static final int ESQUEMA_INCOMPLETO_TODOS = 1700;
	public static final int ESQUEMA_INCOMPLETO_ID = 1701;
	public static final Uri ESQUEMA_INCOMPLETO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ESQUEMA_INCOMPLETO_PATH);
	
	//VISTAS
	private static final String CENSO_PATH = "censo";
	public static final int CENSO_TODOS = 1800;
	public static final Uri CENSO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CENSO_PATH);
	
	private static final String VISTA_ESQUEMA_INCOMPLETO_PATH = EsquemasIncompletos.NOMBRE_VISTA;
	public static final int VISTA_ESQUEMA_INCOMPLETO_TODOS = 1810;
	public static final Uri VISTA_ESQUEMA_INCOMPLETO_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + VISTA_ESQUEMA_INCOMPLETO_PATH);
	
	private static final String REPORTE_VACUNAS_PATH = "reporte_vacunas";
	public static final int REPORTE_VACUNAS_TODOS = 1820;
	public static final Uri REPORTE_VACUNAS_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + REPORTE_VACUNAS_PATH);
	
	
	//UriMatcher
	private static final UriMatcher sURIMatcher = new UriMatcher(
	        UriMatcher.NO_MATCH);
	static {
	    sURIMatcher.addURI(AUTHORITY, PERSONA_PATH, PERSONA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PERSONA_PATH + "/#", PERSONA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, USUARIO_PATH, USUARIO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, USUARIO_PATH + "/#", USUARIO_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, USUARIO_INVITADO_PATH, USUARIO_INVITADO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, USUARIO_INVITADO_PATH + "/#", USUARIO_INVITADO_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, GRUPO_PATH, GRUPO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, GRUPO_PATH + "/#", GRUPO_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, PERMISO_PATH, PERMISO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PERMISO_PATH + "/#", PERMISO_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, BITACORA_PATH, BITACORA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, BITACORA_PATH + "/#", BITACORA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, ERROR_SIS_PATH, ERROR_SIS_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ERROR_SIS_PATH + "/#", ERROR_SIS_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, CONTROL_VACUNA_PATH, CONTROL_VACUNA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, CONTROL_VACUNA_PATH + "/#", CONTROL_VACUNA_ID);
	    sURIMatcher.addURI(AUTHORITY, VACUNA_PATH, VACUNA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, VACUNA_PATH + "/#", VACUNA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, NACIONALIDAD_PATH, NACIONALIDAD_TODOS);
	    sURIMatcher.addURI(AUTHORITY, NACIONALIDAD_PATH + "/#", NACIONALIDAD_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, REGISTRO_CIVIL_PATH, REGISTRO_CIVIL_TODOS);
	    sURIMatcher.addURI(AUTHORITY, REGISTRO_CIVIL_PATH + "/#", REGISTRO_CIVIL_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, TIPO_SANGUINEO_PATH, TIPO_SANGUINEO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, TIPO_SANGUINEO_PATH + "/#", TIPO_SANGUINEO_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, ANTIGUO_DOMICILIO_PATH, ANTIGUO_DOMICILIO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ANTIGUO_DOMICILIO_PATH + "/#", ANTIGUO_DOMICILIO_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, ANTIGUA_UM_PATH, ANTIGUA_UM_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ANTIGUA_UM_PATH + "/#", ANTIGUA_UM_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, PERSONA_AFILIACION_PATH, PERSONA_AFILIACION_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PERSONA_AFILIACION_PATH + "/#", PERSONA_AFILIACION_ID);
	    sURIMatcher.addURI(AUTHORITY, AFILIACION_PATH, AFILIACION_TODOS);
	    sURIMatcher.addURI(AUTHORITY, AFILIACION_PATH + "/#", AFILIACION_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, PERSONA_ALERGIA_PATH, PERSONA_ALERGIA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PERSONA_ALERGIA_PATH + "/#", PERSONA_ALERGIA_ID);
	    sURIMatcher.addURI(AUTHORITY, ALERGIA_PATH, ALERGIA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ALERGIA_PATH + "/#", ALERGIA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, PERSONA_TUTOR_PATH, PERSONA_TUTOR_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PERSONA_TUTOR_PATH + "/#", PERSONA_TUTOR_ID);
	    sURIMatcher.addURI(AUTHORITY, TUTOR_PATH, TUTOR_TODOS);
	    sURIMatcher.addURI(AUTHORITY, TUTOR_PATH + "/#", TUTOR_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, PENDIENTES_TARJETA_PATH, PENDIENTES_TARJETA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PENDIENTES_TARJETA_PATH + "/#", PENDIENTES_TARJETA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, NOTIFICACION_PATH, NOTIFICACION_TODOS);
	    sURIMatcher.addURI(AUTHORITY, NOTIFICACION_PATH + "/#", NOTIFICACION_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, ARBOL_SEGMENTACION_PATH, ARBOL_SEGMENTACION_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ARBOL_SEGMENTACION_PATH + "/#", ARBOL_SEGMENTACION_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, OPERADORA_CELULAR_PATH, OPERADORA_CELULAR_TODOS);
	    sURIMatcher.addURI(AUTHORITY, OPERADORA_CELULAR_PATH + "/#", OPERADORA_CELULAR_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, REGLA_VACUNA_PATH, REGLA_VACUNA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, REGLA_VACUNA_PATH + "/#", REGLA_VACUNA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, VIA_VACUNA_PATH, VIA_VACUNA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, VIA_VACUNA_PATH + "/#", VIA_VACUNA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, ESQUEMA_INCOMPLETO_PATH, ESQUEMA_INCOMPLETO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ESQUEMA_INCOMPLETO_PATH + "/#", ESQUEMA_INCOMPLETO_ID);
	    
	    //VISTAS
	    sURIMatcher.addURI(AUTHORITY, CENSO_PATH, CENSO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, VISTA_ESQUEMA_INCOMPLETO_PATH, VISTA_ESQUEMA_INCOMPLETO_TODOS);
	    sURIMatcher.addURI(AUTHORITY, REPORTE_VACUNAS_PATH, REPORTE_VACUNAS_TODOS);
	}
	
	
	@Override
	public boolean onCreate() {
		Log.d(TAG, "Creando proveedor de contenido");
		this.basedatos = new BaseDatos(this.getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		String[] parametros= selectionArgs;
		
		String groupBy = null;
		
		int tipoUri= ProveedorContenido.sURIMatcher.match(uri);
		switch(tipoUri){
		
		case ProveedorContenido.PERSONA_ID:
			builder.setTables(Persona.NOMBRE_TABLA);
			builder.appendWhere(Persona.ID + "=?"); //+DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()) );
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.PERSONA_TODOS:
			builder.setTables(Persona.NOMBRE_TABLA);// No existe filtro
			break;
		
		case ProveedorContenido.USUARIO_ID:
			builder.setTables(Usuario.NOMBRE_TABLA);
			builder.appendWhere(Usuario.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.USUARIO_TODOS:
			builder.setTables(Usuario.NOMBRE_TABLA);// No existe filtro
			break;
		
		case ProveedorContenido.USUARIO_INVITADO_ID:
			builder.setTables(UsuarioInvitado.NOMBRE_TABLA);
			builder.appendWhere(UsuarioInvitado.ID_INVITADO + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.USUARIO_INVITADO_TODOS:
			builder.setTables(UsuarioInvitado.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.GRUPO_ID:
			builder.setTables(Grupo.NOMBRE_TABLA);
			builder.appendWhere(Grupo.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.GRUPO_TODOS:
			builder.setTables(Grupo.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.PERMISO_ID:
			builder.setTables(Permiso.NOMBRE_TABLA);
			builder.appendWhere(Permiso.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.PERMISO_TODOS:
			builder.setTables(Permiso.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.BITACORA_ID:
			builder.setTables(Bitacora.NOMBRE_TABLA);
			builder.appendWhere(Bitacora.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.BITACORA_TODOS:
			builder.setTables(Bitacora.NOMBRE_TABLA);// No existe filtro
			break;
		
		case ProveedorContenido.ERROR_SIS_ID:
			builder.setTables(ErrorSis.NOMBRE_TABLA);
			builder.appendWhere(ErrorSis.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.ERROR_SIS_TODOS:
			builder.setTables(ErrorSis.NOMBRE_TABLA);// No existe filtro
			break;
			
		//VACUNAS
		case ProveedorContenido.CONTROL_VACUNA_ID:
			builder.setTables(ControlVacuna.NOMBRE_TABLA);
			builder.appendWhere(ControlVacuna._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.CONTROL_VACUNA_TODOS:
			builder.setTables(ControlVacuna.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.VACUNA_ID:
			builder.setTables(Vacuna.NOMBRE_TABLA);
			builder.appendWhere(Vacuna.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.VACUNA_TODOS:
			builder.setTables(Vacuna.NOMBRE_TABLA);// No existe filtro
			break;		
			
		case ProveedorContenido.NACIONALIDAD_ID:
			builder.setTables(Nacionalidad.NOMBRE_TABLA);
			builder.appendWhere(Nacionalidad.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.NACIONALIDAD_TODOS:
			builder.setTables(Nacionalidad.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.REGISTRO_CIVIL_ID:
			builder.setTables(RegistroCivil.NOMBRE_TABLA);
			builder.appendWhere(RegistroCivil.ID_PERSONA + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.REGISTRO_CIVIL_TODOS:
			builder.setTables(RegistroCivil.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.TIPO_SANGUINEO_ID:
			builder.setTables(TipoSanguineo.NOMBRE_TABLA);
			builder.appendWhere(TipoSanguineo.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.TIPO_SANGUINEO_TODOS:
			builder.setTables(TipoSanguineo.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.ANTIGUO_DOMICILIO_ID:
			builder.setTables(AntiguoDomicilio.NOMBRE_TABLA);
			builder.appendWhere(AntiguoDomicilio._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.ANTIGUO_DOMICILIO_TODOS:
			builder.setTables(AntiguoDomicilio.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.ANTIGUA_UM_ID:
			builder.setTables(AntiguaUM.NOMBRE_TABLA);
			builder.appendWhere(AntiguaUM._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.ANTIGUA_UM_TODOS:
			builder.setTables(AntiguaUM.NOMBRE_TABLA);// No existe filtro
			break;
			
		//AFILIACIONES
		case ProveedorContenido.PERSONA_AFILIACION_ID:
			builder.setTables(PersonaAfiliacion.NOMBRE_TABLA);
			builder.appendWhere(PersonaAfiliacion._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.PERSONA_AFILIACION_TODOS:
			builder.setTables(PersonaAfiliacion.NOMBRE_TABLA);// No existe filtro
			break;

		case ProveedorContenido.AFILIACION_ID:
			builder.setTables(Afiliacion.NOMBRE_TABLA);
			builder.appendWhere(Afiliacion.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.AFILIACION_TODOS:
			builder.setTables(Afiliacion.NOMBRE_TABLA);// No existe filtro
			break;
			
		//ALERGIAS	
		case ProveedorContenido.PERSONA_ALERGIA_ID:
			builder.setTables(PersonaAlergia.NOMBRE_TABLA);
			builder.appendWhere(PersonaAlergia._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.PERSONA_ALERGIA_TODOS:
			builder.setTables(PersonaAlergia.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.ALERGIA_ID:
			builder.setTables(Alergia.NOMBRE_TABLA);
			builder.appendWhere(Alergia.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.ALERGIA_TODOS:
			builder.setTables(Alergia.NOMBRE_TABLA);// No existe filtro
			break;
			
			
		case ProveedorContenido.PERSONA_TUTOR_ID:
			builder.setTables(PersonaTutor.NOMBRE_TABLA);
			builder.appendWhere(PersonaTutor.ID_TUTOR + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.PERSONA_TUTOR_TODOS:
			builder.setTables(PersonaTutor.NOMBRE_TABLA);// No existe filtro
			break;
		
		case ProveedorContenido.TUTOR_ID:
			builder.setTables(Tutor.NOMBRE_TABLA);
			builder.appendWhere(Tutor.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.TUTOR_TODOS:
			builder.setTables(Tutor.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.PENDIENTES_TARJETA_TODOS:
			builder.setTables(PendientesTarjeta.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.NOTIFICACION_ID:
			builder.setTables(Notificacion.NOMBRE_TABLA);
			builder.appendWhere(Notificacion.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.NOTIFICACION_TODOS:
			builder.setTables(Notificacion.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.ARBOL_SEGMENTACION_ID:
			builder.setTables(ArbolSegmentacion.NOMBRE_TABLA);
			builder.appendWhere(ArbolSegmentacion.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.ARBOL_SEGMENTACION_TODOS:
			builder.setTables(ArbolSegmentacion.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.OPERADORA_CELULAR_ID:
			builder.setTables(OperadoraCelular.NOMBRE_TABLA);
			builder.appendWhere(OperadoraCelular.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.OPERADORA_CELULAR_TODOS:
			builder.setTables(OperadoraCelular.NOMBRE_TABLA);// No existe filtro
			break;
		
		case ProveedorContenido.REGLA_VACUNA_ID:
			builder.setTables(ReglaVacuna.NOMBRE_TABLA);
			builder.appendWhere(ReglaVacuna.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.REGLA_VACUNA_TODOS:
			builder.setTables(ReglaVacuna.NOMBRE_TABLA);// No existe filtro
			break;
		
		case ProveedorContenido.VIA_VACUNA_ID:
			builder.setTables(ViaVacuna.NOMBRE_TABLA);
			builder.appendWhere(ViaVacuna.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.VIA_VACUNA_TODOS:
			builder.setTables(ViaVacuna.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.ESQUEMA_INCOMPLETO_TODOS:
			builder.setTables(EsquemaIncompleto.NOMBRE_TABLA);// No existe filtro
			break;
		
			//VISTAS
			
		case ProveedorContenido.CENSO_TODOS:
			projection = Censo.COLUMNAS;
			builder.setTables(Censo.TABLAS);
			break;
		case ProveedorContenido.VISTA_ESQUEMA_INCOMPLETO_TODOS:
			//projection = EsquemasIncompletos.COLUMNAS;
			builder.setTables(EsquemasIncompletos.NOMBRE_VISTA);
			//selection = EsquemasIncompletos.WHERE + (selection==null? "": " AND "+selection);
			break;
		case ProveedorContenido.REPORTE_VACUNAS_TODOS:
			projection = ReportesVacunas.COLUMNAS;
			builder.setTables(ReportesVacunas.TABLAS);
			groupBy = ReportesVacunas.GROUPBY;
			break;
		default:
			throw new IllegalArgumentException("Uri desconocido "+tipoUri);
		}
		
		//Contin�a consulta con o sin par�metros seg�n uri
		Cursor cur= builder.query(this.basedatos.getReadableDatabase(), 
				projection, selection, parametros, groupBy, null, sortOrder);
		cur.setNotificationUri(this.getContext().getContentResolver(), uri);
		return cur;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int tipoUri= sURIMatcher.match(uri);
		SQLiteDatabase db=this.basedatos.getWritableDatabase();
		int afectadas=0;
		//helpers
		String tabla="";
		String where="1";
		//id solicitado a modificar
		String id= uri.getLastPathSegment();
		if(!TextUtils.isEmpty(id))
			id = DatabaseUtils.sqlEscapeString(id);
		
		switch(tipoUri){
		case ProveedorContenido.PERSONA_TODOS:
			tabla=Persona.NOMBRE_TABLA;
			break;
		case ProveedorContenido.USUARIO_TODOS:
			tabla=Usuario.NOMBRE_TABLA;
			break;
		case ProveedorContenido.USUARIO_INVITADO_TODOS:
			tabla=UsuarioInvitado.NOMBRE_TABLA;
			break;
		case ProveedorContenido.TUTOR_TODOS:
			tabla=Tutor.NOMBRE_TABLA;
			break;
		case ProveedorContenido.GRUPO_TODOS:
			tabla=Grupo.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERMISO_TODOS:
			tabla=Permiso.NOMBRE_TABLA;
			break;
		case ProveedorContenido.NOTIFICACION_TODOS:
			tabla=Notificacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.TIPO_SANGUINEO_TODOS:
			tabla=TipoSanguineo.NOMBRE_TABLA;
			break;
		case ProveedorContenido.VACUNA_TODOS:
			tabla=Vacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ALERGIA_TODOS:
			tabla=Alergia.NOMBRE_TABLA;
			break;
		case ProveedorContenido.AFILIACION_TODOS:
			tabla=Afiliacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.NACIONALIDAD_TODOS:
			tabla=Nacionalidad.NOMBRE_TABLA;
			break;
		case ProveedorContenido.OPERADORA_CELULAR_TODOS:
			tabla=OperadoraCelular.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PENDIENTES_TARJETA_TODOS:
			tabla=PendientesTarjeta.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ARBOL_SEGMENTACION_TODOS:
			tabla=ArbolSegmentacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_TUTOR_TODOS:
			tabla=PersonaTutor.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ANTIGUA_UM_TODOS:
			tabla=AntiguaUM.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ANTIGUO_DOMICILIO_TODOS:
			tabla=AntiguoDomicilio.NOMBRE_TABLA;
			break;
		case ProveedorContenido.REGISTRO_CIVIL_TODOS:
			tabla=RegistroCivil.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_ALERGIA_TODOS:
			tabla=PersonaAlergia.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_AFILIACION_TODOS:
			tabla=PersonaAfiliacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.CONTROL_VACUNA_TODOS:
			tabla=ControlVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.REGLA_VACUNA_TODOS:
			tabla=ReglaVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.VIA_VACUNA_TODOS:
			tabla=ViaVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ESQUEMA_INCOMPLETO_TODOS:
			tabla=EsquemaIncompleto.NOMBRE_TABLA;
			break;
		
		default:
			throw new IllegalArgumentException("Uri desconocido "+uri);
		}//fin casos
		
		if(!TextUtils.isEmpty(selection))
			where += " and " + selection;
		
		//actualizamos registros
		try{
			afectadas = db.delete(tabla, where, selectionArgs);
			if(afectadas>0)
				this.getContext().getContentResolver().notifyChange(uri, null);
			return afectadas;
		}catch(SQLiteConstraintException ex){
			Log.i(TAG, "Error de SQLite haciendo DELETE de uri "+uri+ ", con where= "+where);
		}
		
		return 0;
	}//fin delete

	@Override
	public String getType(Uri uri) {
		return null;
	}

	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int tipoUri= sURIMatcher.match(uri);
		SQLiteDatabase db=this.basedatos.getWritableDatabase();
		long newID=0;
		String tabla="";
		
		switch(tipoUri){
		case ProveedorContenido.PERSONA_TODOS:
			tabla=Persona.NOMBRE_TABLA;
			break;
		case ProveedorContenido.USUARIO_TODOS:
			tabla=Usuario.NOMBRE_TABLA;
			break;
		case ProveedorContenido.USUARIO_INVITADO_TODOS:
			tabla=UsuarioInvitado.NOMBRE_TABLA;
			break;
		case ProveedorContenido.TUTOR_TODOS:
			tabla=Tutor.NOMBRE_TABLA;
			break;
		case ProveedorContenido.GRUPO_TODOS:
			tabla=Grupo.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERMISO_TODOS:
			tabla=Permiso.NOMBRE_TABLA;
			break;
		case ProveedorContenido.NOTIFICACION_TODOS:
			tabla=Notificacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.TIPO_SANGUINEO_TODOS:
			tabla=TipoSanguineo.NOMBRE_TABLA;
			break;
		case ProveedorContenido.VACUNA_TODOS:
			tabla=Vacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ALERGIA_TODOS:
			tabla=Alergia.NOMBRE_TABLA;
			break;
		case ProveedorContenido.AFILIACION_TODOS:
			tabla=Afiliacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.NACIONALIDAD_TODOS:
			tabla=Nacionalidad.NOMBRE_TABLA;
			break;
		case ProveedorContenido.OPERADORA_CELULAR_TODOS:
			tabla=OperadoraCelular.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PENDIENTES_TARJETA_TODOS:
			tabla=PendientesTarjeta.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ARBOL_SEGMENTACION_TODOS:
			tabla=ArbolSegmentacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_TUTOR_TODOS:
			tabla=PersonaTutor.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ANTIGUA_UM_TODOS:
			tabla=AntiguaUM.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ANTIGUO_DOMICILIO_TODOS:
			tabla=AntiguoDomicilio.NOMBRE_TABLA;
			break;
		case ProveedorContenido.REGISTRO_CIVIL_TODOS:
			tabla=RegistroCivil.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_ALERGIA_TODOS:
			tabla=PersonaAlergia.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_AFILIACION_TODOS:
			tabla=PersonaAfiliacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.CONTROL_VACUNA_TODOS:
			tabla=ControlVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.REGLA_VACUNA_TODOS:
			tabla=ReglaVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.VIA_VACUNA_TODOS:
			tabla=ViaVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ESQUEMA_INCOMPLETO_TODOS:
			tabla=EsquemaIncompleto.NOMBRE_TABLA;
			break;
		case ProveedorContenido.BITACORA_TODOS:
			tabla=Bitacora.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ERROR_SIS_TODOS:
			tabla=ErrorSis.NOMBRE_TABLA;
			break;
			
		default:
			throw new IllegalArgumentException("Uri desconocido "+uri);
		}//fin casos
		
		//Insertamos registro
		try{
			newID=db.insertOrThrow(tabla, null, values);
			if(newID>0){
				Uri newUri=ContentUris.withAppendedId(uri, newID);
				this.getContext().getContentResolver().notifyChange(uri, null);
				return newUri;
			}else{ throw new SQLException("Fall� al insertar fila en "+uri);}
		}catch(SQLiteConstraintException ex){
			Log.i(TAG, "Ignorando fila repetida "+uri+ ", "+values);
		}
		
		return null;
	}//fin insert

	
	
	@Override
	/**
	 * Genera acciones UPDATE en base de datos seg�n el tipo de uri. 
	 */
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int tipoUri= sURIMatcher.match(uri);
		SQLiteDatabase db=this.basedatos.getWritableDatabase();
		int afectadas=0;
		//helpers
		String tabla="";
		String where="1";
		//id solicitado a modificar
		String id= uri.getLastPathSegment();
		if(!TextUtils.isEmpty(id))
			id = DatabaseUtils.sqlEscapeString(id);
		
		switch(tipoUri){
		case ProveedorContenido.PERSONA_TODOS:
			tabla=Persona.NOMBRE_TABLA;
			break;
		//case ProveedorContenido.PERSONA_ID:
		//	tabla=Persona.NOMBRE_TABLA;
		//	where = Persona.ID + "=" + id;
		//	break;
		case ProveedorContenido.USUARIO_TODOS:
			tabla=Usuario.NOMBRE_TABLA;
			break;
		case ProveedorContenido.USUARIO_INVITADO_TODOS:
			tabla=UsuarioInvitado.NOMBRE_TABLA;
			break;
		case ProveedorContenido.TUTOR_TODOS:
			tabla=Tutor.NOMBRE_TABLA;
			break;
		case ProveedorContenido.GRUPO_TODOS:
			tabla=Grupo.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERMISO_TODOS:
			tabla=Permiso.NOMBRE_TABLA;
			break;
		case ProveedorContenido.NOTIFICACION_TODOS:
			tabla=Notificacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.TIPO_SANGUINEO_TODOS:
			tabla=TipoSanguineo.NOMBRE_TABLA;
			break;
		case ProveedorContenido.VACUNA_TODOS:
			tabla=Vacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ALERGIA_TODOS:
			tabla=Alergia.NOMBRE_TABLA;
			break;
		case ProveedorContenido.AFILIACION_TODOS:
			tabla=Afiliacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.NACIONALIDAD_TODOS:
			tabla=Nacionalidad.NOMBRE_TABLA;
			break;
		case ProveedorContenido.OPERADORA_CELULAR_TODOS:
			tabla=OperadoraCelular.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PENDIENTES_TARJETA_TODOS:
			tabla=PendientesTarjeta.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ARBOL_SEGMENTACION_TODOS:
			tabla=ArbolSegmentacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_TUTOR_TODOS:
			tabla=PersonaTutor.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ANTIGUA_UM_TODOS:
			tabla=AntiguaUM.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ANTIGUO_DOMICILIO_TODOS:
			tabla=AntiguoDomicilio.NOMBRE_TABLA;
			break;
		case ProveedorContenido.REGISTRO_CIVIL_TODOS:
			tabla=RegistroCivil.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_ALERGIA_TODOS:
			tabla=PersonaAlergia.NOMBRE_TABLA;
			break;
		case ProveedorContenido.PERSONA_AFILIACION_TODOS:
			tabla=PersonaAfiliacion.NOMBRE_TABLA;
			break;
		case ProveedorContenido.CONTROL_VACUNA_TODOS:
			tabla=ControlVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.REGLA_VACUNA_TODOS:
			tabla=ReglaVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.VIA_VACUNA_TODOS:
			tabla=ViaVacuna.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ESQUEMA_INCOMPLETO_TODOS:
			tabla=EsquemaIncompleto.NOMBRE_TABLA;
			break;
		case ProveedorContenido.BITACORA_TODOS:
			tabla=Bitacora.NOMBRE_TABLA;
			break;
		case ProveedorContenido.ERROR_SIS_TODOS:
			tabla=ErrorSis.NOMBRE_TABLA;
			break;
			
		default:
			throw new IllegalArgumentException("Uri desconocido "+uri);
		}//fin casos
		
		if(!TextUtils.isEmpty(selection))
			where += " and " + selection;
		
		//actualizamos registros
		try{
			afectadas = db.update(tabla, values, where, selectionArgs);
			if(afectadas>0)
				this.getContext().getContentResolver().notifyChange(uri, null);
			return afectadas;
		}catch(SQLiteConstraintException ex){
			Log.i(TAG, "Error de SQLite haciendo UPDATE de uri "+uri+ ", con valores "+values);
		}
		
		return 0;
	}//fin update

	
	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		ContentProviderResult[] salida;
		SQLiteDatabase db = this.basedatos.getWritableDatabase();
		db.beginTransaction();
		try{
			salida = super.applyBatch(operations);
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
		return salida;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int salida;
		SQLiteDatabase db = this.basedatos.getWritableDatabase();
		db.beginTransaction();
		try{
			salida= super.bulkInsert(uri, values);
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
		return salida;
	}

}//fin clase
