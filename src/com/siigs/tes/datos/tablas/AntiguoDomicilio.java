package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class AntiguoDomicilio {

	public final static String NOMBRE_TABLA = "cns_antiguo_domicilio"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA_CAMBIO = "fecha_cambio";
	public final static String ID_ASU_LOCALIDAD_DOMICILIO = "id_asu_localidad_domicilio";
	public final static String CALLE_DOMICILIO = "calle_domicilio";
	public final static String NUMERO_DOMICILIO = "numero_domicilio";
	public final static String COLONIA_DOMICILIO = "colonia_domicilio";
	public final static String REFERENCIA_DOMICILIO = "referencia_domicilio";
	public final static String CP_DOMICILIO = "cp_domicilio";
	
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		FECHA_CAMBIO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_LOCALIDAD_DOMICILIO + " INTEGER NOT NULL, "+
		CALLE_DOMICILIO + " TEXT NOT NULL, " +
		NUMERO_DOMICILIO + " TEXT DEFAULT NULL, " +
		COLONIA_DOMICILIO + " TEXT DEFAULT NULL, "+
		REFERENCIA_DOMICILIO + " TEXT DEFAULT NULL, " +
		CP_DOMICILIO + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA_CAMBIO + ")" +
		"); ";
	
}
