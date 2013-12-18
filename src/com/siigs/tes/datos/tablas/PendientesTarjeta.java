package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PendientesTarjeta {

	public final static String NOMBRE_TABLA = "tes_pendientes_tarjeta"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String ID_PERSONA = "id_persona";
	public final static String TABLA = "tabla";
	public final static String REGISTRO_JSON = "registro_json";
	public final static String ES_PENDIENTE_LOCAL = "es_pendiente_local";
	public final static String YA_ESTA_EN_NUBE = "ya_esta_en_nube";
	public final static String RESUELTO = "resuelto";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_PERSONA + " TEXT NOT NULL, "+
		//ID_REMOTO + " INTEGER DEFAULT -1" + //YA_ESTA_EN_NUBE DEFINIRÍA SI TOMA UN VALOR
		TABLA + " TEXT NOT NULL, "+
		REGISTRO_JSON + " TEXT NOT NULL, " +
		ES_PENDIENTE_LOCAL + " INTEGER NOT NULL DEFAULT (0), " +
		YA_ESTA_EN_NUBE + " INTEGER NOT NULL DEFAULT (0), " +
		RESUELTO + " INTEGER NOT NULL DEFAULT (0) "+
		//", UNIQUE (" + ID_PERSONA + "," + REGISTRO_JSON + ") "
		"); "; 

	//POJO
	public String id_persona;
	public String tabla;
	public String registro_json;
}
