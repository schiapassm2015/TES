package com.siigs.tes.datos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAfiliacion;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.RegistroCivil;
import com.siigs.tes.datos.tablas.Tutor;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

public class ManejadorNfc {
	//CRITERIOS PARA PARSEAR ESTRUCTURAS EN TEXTO DE TARJETA NFC
	private static final String SEPARADOR_TABLA = "~";
	private static final String SEPARADOR_REGISTRO = "°";
	private static final String SEPARADOR_CAMPO = "=";
	private static final String SIMBOLO_NULO = "¬";
	
	//VERSIONES POSIBLES DE DATOS A CONSIDERAR
	private static final String VERSION_1 ="1";
	
	public static void LeerDatosNFC(Tag nfcTag, Context contexto) throws Exception{
		TesAplicacion aplicacion = (TesAplicacion)contexto.getApplicationContext();
		
		String contenido = LeerTextoPlano(nfcTag);
		String[] piezas = contenido.split(SEPARADOR_TABLA);
		
		//TODO validar si leí una tarjeta real y no otro dispositivo NFC
		
		Sesion.DatosPaciente datosPaciente=null;
		String version = piezas[0];
		if(version.equals(VERSION_1)){
			datosPaciente = LeerVersion1(piezas, version);
		} //else if(version.equals(VERSION_X)){}
		else{
			throw new Exception("Versión de datos no reconocida");
		}
		
		//TODO checar si tiene pendientes, insertar en DatosPaciente, actualizar tes_pendientes si le hice algo
		//TODO 			si agregué pendientes, reescribir DatosPaciente en NFC
		
		aplicacion.getSesion().setDatosPacienteNuevo(datosPaciente);
	}
	
