package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Vacuna {

	public final static String NOMBRE_TABLA = "cns_vacuna"; //nombre en BD
	
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
	
	public static List<Vacuna> getVacunasActivas(Context context){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.VACUNA_CONTENT_URI, null, ACTIVO + "=1", null, DESCRIPCION);
		List<Vacuna> salida = DatosUtil.ObjetosDesdeCursor(cur, Vacuna.class);
		cur.close();
		return salida;
	}
}
