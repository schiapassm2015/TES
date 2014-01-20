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
	public final static String ID_VACUNA = "id_vacuna	";
	public final static String DIA_INICIO_APLICACION_NACIDO = "dia_inicio_aplicacion_nacido";
	public final static String DIA_FIN_APLICACION_NACIDO = "dia_fin_aplicacion_nacido";
	public final static String ID_VACUNA_SECUENCIAL = "id_vacuna_secuencial";
	public final static String DIA_INICIO_APLICACION_SECUENCIAL = "dia_inicio_aplicacion_secuencial";
	public final static String DIA_FIN_APLICACION_SECUENCIAL = "dia_fin_aplicacion_secuencial";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	public final static String ACTIVO = "activo";
	public final static String ID_VIA_VACUNA = "id_via_vacuna";
	public final static String DOSIS = "dosis";
	public final static String REGION = "region";
	public final static String ESQ_COM = "esq_com";
	public final static String ORDEN_ESQ_COM = "orden_esq_com";
	public final static String ALERGIAS = "alergias";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_VACUNA + " INTEGER NOT NULL, " +
		DIA_INICIO_APLICACION_NACIDO + " INTEGER DEFAULT NULL, " +
		DIA_FIN_APLICACION_NACIDO + " INTEGER DEFAULT NULL, " +
		ID_VACUNA_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		DIA_INICIO_APLICACION_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		DIA_FIN_APLICACION_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ACTIVO + " INTEGER NOT NULL, " +
		ID_VIA_VACUNA + " INTEGER NOT NULL, " +
		DOSIS + " NUMERIC DEFAULT NULL, " +
		REGION + " TEXT DEFAULT NULL, " +
		ESQ_COM + " INTEGER NOT NULL, " +
		ORDEN_ESQ_COM + " INTEGER DEFAULT NULL, " +
		ALERGIAS + " TEXT DEFAULT NULL " +
		"); ";
	
	//POJO
	public int id;
	public int id_vacuna;
	public Integer dia_inicio_aplicacion_nacido;
	public Integer dia_fin_aplicacion_nacido;
	public Integer id_vacuna_secuencial;
	public Integer dia_inicio_aplicacion_secuencial;
	public Integer dia_fin_aplicacion_secuencial;
	public String ultima_actualizacion;
	public int activo;
	public int id_via_vacuna;
	public Double dosis;
	public String region;
	public int esq_com;
	public Integer orden_esq_com;
	public String alergias;
}
