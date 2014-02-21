package com.siigs.tes.datos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonSyntaxException;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
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
	
	/**
	 * Ejecuta lectura ...
	 * @param nfcTag
	 * @param contexto
	 * @return
	 * @throws Exception
	 */
	public static List<PendientesTarjeta> LeerDatosNFC(Tag nfcTag, Context contexto) throws Exception{
		TesAplicacion aplicacion = (TesAplicacion)contexto.getApplicationContext();
		
		Sesion.DatosPaciente datosPaciente = getDatosPaciente(nfcTag);
		
		//Checamos si hay pendientes para el paciente
		List<PendientesTarjeta> pendientesResueltos = validarPendientesResueltos(contexto, datosPaciente);
		
		//Ponemos este paciente le�do como el actual en atenci�n
		aplicacion.getSesion().setDatosPacienteNuevo(datosPaciente);
		
		return pendientesResueltos;
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
		String[] piezas = separar(contenido, SEPARADOR_TABLA);
				
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
		String[] datosPersona = separar(piezas[1], SEPARADOR_CAMPO);
		Persona persona = new Persona();
		int n=0;
		persona.id = datosPersona[n++];
		persona.curp = existeString(datosPersona[n++]);
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
		persona.ageb = existeString(datosPersona[n++]);
		persona.manzana = existeString(datosPersona[n++]);
		persona.sector = existeString(datosPersona[n++]);
		persona.id_asu_localidad_domicilio = Integer.parseInt(datosPersona[n++]);
		persona.cp_domicilio = existeInt(datosPersona[n++]);
		persona.telefono_domicilio = existeString(datosPersona[n++]);
		persona.fecha_registro = datosPersona[n++];
		persona.id_asu_um_tratante = Integer.parseInt(datosPersona[n++]);
		persona.celular = existeString(datosPersona[n++]);
		persona.ultima_actualizacion = datosPersona[n++];
		persona.id_nacionalidad = Integer.parseInt(datosPersona[n++]);
		persona.id_operadora_celular = existeInt(datosPersona[n++]);
		
		String[] datosTutor = separar(piezas[2], SEPARADOR_CAMPO);
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
		
		String[] datosRegistro = separar(piezas[3], SEPARADOR_CAMPO);
		RegistroCivil registro = new RegistroCivil();
		n=0;
		registro.id_persona = persona.id;
		registro.id_localidad_registro_civil = Integer.parseInt(datosRegistro[n++]);
		registro.fecha_registro = datosRegistro[n++];
		
		String[] listaAlergias = piezas[4].equals("")? new String[]{} : separar(piezas[4], SEPARADOR_REGISTRO);
		List<PersonaAlergia> alergias = new ArrayList<PersonaAlergia>();
		for(String regAlergia : listaAlergias){
			String[] datosAlergia = separar(regAlergia, SEPARADOR_CAMPO);
			PersonaAlergia alergia = new PersonaAlergia();
			alergia.id_persona = persona.id;
			alergia.id_alergia = Integer.parseInt(datosAlergia[0]);
			alergias.add(alergia);
		}
		
		String[] listaAfiliaciones = piezas[5].equals("")? new String[]{} : separar(piezas[5], SEPARADOR_REGISTRO);
		List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		for(String regAfiliacion : listaAfiliaciones){
			String[] datosAfiliacion = separar(regAfiliacion, SEPARADOR_CAMPO);
			PersonaAfiliacion afiliacion = new PersonaAfiliacion();
			afiliacion.id_persona = persona.id;
			afiliacion.id_afiliacion = Integer.parseInt(datosAfiliacion[0]);
			afiliaciones.add(afiliacion);
		}
		
		String[] listaVacunas = piezas[6].equals("")? new String[]{} : separar(piezas[6], SEPARADOR_REGISTRO);
		List<ControlVacuna> vacunas = new ArrayList<ControlVacuna>();
		for(String regVacuna : listaVacunas){
			String[] datosVacuna = separar(regVacuna, SEPARADOR_CAMPO);
			ControlVacuna vacuna = new ControlVacuna();
			vacuna.id_persona = persona.id;
			vacuna.id_vacuna = Integer.parseInt(datosVacuna[0]);
			vacuna.fecha = datosVacuna[1];
			vacuna.id_asu_um = Integer.parseInt(datosVacuna[2]);
			vacunas.add(vacuna);
		}
		
		//Reservado para Iras

		//Reservado para Edas
		
		//Reservado para Consultas

		//Reservado para Acciones Nutricionales

		//Reservado para Controles Nutricionales
		
		return new Sesion.DatosPaciente(persona, tutor, registro, alergias, afiliaciones,
				vacunas, true);
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
		salida.append(convertirString(datos.persona.curp) + SEPARADOR_CAMPO);
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
		salida.append(convertirString(datos.persona.ageb) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.manzana) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.sector) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.id_asu_localidad_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.cp_domicilio) + SEPARADOR_CAMPO);
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
			salida.append(datos.vacunas.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.vacunas.get(i).id_asu_um);
			if(i!=datos.vacunas.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Reservado para Datos iras
		salida.append(SEPARADOR_TABLA);
		
		//Reservado para Datos edas
		salida.append(SEPARADOR_TABLA);
		
		//Reservado para Datos consultas
		salida.append(SEPARADOR_TABLA);
		
		//Reservado para Datos acciones nutricionales
		salida.append(SEPARADOR_TABLA);
		
		//Reservado para Datos controles nutricionales
		//salida.append(SEPARADOR_TABLA);
		
		//Espacio disponible previa reservaci�n de espacio
		
		EscribirTextoPlano(nfcTag, salida.toString());
	}

	//HELPERS PARA PARSEO
	private static String existeString(String texto){return SIMBOLO_NULO.equals(texto)? null : texto;}
	private static Integer existeInt(String texto){return SIMBOLO_NULO.equals(texto)? null : Integer.parseInt(texto);}
	private static String convertirString(String texto){return texto == null? SIMBOLO_NULO : texto;}
	private static String convertirInt(Integer numero){return numero == null? SIMBOLO_NULO : numero+"";}
	
	private static String[] separar(String cadenaSeparar, String simbolo){
		List<String> salida = new ArrayList<String>();
		int indexSimbolo=-1, indexInicioCopia=0;
		while( (indexSimbolo=cadenaSeparar.indexOf(simbolo, indexInicioCopia)) >=0 ){
			salida.add(cadenaSeparar.substring(indexInicioCopia, indexSimbolo));
			indexInicioCopia = indexSimbolo+1;
		}
		if(salida.size()>0) //si encontr� al menos uno, puede pasar una de dos ...
			if(cadenaSeparar.endsWith(simbolo)) //... acaba con simbolo, y por tanto a la derecha hay vac�o
				salida.add(""); //... lo remarcamos as�
			else
				salida.add(cadenaSeparar.substring(indexInicioCopia));  //... recuperamos el �ltimo pedazo que no captur�
		if(salida.size()==0)return new String[]{cadenaSeparar};
		return salida.toArray(new String[]{});
	}
	
	//HELPERS PARA PENDIENTES
	/**
	 * Verifica los pendientes por resolver para el paciente definido en <b>datosPaciente</b>
	 * @param contexto
	 * @param datosPaciente
	 * @return
	 */
	private static List<PendientesTarjeta> validarPendientesResueltos(Context contexto, Sesion.DatosPaciente datosPaciente){
		//Checamos si hay pendientes para el paciente
		List<PendientesTarjeta> pendientesResueltos = new ArrayList<PendientesTarjeta>();
		List<PendientesTarjeta> pendientesResolver = 
				PendientesTarjeta.getPendientesPaciente(contexto, datosPaciente.persona.id);
		if(pendientesResolver.size()>0){
			for(PendientesTarjeta pendiente : pendientesResolver){ 
				try{
					if(pendiente.tabla.equals(ControlVacuna.NOMBRE_TABLA)){
						ControlVacuna nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlVacuna.class);
						if(!InsertarEnLista(nuevo, datosPaciente.vacunas))
							continue;
						
						//ESTOS ELEMENTOS SE AGREGAN DIRECTO PUES NO HAY ORDEN ESPEC�FICO REQUERIDO
					}else if(pendiente.tabla.equals(PersonaAlergia.NOMBRE_TABLA)){
						PersonaAlergia nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, PersonaAlergia.class);
						if(datosPaciente.alergias.contains(nuevo))continue;
						else datosPaciente.alergias.add(nuevo);
					}else if(pendiente.tabla.equals(PersonaAfiliacion.NOMBRE_TABLA)){
						PersonaAfiliacion nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, PersonaAfiliacion.class);
						if(datosPaciente.afiliaciones.contains(nuevo))continue;
						else datosPaciente.afiliaciones.add(nuevo);
						//PENDIENTES DE ASIGNACI�N DIRECTA (SIN LISTA)
					}else if(pendiente.tabla.equals(RegistroCivil.NOMBRE_TABLA)){
						RegistroCivil nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, RegistroCivil.class);
						datosPaciente.registroCivil = nuevo;
					}else if(pendiente.tabla.equals(Tutor.NOMBRE_TABLA)){
						Tutor nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, Tutor.class);
						if(esFechaHoraMenor(datosPaciente.tutor.ultima_actualizacion, nuevo.ultima_actualizacion))
							datosPaciente.tutor = nuevo;
						else continue;
					}else if(pendiente.tabla.equals(Persona.NOMBRE_TABLA)){
						Persona nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, Persona.class);
						if(esFechaHoraMenor(datosPaciente.persona.ultima_actualizacion, nuevo.ultima_actualizacion))
							datosPaciente.persona = nuevo;
						else continue;
					}else{
						continue; //ESTO NUNCA DEBER�A SUCEDER a menos que se tratara de un tipo de pendiente muy nuevo en app vieja 
					}
					pendientesResueltos.add(pendiente);
					//PendientesTarjeta.MarcarPendienteResuelto(contexto, pendiente);
					
				}catch(JsonSyntaxException jse){
					int idUsuario=((TesAplicacion)contexto.getApplicationContext()).getSesion().getUsuario()._id;
					ErrorSis.AgregarError(contexto, idUsuario, ErrorSis.ERROR_DESCONOCIDO, "Json incorrecto en pendiente de persona:"+
							pendiente.id_persona+", fecha:"+pendiente.fecha+", tabla:"+pendiente.tabla);
				}
				
			}//fin ciclo pendientes
			
			//Esta l�nea no se puede ejecutar pues la tarjeta fue le�da previamente y hay que pasarla de nuevo
			//f�sicamente antes de poder escribir. En consecuencia tampoco se puede marcar como resuelto su pendiente a�n
			//EscribirDatosNFC(nfcTag, datosPaciente);
		}//fin si hay pendientes
		return pendientesResueltos;
	}
	
	/**
	 * Inserta objeto en lista en posici�n ordenada de menor a mayor.
	 * Si objeto tiene una fecha menor que otros elementos de lista, se insertar� en medio
	 * NOTA: Esta implementaci�n improvisada puede cambiarse por una llamada a 
	 * Collections.sort(lista, new InstanciaComparador() ) donde InstanciaComparador es
	 * una clase que implementa Comparator<T>{Compare();}
	 * @param objeto
	 * @param lista
	 */
	private static <T> boolean InsertarEnLista(T objeto, List<T> lista){
		if(lista.contains(objeto))return false; //pues no es necesario agregarlo m�s veces
		
		int indice;
		for(indice=0; indice < lista.size(); indice++){
			
			if(objeto instanceof ControlVacuna){
				if(esFechaHoraMenor( ((ControlVacuna) objeto).fecha, 
						((ControlVacuna)lista.get(indice)).fecha) )
					break;
			}//else if(...){}
		}
		//Inserta objeto en la posici�n adecuada seg�n su fecha
		lista.add(indice, objeto);
		return true;
	}
	//Helper para convertir tiempo
	private static boolean esFechaHoraMenor(String fecha1, String feccha2){
		try{
			return DatosUtil.esFechaHoraMenor(fecha1, feccha2);
		}catch(Exception e){
			return false;}
	}
	
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
	

}
