package com.siigs.tes.datos.vistas;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaTutor;
import com.siigs.tes.datos.tablas.Tutor;

/**
 * Describe la consulta anidada de Censo con datos de Paciente, Tutor, Vacunas
 * @author Axel
 *
 */
public class Censo {

	public static final String MASCULINO = "M";
	public static final String FEMENINO = "F";
	
	public static CursorLoader getCenso(Context context, String nombre, Integer anoNacimiento, String sexo){
		String selection = "1=1";
		if(nombre != null)
			selection += " AND (p."+Persona.NOMBRE + " LIKE '%"+nombre+"%' OR p."+Persona.APELLIDO_PATERNO
			+" LIKE '%"+nombre+"%' OR p."+Persona.APELLIDO_MATERNO+" LIKE '%"+nombre+"%' )";
		if(anoNacimiento != null)
			selection += " AND '" + anoNacimiento + "'=" + "strftime('%Y', p."+Persona.FECHA_NACIMIENTO+")";
		if(sexo != null)
			selection += " AND " + "p."+Persona.SEXO + "='"+sexo+"'";
		return new CursorLoader(context, ProveedorContenido.CENSO_CONTENT_URI, null, selection, null, null);
	}
	
	//COLUMNAS PARA CURSOR Y PARA QUERY DE CONTENT PROVIDER
	//Cursor de android ridículamente no permite prefijos de tablas estílo "tabla.columna" al momento de
	//consultar sus datos con la forma cursor.getColumnIndex("tabla.columna") Es decir, se come el prefijo "tabla."
	//Por ese motivo, se deben usar variables por un lado para el content provider (que si ocupa estílo "tabla.columna")
	//y por el otro lado con alias para el consumo de datos en la forma cursor.getColumnIndex(NOMBRE_PACIENTE)
	public final static String _ID_PACIENTE = Persona._ID;
	private final static String COL__ID_PACIENTE = "p." + Persona._ID;
	
	public final static String NOMBRE_PACIENTE = "nombre_paciente";
	private final static String COL_NOMBRE_PACIENTE = "p." + Persona.NOMBRE + " " + NOMBRE_PACIENTE;
	
	public final static String APPAT_PACIENTE = "appat_paciente"; 
	private final static String COL_APPAT_PACIENTE = "p." + Persona.APELLIDO_PATERNO + " " + APPAT_PACIENTE;
	
	public final static String APMAT_PACIENTE = "apmat_paciente"; 
	private final static String COL_APMAT_PACIENTE = "p." + Persona.APELLIDO_MATERNO + " " + APMAT_PACIENTE;
	
	public final static String NOMBRE_TUTOR = "nombre_tutor";
	private final static String COL_NOMBRE_TUTOR = "t." + Tutor.NOMBRE + " " + NOMBRE_TUTOR;
	
	public final static String APPAT_TUTOR = "appat_tutor"; 
	private final static String COL_APPAT_TUTOR = "t." + Tutor.APELLIDO_PATERNO + " " + APPAT_TUTOR;
	
	public final static String APMAT_TUTOR = "apmat_tutor"; 
	private final static String COL_APMAT_TUTOR = "t." + Tutor.APELLIDO_MATERNO + " " + APMAT_TUTOR;

	public final static String CALLE_DOMICILIO = Persona.CALLE_DOMICILIO;
	private final static String COL_CALLE_DOMICILIO = "p." + Persona.CALLE_DOMICILIO + " " + CALLE_DOMICILIO;
	
	public final static String NUMERO_DOMICILIO = Persona.NUMERO_DOMICILIO;
	private final static String COL_NUMERO_DOMICILIO = "p." + Persona.NUMERO_DOMICILIO + " " + NUMERO_DOMICILIO;

	public final static String COLONIA_DOMICILIO = Persona.COLONIA_DOMICILIO;
	private final static String COL_COLONIA_DOMICILIO = "p." + Persona.COLONIA_DOMICILIO + " " + COLONIA_DOMICILIO;
	
	public final static String REFERENCIA_DOMICILIO = Persona.REFERENCIA_DOMICILIO;
	private final static String COL_REFERENCIA_DOMICILIO = "p." + Persona.REFERENCIA_DOMICILIO + " " + REFERENCIA_DOMICILIO;

	public final static String CURP = Persona.CURP;
	private final static String COL_CURP = "p." + Persona.CURP+ " " + CURP;
	
	public final static String FECHA_NACIMIENTO = Persona.FECHA_NACIMIENTO;
	private final static String COL_FECHA_NACIMIENTO = "p." + Persona.FECHA_NACIMIENTO+ " " + FECHA_NACIMIENTO;
	
	public final static String SEXO = Persona.SEXO;
	private final static String COL_SEXO = "p." + Persona.SEXO+ " " + SEXO;

	public final static String BCG = "bcg";
	private final static String COL_BCG = "cv1." + ControlVacuna.ID_VACUNA + " " + BCG;

