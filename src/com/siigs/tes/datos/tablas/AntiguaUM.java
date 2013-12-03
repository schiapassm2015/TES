package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class AntiguaUM {

	public final static String NOMBRE_TABLA = "cns_antigua_um"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA_CAMBIO = "fecha_cambio";
	public final static String ID_ASU_UM_TRATANTE = "id_asu_um_tratante";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		FECHA_CAMBIO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM_TRATANTE + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA_CAMBIO + ")" +
		"); ";
}