	/**
	 * Parsea las piezas de elementos recibidos en tarjeta de acuerdo
	 * a esta versión.
	 * @param piezas Cadenas que representan tablas/elementos de acuerdo
	 * modelo de metadatos descrito en los comentarios al fondo de este archivo
	 * @param version No se usa actualmente, pero si en el futuro hubiera una  versión
	 * nueva cuya diferencia al método de parseo de ésta es de unos pocos detalles,
	 * en pocos puntos del código, esta función puede recodificarse agregando 
	 * las funcionalidades correspondientes usando {@lilnk version} para discriminar.
	 * Sin embargo si los cambios de parseo/comportamiento entre una versión y otra
	 * fueran muy amplios, sería mejor escribir una función nueva que haga
	 * sus propias implementaciones y agregar un "else if()" para llamar dicha
	 * función nueva desde LeerDatosNFC()
	 */
	private static Sesion.DatosPaciente LeerVersion1(String[] piezas, String version){
		//TODO leer string
		String[] datosPersona = piezas[1].split(SEPARADOR_CAMPO);
		Persona persona = new Persona();
		int n=0;
		persona.id = datosPersona[n++];
		persona.curp = datosPersona[n++];
		persona.nombre = datosPersona[n++];
		persona.apellido_paterno = datosPersona[n++];
		persona.apellido_materno = datosPersona[n++];
		persona.sexo = datosPersona[n++];
		persona.id_tipo_sanguineo = Integer.parseInt(datosPersona[n++]);
		persona.fecha_nacimiento = datosPersona[n++];
		persona.id_asu_localidad_nacimiento = Integer.parseInt(datosPersona[n++]);
		persona.calle_domicilio = datosPersona[n++];
		persona.numero_domicilio = existeString(datosPersona[n++]);
		persona.colonia_domicilio = existeString(datosPersona[n++]);
		persona.referencia_domicilio = existeString(datosPersona[n++]);
		persona.id_asu_localidad_domicilio = Integer.parseInt(datosPersona[n++]);
		persona.cp_domicilio = Integer.parseInt(datosPersona[n++]);
		persona.telefono_domicilio = existeString(datosPersona[n++]);
		persona.fecha_registro = datosPersona[n++];
		persona.id_asu_um_tratante = Integer.parseInt(datosPersona[n++]);
		persona.celular = existeString(datosPersona[n++]);
		persona.ultima_actualizacion = datosPersona[n++];
		persona.id_nacionalidad = Integer.parseInt(datosPersona[n++]);
		persona.id_operadora_celular = existeInt(datosPersona[n++]);
		//persona.ultima_actualizacion = ... TesAplicacion.getAhora()???
		
		String[] datosTutor = piezas[2].split(SEPARADOR_CAMPO);
		Tutor tutor = new Tutor();
		n=0;
		tutor.id = datosTutor[n++];
		tutor.curp = datosTutor[n++];
		tutor.nombre = datosTutor[n++];
		tutor.apellido_paterno = datosTutor[n++];
		tutor.apellido_materno = datosTutor[n++];
		tutor.sexo = datosTutor[n++];
		tutor.telefono = existeString(datosTutor[n++]);
		tutor.celular = existeString(datosTutor[n++]);
		tutor.id_operadora_celular = existeInt(datosTutor[n++]);
		
		String[] datosRegistro = piezas[3].split(SEPARADOR_CAMPO);
		RegistroCivil registro = new RegistroCivil();
		n=0;
		registro.id_persona = persona.id;
		registro.id_localidad_registro_civil = Integer.parseInt(datosRegistro[n++]);
		registro.fecha_registro = datosRegistro[n++];
		
		String[] listaAlergias = piezas[4].equals("")? new String[]{} : piezas[4].split(SEPARADOR_REGISTRO);
		List<PersonaAlergia> alergias = new ArrayList<PersonaAlergia>();
		for(String regAlergia : listaAlergias){
			String[] datosAlergia = regAlergia.split(SEPARADOR_CAMPO);
			PersonaAlergia alergia = new PersonaAlergia();
			alergia.id_persona = persona.id;
			alergia.id_alergia = Integer.parseInt(datosAlergia[0]);
			alergias.add(alergia);
		}
		//TODO ¿AFILIACIÓN ES OBLIGATORIO O PUEDE ESTAR VACÍO?
		String[] datosAfiliacion = piezas[5].equals("")? new String[]{} : piezas[5].split(SEPARADOR_CAMPO);
		PersonaAfiliacion afiliacion = new PersonaAfiliacion();
		n=0;
		afiliacion.id_persona = persona.id;
		afiliacion.id_afiliacion = Integer.parseInt(datosAfiliacion[0]);
		
		String[] listaVacunas = piezas[6].equals("")? new String[]{} : piezas[6].split(SEPARADOR_REGISTRO);
		List<ControlVacuna> vacunas = new ArrayList<ControlVacuna>();
		for(String regVacuna : listaVacunas){
			String[] datosVacuna = regVacuna.split(SEPARADOR_CAMPO);
			ControlVacuna vacuna = new ControlVacuna();
			vacuna.id_persona = persona.id;
			vacuna.id_vacuna = Integer.parseInt(datosVacuna[0]);
			vacuna.fecha = datosVacuna[1];
			vacunas.add(vacuna);
		}
		
		String[] listaIras = piezas[7].equals("")? new String[]{} : piezas[7].split(SEPARADOR_REGISTRO);
		List<ControlIra> iras = new ArrayList<ControlIra>();
		for(String regIra : listaIras){
			String[] datosIra = regIra.split(SEPARADOR_CAMPO);
			ControlIra ira = new ControlIra();
			ira.id_persona = persona.id;
			ira.id_ira = Integer.parseInt(datosIra[0]);
			ira.fecha = datosIra[1];
			iras.add(ira);
		}
		
		String[] listaEdas = piezas[8].equals("")? new String[]{} : piezas[8].split(SEPARADOR_REGISTRO);
		List<ControlEda> edas = new ArrayList<ControlEda>();
		for(String regEda : listaEdas){
			String[] datosEda = regEda.split(SEPARADOR_CAMPO);
			ControlEda eda = new ControlEda();
			eda.id_persona = persona.id;
			eda.id_eda = Integer.parseInt(datosEda[0]);
			eda.fecha = datosEda[1];
			edas.add(eda);
		}
		
		String[] listaConsultas = piezas[9].equals("")? new String[]{} : piezas[9].split(SEPARADOR_REGISTRO);
		List<ControlConsulta> consultas = new ArrayList<ControlConsulta>();
		for(String regConsulta : listaConsultas){
			String[] datosConsulta = regConsulta.split(SEPARADOR_CAMPO);
			ControlConsulta consulta = new ControlConsulta();
			consulta.id_persona = persona.id;
			consulta.id_consulta = Integer.parseInt(datosConsulta[0]);
			consulta.fecha = datosConsulta[1];
			consultas.add(consulta);
		}
		
		String[] listaAcciones = piezas[10].equals("")? new String[]{} : piezas[10].split(SEPARADOR_REGISTRO);
		List<ControlAccionNutricional> acciones = new ArrayList<ControlAccionNutricional>();
		for(String regAccion : listaAcciones){
			String[] datosAccion = regAccion.split(SEPARADOR_CAMPO);
			ControlAccionNutricional accion = new ControlAccionNutricional();
			accion.id_persona = persona.id;
			accion.id_accion_nutricional = Integer.parseInt(datosAccion[0]);
			accion.fecha = datosAccion[1];
			acciones.add(accion);
		}
		
		//Si persona no tiene controles, no existirá última localidad, así que la validamos
		String[] listaControles = piezas.length<12?new String[]{} : piezas[11].split(SEPARADOR_REGISTRO);
		List<ControlNutricional> controles = new ArrayList<ControlNutricional>();
		for(String regControl : listaControles){
			String[] datosControl = regControl.split(SEPARADOR_CAMPO);
			ControlNutricional control = new ControlNutricional();
			control.id_persona = persona.id;
			control.peso = Double.parseDouble(datosControl[0]);
			control.altura = Integer.parseInt(datosControl[1]);
			control.talla = Integer.parseInt(datosControl[2]);
			control.fecha = datosControl[3];
			controles.add(control);
		}
		
		return new Sesion.DatosPaciente(persona, tutor, registro, afiliacion, alergias, 
				vacunas, iras, edas, consultas, acciones, controles);
	}//fin LeerVersion1
	
	
	public static void EscribirDatosNFC(Tag nfcTag, Sesion.DatosPaciente datos){
		//TODO escribir según metadatos de jaime
	}

