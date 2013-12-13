package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ControlNutricional {

	public final static String NOMBRE_TABLA = "cns_control_nutricional"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String PESO = "peso";
	public final static String ALTURA = "altura";
	public final static String TALLA = "talla";
	public final static String ID_INVITADO = "id_invitado";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		PESO + " NUMERIC NOT NULL, "+
		ALTURA + " INTEGER NOT NULL, "+
		TALLA + " INTEGER NOT NULL, "+
		ID_INVITADO + " INTEGER DEFAULT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public String fecha;
	public int id_asu_um;
	public double peso;
	public int altura;
	public int talla;
	//id_invitado NO se env�a en JSON
}
