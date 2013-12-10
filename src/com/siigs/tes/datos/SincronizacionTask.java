package com.siigs.tes.datos;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.*;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Axel
 * 
 * Los 3 tipos de datos son <Parametros, Progreso, Resultado>
 * 
 * El parseo de JSON se hace con la librería de Android. Existe la posibilidad de que
 * el texto recibido en post sea más grande que la capacidad de String o simplemente lo
 * bastante grande para que dicho parseo cause excepciones por falta de memoria.
 * Si eso pasara es necesario parsear desde el stream del post en vez de convertirlo a String.
 * Dado que es stream, hay que cambiar a una de dos: 
 * 1) Usar la clase android.util.JsonReader
 * 2) Usar gson {@link https://sites.google.com/site/gson/streaming}
 */
public class SincronizacionTask extends AsyncTask<String, Integer, String> {

	private final static String TAG= SincronizacionTask.class.getSimpleName();
		
	//Constantes de acciones en sincronización
	private final static String PARAMETRO_ID_TAB="id_tab";
	private final static String PARAMETRO_VERSION_APK = "version_apk";
	private final static String PARAMETRO_SESION="id_sesion";
	private final static String PARAMETRO_ACCION ="id_accion";
	private final static String PARAMETRO_RESULTADO_MSG = "msg";
	private final static String PARAMETRO_DATOS = "datos";
	private final static String RESULTADO_OK = "ok";
	private final static String ACCION_INICIAR_SESION="1";
	private final static String ACCION_PRIMEROS_CATALOGOS="2";
	private final static String ACCION_RESULTADO_PRIMEROS_CATALOGOS="3"; //Para informar resultado de una operación (ok/error/etc)
	private final static String ACCION_PRIMEROS_DATOS = "4";
	private final static String ACCION_RESULTADO_PRIMEROS_DATOS ="4.1"; //Para informar resultado de una operación (ok/error/etc)
	private final static String ACCION_ENVIAR_SERVIDOR = "5";
		
	//Estados de comunicacióni HTTP
	private final static int HTTP_STATUS_OK = 200;
	private final static int HTTP_STATUS_NOT_FOUND = 404;
	
	
	private ProgressDialog pdProgreso;
	private Context contexto;
	private Activity invocador;
	private TesAplicacion aplicacion;
	
	private HttpHelper webHelper;
		
	
	public SincronizacionTask(Activity invocador){
		super();
		this.invocador=invocador;
		this.aplicacion = (TesAplicacion)invocador.getApplication();
		this.contexto=invocador.getApplicationContext();
		this.webHelper = new HttpHelper();
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 * Se ejecuta en Thread de UI, así que mostramos diálogo de progreso antes de comenzar tarea asíncrona.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		boolean indeterminado=true, cancelable=false;
		pdProgreso= ProgressDialog.show(invocador, "Título PreExecute", 
				"pre-ejecutando",indeterminado, cancelable);
	}


	/**
	 * Ejecutado en hilo asíncrono. Recibe uno o más parámetros en forma de arreglo ej: parametros[0]
	 */
	@Override
	protected String doInBackground(String... parametros) {
		try{
			Log.d(TAG, "Sincronización en fondo "+ Thread.currentThread().getName());
			SincronizacionTotal();
		}catch(Exception ex){
			Log.d(TAG, "Error:"+ex.toString());
		}
		return null;
	}

	/**
	 * Se ejecuta en Thread de UI. Quitamos el diálogo de progreso.
	 */
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		Log.d(TAG, "Terminado proceso en fondo "+ Thread.currentThread().getName());
		pdProgreso.dismiss();
	} 	
	

	protected synchronized void SincronizacionTotal(){
		aplicacion.setUrlSincronizacion("http://192.168.1.16/tes/servicios/prueba");///////BORRRRRRARRRR
		
		boolean esNueva= this.aplicacion.getEsInstalacionNueva();

		String idSesion = AccionIniciarSesion();
		Log.d(TAG,"Se ha obtenido llave de sesión:"+idSesion);
		
		if(esNueva){
			AccionPrimerosCatalogos(idSesion);
			AccionPrimerosDatos(idSesion);
			this.aplicacion.setEsInstalacionNueva(false);
			this.aplicacion.setFechaUltimaSincronizacion();
		}else{
			String ultimaSinc = this.aplicacion.getFechaUltimaSincronizacion();
			AccionEnviarCambiosServidor(idSesion, ultimaSinc);
		}
 	}

	/**
	 * Informa de resultados al servidor.
	 * @param idSesion Sesión que se lleva a cabo
	 * @param idAccion La acción que identifica el resultado que enviamos
	 * @param idResultado Identificador del tipo de resultado (éxito o error) que informamos
	 * @param descripcion Describe a detalle la causa de idResultado en caso de tratarse de un error
	 * @return
	 */
	private String EnviarResultado(String idSesion, String idAccion, String idResultado, String descripcion){
		JSONObject msgSalida=new JSONObject();
		try {
			msgSalida.put("id_resultado", idResultado);
			msgSalida.put("descripcion", descripcion == null?"":descripcion);
		} catch (JSONException e) {
			Log.e(TAG, "Esto nunca en la vida debería suceder. No se pudo encapsular resultado en JSON."+e.toString());
		}
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
		parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, idAccion));
        parametros.add(new BasicNameValuePair(PARAMETRO_RESULTADO_MSG, msgSalida.toString() ));
		
        Log.d(TAG,"Enviando resultado en acción:" + idAccion +" con mensaje:"+ idResultado+ " y descripción:"+descripcion);
		return webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
	}
	
	/**
	 * Esta acción recibe como respuesta una cadena JSON con el id de la sesión de sincronización
	 * @return id de la sesión
	 * @throws Exception 
	 */
	private String AccionIniciarSesion() {
		WifiManager wifi=(WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		String macaddress = wifi.getConnectionInfo().getMacAddress();
		if(macaddress == null)macaddress="";
		macaddress = macaddress.replace(":", "");
		macaddress= "123456789"; /////////////////////////BORRRRRRRRARRRRRR
		Log.d(TAG, "mac es:"+macaddress);
		
		String version="";
		try {
			version += contexto.getPackageManager().getPackageInfo(
					contexto.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e1) {
			version="imposible determinar version";
		}
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_INICIAR_SESION));
        parametros.add(new BasicNameValuePair(PARAMETRO_ID_TAB, macaddress));
        parametros.add(new BasicNameValuePair(PARAMETRO_VERSION_APK, version+"" ));
		
        Log.d(TAG,"Request Inicio de sesión");
		String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		
		try {
			JSONObject jo=new JSONObject(json);
			return jo.getString(PARAMETRO_SESION);
		} catch (JSONException e) {
			Log.e(TAG, "No se interpretó llave sesión en json:"+json+" "+e.toString());
		}
		return null;
	}//fin AccionIniciarSesion
	
	
	/**
	 * Esta acción manda credenciales y recibe primeros catálogos a insertar en base de datos
	 * @param idSesion
	 */
	private void AccionPrimerosCatalogos(String idSesion){
		String msgError = null;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_CATALOGOS));

        Log.d(TAG, "Request primeros catálogos");
		String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		
		try {
			JSONObject jo=new JSONObject(json);
			
			//ACTUALIZAMOS VARIABLES DE ENTORNO
			aplicacion.setTipoCenso( jo.getInt("id_tipo_censo") );
			aplicacion.setUnidadMedica( jo.getInt("id_asu_um") );
			
			
			//TABLAS GENERALES DEL SISTEMA
			ContentResolver cr = contexto.getContentResolver();
			ContentValues[] filas; //parámetros en filas para inserciones múltiples
			
			//TABLA GRUPOS
			JSONArray grupos = jo.getJSONArray(Grupo.NOMBRE_TABLA);
			filas=new ContentValues[grupos.length()];
			for(int i=0;i<grupos.length();i++){
				JSONObject grupo=grupos.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Grupo.ID, grupo.getInt(Grupo._REMOTO_ID));
				filas[i].put(Grupo.NOMBRE, grupo.getString(Grupo.NOMBRE));
				if(!grupo.isNull(Grupo.DESCRIPCION))
					filas[i].put(Grupo.DESCRIPCION, grupo.getString(Grupo.DESCRIPCION)); 
			}
			cr.bulkInsert(ProveedorContenido.GRUPO_CONTENT_URI, filas);
			
			//TABLA USUARIOS
			JSONArray usuarios = jo.getJSONArray(Usuario.NOMBRE_TABLA);
			filas=new ContentValues[usuarios.length()];
			for(int i=0;i<usuarios.length();i++){
				JSONObject usuario=usuarios.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Usuario.ID, usuario.getInt(Usuario._REMOTO_ID));
				filas[i].put(Usuario.NOMBRE_USUARIO, usuario.getString(Usuario.NOMBRE_USUARIO));
				filas[i].put(Usuario.CLAVE, usuario.getString(Usuario.CLAVE));
				filas[i].put(Usuario.NOMBRE, usuario.getString(Usuario.NOMBRE));
				filas[i].put(Usuario.APELLIDO_PATERNO, usuario.getString(Usuario.APELLIDO_PATERNO));
				filas[i].put(Usuario.APELLIDO_MATERNO, usuario.getString(Usuario.APELLIDO_MATERNO));
				filas[i].put(Usuario.CORREO, usuario.getString(Usuario.CORREO));
				filas[i].put(Usuario.ACTIVO, usuario.getInt(Usuario.ACTIVO));
				filas[i].put(Usuario.ID_GRUPO, usuario.getInt(Usuario.ID_GRUPO));
			}
			cr.bulkInsert(ProveedorContenido.USUARIO_CONTENT_URI, filas);
			
			//TABLA PERMISOS
			JSONArray permisos = jo.getJSONArray(Permiso.NOMBRE_TABLA);
			filas=new ContentValues[permisos.length()];
			for(int i=0;i<permisos.length();i++){
				JSONObject permiso=permisos.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Permiso.ID, permiso.getInt(Permiso._REMOTO_ID));
				filas[i].put(Permiso.ID_GRUPO, permiso.getInt(Permiso.ID_GRUPO));
				filas[i].put(Permiso.ID_CONTROLADOR_ACCION, permiso.getInt(Permiso.ID_CONTROLADOR_ACCION));
				filas[i].put(Permiso.FECHA, permiso.getString(Permiso.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.PERMISO_CONTENT_URI, filas);
			
			//TABLA NOTIFICACIONES
			JSONArray notificaciones = jo.optJSONArray(Notificacion.NOMBRE_TABLA);
			if(notificaciones!=null){
				filas=new ContentValues[notificaciones.length()];
				for(int i=0;i<notificaciones.length();i++){
					JSONObject notificacion=notificaciones.getJSONObject(i);
					filas[i]=new ContentValues();
					filas[i].put(Notificacion.ID, notificacion.getInt(Notificacion._REMOTO_ID));
					filas[i].put(Notificacion.TITULO, notificacion.getString(Notificacion.TITULO));
					filas[i].put(Notificacion.CONTENIDO, notificacion.getString(Notificacion.CONTENIDO));
					filas[i].put(Notificacion.FECHA_INICIO, notificacion.getString(Notificacion.FECHA_INICIO));
					if(!notificacion.isNull(Notificacion.FECHA_FIN))
						filas[i].put(Notificacion.FECHA_FIN, notificacion.getString(Notificacion.FECHA_FIN));
				}
				cr.bulkInsert(ProveedorContenido.NOTIFICACION_CONTENT_URI, filas);
			}
			
			//TABLA TIPO SANGUINEO
			JSONArray tiposSangre = jo.getJSONArray(TipoSanguineo.NOMBRE_TABLA);
			filas=new ContentValues[tiposSangre.length()];
			for(int i=0;i<tiposSangre.length();i++){
				JSONObject sangre=tiposSangre.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(TipoSanguineo.ID, sangre.getInt(TipoSanguineo._REMOTO_ID));
				filas[i].put(TipoSanguineo.DESCRIPCION, sangre.getString(TipoSanguineo.DESCRIPCION));
				filas[i].put(TipoSanguineo.ACTIVO, sangre.getInt(TipoSanguineo.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.TIPO_SANGUINEO_CONTENT_URI, filas);
			
			//TABLA VACUNAS
			JSONArray vacunas = jo.getJSONArray(Vacuna.NOMBRE_TABLA);
			filas=new ContentValues[vacunas.length()];
			for(int i=0;i<vacunas.length();i++){
				JSONObject vacuna=vacunas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Vacuna.ID, vacuna.getInt(Vacuna._REMOTO_ID));
				filas[i].put(Vacuna.DESCRIPCION, vacuna.getString(Vacuna.DESCRIPCION));
				filas[i].put(Vacuna.ACTIVO, vacuna.getInt(Vacuna.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.VACUNA_CONTENT_URI, filas);
			
			//TABLA ACCIONES NUTRICIONALES
			JSONArray accionesNutricionales = jo.getJSONArray(AccionNutricional.NOMBRE_TABLA);
			filas=new ContentValues[accionesNutricionales.length()];
			for(int i=0;i<accionesNutricionales.length();i++){
				JSONObject accion=accionesNutricionales.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(AccionNutricional.ID, accion.getInt(AccionNutricional._REMOTO_ID));
				filas[i].put(AccionNutricional.DESCRIPCION, accion.getString(AccionNutricional.DESCRIPCION));
				filas[i].put(AccionNutricional.ACTIVO, accion.getInt(AccionNutricional.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.ACCION_NUTRICIONAL_CONTENT_URI, filas);
			
			//TABLA IRAS
			JSONArray iras = jo.getJSONArray(Ira.NOMBRE_TABLA);
			filas=new ContentValues[iras.length()];
			for(int i=0;i<iras.length();i++){
				JSONObject ira=iras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Ira.ID, ira.getInt(Ira._REMOTO_ID));
				filas[i].put(Ira.ID_CIE10, ira.getInt(Ira.ID_CIE10));
				filas[i].put(Ira.DESCRIPCION, ira.getString(Ira.DESCRIPCION));
				filas[i].put(Ira.ACTIVO, ira.getInt(Ira.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.IRA_CONTENT_URI, filas);
			
			//TABLA EDAS
			JSONArray edas = jo.getJSONArray(Eda.NOMBRE_TABLA);
			filas=new ContentValues[edas.length()];
			for(int i=0;i<edas.length();i++){
				JSONObject eda=edas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Eda.ID, eda.getInt(Eda._REMOTO_ID));
				filas[i].put(Eda.ID_CIE10, eda.getInt(Eda.ID_CIE10));
				filas[i].put(Eda.DESCRIPCION, eda.getString(Eda.DESCRIPCION));
				filas[i].put(Eda.ACTIVO, eda.getInt(Eda.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.EDA_CONTENT_URI, filas);
			
			//TABLA CONSULTAS
			JSONArray consultas = jo.getJSONArray(Consulta.NOMBRE_TABLA);
			filas=new ContentValues[consultas.length()];
			for(int i=0;i<consultas.length();i++){
				JSONObject consulta=consultas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Consulta.ID, consulta.getInt(Consulta._REMOTO_ID));
				filas[i].put(Consulta.ID_CIE10, consulta.getInt(Consulta.ID_CIE10));
				filas[i].put(Consulta.DESCRIPCION, consulta.getString(Consulta.DESCRIPCION));
				filas[i].put(Consulta.ACTIVO, consulta.getInt(Consulta.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.CONSULTA_CONTENT_URI, filas);
			
			//TABLA ALERGIAS
			JSONArray alergias = jo.getJSONArray(Alergia.NOMBRE_TABLA);
			filas=new ContentValues[alergias.length()];
			for(int i=0;i<alergias.length();i++){
				JSONObject alergia=alergias.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Alergia.ID, alergia.getInt(Alergia._REMOTO_ID));
				filas[i].put(Alergia.TIPO, alergia.getString(Alergia.TIPO));
				filas[i].put(Alergia.DESCRIPCION, alergia.getString(Alergia.DESCRIPCION));
				filas[i].put(Alergia.ACTIVO, alergia.getInt(Alergia.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.ALERGIA_CONTENT_URI, filas);
			
			//TABLA AFILIACIONES
			JSONArray afiliaciones = jo.getJSONArray(Afiliacion.NOMBRE_TABLA);
			filas=new ContentValues[afiliaciones.length()];
			for(int i=0;i<afiliaciones.length();i++){
				JSONObject afiliacion=afiliaciones.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Afiliacion.ID, afiliacion.getInt(Afiliacion._REMOTO_ID));
				filas[i].put(Afiliacion.DESCRIPCION, afiliacion.getString(Afiliacion.DESCRIPCION));
				filas[i].put(Afiliacion.ACTIVO, afiliacion.getInt(Afiliacion.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.AFILIACION_CONTENT_URI, filas);
			
			//TABLA NACIONALIDADES
			JSONArray nacionalidades = jo.getJSONArray(Nacionalidad.NOMBRE_TABLA);
			filas=new ContentValues[nacionalidades.length()];
			for(int i=0;i<nacionalidades.length();i++){
				JSONObject nacionalidad=nacionalidades.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Nacionalidad.ID, nacionalidad.getInt(Nacionalidad._REMOTO_ID));
				filas[i].put(Nacionalidad.DESCRIPCION, nacionalidad.getString(Nacionalidad.DESCRIPCION));
				filas[i].put(Nacionalidad.CODIGO, nacionalidad.getString(Nacionalidad.CODIGO));
				filas[i].put(Nacionalidad.ACTIVO, nacionalidad.getInt(Nacionalidad.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.NACIONALIDAD_CONTENT_URI, filas);
			
			//TABLA OPERADORAS CELULARES
			JSONArray operadoras = jo.getJSONArray(OperadoraCelular.NOMBRE_TABLA);
			filas=new ContentValues[operadoras.length()];
			for(int i=0;i<operadoras.length();i++){
				JSONObject operadora=operadoras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(OperadoraCelular.ID, operadora.getInt(OperadoraCelular._REMOTO_ID));
				filas[i].put(OperadoraCelular.DESCRIPCION, operadora.getString(OperadoraCelular.DESCRIPCION));
				filas[i].put(OperadoraCelular.ACTIVO, operadora.getInt(OperadoraCelular.ACTIVO));
			}
			cr.bulkInsert(ProveedorContenido.OPERADORA_CELULAR_CONTENT_URI, filas);
			
			//TABLA PENDIENTES TARJETA
			JSONArray pendientes = jo.optJSONArray(PendientesTarjeta.NOMBRE_TABLA);
			if(pendientes!=null){
				filas=new ContentValues[pendientes.length()];
				for(int i=0;i<pendientes.length();i++){
					JSONObject pendiente=pendientes.getJSONObject(i);
					filas[i]=new ContentValues();
					filas[i].put(PendientesTarjeta.ID, pendiente.getInt(PendientesTarjeta._REMOTO_ID));
					filas[i].put(PendientesTarjeta.ID_PERSONA, pendiente.getInt(PendientesTarjeta.ID_PERSONA));
					filas[i].put(PendientesTarjeta.TABLA_PENDIENTE, pendiente.getString(PendientesTarjeta.TABLA_PENDIENTE));
					filas[i].put(PendientesTarjeta.REGISTRO_JSON, pendiente.getString(PendientesTarjeta.REGISTRO_JSON));
					filas[i].put(PendientesTarjeta.RESUELTO, 0 );
					filas[i].put(PendientesTarjeta.ES_PENDIENTE_LOCAL, 0 );
					filas[i].put(PendientesTarjeta.YA_ESTA_EN_NUBE, 0 );
				}
				cr.bulkInsert(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, filas);
			}
			
			//TABLA REGLA VACUNA
/*			JSONArray reglas = jo.getJSONArray(ReglaVacuna.NOMBRE_TABLA);
			filas=new ContentValues[reglas.length()];
			for(int i=0;i<reglas.length();i++){
				JSONObject regla=reglas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ReglaVacuna.ID, regla.getInt(ReglaVacuna.ID));
				filas[i].put(ReglaVacuna.ID_ECE_VACUNA, regla.getInt(ReglaVacuna.ID_ECE_VACUNA));
				if(!regla.isNull(ReglaVacuna.DIA_INICIO_APLICACION_NACIDO))
					filas[i].put(ReglaVacuna.DIA_INICIO_APLICACION_NACIDO, regla.getInt(ReglaVacuna.DIA_INICIO_APLICACION_NACIDO));
				if(!regla.isNull(ReglaVacuna.DIA_FIN_APLICACION_NACIDO))
					filas[i].put(ReglaVacuna.DIA_FIN_APLICACION_NACIDO, regla.getInt(ReglaVacuna.DIA_FIN_APLICACION_NACIDO));
				if(!regla.isNull(ReglaVacuna.DIA_INICIO_APLICACION_SECUENCIAL))
					filas[i].put(ReglaVacuna.DIA_INICIO_APLICACION_SECUENCIAL, regla.getInt(ReglaVacuna.DIA_INICIO_APLICACION_SECUENCIAL));
				if(!regla.isNull(ReglaVacuna.DIA_FIN_APLICACION_SECUENCIAL))
					filas[i].put(ReglaVacuna.DIA_FIN_APLICACION_SECUENCIAL, regla.getInt(ReglaVacuna.DIA_FIN_APLICACION_SECUENCIAL));
				if(!regla.isNull(ReglaVacuna.ID_ECE_REGLA_VACUNA_SECUENCIAL))
					filas[i].put(ReglaVacuna.ID_ECE_REGLA_VACUNA_SECUENCIAL, regla.getInt(ReglaVacuna.ID_ECE_REGLA_VACUNA_SECUENCIAL));
			}
			cr.bulkInsert(ProveedorContenido.REGLA_VACUNA_CONTENT_URI, filas);*/
			
			//TABLA ARBOL SEGMENTACION
			JSONArray arboles = jo.getJSONArray(ArbolSegmentacion.NOMBRE_TABLA);
			filas=new ContentValues[arboles.length()];
			for(int i=0;i<arboles.length();i++){
				JSONObject arbol=arboles.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ArbolSegmentacion.ID, arbol.getInt(ArbolSegmentacion._REMOTO_ID));
				filas[i].put(ArbolSegmentacion.GRADO_SEGMENTACION, arbol.getInt(ArbolSegmentacion.GRADO_SEGMENTACION));
				filas[i].put(ArbolSegmentacion.ID_PADRE, arbol.getInt(ArbolSegmentacion.ID_PADRE));
				filas[i].put(ArbolSegmentacion.ORDEN, arbol.getInt(ArbolSegmentacion.ORDEN));
				filas[i].put(ArbolSegmentacion.VISIBLE, arbol.getInt(ArbolSegmentacion.VISIBLE));
				filas[i].put(ArbolSegmentacion.DESCRIPCION, arbol.getString(ArbolSegmentacion.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI, filas);
			
			EnviarResultado(idSesion, ACCION_RESULTADO_PRIMEROS_CATALOGOS, RESULTADO_OK,null);
			
		} catch (JSONException e) {
			msgError = "Error en json:" + e.toString()+ "La cadena fue:"+json; 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, ACCION_RESULTADO_PRIMEROS_CATALOGOS, "JSONException", msgError);
		} catch (Exception e){
			msgError = "Error desconocido:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, ACCION_RESULTADO_PRIMEROS_CATALOGOS, "Excepcion", msgError);
		}
	}//fin AccionPrimerosCatalogos
	
	
	/**
	 * Esta acción manda credenciales y recibe primeros datos a insertar en base de datos
	 * @param idSesion
	 */
	private void AccionPrimerosDatos(String idSesion){
		String msgError = null;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_DATOS));

        Log.d(TAG, "Request primeros datos");
		String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		
		try {
			JSONObject jo=new JSONObject(json);			
			
			//TABLAS DE DATOS (TRANSACCIONALES) DEL SISTEMA
			ContentResolver cr = contexto.getContentResolver();
			ContentValues[] filas; //parámetros en filas para inserciones múltiples
			
			//TABLA TUTORES
			JSONArray tutores = jo.getJSONArray(Tutor.NOMBRE_TABLA);
			filas=new ContentValues[tutores.length()];
			for(int i=0;i<tutores.length();i++){
				JSONObject tutor=tutores.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Tutor.ID, tutor.getInt(Tutor.ID));
				filas[i].put(Tutor.NOMBRE, tutor.getString(Tutor.NOMBRE));
				filas[i].put(Tutor.APELLIDO_PATERNO, tutor.getString(Tutor.APELLIDO_PATERNO));
				filas[i].put(Tutor.APELLIDO_MATERNO, tutor.getString(Tutor.APELLIDO_MATERNO));
				filas[i].put(Tutor.CURP, tutor.getString(Tutor.CURP));
				filas[i].put(Tutor.SEXO, tutor.getString(Tutor.SEXO));
				if(!tutor.isNull(Tutor.TELEFONO))
					filas[i].put(Tutor.TELEFONO, tutor.getString(Tutor.TELEFONO));
				if(!tutor.isNull(Tutor.CELULAR))
					filas[i].put(Tutor.CELULAR, tutor.getString(Tutor.CELULAR));
				if(!tutor.isNull(Tutor.ID_OPERADORA_CELULAR))
					filas[i].put(Tutor.ID_OPERADORA_CELULAR, tutor.getInt(Tutor.ID_OPERADORA_CELULAR));
			}
			cr.bulkInsert(ProveedorContenido.TUTOR_CONTENT_URI, filas);
			
			//TABLA PERSONAS
			JSONArray personas = jo.getJSONArray(Persona.NOMBRE_TABLA);
			filas=new ContentValues[personas.length()];
			for(int i=0;i<personas.length();i++){
				JSONObject persona=personas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Persona.ID, persona.getInt(Persona.ID));
				filas[i].put(Persona.NOMBRE, persona.getString(Persona.NOMBRE));
				filas[i].put(Persona.APELLIDO_PATERNO, persona.getString(Persona.APELLIDO_PATERNO));
				filas[i].put(Persona.APELLIDO_MATERNO, persona.getString(Persona.APELLIDO_MATERNO));
				filas[i].put(Persona.CURP, persona.getString(Persona.CURP));
				filas[i].put(Persona.SEXO, persona.getString(Persona.SEXO));
				filas[i].put(Persona.ID_TIPO_SANGUINEO, persona.getInt(Persona.ID_TIPO_SANGUINEO));
				filas[i].put(Persona.FECHA_NACIMIENTO, persona.getString(Persona.FECHA_NACIMIENTO));
				filas[i].put(Persona.ID_ASU_LOCALIDAD_NACIMIENTO, persona.getInt(Persona.ID_ASU_LOCALIDAD_NACIMIENTO));
				filas[i].put(Persona.CALLE_DOMICILIO, persona.getString(Persona.CALLE_DOMICILIO));
				if(!persona.isNull(Persona.NUMERO_DOMICILIO))
					filas[i].put(Persona.NUMERO_DOMICILIO, persona.getString(Persona.NUMERO_DOMICILIO));
				if(!persona.isNull(Persona.COLONIA_DOMICILIO))
					filas[i].put(Persona.COLONIA_DOMICILIO, persona.getString(Persona.COLONIA_DOMICILIO));
				if(!persona.isNull(Persona.REFERENCIA_DOMICILIO))
					filas[i].put(Persona.REFERENCIA_DOMICILIO, persona.getString(Persona.REFERENCIA_DOMICILIO));
				filas[i].put(Persona.ID_ASU_LOCALIDAD_DOMICILIO, persona.getInt(Persona.ID_ASU_LOCALIDAD_DOMICILIO));
				filas[i].put(Persona.CP_DOMICILIO, persona.getInt(Persona.CP_DOMICILIO));
				if(!persona.isNull(Persona.TELEFONO_DOMICILIO))
					filas[i].put(Persona.TELEFONO_DOMICILIO, persona.getString(Persona.TELEFONO_DOMICILIO));
				filas[i].put(Persona.FECHA_REGISTRO, persona.getString(Persona.FECHA_REGISTRO));
				filas[i].put(Persona.ID_ASU_UM_TRATANTE, persona.getInt(Persona.ID_ASU_UM_TRATANTE));
				if(!persona.isNull(Persona.CELULAR))
					filas[i].put(Persona.CELULAR, persona.getString(Persona.CELULAR));
				if(!persona.isNull(Persona.ID_OPERADORA_CELULAR))
					filas[i].put(Persona.ID_OPERADORA_CELULAR, persona.getInt(Persona.ID_OPERADORA_CELULAR));
				filas[i].put(Persona.ID_NACIONALIDAD, persona.getInt(Persona.ID_NACIONALIDAD));
				filas[i].put(Persona.ULTIMA_ACTUALIZACION, persona.getString(Persona.ULTIMA_ACTUALIZACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_CONTENT_URI, filas);
			
			//TABLA PERSONA_X_TUTOR
			JSONArray personas_tutores = jo.getJSONArray(PersonaTutor.NOMBRE_TABLA);
			filas=new ContentValues[personas_tutores.length()];
			for(int i=0;i<personas_tutores.length();i++){
				JSONObject persona_tutor=personas_tutores.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(PersonaTutor.ID_PERSONA, persona_tutor.getInt(PersonaTutor._REMOTO_ID_PERSONA));
				filas[i].put(PersonaTutor.ID_TUTOR, persona_tutor.getInt(PersonaTutor.ID_TUTOR));
				filas[i].put(PersonaTutor.ULTIMA_ACTUALIZACION, persona_tutor.getString(PersonaTutor.ULTIMA_ACTUALIZACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_TUTOR_CONTENT_URI, filas);
			
			//TABLA PERSONA_X_ALERGIA
			JSONArray personas_alergias = jo.getJSONArray(PersonaAlergia.NOMBRE_TABLA);
			filas=new ContentValues[personas_alergias.length()];
			for(int i=0;i<personas_alergias.length();i++){
				JSONObject persona_alergia=personas_alergias.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(PersonaAlergia.ID_PERSONA, persona_alergia.getInt(PersonaAlergia.ID_PERSONA));
				filas[i].put(PersonaAlergia.ID_ECE_ALERGIA, persona_alergia.getInt(PersonaAlergia.ID_ECE_ALERGIA));
				filas[i].put(PersonaAlergia.ULTIMA_ACTUALIZACION, persona_alergia.getString(PersonaAlergia.ULTIMA_ACTUALIZACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, filas);
			
			//TABLA PERSONA_X_AFILIACION
			JSONArray personas_afiliaciones = jo.getJSONArray(PersonaAfiliacion.NOMBRE_TABLA);
			filas=new ContentValues[personas_afiliaciones.length()];
			for(int i=0;i<personas_afiliaciones.length();i++){
				JSONObject persona_afiliacion=personas_afiliaciones.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(PersonaAfiliacion.ID_PERSONA, persona_afiliacion.getInt(PersonaAfiliacion.ID_PERSONA));
				filas[i].put(PersonaAfiliacion.ID_AFILIACION, persona_afiliacion.getInt(PersonaAfiliacion.ID_AFILIACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, filas);
			
			//TABLA REGISTRO_CIVIL
			JSONArray civiles = jo.getJSONArray(RegistroCivil.NOMBRE_TABLA);
			filas=new ContentValues[civiles.length()];
			for(int i=0;i<civiles.length();i++){
				JSONObject civil=civiles.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(RegistroCivil.ID_PERSONA, civil.getInt(RegistroCivil._REMOTO_ID_PERSONA));
				filas[i].put(RegistroCivil.ID_LOCALIDAD_REGISTRO_CIVIL, civil.getInt(RegistroCivil.ID_LOCALIDAD_REGISTRO_CIVIL));
				filas[i].put(RegistroCivil.FECHA_REGISTRO, civil.getString(RegistroCivil.FECHA_REGISTRO));
			}
			cr.bulkInsert(ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI, filas);
			
			//TABLA ANTIGUA UM
			JSONArray antiguasUM = jo.getJSONArray(AntiguaUM.NOMBRE_TABLA);
			filas=new ContentValues[antiguasUM.length()];
			for(int i=0;i<antiguasUM.length();i++){
				JSONObject antiguaUM=antiguasUM.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(AntiguaUM.ID_PERSONA, antiguaUM.getInt(AntiguaUM.ID_PERSONA));
				filas[i].put(AntiguaUM.ID_ASU_UM_TRATANTE, antiguaUM.getInt(AntiguaUM.ID_ASU_UM_TRATANTE));
				filas[i].put(AntiguaUM.FECHA_CAMBIO, antiguaUM.getString(AntiguaUM.FECHA_CAMBIO));
			}
			cr.bulkInsert(ProveedorContenido.ANTIGUA_UM_CONTENT_URI, filas);
			
			//TABLA ANTIGUO DOMICILIO
			JSONArray domicilios = jo.getJSONArray(AntiguoDomicilio.NOMBRE_TABLA);
			filas=new ContentValues[domicilios.length()];
			for(int i=0;i<domicilios.length();i++){
				JSONObject domicilio=domicilios.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(AntiguoDomicilio.ID_PERSONA, domicilio.getInt(AntiguoDomicilio.ID_PERSONA));
				filas[i].put(AntiguoDomicilio.CALLE_DOMICILIO, domicilio.getString(AntiguoDomicilio.CALLE_DOMICILIO));
				if(!domicilio.isNull(AntiguoDomicilio.NUMERO_DOMICILIO))
					filas[i].put(AntiguoDomicilio.NUMERO_DOMICILIO, domicilio.getString(AntiguoDomicilio.NUMERO_DOMICILIO));
				if(!domicilio.isNull(AntiguoDomicilio.COLONIA_DOMICILIO))
					filas[i].put(AntiguoDomicilio.COLONIA_DOMICILIO, domicilio.getString(AntiguoDomicilio.COLONIA_DOMICILIO));
				if(!domicilio.isNull(AntiguoDomicilio.REFERENCIA_DOMICILIO))
					filas[i].put(AntiguoDomicilio.REFERENCIA_DOMICILIO, domicilio.getString(AntiguoDomicilio.REFERENCIA_DOMICILIO));
				filas[i].put(AntiguoDomicilio.CP_DOMICILIO, domicilio.getInt(AntiguoDomicilio.CP_DOMICILIO));
				filas[i].put(AntiguoDomicilio.ID_ASU_LOCALIDAD_DOMICILIO, domicilio.getInt(AntiguoDomicilio.ID_ASU_LOCALIDAD_DOMICILIO));
				filas[i].put(AntiguoDomicilio.FECHA_CAMBIO, domicilio.getInt(AntiguoDomicilio.FECHA_CAMBIO));
			}
			cr.bulkInsert(ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI, filas);
			
			//TABLA CONTROL VACUNA
			JSONArray vacunas = jo.getJSONArray(ControlVacuna.NOMBRE_TABLA);
			filas=new ContentValues[vacunas.length()];
			for(int i=0;i<vacunas.length();i++){
				JSONObject vacuna=vacunas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlVacuna.ID_PERSONA, vacuna.getInt(ControlVacuna.ID_PERSONA));
				filas[i].put(ControlVacuna.ID_VACUNA, vacuna.getInt(ControlVacuna.ID_VACUNA));
				filas[i].put(ControlVacuna.ID_ASU_UM, vacuna.getInt(ControlVacuna.ID_ASU_UM));
				filas[i].put(ControlVacuna.FECHA, vacuna.getString(ControlVacuna.FECHA));
				if(!vacuna.isNull(ControlVacuna.CODIGO_BARRAS))
					filas[i].put(ControlVacuna.CODIGO_BARRAS, vacuna.getString(ControlVacuna.CODIGO_BARRAS));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, filas);
			
			//TABLA CONTROL IRA
			JSONArray iras = jo.getJSONArray(ControlIra.NOMBRE_TABLA);
			filas=new ContentValues[iras.length()];
			for(int i=0;i<iras.length();i++){
				JSONObject ira=iras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlIra.ID_PERSONA, ira.getInt(ControlIra.ID_PERSONA));
				filas[i].put(ControlIra.ID_IRA, ira.getInt(ControlIra.ID_IRA));
				filas[i].put(ControlIra.ID_ASU_UM, ira.getInt(ControlIra.ID_ASU_UM));
				filas[i].put(ControlIra.FECHA, ira.getString(ControlIra.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_IRA_CONTENT_URI, filas);
			
			//TABLA CONTROL EDA
			JSONArray edas = jo.getJSONArray(ControlEda.NOMBRE_TABLA);
			filas=new ContentValues[edas.length()];
			for(int i=0;i<edas.length();i++){
				JSONObject eda=edas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlEda.ID_PERSONA, eda.getInt(ControlEda.ID_PERSONA));
				filas[i].put(ControlEda.ID_EDA, eda.getInt(ControlEda.ID_EDA));
				filas[i].put(ControlEda.ID_ASU_UM, eda.getInt(ControlEda.ID_ASU_UM));
				filas[i].put(ControlEda.FECHA, eda.getString(ControlEda.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_EDA_CONTENT_URI, filas);
			
			//TABLA CONTROL CONSULTA
			JSONArray consultas = jo.getJSONArray(ControlConsulta.NOMBRE_TABLA);
			filas=new ContentValues[consultas.length()];
			for(int i=0;i<consultas.length();i++){
				JSONObject consulta=consultas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlConsulta.ID_PERSONA, consulta.getInt(ControlConsulta.ID_PERSONA));
				filas[i].put(ControlConsulta.ID_CONSULTA, consulta.getInt(ControlConsulta.ID_CONSULTA));
				filas[i].put(ControlConsulta.ID_ASU_UM, consulta.getInt(ControlConsulta.ID_ASU_UM));
				filas[i].put(ControlConsulta.FECHA, consulta.getString(ControlConsulta.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, filas);
			
			//TABLA CONTROL ACCION NUTRICIONAL
			JSONArray acciones = jo.getJSONArray(ControlAccionNutricional.NOMBRE_TABLA);
			filas=new ContentValues[acciones.length()];
			for(int i=0;i<acciones.length();i++){
				JSONObject accion=acciones.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlAccionNutricional.ID_PERSONA, accion.getInt(ControlAccionNutricional.ID_PERSONA));
				filas[i].put(ControlAccionNutricional.ID_ACCION_NUTRICIONAL, accion.getInt(ControlAccionNutricional.ID_ACCION_NUTRICIONAL));
				filas[i].put(ControlAccionNutricional.ID_ASU_UM, accion.getInt(ControlAccionNutricional.ID_ASU_UM));
				filas[i].put(ControlAccionNutricional.FECHA, accion.getString(ControlAccionNutricional.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI, filas);
			
			
			//TABLA CONTROL NUTRICIONAL
			JSONArray nutricionales = jo.getJSONArray(ControlNutricional.NOMBRE_TABLA);
			filas=new ContentValues[nutricionales.length()];
			for(int i=0;i<nutricionales.length();i++){
				JSONObject nutricional=nutricionales.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlNutricional.ID_PERSONA, nutricional.getInt(ControlNutricional.ID_PERSONA));
				filas[i].put(ControlNutricional.PESO, nutricional.getDouble(ControlNutricional.PESO));
				filas[i].put(ControlNutricional.ALTURA, nutricional.getDouble(ControlNutricional.ALTURA));
				filas[i].put(ControlNutricional.TALLA, nutricional.getDouble(ControlNutricional.TALLA));
				filas[i].put(ControlNutricional.ID_ASU_UM, nutricional.getInt(ControlNutricional.ID_ASU_UM));
				filas[i].put(ControlNutricional.FECHA, nutricional.getString(ControlNutricional.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, filas);
			
			
			EnviarResultado(idSesion,ACCION_RESULTADO_PRIMEROS_DATOS, RESULTADO_OK, null);
			
		} catch (JSONException e) {
			msgError = "Error en json:"+e.toString()+" con cadena:"+json; 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, ACCION_RESULTADO_PRIMEROS_DATOS, "JSONException", msgError);
		} catch (Exception e){
			msgError = "Error desconocido:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, ACCION_RESULTADO_PRIMEROS_DATOS, "Exception", msgError);
		}		
	}//fin AccionPrimerosDatos
	
	
	/**
	 * Envia al servidor los cambios desde la última actualización
	 * @param idSesion
	 * @param ultimaSinc
	 */
	private void AccionEnviarCambiosServidor(String idSesion, String ultimaSinc){
		String msgError = null;
		
		//TABLAS DE DATOS (TRANSACCIONALES) DEL SISTEMA
		ContentResolver cr = contexto.getContentResolver();
		Cursor cur;
		String where="";
		String[] valoresWhere;
		ContentValues[] filas; //parámetros en filas para inserciones múltiples
		//String[] columnas;
		
		//TABLA TUTOR
		where = Tutor.ULTIMA_ACTUALIZACION + "=?";
		valoresWhere= new String[]{ultimaSinc};
		cr.query(ProveedorContenido.TUTOR_CONTENT_URI, null, where, valoresWhere, null);
		
		
		JSONObject datos = new JSONObject();
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_ENVIAR_SERVIDOR));
        parametros.add(new BasicNameValuePair(PARAMETRO_DATOS, datos.toString() ));

        Log.d(TAG, "Request envío de cambios a servidor");
		String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		
		try {
			JSONObject jo=new JSONObject(json);
			
			
			
			//TABLA TUTORES
			JSONArray tutores = jo.getJSONArray("tutor");
			filas=new ContentValues[tutores.length()];
			for(int i=0;i<tutores.length();i++){
				JSONObject tutor=tutores.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Tutor.ID, tutor.getInt(Tutor.ID));
				filas[i].put(Tutor.NOMBRE, tutor.getString(Tutor.NOMBRE));
				filas[i].put(Tutor.APELLIDO_PATERNO, tutor.getString(Tutor.APELLIDO_PATERNO));
				filas[i].put(Tutor.APELLIDO_MATERNO, tutor.getString(Tutor.APELLIDO_MATERNO));
				filas[i].put(Tutor.CURP, tutor.getString(Tutor.CURP));
				filas[i].put(Tutor.SEXO, tutor.getString(Tutor.SEXO));
				if(!tutor.isNull(Tutor.TELEFONO))
					filas[i].put(Tutor.TELEFONO, tutor.getString(Tutor.TELEFONO));
				if(!tutor.isNull(Tutor.CELULAR))
					filas[i].put(Tutor.CELULAR, tutor.getString(Tutor.CELULAR));
				if(!tutor.isNull(Tutor.ID_OPERADORA_CELULAR))
					filas[i].put(Tutor.ID_OPERADORA_CELULAR, tutor.getInt(Tutor.ID_OPERADORA_CELULAR));
			}
			cr.bulkInsert(ProveedorContenido.TUTOR_CONTENT_URI, filas);
			
			//TABLA PERSONAS
			JSONArray personas = jo.getJSONArray("persona");
			filas=new ContentValues[personas.length()];
			for(int i=0;i<personas.length();i++){
				JSONObject persona=personas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Persona.ID, persona.getInt(Persona.ID));
				filas[i].put(Persona.NOMBRE, persona.getString(Persona.NOMBRE));
				filas[i].put(Persona.APELLIDO_PATERNO, persona.getString(Persona.APELLIDO_PATERNO));
				filas[i].put(Persona.APELLIDO_MATERNO, persona.getString(Persona.APELLIDO_MATERNO));
				filas[i].put(Persona.CURP, persona.getString(Persona.CURP));
				filas[i].put(Persona.SEXO, persona.getString(Persona.SEXO));
				filas[i].put(Persona.ID_TIPO_SANGUINEO, persona.getInt(Persona.ID_TIPO_SANGUINEO));
				filas[i].put(Persona.FECHA_NACIMIENTO, persona.getString(Persona.FECHA_NACIMIENTO));
				filas[i].put(Persona.ID_ASU_LOCALIDAD_NACIMIENTO, persona.getInt(Persona.ID_ASU_LOCALIDAD_NACIMIENTO));
				filas[i].put(Persona.CALLE_DOMICILIO, persona.getString(Persona.CALLE_DOMICILIO));
				if(!persona.isNull(Persona.NUMERO_DOMICILIO))
					filas[i].put(Persona.NUMERO_DOMICILIO, persona.getString(Persona.NUMERO_DOMICILIO));
				if(!persona.isNull(Persona.COLONIA_DOMICILIO))
					filas[i].put(Persona.COLONIA_DOMICILIO, persona.getString(Persona.COLONIA_DOMICILIO));
				if(!persona.isNull(Persona.REFERENCIA_DOMICILIO))
					filas[i].put(Persona.REFERENCIA_DOMICILIO, persona.getString(Persona.REFERENCIA_DOMICILIO));
				filas[i].put(Persona.ID_ASU_LOCALIDAD_DOMICILIO, persona.getInt(Persona.ID_ASU_LOCALIDAD_DOMICILIO));
				filas[i].put(Persona.CP_DOMICILIO, persona.getInt(Persona.CP_DOMICILIO));
				if(!persona.isNull(Persona.TELEFONO_DOMICILIO))
					filas[i].put(Persona.TELEFONO_DOMICILIO, persona.getString(Persona.TELEFONO_DOMICILIO));
				filas[i].put(Persona.FECHA_REGISTRO, persona.getString(Persona.FECHA_REGISTRO));
				filas[i].put(Persona.ID_ASU_UM_TRATANTE, persona.getInt(Persona.ID_ASU_UM_TRATANTE));
				if(!persona.isNull(Persona.CELULAR))
					filas[i].put(Persona.CELULAR, persona.getString(Persona.CELULAR));
				if(!persona.isNull(Persona.ID_OPERADORA_CELULAR))
					filas[i].put(Persona.ID_OPERADORA_CELULAR, persona.getInt(Persona.ID_OPERADORA_CELULAR));
				filas[i].put(Persona.ID_NACIONALIDAD, persona.getInt(Persona.ID_NACIONALIDAD));
				filas[i].put(Persona.ULTIMA_ACTUALIZACION, persona.getString(Persona.ULTIMA_ACTUALIZACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_CONTENT_URI, filas);
			
			//TABLA PERSONA_X_TUTOR
			JSONArray personas_tutores = jo.getJSONArray("persona_x_tutor");
			filas=new ContentValues[personas_tutores.length()];
			for(int i=0;i<personas_tutores.length();i++){
				JSONObject persona_tutor=personas_tutores.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(PersonaTutor.ID_PERSONA, persona_tutor.getInt(PersonaTutor._REMOTO_ID_PERSONA));
				filas[i].put(PersonaTutor.ID_TUTOR, persona_tutor.getInt(PersonaTutor.ID_TUTOR));
				filas[i].put(PersonaTutor.ULTIMA_ACTUALIZACION, persona_tutor.getString(PersonaTutor.ULTIMA_ACTUALIZACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_TUTOR_CONTENT_URI, filas);
			
			//TABLA PERSONA_X_ALERGIA
			JSONArray personas_alergias = jo.getJSONArray("persona_x_alergia");
			filas=new ContentValues[personas_alergias.length()];
			for(int i=0;i<personas_alergias.length();i++){
				JSONObject persona_alergia=personas_alergias.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(PersonaAlergia.ID_PERSONA, persona_alergia.getInt(PersonaAlergia.ID_PERSONA));
				filas[i].put(PersonaAlergia.ID_ECE_ALERGIA, persona_alergia.getInt(PersonaAlergia.ID_ECE_ALERGIA));
				filas[i].put(PersonaAlergia.ULTIMA_ACTUALIZACION, persona_alergia.getString(PersonaAlergia.ULTIMA_ACTUALIZACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, filas);
			
			//TABLA PERSONA_X_AFILIACION
			JSONArray personas_afiliaciones = jo.getJSONArray("persona_x_afiliacion");
			filas=new ContentValues[personas_afiliaciones.length()];
			for(int i=0;i<personas_afiliaciones.length();i++){
				JSONObject persona_afiliacion=personas_afiliaciones.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(PersonaAfiliacion.ID_PERSONA, persona_afiliacion.getInt(PersonaAfiliacion.ID_PERSONA));
				filas[i].put(PersonaAfiliacion.ID_AFILIACION, persona_afiliacion.getInt(PersonaAfiliacion.ID_AFILIACION));
			}
			cr.bulkInsert(ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, filas);
			
			//TABLA REGISTRO_CIVIL
			JSONArray civiles = jo.getJSONArray("registro_civil");
			filas=new ContentValues[civiles.length()];
			for(int i=0;i<civiles.length();i++){
				JSONObject civil=civiles.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(RegistroCivil.ID_PERSONA, civil.getInt(RegistroCivil._REMOTO_ID_PERSONA));
				filas[i].put(RegistroCivil.ID_LOCALIDAD_REGISTRO_CIVIL, civil.getInt(RegistroCivil.ID_LOCALIDAD_REGISTRO_CIVIL));
				filas[i].put(RegistroCivil.FECHA_REGISTRO, civil.getString(RegistroCivil.FECHA_REGISTRO));
			}
			cr.bulkInsert(ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI, filas);
			
			//TABLA ANTIGUA UM
			JSONArray antiguasUM = jo.getJSONArray("antigua_um");
			filas=new ContentValues[antiguasUM.length()];
			for(int i=0;i<antiguasUM.length();i++){
				JSONObject antiguaUM=antiguasUM.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(AntiguaUM.ID_PERSONA, antiguaUM.getInt(AntiguaUM.ID_PERSONA));
				filas[i].put(AntiguaUM.ID_ASU_UM_TRATANTE, antiguaUM.getInt(AntiguaUM.ID_ASU_UM_TRATANTE));
				filas[i].put(AntiguaUM.FECHA_CAMBIO, antiguaUM.getString(AntiguaUM.FECHA_CAMBIO));
			}
			cr.bulkInsert(ProveedorContenido.ANTIGUA_UM_CONTENT_URI, filas);
			
			//TABLA ANTIGUO DOMICILIO
			JSONArray domicilios = jo.getJSONArray("antiguo_domicilio");
			filas=new ContentValues[domicilios.length()];
			for(int i=0;i<domicilios.length();i++){
				JSONObject domicilio=domicilios.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(AntiguoDomicilio.ID_PERSONA, domicilio.getInt(AntiguoDomicilio.ID_PERSONA));
				filas[i].put(AntiguoDomicilio.CALLE_DOMICILIO, domicilio.getString(AntiguoDomicilio.CALLE_DOMICILIO));
				if(!domicilio.isNull(AntiguoDomicilio.NUMERO_DOMICILIO))
					filas[i].put(AntiguoDomicilio.NUMERO_DOMICILIO, domicilio.getString(AntiguoDomicilio.NUMERO_DOMICILIO));
				if(!domicilio.isNull(AntiguoDomicilio.COLONIA_DOMICILIO))
					filas[i].put(AntiguoDomicilio.COLONIA_DOMICILIO, domicilio.getString(AntiguoDomicilio.COLONIA_DOMICILIO));
				if(!domicilio.isNull(AntiguoDomicilio.REFERENCIA_DOMICILIO))
					filas[i].put(AntiguoDomicilio.REFERENCIA_DOMICILIO, domicilio.getString(AntiguoDomicilio.REFERENCIA_DOMICILIO));
				filas[i].put(AntiguoDomicilio.CP_DOMICILIO, domicilio.getInt(AntiguoDomicilio.CP_DOMICILIO));
				filas[i].put(AntiguoDomicilio.ID_ASU_LOCALIDAD_DOMICILIO, domicilio.getInt(AntiguoDomicilio.ID_ASU_LOCALIDAD_DOMICILIO));
				filas[i].put(AntiguoDomicilio.FECHA_CAMBIO, domicilio.getInt(AntiguoDomicilio.FECHA_CAMBIO));
			}
			cr.bulkInsert(ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI, filas);
			
			//TABLA CONTROL VACUNA
			JSONArray vacunas = jo.getJSONArray("control_vacuna");
			filas=new ContentValues[vacunas.length()];
			for(int i=0;i<vacunas.length();i++){
				JSONObject vacuna=vacunas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlVacuna.ID_PERSONA, vacuna.getInt(ControlVacuna.ID_PERSONA));
				filas[i].put(ControlVacuna.ID_VACUNA, vacuna.getInt(ControlVacuna.ID_VACUNA));
				filas[i].put(ControlVacuna.ID_ASU_UM, vacuna.getInt(ControlVacuna.ID_ASU_UM));
				filas[i].put(ControlVacuna.FECHA, vacuna.getString(ControlVacuna.FECHA));
				if(!vacuna.isNull(ControlVacuna.CODIGO_BARRAS))
					filas[i].put(ControlVacuna.CODIGO_BARRAS, vacuna.getString(ControlVacuna.CODIGO_BARRAS));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, filas);
			
			//TABLA CONTROL IRA
			JSONArray iras = jo.getJSONArray("control_ira");
			filas=new ContentValues[iras.length()];
			for(int i=0;i<iras.length();i++){
				JSONObject ira=iras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlIra.ID_PERSONA, ira.getInt(ControlIra.ID_PERSONA));
				filas[i].put(ControlIra.ID_IRA, ira.getInt(ControlIra.ID_IRA));
				filas[i].put(ControlIra.ID_ASU_UM, ira.getInt(ControlIra.ID_ASU_UM));
				filas[i].put(ControlIra.FECHA, ira.getString(ControlIra.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_IRA_CONTENT_URI, filas);
			
			//TABLA CONTROL EDA
			JSONArray edas = jo.getJSONArray("control_eda");
			filas=new ContentValues[edas.length()];
			for(int i=0;i<edas.length();i++){
				JSONObject eda=edas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlEda.ID_PERSONA, eda.getInt(ControlEda.ID_PERSONA));
				filas[i].put(ControlEda.ID_EDA, eda.getInt(ControlEda.ID_EDA));
				filas[i].put(ControlEda.ID_ASU_UM, eda.getInt(ControlEda.ID_ASU_UM));
				filas[i].put(ControlEda.FECHA, eda.getString(ControlEda.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_EDA_CONTENT_URI, filas);
			
			//TABLA CONTROL CONSULTA
			JSONArray consultas = jo.getJSONArray("control_consulta");
			filas=new ContentValues[consultas.length()];
			for(int i=0;i<consultas.length();i++){
				JSONObject consulta=consultas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlConsulta.ID_PERSONA, consulta.getInt(ControlConsulta.ID_PERSONA));
				filas[i].put(ControlConsulta.ID_CONSULTA, consulta.getInt(ControlConsulta.ID_CONSULTA));
				filas[i].put(ControlConsulta.ID_ASU_UM, consulta.getInt(ControlConsulta.ID_ASU_UM));
				filas[i].put(ControlConsulta.FECHA, consulta.getString(ControlConsulta.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, filas);
			
			//TABLA CONTROL ACCION NUTRICIONAL
			JSONArray acciones = jo.getJSONArray("control_accion_nutricional");
			filas=new ContentValues[acciones.length()];
			for(int i=0;i<acciones.length();i++){
				JSONObject accion=acciones.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlAccionNutricional.ID_PERSONA, accion.getInt(ControlAccionNutricional.ID_PERSONA));
				filas[i].put(ControlAccionNutricional.ID_ACCION_NUTRICIONAL, accion.getInt(ControlAccionNutricional.ID_ACCION_NUTRICIONAL));
				filas[i].put(ControlAccionNutricional.ID_ASU_UM, accion.getInt(ControlAccionNutricional.ID_ASU_UM));
				filas[i].put(ControlAccionNutricional.FECHA, accion.getString(ControlAccionNutricional.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI, filas);
			
			
			//TABLA CONTROL NUTRICIONAL
			JSONArray nutricionales = jo.getJSONArray("control_nutricional");
			filas=new ContentValues[nutricionales.length()];
			for(int i=0;i<nutricionales.length();i++){
				JSONObject nutricional=nutricionales.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(ControlNutricional.ID_PERSONA, nutricional.getInt(ControlNutricional.ID_PERSONA));
				filas[i].put(ControlNutricional.PESO, nutricional.getDouble(ControlNutricional.PESO));
				filas[i].put(ControlNutricional.ALTURA, nutricional.getDouble(ControlNutricional.ALTURA));
				filas[i].put(ControlNutricional.TALLA, nutricional.getDouble(ControlNutricional.TALLA));
				filas[i].put(ControlNutricional.ID_ASU_UM, nutricional.getInt(ControlNutricional.ID_ASU_UM));
				filas[i].put(ControlNutricional.FECHA, nutricional.getString(ControlNutricional.FECHA));
			}
			cr.bulkInsert(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, filas);
			
			
			//EnviarResultado(idSesion, RESULTADO_OK);
			
		} catch (JSONException e) {
			msgError = "Error en json:"+json+" "+e.toString(); 
			Log.e(TAG, msgError);
			//EnviarResultado(idSesion, msgError);
		} catch (Exception e){
			msgError = "Error desconocido:"+e.toString();
			Log.e(TAG, msgError);
			//EnviarResultado(idSesion, msgError);
		}
	}//fin AccionEnviarServidor
	
	
	
	private void GenerarAccionX(){
		String jsonString="{\"llave_sesion\":\"milla'veSe,sion\",\"unidad_medica\":321,\"atributo2\":\"\",\"atributo3\":\"abc\",\"catalogo1\":[{\"id\":789,\"campo2\":\"qwe\",\"campo3\":\"valCampo3\"},{\"id\":456,\"campo2\":\"txt\",\"campo3\":\"bvc\"}],\"catalogo2\":[{\"id\":987,\"campo2\":\"poi\",\"campo3\":\"rfv\"},{\"id\":963,\"campo2\":\"kjh\",\"campo3\":\"olm\"}],\"catalogo3\":[{\"id\":741,\"campo2\":\"c3val2\",\"campo3\":\"c3val3\"},{\"id\":147,\"campo2\":\"c3f2v2\",\"campo3\":\"c3f2v3\"}]}";
		
		try {
			JSONObject js=new JSONObject(jsonString);
			String llave, atr2, atr3;
			llave=js.getString("llave_sesion");
			atr2=js.getString("atributo2");
			atr3=js.getString("atributo3");
			String salida="llave:"+llave+", um:"+js.getInt("unidad_medica")+", atr2:"+atr2+", atr3:"+atr3;
			
			JSONArray[] catalogos={js.getJSONArray("catalogo1"), 
					js.getJSONArray("catalogo2"), js.getJSONArray("catalogo3")};
			for(int c=0;c<catalogos.length;c++){
				JSONArray cat=catalogos[c];
				salida+="--catalogo"+c+"--";
				for(int i=0;i<cat.length();i++){
					JSONObject fila= cat.getJSONObject(i);
					salida+="fila: id="+fila.getInt("id")+", c2="+fila.getString("campo2")+", c3="+fila.getString("campo3");
				}
			}
			
			
			
			JSONObject nuevo = new JSONObject();
			JSONObject fil=new JSONObject();fil.put("id", 123).put("columna2", "c2").put("columna3", "c3");
			JSONArray ncat=new JSONArray();ncat.put(fil);
			fil=new JSONObject();fil.put("id", 987).put("columna2", "f2c2").put("columna3", "f2c3");
			ncat.put(fil);
			nuevo.put("llavesesion", 852);nuevo.put("um", 741);nuevo.put("atributo2", "atr2").put("atributo3", nuevo.toString())
			.put("catalogo1",ncat);
			Log.d(TAG, "JSON nuevo: "+nuevo.toString(3));
			Log.d(TAG, "Parseado correcto: "+salida);
		} catch (JSONException e) {
			Log.d(TAG, "Error al parsear JSON: "+e.toString());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Clase helper para métodos de consulta de servicios en web
	 * @author Axel
	 *
	 */
	private class HttpHelper {
		
		HttpClient cliente;
		HttpContext contextoHttp;
		
		public HttpHelper(){
			cliente = new DefaultHttpClient();
			contextoHttp = new BasicHttpContext();
		}
		
		/**
		 * Función helper para mandar request tipo POST y regresar la respuesta del servidor.
		 * @param url Dirección a conectar
		 * @param parametros Valores a incluir en request
		 * @return String con resultado del request
		 * @throws Exceptiono Al no poder terminar request
		 */
		public synchronized String RequestPost(String url, List<NameValuePair> parametros){
			String salida=null;
			byte[] buffer=new byte[1024];
			HttpPost request = new HttpPost(url);

			try{
				//Agregamos parámetros al cuerpo del request (solo en POST tipo application/x-www-form-urlencoded)
				if(parametros!=null)
					request.setEntity(new UrlEncodedFormEntity(parametros));
				
				HttpResponse respuesta = cliente.execute(request);
				StatusLine estado = respuesta.getStatusLine();
				
				if(estado.getStatusCode()!= SincronizacionTask.HTTP_STATUS_OK)
					throw new Exception("Error en conexión con status: "+estado.toString());
				
				//Procesamos respuesta
				InputStream ist = respuesta.getEntity().getContent();
				ByteArrayOutputStream contenido = new ByteArrayOutputStream();
				
				int bytesLeidos=0;
				while( (bytesLeidos=ist.read(buffer)) != -1 )
					contenido.write(buffer, 0, bytesLeidos);
				
				salida= new String( contenido.toByteArray());
				
				Log.d(TAG, "En POST se descargó: "+salida);
			}catch(Exception ex){
				Exception e2 = new Exception("Error en request tipo POST a url:"+url+"\n"+ex.toString(),ex);
				Log.d(TAG, e2.toString());
				//throw e2;
			}
			return salida;			
		}
		
		public synchronized String RequestGet(String url){
			String salida=null;
			byte[] buffer=new byte[1024];
			HttpGet request = new HttpGet(url);

			try{				
				HttpResponse respuesta = cliente.execute(request);
				StatusLine estado = respuesta.getStatusLine();
				
				if(estado.getStatusCode()!= SincronizacionTask.HTTP_STATUS_OK)
					throw new Exception("Error en conexión con status: "+estado.toString());
				
				//Procesamos respuesta
				InputStream ist = respuesta.getEntity().getContent();
				ByteArrayOutputStream contenido = new ByteArrayOutputStream();
				
				int bytesLeidos=0;
				while( (bytesLeidos=ist.read(buffer)) != -1 )
					contenido.write(buffer, 0, bytesLeidos);
				
				salida= new String( contenido.toByteArray());
				
				Log.d(TAG, "En POST se descargó: "+salida);
			}catch(Exception ex){
				Exception e2 = new Exception("Error en request tipo GET a url:"+url+"\n"+ex.toString(),ex);
				Log.d(TAG, e2.toString());
				//throw e2;
			}
			return salida;			
		}
		
	}//fin HttpHelper
	
	
}//fin clase