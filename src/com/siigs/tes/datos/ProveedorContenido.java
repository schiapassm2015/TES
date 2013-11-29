package com.siigs.tes.datos;


import com.siigs.tes.datos.tablas.*;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * @author Axel
 * Principal Proveedor de contenido de datos de la aplicación. 
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
	
	private static final String CONTROL_IRA_PATH = ControlIra.NOMBRE_TABLA;
	public static final int CONTROL_IRA_TODOS = 800;
	public static final int CONTROL_IRA_ID = 810;
	public static final Uri CONTROL_IRA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CONTROL_IRA_PATH);
	
	private static final String IRA_PATH = Ira.NOMBRE_TABLA;
	public static final int IRA_TODOS = 820;
	public static final int IRA_ID = 821;
	public static final Uri IRA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + IRA_PATH);
	
	private static final String CONTROL_EDA_PATH = ControlEda.NOMBRE_TABLA;
	public static final int CONTROL_EDA_TODOS = 900;
	public static final int CONTROL_EDA_ID = 910;
	public static final Uri CONTROL_EDA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CONTROL_EDA_PATH);
	
	private static final String EDA_PATH = Eda.NOMBRE_TABLA;
	public static final int EDA_TODOS = 920;
	public static final int EDA_ID = 921;
	public static final Uri EDA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + EDA_PATH);
	
	private static final String CONTROL_CONSULTA_PATH = ControlConsulta.NOMBRE_TABLA;
	public static final int CONTROL_CONSULTA_TODOS = 1000;
	public static final int CONTROL_CONSULTA_ID = 1010;
	public static final Uri CONTROL_CONSULTA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CONTROL_CONSULTA_PATH);
	
	private static final String CONSULTA_PATH = Consulta.NOMBRE_TABLA;
	public static final int CONSULTA_TODOS = 1020;
	public static final int CONSULTA_ID = 1021;
	public static final Uri CONSULTA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CONSULTA_PATH);
	
	private static final String CONTROL_ACCION_NUTRICIONAL_PATH = ControlAccionNutricional.NOMBRE_TABLA;
	public static final int CONTROL_ACCION_NUTRICIONAL_TODOS = 1100;
	public static final int CONTROL_ACCION_NUTRICIONAL_ID = 1110;
	public static final Uri CONTROL_ACCION_NUTRICIONAL_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CONTROL_ACCION_NUTRICIONAL_PATH);
	
	private static final String ACCION_NUTRICIONAL_PATH = AccionNutricional.NOMBRE_TABLA;
	public static final int ACCION_NUTRICIONAL_TODOS = 1120;
	public static final int ACCION_NUTRICIONAL_ID = 1121;
	public static final Uri ACCION_NUTRICIONAL_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ACCION_NUTRICIONAL_PATH);
	
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
	
	/*
	public static final String CONTENT_ITEM_TYPE = 
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/persona";
	public static final String CONTENT_TYPE = 
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/personas";
	*/
	
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
	    
	    sURIMatcher.addURI(AUTHORITY, CONTROL_IRA_PATH, CONTROL_IRA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, CONTROL_IRA_PATH + "/#", CONTROL_IRA_ID);
	    sURIMatcher.addURI(AUTHORITY, IRA_PATH, IRA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, IRA_PATH + "/#", IRA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, CONTROL_EDA_PATH, CONTROL_EDA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, CONTROL_EDA_PATH + "/#", CONTROL_EDA_ID);
	    sURIMatcher.addURI(AUTHORITY, EDA_PATH, EDA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, EDA_PATH + "/#", EDA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, CONTROL_CONSULTA_PATH, CONTROL_CONSULTA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, CONTROL_CONSULTA_PATH + "/#", CONTROL_CONSULTA_ID);
	    sURIMatcher.addURI(AUTHORITY, CONSULTA_PATH, CONSULTA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, CONSULTA_PATH + "/#", CONSULTA_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, CONTROL_ACCION_NUTRICIONAL_PATH, CONTROL_ACCION_NUTRICIONAL_TODOS);
	    sURIMatcher.addURI(AUTHORITY, CONTROL_ACCION_NUTRICIONAL_PATH + "/#", CONTROL_ACCION_NUTRICIONAL_ID);
	    sURIMatcher.addURI(AUTHORITY, ACCION_NUTRICIONAL_PATH, ACCION_NUTRICIONAL_TODOS);
	    sURIMatcher.addURI(AUTHORITY, ACCION_NUTRICIONAL_PATH + "/#", ACCION_NUTRICIONAL_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, NACIONALIDAD_PATH, NACIONALIDAD_TODOS);
	    sURIMatcher.addURI(AUTHORITY, NACIONALIDAD_PATH + "/#", NACIONALIDAD_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, REGISTRO_CIVIL_PATH, REGISTRO_CIVIL_TODOS);
	    sURIMatcher.addURI(AUTHORITY, REGISTRO_CIVIL_PATH + "/#", REGISTRO_CIVIL_ID);
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
			
		//IRAS	
		case ProveedorContenido.CONTROL_IRA_ID:
			builder.setTables(ControlIra.NOMBRE_TABLA);
			builder.appendWhere(ControlIra._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.CONTROL_IRA_TODOS:
			builder.setTables(ControlIra.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.IRA_ID:
			builder.setTables(Ira.NOMBRE_TABLA);
			builder.appendWhere(Ira.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.IRA_TODOS:
			builder.setTables(Ira.NOMBRE_TABLA);// No existe filtro
			break;
			
		//EDAS
		case ProveedorContenido.CONTROL_EDA_ID:
			builder.setTables(ControlEda.NOMBRE_TABLA);
			builder.appendWhere(ControlEda._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.CONTROL_EDA_TODOS:
			builder.setTables(ControlEda.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.EDA_ID:
			builder.setTables(Eda.NOMBRE_TABLA);
			builder.appendWhere(Eda.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.EDA_TODOS:
			builder.setTables(Eda.NOMBRE_TABLA);// No existe filtro
			break;
			
		//CONSULTAS
		case ProveedorContenido.CONTROL_CONSULTA_ID:
			builder.setTables(ControlConsulta.NOMBRE_TABLA);
			builder.appendWhere(ControlConsulta._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.CONTROL_CONSULTA_TODOS:
			builder.setTables(ControlConsulta.NOMBRE_TABLA);// No existe filtro
			break;
			
		case ProveedorContenido.CONSULTA_ID:
			builder.setTables(Consulta.NOMBRE_TABLA);
			builder.appendWhere(Consulta.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.CONSULTA_TODOS:
			builder.setTables(Consulta.NOMBRE_TABLA);// No existe filtro
			break;
			
		//ACCIONES NUTRICIONALES
		case ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_ID:
			builder.setTables(ControlAccionNutricional.NOMBRE_TABLA);
			builder.appendWhere(ControlAccionNutricional._ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;			
		case ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_TODOS:
			builder.setTables(ControlAccionNutricional.NOMBRE_TABLA);// No existe filtro
			break;

		case ProveedorContenido.ACCION_NUTRICIONAL_ID:
			builder.setTables(AccionNutricional.NOMBRE_TABLA);
			builder.appendWhere(AccionNutricional.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.ACCION_NUTRICIONAL_TODOS:
			builder.setTables(AccionNutricional.NOMBRE_TABLA);// No existe filtro
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
			builder.appendWhere(RegistroCivil.ID + "=?");
			parametros=new String[]{uri.getLastPathSegment()};
			break;
		case ProveedorContenido.REGISTRO_CIVIL_TODOS:
			builder.setTables(RegistroCivil.NOMBRE_TABLA);// No existe filtro
			break;
			
		default:
			throw new IllegalArgumentException("Uri desconocido "+tipoUri);
		}
		
		//Continúa consulta con o sin parámetros según uri
		Cursor cur= builder.query(this.basedatos.getReadableDatabase(), 
				projection, selection, parametros, null, null, sortOrder);
		//The setNotificationUri() method simply places a watch on the caller’s 
		//content resolver such that if the data changes and the caller has 
		//a registered change watcher, they’ll be notified. 
		//Here we just use the same URI.
		cur.setNotificationUri(this.getContext().getContentResolver(), uri);
		return cur;
	}
	
	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
