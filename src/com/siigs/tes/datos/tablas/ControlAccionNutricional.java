package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ControlAccionNutricional {

	public final static String NOMBRE_TABLA = "cns_control_accion_nutricional"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_ACCION_NUTRICIONAL = "id_accion_nutricional";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String ID_INVITADO = "id_invitado";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		ID_ACCION_NUTRICIONAL + " INTEGER NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		ID_INVITADO + " INTEGER DEFAULT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public int id_accion_nutricional;
	public String fecha;
	public int id_asu_um;
	public transient Integer id_invitado; //transient pues no se envía en JSON
}
