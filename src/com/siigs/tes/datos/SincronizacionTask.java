package com.siigs.tes.datos;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Axel
 * 
 * Los 3 tipos de datos son <Parametros, Progreso, Resultado>
 * 
 */
public class SincronizacionTask extends AsyncTask<String, String, String> {

	private final static String TAG= SincronizacionTask.class.getSimpleName();
	
	//Constantes de acciones realizables con servidor
	private final static String ACCION_INICIAR_SESION="1";
	private final static String ACCION_PRIMEROS_CATALOGOS="2";
	private final static String ACCION_RESULTADO="3"; //Para informar resultado de una operación (ok/error/etc)
	private final static String ACCION_PRIMEROS_DATOS = "4";
	private final static String ACCION_ENVIAR_SERVIDOR = "5";
	private final static String ACCION_RECIBIR_ACTUALIZACIONES = "6";
	//Constantes de parámetros enviados dentro de acciones de sincronización
	private final static String PARAMETRO_ID_TAB="id_tab";
	private final static String PARAMETRO_VERSION_APK = "version_apk";
	private final static String PARAMETRO_SESION ="id_sesion";
	private final static String PARAMETRO_ACCION ="id_accion";
	private final static String PARAMETRO_RESULTADO_MSG = "msg";
	private final static String PARAMETRO_DATOS = "datos";
	//Constantes de resultados enviables al servidor en ACCION_RESULTADO
	private final static String RESULTADO_OK = "ok";
	private final static String RESULTADO_JSON_EXCEPTION = "JSONException";
	private final static String RESULTADO_EXCEPTION = "Exception";
	private final static String RESULTADO_NULL_POINTER_EXCEPTION = "NullPointerException";
	private final static String RESULTADO_SQLITE_EXCEPTION = "SQLiteException";
	//Constantes de respuestas que puede devolver servidor en ACCION_INICIAR_SESION o ACCION_RESULTADO
	private final static String RESPUESTA_SESION = "id_sesion";
	private final static String RESPUESTA_INESPERADO = "id_resultado";
		//Constantes de los valores que RESPUESTA_INESPERADO contiene en json
		private final static String RESPUESTA_INESPERADO_DESACTUALIZADO = "Desactualizado";
	private final static String RESPUESTA_URL = "url"; //cuando RESPUESTA_INESPERADO vale 
											//RESPUESTA_INESPERADO_DESACTUALIZADO, esta constante es regresada también
		
	//Estados de comunicación HTTP
	private final static int HTTP_STATUS_OK = 200;
	private final static int HTTP_STATUS_NOT_FOUND = 404;
	
	private final static String ARCHIVO_JSON = "descarga.json";
	
	
	//private ProgressDialog pdProgreso;
	private AlertDialog dlgResultado; //guarda dialogo que visualizará salida
	private Context contexto;
	private Activity invocador;
	private TesAplicacion aplicacion;
	
	private HttpHelper webHelper;
		
	
	public SincronizacionTask(Activity invocador, AlertDialog resultado){
		super();
		this.invocador=invocador;
		this.dlgResultado=resultado;
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
		//aplicacion.setUrlSincronizacion("http://192.168.3.14/tes/servicios/prueba");///////TODO BORRRRRRARRRR
		aplicacion.crearDialogoProgreso(invocador);
	}


	/**
	 * Ejecutado en hilo asíncrono. Recibe uno o más parámetros en forma de arreglo ej: parametros[0]
	 */
	@Override
	protected String doInBackground(String... parametros) {
		/*try {
			Thread.sleep(5000);
			this.publishProgress("ACTUALIZACIÓN");
		} catch (InterruptedException e) {
			Log.e(TAG, "interrumpido trabajo "+ e);
		}
		try {
			Thread.sleep(5000);
			this.publishProgress("otra cosa");
		} catch (InterruptedException e) {
			Log.e(TAG, "interrumpido trabajo "+ e);
		}
		return "terminado";*/
		try{
			Log.d(TAG, "Sincronización en fondo "+ Thread.currentThread().getName());
			SincronizacionTotal();
			return "Sincronización exitosa";
		}catch(Exception ex){
			Log.d(TAG, "Error:"+ex.toString());
			if(ex.getCause()!=null && (ex.getCause() instanceof NoHttpResponseException 
					||  ex.getCause() instanceof SocketTimeoutException) )
				return "No se pudo comunicar con el servidor. Verifique su conexión a Internet e intente de nuevo.";
			return "Hubo un problema al sincronizar" +
					"\n\nDetalles: "+ex.toString();
		}
	}

	/**
	 * Se ejecuta en Thread de UI. Quitamos el diálogo de progreso.
	 */
	@Override
	protected void onPostExecute(String resultado) {
		super.onPostExecute(resultado);
		
		Log.d(TAG, "Terminado proceso en fondo "+ Thread.currentThread().getName());
		if(aplicacion.getDialogoProgreso()!=null){
			aplicacion.getDialogoProgreso().dismiss();
			aplicacion.destruirDialogoProgreso();
		}
		
		
		//NOTA: ESTE CÓDIGO ES TEMPORAL Y DEBE SER REEMPLAZADO POR UNA FUNCIÓN ÚNICA QUE NOTIFICA
		//EL MENSAJE DE ABAJO PARA EL CASO DE PEDIR ACTUALIZACIÓN DEL SOFTWARE
		this.dlgResultado.setMessage(resultado);
		
		if(aplicacion.getRequiereActualizarApk()){
			aplicacion.ValidarRequiereActualizarApk(dlgResultado);
		}else{
			this.dlgResultado.show();
		}
	}

