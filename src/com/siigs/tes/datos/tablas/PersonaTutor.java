package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PersonaTutor {

	public final static String NOMBRE_TABLA = "cns_persona_x_tutor"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_TUTOR = "id_tutor";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT UNIQUE NOT NULL, " +
		ID_TUTOR + " TEXT NOT NULL, "+
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')) "+
		"); ";
	
	//POJO
	public String id_persona;
	public String id_tutor;
	public String ultima_actualizacion;
}