	//HELPERS PARA PARSEO
	private static String existeString(String texto){return SIMBOLO_NULO.equals(texto)? null : texto;}
	private static Integer existeInt(String texto){return SIMBOLO_NULO.equals(texto)? null : Integer.parseInt(texto);}
	
	/**
	 * Intenta extraer texto plano del tag NFC recibido
	 * @param nfcTag proveedor de los datos
	 * @return cadena de texto contenida en {@link nfcTag}
	 * @throws Exception
	 */
	private static String LeerTextoPlano(Tag nfcTag) throws Exception{
		Ndef ndef = Ndef.get(nfcTag);
		if(ndef==null)return "";
		try {
			ndef.connect();
			NdefMessage mensaje = ndef.getNdefMessage();
			NdefRecord record = mensaje.getRecords()[0];
			byte[] payload = record.getPayload();
			ndef.close();
			
			int bytesHeader = 1 + "en".getBytes("US-ASCII").length;
			int bytesDatos = payload.length - bytesHeader;
			byte[] datos = new byte[bytesDatos];
			System.arraycopy(payload, bytesHeader, datos, 0, bytesDatos);
			
			return new String(datos);
		} catch (IOException e) {
			throw new Exception("No se pudo leer la tarjeta:"+e);
		} catch (FormatException e) {
			throw new Exception("Mal formato al leer texto plano"+e);
		}

	}
	
	/**
	 * Escribe el texto recibido en un tag nfc
	 * @param nfcTag
	 * @param texto
	 * @throws Exception
	 */
	private static void EscribirTextoPlano(Tag nfcTag, String texto) throws Exception{
		NdefRecord[] records = { crearRecordNfc(texto) };
		NdefMessage  mensaje = new NdefMessage(records);
		
		Ndef ndef = Ndef.get(nfcTag);
		// vemos si el tag tiene formato al validar nulo
		if (ndef != null) {
			ndef.connect();

			if (!ndef.isWritable())
				throw new Exception("La tarjeta es de SOLO lectura");
			
			// validamos si hay espacio suficiente
			int size = mensaje.getByteArrayLength();
			if (ndef.getMaxSize() < size)
				throw new Exception("No hay suficiente espacio en tarjeta para guardar información");

			ndef.writeNdefMessage(mensaje);
			ndef.close();
		} else {
			// Intentamos formatear tarjeta
			//Nunca debería ocurrir pues escribimos en tarjetas previamente leídas correctamente
			NdefFormatable format = NdefFormatable.get(nfcTag);
			if (format != null) {
				try {
					format.connect();
					format.format(mensaje);
					format.close();
				} catch (IOException e) {
					format.close();
					throw new Exception("No se pudo formatear tarjeta al intentar escribir:"+ e);
				}
			} else {
				throw new Exception("Tarjeta no parece soportar formato NDEF.");
			}
		}
	}
	
