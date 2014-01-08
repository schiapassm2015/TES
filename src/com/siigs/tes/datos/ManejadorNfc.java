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
	private static final String SEPARADOR_REGISTRO = "�";
	private static final String SEPARADOR_CAMPO = "=";
	private static final String SIMBOLO_NULO = "�";
	
	//VERSIONES POSIBLES DE DATOS A CONSIDERAR
	private static final String VERSION_1 ="1";
	
	public static void LeerDatosNFC(Tag nfcTag, Context contexto) throws Exception{
		TesAplicacion aplicacion = (TesAplicacion)contexto.getApplicationContext();
		
		Sesion.DatosPaciente datosPaciente = getDatosPaciente(nfcTag);
		
		//TODO checar si tiene pendientes, insertar en datosPaciente, actualizar tes_pendientes si le hice algo
		//TODO 			si agregu� pendientes, reescribir DatosPaciente en NFC
		
		aplicacion.getSesion().setDatosPacienteNuevo(datosPaciente);
	}
	
	public static boolean nfcTagPerteneceApersona(String idUsuarioValidar, Tag nfcTag){
		try{
			return getDatosPaciente(nfcTag).persona.id.equals(idUsuarioValidar);
		}catch(Exception e){return false;}
	}
	
	/**
	 * Genera informaci�n de paciente con el tag recibido seg�n la versi�n del contenido.
	 * @param nfcTag tag que contiene la informaci�n a leer
	 * @return
	 * @throws Exception
	 */
	private static Sesion.DatosPaciente getDatosPaciente(Tag nfcTag) throws Exception{
		String contenido = LeerTextoPlano(nfcTag);
		String[] piezas = contenido.split(SEPARADOR_TABLA);
				
		Sesion.DatosPaciente datosPaciente=null;
		String version = piezas[0];
		if(version.equals(VERSION_1)){
			datosPaciente = LeerVersion1(piezas, version);
		} //else if(version.equals(VERSION_X)){}
		else{
			throw new Exception("Versi�n de datos no reconocida");
		}
		
		return datosPaciente;
	}
	
	/**
	 * Parsea las piezas de elementos recibidos en tarjeta de acuerdo
	 * a esta versi�n.
	 * @param piezas Cadenas que representan tablas/elementos de acuerdo
	 * modelo de metadatos descrito en los comentarios al fondo de este archivo
	 * @param version No se usa actualmente, pero si en el futuro hubiera una  versi�n
	 * nueva cuya diferencia al m�todo de parseo de �sta es de unos pocos detalles,
	 * en pocos puntos del c�digo, esta funci�n puede recodificarse agregando 
	 * las funcionalidades correspondientes usando {@lilnk version} para discriminar.
	 * Sin embargo si los cambios de parseo/comportamiento entre una versi�n y otra
	 * fueran muy amplios, ser�a mejor escribir una funci�n nueva que haga
	 * sus propias implementaciones y agregar un "else if()" para llamar dicha
	 * funci�n nueva desde LeerDatosNFC()
	 */
	private static Sesion.DatosPaciente LeerVersion1(String[] piezas, String version){
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
		
		String[] listaAfiliaciones = piezas[5].equals("")? new String[]{} : piezas[5].split(SEPARADOR_REGISTRO);
		List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		for(String regAfiliacion : listaAfiliaciones){
			String[] datosAfiliacion = regAfiliacion.split(SEPARADOR_CAMPO);
			PersonaAfiliacion afiliacion = new PersonaAfiliacion();
			afiliacion.id_persona = persona.id;
			afiliacion.id_afiliacion = Integer.parseInt(datosAfiliacion[0]);
			afiliaciones.add(afiliacion);
		}
		
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
		
		//Si persona no tiene controles, no existir� �ltima localidad, as� que la validamos
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
		
		return new Sesion.DatosPaciente(persona, tutor, registro, alergias, afiliaciones,
				vacunas, iras, edas, consultas, acciones, controles);
	}//fin LeerVersion1
	
	/**
	 * Convierte los datos del paciente en String con formato adecuado para guardarse
	 * en nfcTag.
	 * En caso de que la versi�n de base de datos cambie, se deber� cambiar esta funci�n
	 * al menos en la primera l�nea que imprime VERSION_1 y lo que sea necesario despu�s.
	 * As� mismo un cambio de versi�n requerir� cambios en la funci�n de lectura.
	 * @param nfcTag
	 * @param datos
	 * @throws Exception 
	 */
	public static void EscribirDatosNFC(Tag nfcTag, Sesion.DatosPaciente datos) throws Exception{
		StringBuilder salida = new StringBuilder();
		salida.append(VERSION_1+SEPARADOR_TABLA);
		
		//Datos de persona
		salida.append(datos.persona.id + SEPARADOR_CAMPO);
		salida.append(datos.persona.curp + SEPARADOR_CAMPO);
		salida.append(datos.persona.nombre + SEPARADOR_CAMPO);
		salida.append(datos.persona.apellido_paterno + SEPARADOR_CAMPO);
		salida.append(datos.persona.apellido_materno + SEPARADOR_CAMPO);
		salida.append(datos.persona.sexo + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_tipo_sanguineo + SEPARADOR_CAMPO);
		salida.append(datos.persona.fecha_nacimiento + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_asu_localidad_nacimiento + SEPARADOR_CAMPO);
		salida.append(datos.persona.calle_domicilio + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.numero_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.colonia_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.referencia_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.id_asu_localidad_domicilio) + SEPARADOR_CAMPO);
		salida.append(datos.persona.cp_domicilio + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.telefono_domicilio) + SEPARADOR_CAMPO);
		salida.append(datos.persona.fecha_registro + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_asu_um_tratante + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.celular) + SEPARADOR_CAMPO);
		salida.append(datos.persona.ultima_actualizacion + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_nacionalidad + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.id_operadora_celular) + SEPARADOR_TABLA);
		
		//Datos de tutor
		salida.append(datos.tutor.id + SEPARADOR_CAMPO);
		salida.append(datos.tutor.curp + SEPARADOR_CAMPO);
		salida.append(datos.tutor.nombre + SEPARADOR_CAMPO);
		salida.append(datos.tutor.apellido_paterno + SEPARADOR_CAMPO);
		salida.append(datos.tutor.apellido_materno + SEPARADOR_CAMPO);
		salida.append(datos.tutor.sexo + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.tutor.telefono) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.tutor.celular) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.tutor.id_operadora_celular) + SEPARADOR_TABLA);
		
		//Datos de registro civil
		salida.append(datos.registroCivil.id_localidad_registro_civil + SEPARADOR_CAMPO);
		salida.append(datos.registroCivil.fecha_registro + SEPARADOR_TABLA);
		
		//Datos de alergias
		for(int i=0;i<datos.alergias.size();i++){
			salida.append(datos.alergias.get(i).id_alergia);
			if(i!=datos.alergias.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos afiliaci�n
		for(int i=0;i<datos.afiliaciones.size();i++){
			salida.append(datos.afiliaciones.get(i).id_afiliacion);
			if(i!=datos.afiliaciones.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos vacunas
		for(int i=0;i<datos.vacunas.size();i++){
			salida.append(datos.vacunas.get(i).id_vacuna + SEPARADOR_CAMPO);
			salida.append(datos.vacunas.get(i).fecha);
			if(i!=datos.vacunas.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos iras
		for(int i=0;i<datos.iras.size();i++){
			salida.append(datos.iras.get(i).id_ira + SEPARADOR_CAMPO);
			salida.append(datos.iras.get(i).fecha);
			if(i!=datos.iras.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos edas
		for (int i = 0; i < datos.edas.size(); i++) {
			salida.append(datos.edas.get(i).id_eda + SEPARADOR_CAMPO);
			salida.append(datos.edas.get(i).fecha);
			if (i != datos.edas.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos consultas
		for (int i = 0; i < datos.consultas.size(); i++) {
			salida.append(datos.consultas.get(i).id_consulta + SEPARADOR_CAMPO);
			salida.append(datos.consultas.get(i).fecha);
			if (i != datos.consultas.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos acciones nutricionales
		for (int i = 0; i < datos.accionesNutricionales.size(); i++) {
			salida.append(datos.accionesNutricionales.get(i).id_accion_nutricional + SEPARADOR_CAMPO);
			salida.append(datos.accionesNutricionales.get(i).fecha);
			if (i != datos.accionesNutricionales.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos controles nutricionales
		for (int i = 0; i < datos.controlesNutricionales.size(); i++) {
			salida.append(datos.controlesNutricionales.get(i).peso + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).altura + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).talla + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).fecha);
			if (i != datos.controlesNutricionales.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		EscribirTextoPlano(nfcTag, salida.toString());
	}

	//HELPERS PARA PARSEO
	private static String existeString(String texto){return SIMBOLO_NULO.equals(texto)? null : texto;}
	private static Integer existeInt(String texto){return SIMBOLO_NULO.equals(texto)? null : Integer.parseInt(texto);}
	private static String convertirString(String texto){return texto == null? SIMBOLO_NULO : texto;}
	private static String convertirInt(Integer numero){return numero == null? SIMBOLO_NULO : numero+"";}
	
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
			ndef.close();
			NdefRecord record = mensaje.getRecords()[0];
			byte[] payload = record.getPayload();
			
			int bytesHeader = 1 + "en".getBytes("US-ASCII").length;
			int bytesDatos = payload.length - bytesHeader;
			byte[] datos = new byte[bytesDatos];
			System.arraycopy(payload, bytesHeader, datos, 0, bytesDatos);
			
			return new String(datos);
		} catch (IOException e) {
			if(ndef!=null && ndef.isConnected())ndef.close();
			throw new Exception("No se pudo leer la tarjeta:"+e);
		} catch (FormatException e) {
			if(ndef!=null && ndef.isConnected())ndef.close();
			throw new Exception("Mal formato al leer texto plano"+e);
		} catch (Exception e){
			if(ndef!=null && ndef.isConnected())ndef.close();
			throw new Exception("Tarjeta no reconocida al leer:"+e);
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
				throw new Exception("No hay suficiente espacio en tarjeta para guardar informaci�n");

			ndef.writeNdefMessage(mensaje);
			ndef.close();
		} else {
			// Intentamos formatear tarjeta
			//Nunca deber�a ocurrir pues escribimos en tarjetas previamente le�das correctamente
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
			// Nunca deber�a pasar
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
	 * VERSI�N 1 de metadatos ----------------
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
