package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class UsuarioInvitado {

	public final static String NOMBRE_TABLA = "invitado"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_INVITADO = "_id";
	public final static String ID_USUARIO_CREADOR = "id_usuario_creador";
	public final static String NOMBRE = "nombre";
	public final static String FECHA_CREACION = "fecha_creacion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID_INVITADO + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_USUARIO_CREADOR + " INTEGER NOT NULL, " +
		NOMBRE + " TEXT NOT NULL, "+
		FECHA_CREACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ACTIVO + " INTEGER NOT NULL DEFAULT 1"+
		"); ";
	
}