	/**
	 * Se ejecuta en Thread de UI. 
	 * Es llamado desde este thread asincrono con publishProgress()
	 * Actualiza el contenido del mensaje de espera.
	 * @param values
	 */
	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		
		if(aplicacion.getDialogoProgreso()!=null){
			aplicacion.getDialogoProgreso().setMessage(values[0]);
		}
		Log.d(TAG, values[0]);
	}
	

	/**
	 * Implementa la sincronización de datos
	 * @throws Exception 
	 */
	protected synchronized void SincronizacionTotal() throws Exception{
		boolean esNueva= this.aplicacion.getEsInstalacionNueva();

		String idSesion = AccionIniciarSesion();
		Log.d(TAG,"Se ha obtenido llave de sesión:"+idSesion);
		publishProgress("Sesión iniciada...");
		
		if(esNueva){
			AccionPrimerosCatalogos(idSesion);
			AccionPrimerosDatos(idSesion);
			//Si por accidente externo a tableta no hubieran usuarios, evitamos bloquear sincronización futura
			if(Usuario.getTotalUsuariosActivos(contexto)>0)
				this.aplicacion.setEsInstalacionNueva(false);
		}else{
			String ultimaSinc = this.aplicacion.getFechaUltimaSincronizacion();
			AccionEnviarCambiosServidor(idSesion, ultimaSinc);
			if(!this.aplicacion.getRequiereActualizarApk()){
				BorrarDatosAntesSinc();
				AccionRecibirActualizaciones(idSesion);
			}
		}
		this.aplicacion.setFechaUltimaSincronizacion();
 	}

	/**
	 * Informa de resultados al servidor.
	 * @param idSesion Sesión que se lleva a cabo
	 * @param idResultado Identificador del tipo de resultado (éxito o error) que informamos
	 * @param descripcion Describe a detalle la causa de idResultado en caso de tratarse de un error
	 * @return
	 */
	private String EnviarResultado(String idSesion, String idResultado, String descripcion){
		JSONObject msgSalida=new JSONObject();
		try {
			msgSalida.put("id_resultado", idResultado);
			msgSalida.put("descripcion", descripcion == null?"":descripcion);
		} catch (JSONException e) {
			Log.e(TAG, "Esto nunca en la vida debería suceder. No se pudo encapsular resultado en JSON."+e.toString());
		}
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
		parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_RESULTADO));
        parametros.add(new BasicNameValuePair(PARAMETRO_RESULTADO_MSG, msgSalida.toString() ));
		
        Log.d(TAG,"Enviando resultado de acción con id_resultado:"+ idResultado+ " y descripción:"+descripcion);
		try {
			return webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		} catch (Exception e) {
			Log.d(TAG,"No se pudo enviar resultado. "+e);
		}
		return null;
	}
	
	/**
	 * Esta acción recibe como respuesta una cadena JSON con el id de la sesión de sincronización
	 * @return id de la sesión
	 * @throws Exception 
	 */
	private String AccionIniciarSesion() throws Exception {
		WifiManager wifi=(WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		String macaddress = wifi.getConnectionInfo().getMacAddress();
		if(macaddress == null)macaddress="";
		macaddress = macaddress.replace(":", "");
		if(macaddress.equals(""))macaddress= "123456789"; //PARA DEBUGEO, EN DISPOSITIVO REAL NO DEBERÍA PASAR
		Log.d(TAG, "mac es:"+macaddress);
		
		String version= aplicacion.getVersionApk();
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_INICIAR_SESION));
        parametros.add(new BasicNameValuePair(PARAMETRO_ID_TAB, macaddress));
        parametros.add(new BasicNameValuePair(PARAMETRO_VERSION_APK, version+"" ));
		
        Log.d(TAG, "Request Inicio de sesión");
        publishProgress("Conectando con servidor...");
		String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		
		JSONObject jo;
		try {
			jo = new JSONObject(json);
		} catch (JSONException e) {
			String msgError="No se interpretó el resultado del servidor como json:"+json+"\n"+e.toString();
			Log.e(TAG, msgError);
			throw new Exception(msgError);
		}
		
		if(jo.has(PARAMETRO_SESION))
			return jo.getString(RESPUESTA_SESION);
		
		//Como no llegó id sesión, asumimos que hay un mensaje detallado en RESPUESTA_INESPERADO
		String inesperado = jo.getString(RESPUESTA_INESPERADO);
		if(inesperado.equalsIgnoreCase(RESPUESTA_INESPERADO_DESACTUALIZADO)){
			DefinirComoDispositivoSinActualizar(jo.getString(RESPUESTA_URL));
			throw new Exception("Esta aplicación requiere actualizarse antes de sincronizarse");
		}
		
		throw new Exception(inesperado);
	}//fin AccionIniciarSesion
	
	
	/**
	 * Esta acción manda credenciales y recibe primeros catálogos a insertar en base de datos
	 * @param idSesion
	 * @throws Exception 
	 */
	private void AccionPrimerosCatalogos(String idSesion) throws Exception{
		String msgError;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_CATALOGOS));

        publishProgress("Solicitando primeros catálogos");
		InputStream stream = webHelper.RequestStreamPost(aplicacion.getUrlSincronizacion(), parametros);

		stream = GuardarAbrirStream(stream);
		
		try {
			InterpretarDatosServidor(stream);
			
			EnviarResultado(idSesion, RESULTADO_OK,null);
			
		}catch (SQLiteException e){
			msgError = "Error al interpretar primeros catálogos en base de datos local:" + e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_SQLITE_EXCEPTION, msgError);
			throw e;
		}catch (NullPointerException e){
			msgError = "Error al interpretar primeros catálogos. Se intentó accesar algo que no existe:"+ e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_NULL_POINTER_EXCEPTION, msgError);
			throw e;
		} catch (Exception e){
			msgError = "Error desconocido al interpretar primeros catálogos:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw e;
		}
		//... validar llegó lo mínimo, caso contrario, error
	}
	
	/**
	 * Esta acción manda credenciales y recibe primeras tablas transaccionales a insertar en base de datos
	 * @param idSesion
	 * @throws Exception 
	 */
	private void AccionPrimerosDatos(String idSesion) throws Exception{
		String msgError;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_DATOS));

        publishProgress("Solicitando primeros datos transaccionales");
		InputStream stream = webHelper.RequestStreamPost(aplicacion.getUrlSincronizacion(), parametros);
		
		stream = GuardarAbrirStream(stream);
		
		try {
			InterpretarDatosServidor(stream);
			
			EnviarResultado(idSesion, RESULTADO_OK,null);
			
		}catch (SQLiteException e){
			msgError = "Error al interpretar primeros catálogos en base de datos local:" + e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_SQLITE_EXCEPTION, msgError);
			throw e;
		}catch (NullPointerException e){
			msgError = "Error al interpretar primeros catálogos. Se intentó accesar algo que no existe:"+ e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_NULL_POINTER_EXCEPTION, msgError);
			throw e;
		} catch (Exception e){
			msgError = "Error desconocido al interpretar primeros catálogos:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw e;
		}
	}
	
	/**
	 * Esta acción manda credenciales y recibe cambios a realizar en base de datos
	 * @param idSesion
	 * @throws Exception 
	 */
	private void AccionRecibirActualizaciones(String idSesion) throws Exception{
		String msgError;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_RECIBIR_ACTUALIZACIONES));

        publishProgress("Solicitando actualizaciones de datos");
		InputStream stream = webHelper.RequestStreamPost(aplicacion.getUrlSincronizacion(), parametros);
		
		stream = GuardarAbrirStream(stream);
		
		try {
			InterpretarDatosServidor(stream);
			
			EnviarResultado(idSesion, RESULTADO_OK,null);
			
		}catch (SQLiteException e){
			msgError = "Error al interpretar actualizaciones recibidas para base de datos local:" + e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_SQLITE_EXCEPTION, msgError);
			throw e;
		}catch (NullPointerException e){
			msgError = "Error al interpretar actualizaciones. Se intentó accesar algo que no existe:"+ e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_NULL_POINTER_EXCEPTION, msgError);
			throw e;
		} catch (Exception e){
			msgError = "Error desconocido al interpretar actualizaciones:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw e;
		}
	}
	
	/**
	 * Interpreta los datos recibidos del servidor que deben estar en JSON.
	 * @param stream
	 * @throws Exception
	 */
	private void InterpretarDatosServidor(InputStream stream) throws Exception{
		ContentResolver cr = contexto.getContentResolver();
		
		JsonReader reader;
		Gson gson=new Gson();
		String atributo=""; //para iterar atributos json
		
		Uri uri; //helper
		ContentValues fila; //helper
		
		try {
			reader = new JsonReader(new InputStreamReader(stream,"UTF-8"));	
			reader.beginObject(); //Lee objeto principal
			//Lectura de catálogos/datos fijos
			while(reader.hasNext()){
				atributo=reader.nextName();
				
				if(atributo.equalsIgnoreCase("id_tipo_censo")){
					publishProgress("Asignando tipo de senso");
					aplicacion.setTipoCenso( reader.nextInt() );
					
				}else if(atributo.equalsIgnoreCase("id_asu_um")){
					publishProgress("Asignando unidad médica");
					aplicacion.setUnidadMedica( reader.nextInt() );
					
				}else if(atributo.equalsIgnoreCase(Grupo.NOMBRE_TABLA)){
					publishProgress("Interpretando Grupos");
					reader.beginArray();
					while(reader.hasNext()){
						Grupo grupo = gson.fromJson(reader, Grupo.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(grupo);
						uri = ProveedorContenido.GRUPO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Grupo.ID+"="+grupo._id,null);
					}
					reader.endArray();

				}else if(atributo.equalsIgnoreCase(Usuario.NOMBRE_TABLA)){
					publishProgress("Interpretando Usuarios");
					reader.beginArray();
					while(reader.hasNext()){
						Usuario usuario = gson.fromJson(reader, Usuario.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(usuario);
						uri = ProveedorContenido.USUARIO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Usuario.ID+"="+usuario._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Permiso.NOMBRE_TABLA)){
					publishProgress("Interpretando Permisos");
					reader.beginArray();
					while(reader.hasNext()){
						Permiso permiso = gson.fromJson(reader, Permiso.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(permiso);
						uri = ProveedorContenido.PERMISO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Permiso.ID+"="+permiso._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Notificacion.NOMBRE_TABLA)){
					publishProgress("Interpretando Notificaciones");
					reader.beginArray();
					while(reader.hasNext()){
						Notificacion notificacion = gson.fromJson(reader, Notificacion.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(notificacion);
						uri = ProveedorContenido.NOTIFICACION_CONTENT_URI;
						cr.insert(uri, fila); //Notificaciones siempre son nuevas
							//cr.update(uri, fila, Notificacion.ID+"="+notificacion._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(TipoSanguineo.NOMBRE_TABLA)){
					publishProgress("Interpretando Tipo Sanguineo");
					reader.beginArray();
					while(reader.hasNext()){
						TipoSanguineo tipoSangre = gson.fromJson(reader, TipoSanguineo.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(tipoSangre);
						uri = ProveedorContenido.TIPO_SANGUINEO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, TipoSanguineo.ID+"="+tipoSangre._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Vacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando Vacunas");
					reader.beginArray();
					while(reader.hasNext()){
						Vacuna vacuna = gson.fromJson(reader, Vacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(vacuna);
						uri = ProveedorContenido.VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Vacuna.ID+"="+vacuna._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(AccionNutricional.NOMBRE_TABLA)){
					publishProgress("Interpretando Acciones Nutricionales");
					reader.beginArray();
					while(reader.hasNext()){
						AccionNutricional accion = gson.fromJson(reader, AccionNutricional.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(accion);
						uri = ProveedorContenido.ACCION_NUTRICIONAL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, AccionNutricional.ID+"="+accion._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Ira.NOMBRE_TABLA)){
					publishProgress("Interpretando Iras");
					reader.beginArray();
					while(reader.hasNext()){
						Ira ira = gson.fromJson(reader, Ira.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(ira);
						uri = ProveedorContenido.IRA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Ira.ID+"="+ira._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Eda.NOMBRE_TABLA)){
					publishProgress("Interpretando Edas");
					reader.beginArray();
					while(reader.hasNext()){
						Eda eda = gson.fromJson(reader, Eda.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(eda);
						uri = ProveedorContenido.EDA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Eda.ID+"="+eda._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Consulta.NOMBRE_TABLA)){
					publishProgress("Interpretando Consultas");
					reader.beginArray();
					while(reader.hasNext()){
						Consulta consulta = gson.fromJson(reader, Consulta.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(consulta);
						uri = ProveedorContenido.CONSULTA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Consulta.ID+"="+consulta._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Alergia.NOMBRE_TABLA)){
					publishProgress("Interpretando Alergias");
					reader.beginArray();
					while(reader.hasNext()){
						Alergia alergia = gson.fromJson(reader, Alergia.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(alergia);
						uri = ProveedorContenido.ALERGIA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Alergia.ID+"="+alergia._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Afiliacion.NOMBRE_TABLA)){
					publishProgress("Interpretando Afiliaciones");
					reader.beginArray();
					while(reader.hasNext()){
						Afiliacion afiliacion = gson.fromJson(reader, Afiliacion.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(afiliacion);
						uri = ProveedorContenido.AFILIACION_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Afiliacion.ID+"="+afiliacion._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Nacionalidad.NOMBRE_TABLA)){
					publishProgress("Interpretando Nacionalidades");
					reader.beginArray();
					while(reader.hasNext()){
						Nacionalidad nacionalidad = gson.fromJson(reader, Nacionalidad.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(nacionalidad);
						uri = ProveedorContenido.NACIONALIDAD_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Nacionalidad.ID+"="+nacionalidad._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(OperadoraCelular.NOMBRE_TABLA)){
					publishProgress("Interpretando Operadoras Celulares");
					reader.beginArray();
					while(reader.hasNext()){
						OperadoraCelular operadora = gson.fromJson(reader, OperadoraCelular.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(operadora);
						uri = ProveedorContenido.OPERADORA_CELULAR_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, OperadoraCelular.ID+"="+operadora._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(PendientesTarjeta.NOMBRE_TABLA)){
					publishProgress("Interpretando Pendientes para Tarjetas");
					reader.beginArray();
					while(reader.hasNext()){
						PendientesTarjeta pendiente = gson.fromJson(reader, PendientesTarjeta.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(pendiente);
						uri = ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI;
						//TODO PENDIENTE BORRAR REGISTROS FORÁNEOS NO RESUELTOS ANTES DE RECIBIR NUEVOS
						cr.insert(uri, fila);
							//cr.update(uri, fila, PendientesTarjeta.ID_PERSONA+"=? and "+PendientesTarjeta.REGISTRO_JSON +"=? ",new String[]{pendiente.id_persona,});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ArbolSegmentacion.NOMBRE_TABLA)){
					InterpretarArbolSegmentacion(gson, reader, cr);
					
				}else if(atributo.equalsIgnoreCase(ReglaVacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando Reglas de vacunación");
					reader.beginArray();
					while(reader.hasNext()){
						ReglaVacuna regla = gson.fromJson(reader, ReglaVacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(regla);
						uri = ProveedorContenido.REGLA_VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ReglaVacuna.ID+"="+regla.id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ViaVacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando Vías Vacuna");
					reader.beginArray();
					while(reader.hasNext()){
						ViaVacuna via = gson.fromJson(reader, ViaVacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(via);
						uri = ProveedorContenido.VIA_VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ViaVacuna.ID+"="+via._id,null);
					}
					reader.endArray();
					
				//////////////////TABLAS TRANSACCIONALES/////////////////
				}else if(atributo.equalsIgnoreCase(Tutor.NOMBRE_TABLA)){
					publishProgress("Interpretando Tutores");
					reader.beginArray();
					while(reader.hasNext()){
						Tutor tutor = gson.fromJson(reader, Tutor.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(tutor);
						uri = ProveedorContenido.TUTOR_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Tutor.ID+"=?",new String[]{tutor.id});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Persona.NOMBRE_TABLA)){
					publishProgress("Interpretando Personas");
					reader.beginArray();
					while(reader.hasNext()){
						Persona persona = gson.fromJson(reader, Persona.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona);
						uri = ProveedorContenido.PERSONA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Persona.ID+"=?",new String[]{persona.id});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(PersonaTutor.NOMBRE_TABLA)){
					publishProgress("Interpretando Personas con Tutores");
					reader.beginArray();
					while(reader.hasNext()){
						PersonaTutor persona_tutor = gson.fromJson(reader, PersonaTutor.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona_tutor);
						uri = ProveedorContenido.PERSONA_TUTOR_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PersonaTutor.ID_PERSONA+"=?",new String[]{persona_tutor.id_persona});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(PersonaAlergia.NOMBRE_TABLA)){
					publishProgress("Interpretando Personas con alergias");
					reader.beginArray();
					while(reader.hasNext()){
						PersonaAlergia persona_alergia = gson.fromJson(reader, PersonaAlergia.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona_alergia);
						uri = ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PersonaAlergia.ID_PERSONA+"=?",new String[]{persona_alergia.id_persona});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(PersonaAfiliacion.NOMBRE_TABLA)){
					publishProgress("Interpretando Personas con afiliaciones");
					reader.beginArray();
					while(reader.hasNext()){
						PersonaAfiliacion persona_afiliacion = gson.fromJson(reader, PersonaAfiliacion.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona_afiliacion);
						uri = ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PersonaAfiliacion.ID_PERSONA+"=? and "
									+PersonaAfiliacion.ID_AFILIACION+"=?",
									new String[]{persona_afiliacion.id_persona, persona_afiliacion.id_afiliacion+""});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(RegistroCivil.NOMBRE_TABLA)){
					publishProgress("Interpretando Registro Civil");
					reader.beginArray();
					while(reader.hasNext()){
						RegistroCivil registro = gson.fromJson(reader, RegistroCivil.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(registro);
						uri = ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, RegistroCivil.ID_PERSONA+"=?",new String[]{registro.id_persona});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(AntiguaUM.NOMBRE_TABLA)){
					publishProgress("Interpretando Antiguas unidades médicas");
					reader.beginArray();
					while(reader.hasNext()){
						AntiguaUM antiguaUM = gson.fromJson(reader, AntiguaUM.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(antiguaUM);
						uri = ProveedorContenido.ANTIGUA_UM_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, AntiguaUM.ID_PERSONA+"=? and "+ AntiguaUM.FECHA_CAMBIO+"=?", 
									new String[]{antiguaUM.id_persona,antiguaUM.fecha_cambio});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(AntiguoDomicilio.NOMBRE_TABLA)){
					publishProgress("Interpretando Antiguos domicilios");
					reader.beginArray();
					while(reader.hasNext()){
						AntiguoDomicilio domicilio = gson.fromJson(reader, AntiguoDomicilio.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(domicilio);
						uri = ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, AntiguoDomicilio.ID_PERSONA+"=? and "+ AntiguoDomicilio.FECHA_CAMBIO+"=?", 
									new String[]{domicilio.id_persona, domicilio.fecha_cambio});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlVacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando Controles de Vacunas");
					reader.beginArray();
					while(reader.hasNext()){
						ControlVacuna control_vacuna = gson.fromJson(reader, ControlVacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_vacuna);
						uri = ProveedorContenido.CONTROL_VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlVacuna.ID_PERSONA+"=? and "+ ControlVacuna.FECHA+"=?", 
									new String[]{control_vacuna.id_persona, control_vacuna.fecha});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlIra.NOMBRE_TABLA)){
					publishProgress("Interpretando Controles de Iras");
					reader.beginArray();
					while(reader.hasNext()){
						ControlIra control_ira = gson.fromJson(reader, ControlIra.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_ira);
						uri = ProveedorContenido.CONTROL_IRA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlIra.ID_PERSONA+"=? and "+ ControlIra.FECHA+"=?", 
									new String[]{control_ira.id_persona, control_ira.fecha});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlEda.NOMBRE_TABLA)){
					publishProgress("Interpretando Controles de Edas");
					reader.beginArray();
					while(reader.hasNext()){
						ControlEda control_eda = gson.fromJson(reader, ControlEda.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_eda);
						uri = ProveedorContenido.CONTROL_EDA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlEda.ID_PERSONA+"=? and "+ ControlEda.FECHA+"=?", 
									new String[]{control_eda.id_persona, control_eda.fecha});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlConsulta.NOMBRE_TABLA)){
					publishProgress("Interpretando Controles de Consultas");
					reader.beginArray();
					while(reader.hasNext()){
						ControlConsulta control_consulta = gson.fromJson(reader, ControlConsulta.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_consulta);
						uri = ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlConsulta.ID_PERSONA+"=? and "+ ControlConsulta.FECHA+"=?", 
									new String[]{control_consulta.id_persona, control_consulta.fecha});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlAccionNutricional.NOMBRE_TABLA)){
					publishProgress("Interpretando Controles de Acciones Nutricionales");
					reader.beginArray();
					while(reader.hasNext()){
						ControlAccionNutricional control_accion = gson.fromJson(reader, ControlAccionNutricional.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_accion);
						uri = ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlAccionNutricional.ID_PERSONA+"=? and "+ ControlAccionNutricional.FECHA+"=?", 
									new String[]{control_accion.id_persona, control_accion.fecha});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlNutricional.NOMBRE_TABLA)){
					publishProgress("Interpretando Controles Nutricionales");
					reader.beginArray();
					while(reader.hasNext()){
						ControlNutricional control_nutricional = gson.fromJson(reader, ControlNutricional.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_nutricional);
						uri = ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlNutricional.ID_PERSONA+"=? and "+ ControlNutricional.FECHA+"=?", 
									new String[]{control_nutricional.id_persona, control_nutricional.fecha});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(EsquemaIncompleto.NOMBRE_TABLA)){
					publishProgress("Interpretando Esquemas incompletos");
					reader.beginArray();
					while(reader.hasNext()){
						EsquemaIncompleto esquema = gson.fromJson(reader, EsquemaIncompleto.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(esquema);
						uri = ProveedorContenido.ESQUEMA_INCOMPLETO_CONTENT_URI;
						cr.insert(uri, fila); //No hay updates en caso de haber repetidos (tabla debería estar vacía antes)
					}
					reader.endArray();
					
				}else{
					//Se recibió algo que NO se puede interpretar (así pues mal formado)
					//Se intentará leer esto que no comprendemos pero si hay error, saldrá esto de evidencia
					atributo = "atributo NO ESPERADO "+atributo;
					publishProgress("Interpretando " + atributo);
					//Se intentará leer lo no reconocido
					if(reader.hasNext())
						reader.skipValue(); //Se salta valor, objeto o arreglo hasta terminarlo
					publishProgress("Se terminó de leer datos no esperados");
				}
			}//fin while reader.hasNext
			
			//reader.endObject();
			reader.close();
			if(!contexto.deleteFile(ARCHIVO_JSON))
				publishProgress("No se borró archivo temporal de descarga");
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			//Sucede al intentar leer UTF-8. Nunca debería suceder
		}catch (IllegalAccessException e){
			Exception ex = new Exception("Error al generar ContentValues en tabla '"+atributo+"' "+e);
			Log.d(TAG, ex.toString());
			throw ex;
		}catch (IOException e) {
			//Error de lectura json
			Exception ex = new Exception("Error al leer json en atributo '"+atributo+"' "+e);
			Log.d(TAG, ex.toString());
			throw ex;
		}catch (Exception e){
			Exception ex = new Exception("Excepción mientras se analizaba atributo '"+atributo+"' "+e);
			Log.d(TAG, ex.toString());
			throw ex;
		}
	}//fin InterpretarDatosServidor
	
	
	private void InterpretarArbolSegmentacion(Gson gson, JsonReader reader, ContentResolver cr) throws IOException, IllegalAccessException{
		publishProgress("Interpretando Arbol de Segmentación");
		Uri uri = ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI;
		int registros=0; //contador de registros procesados
		int limite=500; //límite para acumular bulkinsert (en instalación nueva) o reportar progreso (no nueva)

		reader.beginArray();
		
		if(this.aplicacion.getEsInstalacionNueva()){
			List<ArbolSegmentacion> lista = new ArrayList<ArbolSegmentacion>(limite);
			
			while(reader.hasNext()){
				lista.add((ArbolSegmentacion) gson.fromJson(reader, ArbolSegmentacion.class));
				if(lista.size()==limite || !reader.hasNext()){
					ContentValues[] filas = new ContentValues[lista.size()];
					publishProgress("Generandoo "+lista.size()+" ramas");
					int n=0;
					for(ArbolSegmentacion arbol : lista)
						filas[n++] = DatosUtil.ContentValuesDesdeObjeto(arbol);
					registros+=lista.size();
					publishProgress("Insertando "+lista.size()+" ramas de "+registros+" hasta ahora");
					cr.bulkInsert(uri, filas);
					//	cr.update(uri, fila, ArbolSegmentacion.ID+"="+arbol._id,null);
					lista.clear();
				}
			}
		}else{
			
			while(reader.hasNext()){
				ArbolSegmentacion arbol = gson.fromJson(reader, ArbolSegmentacion.class);
				ContentValues fila = DatosUtil.ContentValuesDesdeObjeto(arbol);
				if(cr.insert(uri, fila)==null)
					cr.update(uri, fila, ArbolSegmentacion.ID+"="+ arbol._id, null);
				if( ++registros % limite == 0 )
					publishProgress("Procesadas "+registros+" ramas");
			}
		}
		
		reader.endArray();
	}//fin InterpretarArbolSegmentacion
	
	/**
	 * Guarda stream en disco. Cierra el stream original y regresa el archivo generado al principio
	 * en un FileInputStream abierto y listo para consumirse
	 * @param stream Stream a guardar en disco.
	 * @return FileInputStream abierto y listo apuntando al archivo generado que contiene información de stream
	 * @throws Exception
	 */
	private InputStream GuardarAbrirStream(InputStream stream) throws Exception{
		FileOutputStream fsalida = contexto.openFileOutput(ARCHIVO_JSON, 0);
		final byte[] buffer = new byte[1024];
		int read;
		int bloque = 100000, bloqueLeido=0 ;
		try {
			while ((read = stream.read(buffer)) != -1){
			    fsalida.write(buffer, 0, read);
			    bloqueLeido += read;
			    if(bloqueLeido > bloque){
			    	publishProgress("Descargando "+bloqueLeido + "bytes");
			    	bloqueLeido=0;
			    }
			}
			fsalida.flush();
			fsalida.close();
			stream.close();
		} catch (IOException e) {
			String desc = "Hubo un error al descargar stream a "+ARCHIVO_JSON;
			Log.d(TAG, desc);
			throw new Exception(desc, e);
		}
		return contexto.openFileInput(ARCHIVO_JSON);
	}
	
	/**
	 * Algunos datos deben vaciarse/borrarse antes de recibir datos del servidor. Aquí se borran esos datos
	 */
	private void BorrarDatosAntesSinc(){
		ContentResolver cr = contexto.getContentResolver();
		cr.delete(ProveedorContenido.ESQUEMA_INCOMPLETO_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, null, null);
	}
	
	
	/**
	 * Envia al servidor los cambios desde la última actualización
	 * @param idSesion
	 * @param ultimaSinc
	 * @throws Exception 
	 */
	private void AccionEnviarCambiosServidor(String idSesion, String ultimaSinc) throws Exception{
		String msgError = null;
		
		//VARIABLES PARA CONSULTAR BASE DE DATOS
		ContentResolver cr = contexto.getContentResolver();
		Cursor cur=null;
		String where="";
		String[] valoresWhere; //contenedor de valores de filtro where
		String[] valoresWhereSincronizacion = {ultimaSinc}; //filtro where más común por comodidad
		//String[] columnas = null;
		
		JSONObject datosSalida = new JSONObject(); //Contenedor del json final
		publishProgress("Generando datos a enviar al servidor...");
		String[] excepciones= null;
		
		try {
			//TABLA TUTOR
			where = Tutor.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.TUTOR_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{Tutor._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Tutor.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			
			//TABLA ANTIGUA UM
			where = AntiguaUM.FECHA_CAMBIO + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ANTIGUA_UM_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{AntiguaUM._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(AntiguaUM.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA ANTIGUO DOMICILIO
			where = AntiguoDomicilio.FECHA_CAMBIO + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{AntiguoDomicilio._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(AntiguoDomicilio.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA
			where = Persona.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere = valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{Persona._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Persona.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA X ALERGIA
			where = PersonaAlergia.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{PersonaAlergia._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(PersonaAlergia.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA X TUTOR
			where = PersonaTutor.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_TUTOR_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{PersonaTutor._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(PersonaTutor.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA X AFILIACION ???????????????????????? REGISTRO CIVIL ?????
			where = PersonaAfiliacion.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{PersonaAfiliacion._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(PersonaAfiliacion.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA REGISTRO CIVIL
			where = RegistroCivil.FECHA_REGISTRO + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{RegistroCivil._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(RegistroCivil.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL VACUNA
			where = ControlVacuna.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlVacuna._ID, ControlVacuna.ID_INVITADO};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlVacuna.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL IRA
			where = ControlIra.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_IRA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlIra._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlIra.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL EDA
			where = ControlEda.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_EDA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlEda._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlEda.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL CONSULTA
			where = ControlConsulta.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlConsulta._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlConsulta.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL ACCION NUTRICIONAL
			where = ControlAccionNutricional.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlAccionNutricional._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlAccionNutricional.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL NUTRICIONAL
			where = ControlNutricional.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlNutricional._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlNutricional.NOMBRE_TABLA, filas);
			}
			cur.close();

			//TABLA BITÁCORA
			where = Bitacora.FECHA_HORA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.BITACORA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{Bitacora.ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Bitacora.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA ERROR
			where = ErrorSis.FECHA_HORA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ERROR_SIS_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ErrorSis.ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ErrorSis.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			
			//Preparamos POST
			List<NameValuePair> parametros = new ArrayList<NameValuePair>();
	        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
	        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_ENVIAR_SERVIDOR));
	        parametros.add(new BasicNameValuePair(PARAMETRO_DATOS, datosSalida.toString() ));

///////////////////////TODO TES_PENDIENTES_TARJETA..... PENDIENTE
	        
	        publishProgress("Enviando a servidor datos de "+datosSalida.length()+" tablas");
			String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
			
			
			//CHECAMOS SI SERVIDOR NOS PIDE ACTUALIZAR LA VERSIÓN DEL SOFTWARE
			JSONObject jo;
			try {
				jo = new JSONObject(json);
			} catch (JSONException e) {
				msgError="Se enviaron los datos con éxito a servidor, pero éste respondió " +
						"con el siguiente mensaje desconocido:"+json;
				Log.e(TAG, msgError);
				throw new Exception(msgError);
			}
			if(jo.has(RESPUESTA_INESPERADO) && 
					jo.getString(RESPUESTA_INESPERADO).equalsIgnoreCase(RESPUESTA_INESPERADO_DESACTUALIZADO))
				DefinirComoDispositivoSinActualizar(jo.getString(RESPUESTA_URL));
			
		} catch (JSONException e) {
			msgError = "Error en json:"+e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_JSON_EXCEPTION, msgError);
			throw new Exception(msgError);
		} catch (Exception e){
			msgError = "Error desconocido:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw new Exception(msgError);
		}finally{
			if(cur!=null)cur.close();
		}
	}//fin AccionEnviarServidor
	
	/**
	 * Cambia el estado de este dispositivo para no permitir uso
	 */
	private void DefinirComoDispositivoSinActualizar(String urlActualizacion){
		this.aplicacion.setRequiereActualizarApk(true);
		this.aplicacion.setUrlActualizacionApk(urlActualizacion);
	}
		
	private void GenerarAccionX(){//TODO Borrar esta función
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
		//HttpContext contextoHttp;
		
		public HttpHelper(){
			//Para limitar el tiempo de espera...
			final HttpParams parametros = new BasicHttpParams();
			//HttpConnectionParams.setSoTimeout(parametros, 10000);
			HttpConnectionParams.setConnectionTimeout(parametros, 10000);

			cliente = new DefaultHttpClient(parametros);
			//contextoHttp = new BasicHttpContext();
		}
		
		/**
		 * Función helper para mandar request tipo POST y regresar la respuesta del servidor.
		 * @param url Dirección a conectar
		 * @param parametros Valores a incluir en request
		 * @return String con resultado del request
		 * @throws Exception 
		 */
		public synchronized String RequestPost(String url, List<NameValuePair> parametros) throws Exception{
			String salida=null;
			byte[] buffer=new byte[1024];

			try{
				//Procesamos respuesta
				InputStream ist = RequestStreamPost(url, parametros);
				ByteArrayOutputStream contenido = new ByteArrayOutputStream();
				
				int bytesLeidos=0;
				while( (bytesLeidos=ist.read(buffer)) != -1 )
					contenido.write(buffer, 0, bytesLeidos);
				//Gson gson=new Gson();gson.
				salida= contenido.toString(); //new String( contenido.toByteArray());

				Log.d(TAG, "POST descargó datos: "+ (salida.length()>1000000? salida.length()+" caracteres" : "'"+salida+"'"));
			}catch(Exception ex){
				Log.d(TAG, "Error en request tipo POST a url:"+url+"\n"+ex.toString() );
				throw ex;
			}
			return salida;
		}

		/**
		 * Función helper para mandar request tipo POST y regresar la respuesta del servidor.
		 * @param url Dirección a conectar
		 * @param parametros Valores a incluir en request
		 * @return InputStream apuntando a la respuesta del servidor
		 * @throws Exception 
		 */
		public synchronized InputStream RequestStreamPost(String url, List<NameValuePair> parametros) throws Exception{
			HttpPost request = new HttpPost(url);

			try{
				//Agregamos parámetros al cuerpo del request (solo en POST tipo application/x-www-form-urlencoded)
				if(parametros!=null)
					request.setEntity(new UrlEncodedFormEntity(parametros, "UTF-8"));
				
				HttpResponse respuesta = cliente.execute(request);
				StatusLine estado = respuesta.getStatusLine();
				
				if(estado.getStatusCode()!= SincronizacionTask.HTTP_STATUS_OK)
					throw new Exception("Error en conexión con status: "+estado.toString());
				
				Log.d(TAG, "POST ha obtenido un InputStream");
				return respuesta.getEntity().getContent();
			}catch(Exception ex){
				Exception e2 = new Exception("Error en request tipo POST a url:"+url+"\n"+ex.toString(),ex);
				Log.d(TAG, e2.toString());
				throw e2;
			}
		}

		public synchronized InputStream RequestGet(String url){
			HttpGet request = new HttpGet(url);
			Log.d(TAG, "Inicia request GET en "+url);
			try{
				HttpResponse respuesta = cliente.execute(request);
				StatusLine estado = respuesta.getStatusLine();
				
				if(estado.getStatusCode()!= SincronizacionTask.HTTP_STATUS_OK)
					throw new Exception("Error en conexión con status: "+estado.toString());
				
				Log.d(TAG, "GET ha obtenido un InputStream");
				return respuesta.getEntity().getContent();
			}catch(Exception ex){
				Exception e2 = new Exception("Error en request tipo GET a url:"+url+"\n"+ex.toString(),ex);
				Log.d(TAG, e2.toString());
				//throw e2;
			}
			return null;
		}
		
	}//fin HttpHelper
	
	
}//fin clase