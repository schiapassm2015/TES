package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Nacionalidad {

	public final static String NOMBRE_TABLA = "cns_nacionalidad"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String CODIGO = "codigo";
	public final static String DESCRIPCION = "descripcion";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		CODIGO + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL "+
		"); ";
}
