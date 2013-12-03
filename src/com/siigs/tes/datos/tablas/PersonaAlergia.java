package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PersonaAlergia {

	public final static String NOMBRE_TABLA = "cns_persona_x_alergia"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_ECE_ALERGIA = "id_ece_alergia";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_PERSONA + " TEXT NOT NULL, " +
		ID_ECE_ALERGIA + " INTEGER NOT NULL, " +
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		"UNIQUE (" + ID_PERSONA + "," + ID_ECE_ALERGIA + ")" +
		"); ";
}
