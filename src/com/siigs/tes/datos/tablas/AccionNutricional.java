package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class AccionNutricional {

	public final static String NOMBRE_TABLA = "cns_accion_nutricional"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String descripcion;
	public int activo; 
	
	public static List<AccionNutricional> getAccionesActivas(Context context){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.ACCION_NUTRICIONAL_CONTENT_URI, null, ACTIVO + "=1", null, DESCRIPCION);
		List<AccionNutricional> salida = DatosUtil.ObjetosDesdeCursor(cur, AccionNutricional.class);
		cur.close();
		return salida;
	}
	
	public static String getDescripcion(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.ACCION_NUTRICIONAL_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		cur.moveToNext(); //deber�a haber resultados
		String salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		cur.close();
		return salida;
	}
}
