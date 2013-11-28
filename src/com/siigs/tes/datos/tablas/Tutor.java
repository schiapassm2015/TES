package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Tutor {

	public final static String NOMBRE_TABLA = "cns_tutor"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String CURP = "curp";
	public final static String NOMBRE = "nombre";
	public final static String APELLIDO_PATERNO = "apellido_paterno";
	public final static String APELLIDO_MATERNO = "apellido_materno";
	public final static String SEXO = "sexo";
	public final static String TELEFONO = "telefono";
	public final static String CELULAR = "celular";
	public final static String ID_OPERADORA_CELULAR = "id_operadora_celular";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER NOT NULL , " +
		CURP + " TEXT NOT NULL, "+
		NOMBRE + " TEXT NOT NULL, "+
		APELLIDO_PATERNO + " TEXT NOT NULL, " +
		APELLIDO_MATERNO + " TEXT NOT NULL, " +
		SEXO + " INTEGER NOT NULL, " +
		TELEFONO + " TEXT DEFAULT NULL, "+
		CELULAR + " TEXT, "+
		ID_OPERADORA_CELULAR + " INTEGER DEFAULT NULL, "+
		" PRIMARY KEY (" + ID + ")"
		+"); "; 
	
}
