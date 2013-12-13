package com.siigs.tes.datos.tablas;

import com.google.gson.annotations.SerializedName;

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
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		CODIGO + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String codigo;
	public String descripcion;
	public int activo;
}
