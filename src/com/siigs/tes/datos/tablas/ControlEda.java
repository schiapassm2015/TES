package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ControlEda {

	public final static String NOMBRE_TABLA = "cns_control_eda"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_EDA = "id_eda";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		ID_EDA + " INTEGER NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public int id_eda;
	public String fecha;
	public int id_asu_um;

	public static List<ControlEda> getEdasPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONTROL_EDA_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, null);
		List<ControlEda> salida = DatosUtil.ObjetosDesdeCursor(cur, ControlEda.class);
		cur.close();
		return salida;
	}

}
