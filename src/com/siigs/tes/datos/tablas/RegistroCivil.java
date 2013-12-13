package com.siigs.tes.datos.tablas;


/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class RegistroCivil {

	public final static String NOMBRE_TABLA = "cns_registro_civil"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_LOCALIDAD_REGISTRO_CIVIL = "id_localidad_registro_civil";
	public final static String FECHA_REGISTRO = "fecha_registro";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_PERSONA + " TEXT UNIQUE NOT NULL, " + 
		ID_LOCALIDAD_REGISTRO_CIVIL + " INTEGER NOT NULL, " +
		FECHA_REGISTRO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')) " +
		"); ";
	
	//POJO
	public String id_persona;
	public int id_localidad_registro_civil;
	public String fecha_registro;
}
