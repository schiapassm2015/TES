package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Notificacion {

	public final static String NOMBRE_TABLA = "cns_notificacion"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String TITULO = "titulo";
	public final static String CONTENIDO = "contenido";
	public final static String FECHA_INICIO = "fecha_inicio";
	public final static String FECHA_FIN = "fecha_fin";
	
	//Columnas de control interno
	public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		TITULO + " TEXT NOT NULL, " +
		CONTENIDO + " TEXT NOT NULL, "+
		FECHA_INICIO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		FECHA_FIN + " INTEGER DEFAULT(strftime('%s','now')) "+
		"); ";
}
