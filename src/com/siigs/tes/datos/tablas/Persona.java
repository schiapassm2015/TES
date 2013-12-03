package com.siigs.tes.datos.tablas;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Persona {

	public final static String NOMBRE_TABLA = "cns_persona"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String CURP = "curp";
	public final static String NOMBRE = "nombre";
	public final static String APELLIDO_PATERNO = "apellido_paterno";
	public final static String APELLIDO_MATERNO = "apellido_materno";
	public final static String SEXO = "sexo";
	public final static String ID_ECE_TIPO_SANGUINEO = "id_ece_tipo_sanguineo";
	public final static String FECHA_NACIMIENTO = "fecha_nacimiento";
	public final static String ID_ASU_LOCALIDAD_NACIMIENTO = "id_asu_localidad_nacimiento";
	public final static String CALLE_DOMICILIO = "calle_domicilio";
	public final static String NUMERO_DOMICILIO = "numero_domicilio";
	public final static String COLONIA_DOMICILIO = "colonia_domicilio";
	public final static String REFERENCIA_DOMICILIO = "referencia_domicilio";
	public final static String ID_ASU_LOCALIDAD_DOMICILIO= "id_asu_localidad_domicilio";
	public final static String CP_DOMICILIO = "cp_domicilio";
	public final static String TELEFONO_DOMICILIO = "telefono_domicilio";
	public final static String FECHA_REGISTRO = "fecha_registro";
	public final static String ID_ASU_UM_TRATANTE = "id_asu_um_tratante"; //Solo se usa en servidor
	public final static String CELULAR = "celular";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	public final static String ID_NACIONALIDAD = "id_nacionalidad";
	public final static String ID_OPERADORA_CELULAR = "id_operadora_celular";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
		ID + " TEXT NOT NULL , " +
		CURP + " TEXT NOT NULL, "+
		NOMBRE + " TEXT NOT NULL, "+
		APELLIDO_PATERNO + " TEXT NOT NULL, " +
		APELLIDO_MATERNO + " TEXT NOT NULL, " +
		SEXO + " INTEGER NOT NULL, " +
		ID_ECE_TIPO_SANGUINEO + " INTEGER NOT NULL, " +
		FECHA_NACIMIENTO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_LOCALIDAD_NACIMIENTO + " INTEGER NOT NULL, "+
		CALLE_DOMICILIO + " TEXT NOT NULL, "+
		NUMERO_DOMICILIO + " TEXT DEFAULT NULL, "+
		COLONIA_DOMICILIO + " TEXT DEFAULT NULL, "+
		REFERENCIA_DOMICILIO + "TEXT DEFAULT NULL, "+
		ID_ASU_LOCALIDAD_DOMICILIO + " INTEGER NOT NULL, "+
		CP_DOMICILIO + " INTEGER NOT NULL, "+
		TELEFONO_DOMICILIO + " TEXT DEFAULT NULL, "+
		FECHA_REGISTRO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM_TRATANTE + " INTEGER NOT NULL, "+
		CELULAR + " TEXT, "+
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_NACIONALIDAD + " INTEGER NOT NULL, "+
		ID_OPERADORA_CELULAR + " INTEGER DEFAULT NULL, "+
		" UNIQUE (" + ID + ")" +
		"); "; 
		/*
		, UNIQUE INDEX " + CURP + " ("+ CURP + " ASC), INDEX fk_persona_ece_tipo_sanguineo1_idx` (`id_ece_tipo_sanguineo` ASC),
			  CONSTRAINT `fk_persona_ece_tipo_sanguineo1`
			    FOREIGN KEY (`id_ece_tipo_sanguineo`)
			    REFERENCES `siigs`.`ece_tipo_sanguineo` (`id`)
			    );
			*/
	
	
}
