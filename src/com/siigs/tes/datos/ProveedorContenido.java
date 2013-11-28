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
	
	private static final String PERSONA_BASE_PATH = Persona.NOMBRE_TABLA;
	public static final int PERSONA_TODOS = 100;
	public static final int PERSONA_ID = 110;
	public static final Uri PERSONA_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PERSONA_BASE_PATH);
	
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
	    sURIMatcher.addURI(AUTHORITY, PERSONA_BASE_PATH, PERSONA_TODOS);
	    sURIMatcher.addURI(AUTHORITY, PERSONA_BASE_PATH + "/#", PERSONA_ID);
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