	public final static String HEPATITIS_1 = "hepatitis_1";
	private final static String COL_HEPATITIS_1 = "cv2." + ControlVacuna.ID_VACUNA + " " + HEPATITIS_1;
	
	public final static String HEPATITIS_2 = "hepatitis_2";
	private final static String COL_HEPATITIS_2 = "cv3." + ControlVacuna.ID_VACUNA + " " + HEPATITIS_2;

	public final static String HEPATITIS_3 = "hepatitis_3";
	private final static String COL_HEPATITIS_3 = "cv4." + ControlVacuna.ID_VACUNA + " " + HEPATITIS_3;

	public final static String PENTAVALENTE_1 = "pentavalente_1";
	private final static String COL_PENTAVALENTE_1 = "cv5." + ControlVacuna.ID_VACUNA + " " + PENTAVALENTE_1;

	public final static String PENTAVALENTE_2 = "pentavalente_2";
	private final static String COL_PENTAVALENTE_2 = "cv6." + ControlVacuna.ID_VACUNA + " " + PENTAVALENTE_2;
	
	public final static String PENTAVALENTE_3 = "pentavalente_3";
	private final static String COL_PENTAVALENTE_3 = "cv7." + ControlVacuna.ID_VACUNA + " " + PENTAVALENTE_3;
	
	public final static String PENTAVALENTE_4 = "pentavalente_4";
	private final static String COL_PENTAVALENTE_4 = "cv8." + ControlVacuna.ID_VACUNA + " " + PENTAVALENTE_4;

	public final static String DPT_R = "dpt_r";
	private final static String COL_DPT_R = "cv9." + ControlVacuna.ID_VACUNA + " " + DPT_R;

	public final static String SRP_1 = "srp_1";
	private final static String COL_SRP_1 = "cv10." + ControlVacuna.ID_VACUNA + " " + SRP_1;

	public final static String SRP_2 = "srp_2";
	private final static String COL_SRP_2 = "cv11." + ControlVacuna.ID_VACUNA + " " + SRP_2;

	public final static String ROTAVIRUS_1 = "rotavirus_1";
	private final static String COL_ROTAVIRUS_1 = "cv12." + ControlVacuna.ID_VACUNA + " " + ROTAVIRUS_1;
	
	public final static String ROTAVIRUS_2 = "rotavirus_2";
	private final static String COL_ROTAVIRUS_2 = "cv13." + ControlVacuna.ID_VACUNA + " " + ROTAVIRUS_2;
	
	public final static String ROTAVIRUS_3 = "rotavirus_3";
	private final static String COL_ROTAVIRUS_3 = "cv14." + ControlVacuna.ID_VACUNA + " " + ROTAVIRUS_3;
	
	public final static String NEUMOCOCO_1 = "neumococo_1";
	private final static String COL_NEUMOCOCO_1 = "cv15." + ControlVacuna.ID_VACUNA + " " + NEUMOCOCO_1;
	
	public final static String NEUMOCOCO_2 = "neumococo_2";
	private final static String COL_NEUMOCOCO_2 = "cv16." + ControlVacuna.ID_VACUNA + " " + NEUMOCOCO_2;
	
	public final static String NEUMOCOCO_3 = "neumococo_3";
	private final static String COL_NEUMOCOCO_3 = "cv17." + ControlVacuna.ID_VACUNA + " " + NEUMOCOCO_3;
	
	public final static String INFLUENZA_1 = "influenza_1";
	private final static String COL_INFLUENZA_1 = "cv18." + ControlVacuna.ID_VACUNA + " " + INFLUENZA_1;
	
	public final static String INFLUENZA_2 = "influenza_2";
	private final static String COL_INFLUENZA_2 = "cv19." + ControlVacuna.ID_VACUNA + " " + INFLUENZA_2;
	
	public final static String INFLUENZA_R = "influenza_r";
	private final static String COL_INFLUENZA_R = "cv20." + ControlVacuna.ID_VACUNA + " " + INFLUENZA_R;
	//public final static String DPT_1 = "cv9." + ControlVacuna.ID_VACUNA;
	//public final static String DPT_2 = "cv10." + ControlVacuna.ID_VACUNA;

	//Columnas para la consulta del ContentProvider
	public final static String[] COLUMNAS = new String[]{COL__ID_PACIENTE, COL_NOMBRE_PACIENTE, COL_APPAT_PACIENTE, 
		COL_APMAT_PACIENTE, COL_NOMBRE_TUTOR, COL_APPAT_TUTOR, COL_APMAT_TUTOR, COL_CALLE_DOMICILIO, 
		COL_NUMERO_DOMICILIO, COL_COLONIA_DOMICILIO, COL_REFERENCIA_DOMICILIO, COL_CURP, COL_FECHA_NACIMIENTO, 
		COL_SEXO, COL_BCG, COL_HEPATITIS_1, COL_HEPATITIS_2, COL_HEPATITIS_3, COL_PENTAVALENTE_1, 
		COL_PENTAVALENTE_2, COL_PENTAVALENTE_3,	COL_PENTAVALENTE_4, COL_DPT_R, COL_SRP_1, COL_SRP_2, 
		COL_ROTAVIRUS_1, COL_ROTAVIRUS_2, COL_ROTAVIRUS_3, COL_NEUMOCOCO_1, COL_NEUMOCOCO_2, COL_NEUMOCOCO_3, 
		COL_INFLUENZA_1, COL_INFLUENZA_2, COL_INFLUENZA_R};
	
