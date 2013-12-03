package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ArbolSegmentacion {

	public final static String NOMBRE_TABLA = "cns_afiliacion"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String GRADO_SEGMENTACION = "grado_segmentacion";
	public final static String ID_PADRE = "id_padre";
	public final static String ORDEN = "orden";
	public final static String VISIBLE = "visible";
	public final static String DESCRIPCION = "descripcion";
	
	//Columnas de control interno
	public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		GRADO_SEGMENTACION + " INTEGER NOT NULL, " +
		ID_PADRE + " INTEGER NOT NULL, " +
		ORDEN + " INTEGER DEFAULT NULL, " +
		VISIBLE + " INTEGER NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL "+
		"); ";
}
