package com.siigs.tes.datos.tablas;


/**
 * Tabla interna que NO se envía a la nube
 * @author Axel
 *
 */
public class EsquemaIncompleto {

	public final static String NOMBRE_TABLA = "esquema_incompleto"; //nombre en BD
	
	//Columnas
	//public final static String ID = "_id"; //para adaptadores android
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_VACUNA = "id_vacuna";
	public final static String PRIORIDAD = "prioridad";
		
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		//ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		ID_VACUNA + " INTEGER NOT NULL, " +
		PRIORIDAD + " INTEGER NOT NULL" +
		"); "
		+"CREATE INDEX idx_"+NOMBRE_TABLA+" ON "+NOMBRE_TABLA+" ( "
		+ID_PERSONA + "," + ID_VACUNA + "); ";
	
	//POJO
	public String id_persona;
	public int id_vacuna;
	public int prioridad;
	
	//Prioridades posibles
	public final static int PRIORIDAD_0 = 0;
	public final static int PRIORIDAD_1 = 1;
}