	//FROM (TABLAS A USAR CON JOINS)
	public final static String TABLAS = Tutor.NOMBRE_TABLA + " t " + 
	" JOIN " + PersonaTutor.NOMBRE_TABLA + " pt ON t."+Tutor.ID + "= pt."+PersonaTutor.ID_TUTOR +
	" JOIN " + Persona.NOMBRE_TABLA + " p ON pt."+PersonaTutor.ID_PERSONA + "= p."+Persona.ID +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv1 ON p." +Persona.ID + "= cv1." + ControlVacuna.ID_PERSONA + " AND cv1." + ControlVacuna.ID_VACUNA + "= 1" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv2 ON cv1." +ControlVacuna.ID_PERSONA + "= cv2." + ControlVacuna.ID_PERSONA + " AND cv2." + ControlVacuna.ID_VACUNA + "= 2" +	
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv3 ON cv2." +ControlVacuna.ID_PERSONA + "= cv3." + ControlVacuna.ID_PERSONA + " AND cv3." + ControlVacuna.ID_VACUNA + "= 3" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv4 ON cv3." +ControlVacuna.ID_PERSONA + "= cv4." + ControlVacuna.ID_PERSONA + " AND cv4." + ControlVacuna.ID_VACUNA + "= 4" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv5 ON cv4." +ControlVacuna.ID_PERSONA + "= cv5." + ControlVacuna.ID_PERSONA + " AND cv5." + ControlVacuna.ID_VACUNA + "= 5" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv6 ON cv5." +ControlVacuna.ID_PERSONA + "= cv6." + ControlVacuna.ID_PERSONA + " AND cv6." + ControlVacuna.ID_VACUNA + "= 6" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv7 ON cv6." +ControlVacuna.ID_PERSONA + "= cv7." + ControlVacuna.ID_PERSONA + " AND cv7." + ControlVacuna.ID_VACUNA + "= 7" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv8 ON cv7." +ControlVacuna.ID_PERSONA + "= cv8." + ControlVacuna.ID_PERSONA + " AND cv8." + ControlVacuna.ID_VACUNA + "= 8" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv9 ON cv8." +ControlVacuna.ID_PERSONA + "= cv9." + ControlVacuna.ID_PERSONA + " AND cv9." + ControlVacuna.ID_VACUNA + "= 9" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv10 ON cv9." +ControlVacuna.ID_PERSONA + "= cv10." + ControlVacuna.ID_PERSONA + " AND cv10." + ControlVacuna.ID_VACUNA + "= 19" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv11 ON cv10." +ControlVacuna.ID_PERSONA + "= cv11." + ControlVacuna.ID_PERSONA + " AND cv11." + ControlVacuna.ID_VACUNA + "= 20" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv12 ON cv11." +ControlVacuna.ID_PERSONA + "= cv12." + ControlVacuna.ID_PERSONA + " AND cv12." + ControlVacuna.ID_VACUNA + "= 10" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv13 ON cv12." +ControlVacuna.ID_PERSONA + "= cv13." + ControlVacuna.ID_PERSONA + " AND cv13." + ControlVacuna.ID_VACUNA + "= 11" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv14 ON cv13." +ControlVacuna.ID_PERSONA + "= cv14." + ControlVacuna.ID_PERSONA + " AND cv14." + ControlVacuna.ID_VACUNA + "= 12" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv15 ON cv14." +ControlVacuna.ID_PERSONA + "= cv15." + ControlVacuna.ID_PERSONA + " AND cv15." + ControlVacuna.ID_VACUNA + "= 13" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv16 ON cv15." +ControlVacuna.ID_PERSONA + "= cv16." + ControlVacuna.ID_PERSONA + " AND cv16." + ControlVacuna.ID_VACUNA + "= 14" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv17 ON cv16." +ControlVacuna.ID_PERSONA + "= cv17." + ControlVacuna.ID_PERSONA + " AND cv17." + ControlVacuna.ID_VACUNA + "= 15" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv18 ON cv17." +ControlVacuna.ID_PERSONA + "= cv18." + ControlVacuna.ID_PERSONA + " AND cv18." + ControlVacuna.ID_VACUNA + "= 16" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv19 ON cv18." +ControlVacuna.ID_PERSONA + "= cv19." + ControlVacuna.ID_PERSONA + " AND cv19." + ControlVacuna.ID_VACUNA + "= 17" +
	" LEFT JOIN " + ControlVacuna.NOMBRE_TABLA + " cv20 ON cv19." +ControlVacuna.ID_PERSONA + "= cv20." + ControlVacuna.ID_PERSONA + " AND cv20." + ControlVacuna.ID_VACUNA + "= 18";
}
