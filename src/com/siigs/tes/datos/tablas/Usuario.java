package com.siigs.tes.datos.tablas;

import com.google.gson.annotations.SerializedName;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Usuario {

	public final static String NOMBRE_TABLA = "sis_usuario"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String NOMBRE_USUARIO = "nombre_usuario";
	public final static String CLAVE = "clave";
	public final static String NOMBRE = "nombre";
	public final static String APELLIDO_PATERNO = "apellido_paterno";
	public final static String APELLIDO_MATERNO = "apellido_materno";
	public final static String CORREO = "correo";
	public final static String ACTIVO = "activo";
	public final static String ID_GRUPO	= "id_grupo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		NOMBRE_USUARIO + " TEXT NOT NULL, " +
		CLAVE + " TEXT NOT NULL, "+
		NOMBRE + " TEXT NOT NULL, "+
		APELLIDO_PATERNO + " TEXT DEFAULT NULL, "+
		APELLIDO_MATERNO + " TEXT DEFAULT NULL, "+
		CORREO + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL, "+
		ID_GRUPO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String nombre_usuario;
	public String clave;
	public String nombre;
	public String apellido_paterno;
	public String apellido_materno;
	public String correo;
	public int activo;
	public int id_grupo;
	
}