	/**
	 * Crea un record nfc para escribirlo en un tag nfc con el texto recibido
	 * @param texto
	 * @return
	 */
	private static NdefRecord crearRecordNfc(String texto) {
		byte[] bytesTexto  = texto.getBytes();
		byte[] bytesIdioma=null;
		try {
			bytesIdioma = "en".getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// Nunca debería pasar
		}
		byte[] payload    = new byte[1 + bytesIdioma.length + bytesTexto.length];

		// set status byte (see NDEF spec for actual bits)
		payload[0] = (byte) bytesIdioma.length;

		// copia bytesLang y bytesTexto a payload
		System.arraycopy(bytesIdioma,  0, payload, 1                     , bytesIdioma.length);
		System.arraycopy(bytesTexto, 0,   payload, 1 + bytesIdioma.length, bytesTexto.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
	}
	
	/**
	 * VERSIÓN 1 de metadatos ----------------
	 * VERSIONBD
persona
  `id` CHAR(32)*******
  `curp` VARCHAR(18)
  `nombre` VARCHAR(35)
  `apellido_paterno` VARCHAR(20)
  `apellido_materno` VARCHAR(20)
  `sexo` CHAR(1)
  `id_ece_tipo_sanguineo` INT(1)
  `fecha_nacimiento` DATE
  `id_localidad_nacimiento` INT(10)
  `calle_domicilio` VARCHAR(60)
  `numero_domicilio` VARCHAR(10)
  `colonia_domicilio` VARCHAR(30)
  `referencia_domicilio` VARCHAR(60)
  `id_localidad_domicilio` INT(6)
  `cp_domicilio` INT(5)
  `telefono_domicilio` VARCHAR(20)
  `fecha_registro` DATE 
  `id_um_tratante` INT(10)
  `celuar` VARCHAR(20) 
  `ultima_actualizacion` DATETIME
  `id_ece_nacionalidad` INT(3)
  `id_operadora_celular` INT(2)
  `ultima_actualizacion` INT(2)

tutor
  `id` CHAR(32) 
  `curp` VARCHAR(18)
  `nombre` VARCHAR(35) 
  `apellido_paterno` VARCHAR(20)
  `apellido_materno` VARCHAR(20) 
  `sexo` char(1) 
  `telefono` VARCHAR(20)
  `celular` VARCHAR(20) 
  `id_operadora_celular` INT(2)

registro_civil
  `id_localidad_registro_civil`
  `fecha_registro` DATE

persona_x_alergia*** UNA ENTRADA POR CADA ALERGIA
  `id_alergia` INT(4)

persona_x_afiliacion*** UNA ENTRADA POR CADA AfILIACION
  `id_afiliacion` INT(2)

control_vacuna*** UNA ENTRADA POR CADA VACUNA
  `id_ece_vacuna` INT(2) 
  `fecha` DATE

control_ira*** UNA ENTRADA POR CADA ENFERMEDAD
  `id_ece_ira` INT(3) 
  `fecha` DATE 

control_eda*** UNA ENTRADA POR CADA ENFERMEDAD
  `id_ece_eda` INT(3) 
  `fecha` DATE  

control_consulta*** UNA ENTRADA POR CADA ENFERMEDAD
  `id_ece_consulta` INT(3) 
  `fecha` DATE 

control_accion_nutricional*** UNA ENTRADA POR CADA CONTROL
  `id_ece_accion_nutricional` INT(2) 
  `fecha` DATETIME 

control_nutricional*** UNA ENTRADA POR CADA CONTROL
  `peso` DECIMAL(5,2) 
  `altura` INT(3)
  `talla` INT(3) 
  `fecha` DATETIME
	 */
}
