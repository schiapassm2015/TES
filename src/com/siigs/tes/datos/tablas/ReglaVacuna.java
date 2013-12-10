package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ReglaVacuna {

	public final static String NOMBRE_TABLA = "cns_regla_vacuna"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String ID_ECE_VACUNA = "id_ece_vacuna	";
	public final static String DIA_INICIO_APLICACION_NACIDO = "dia_inicio_aplicacion_nacido";
	public final static String DIA_FIN_APLICACION_NACIDO = "dia_fin_aplicacion_nacido";
	public final static String ID_ECE_REGLA_VACUNA_SECUENCIAL = "id_ece_regla_vacuna_secuencial";
	public final static String DIA_INICIO_APLICACION_SECUENCIAL = "dia_inicio_aplicacion_secuencial";
	public final static String DIA_FIN_APLICACION_SECUENCIAL = "dia_fin_aplicacion_secuencial";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_ECE_VACUNA + " INTEGER NOT NULL, " +
		DIA_INICIO_APLICACION_NACIDO + " INTEGER DEFAULT NULL, " +
		DIA_FIN_APLICACION_NACIDO + " INTEGER DEFAULT NULL, " +
		ID_ECE_REGLA_VACUNA_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		DIA_INICIO_APLICACION_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		DIA_FIN_APLICACION_SECUENCIAL + " INTEGER DEFAULT NULL " +
		"); ";
}
