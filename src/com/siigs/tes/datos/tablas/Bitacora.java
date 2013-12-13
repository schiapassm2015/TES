package com.siigs.tes.datos.tablas;

import com.google.gson.annotations.SerializedName;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Bitacora {

	public final static String NOMBRE_TABLA = "sis_bitacora"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String ID_USUARIO = "id_usuario";
	public final static String FECHA_HORA = "fecha_hora";
	public final static String PARAMETROS = "parametros";
	public final static String ID_CONTROLADOR_ACCION = "id_controlador_accion";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_USUARIO + " INTEGER NOT NULL, " +
		ID_CONTROLADOR_ACCION + " INTEGER NOT NULL, "+
		FECHA_HORA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		PARAMETROS + " TEXT DEFAULT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public int id_usuario;
	public int id_controlador_accion;
	public String fecha_hora;
	public String parametros;
}
