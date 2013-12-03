package com.siigs.tes.datos;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.siigs.tes.VariablesEntorno;
import com.siigs.tes.datos.tablas.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Axel
 * 
 * Los 3 tipos de datos son <Parametros, Progreso, Resultado>
 */
public class SincronizacionTask extends AsyncTask<String, Integer, String> {

	private final static String TAG= SincronizacionTask.class.getSimpleName();
		
	//Constantes de acciones en sincronización
	private final static String PARAMETRO_TAB="id_tab";
	private final static String PARAMETRO_SESION="id_sesion";
	private final static String PARAMETRO_ACCION ="id_accion";
	private final static String ACCION_INICIAR_SESION="1";
	private final static String ACCION_PRIMEROS_CATALOGOS="2";
	
	//Constantes en intentos
	private static int NUM_INTENTOS = 3;
	private static int TIEMPO_ESPERA = 2000; //tiempo de espera entre intentos (milisegundos)
	
	//Estados de comunicacióni HTTP
	private final static int HTTP_STATUS_OK = 200;
	private final static int HTTP_STATUS_NOT_FOUND = 404;
	
	
	private ProgressDialog pdProgreso;
	private Context contexto;
	private Activity invocador;
	
		
	
	public SincronizacionTask(Activity invocador){
		super();
		this.invocador=invocador;
		this.contexto=invocador.getApplicationContext();
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
			Log.d(TAG, "Proceso en fondo "+ Thread.currentThread().getName());
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
	
	/**
	 * Función helper para mandar request tipo GET y regresar la respuesta del servidor.
	 * @param url Dirección a conectar
	 * @return String con resultado del request
	 * @throws Exception 
	 */
	private static synchronized String RequestGet(String url) {
		String salida=null;
		byte[] buffer=new byte[1024];
		HttpClient cliente = new DefaultHttpClient();
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
			
			Log.d(TAG, "En GET se descargó: "+salida);
		}catch(Exception ex){
			Exception e2 = new Exception("Error en request tipo GET a url:"+url, ex);
			Log.d(TAG, ex.toString());
			//throw e2;
		}
		return salida;
	}
	
	/**
	 * Función helper para mandar request tipo GET y regresar la respuesta del servidor.
	 * @param url Dirección a conectar
	 * @param parametros Valores a incluir en request
	 * @return String con resultado del request
	 * @throws Exceptiono Al no poder terminar request
	 */
	private static synchronized String RequestPost(String url, List<NameValuePair> parametros) {
		String salida=null;
		byte[] buffer=new byte[1024];
		HttpClient cliente = new DefaultHttpClient();
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
			Exception e2 = new Exception("Error en request tipo POST a url:"+url,ex);
			Log.d(TAG, e2.toString());
			//throw e2;
		}
		return salida;
	}
	

	protected synchronized void SincronizacionTotal(){
		
		//identificar contexto de tableta:
		//es nueva??
		//última sincronización no completó??
		String idSesion = AccionIniciarSesion();
		Log.d(TAG,"Se ha obtenido llave de sesión:"+idSesion);
		
		AccionPrimerosCatalogos(idSesion);
		
        //RequestPost(URL_SINCRONIZACION + "receive", parametros);
        GenerarAccionX();
		//String resultado = RequestGet(URL_SINCRONIZACION+ACCION_ENVIAR_MAC+"/123456789");
		
		//JSONObject ojb=new JSONObject();ojb.
		//JsonWriter jWriter = new JsonWriter()
	}

	/**
	 * Esta acción recibe como respuesta una cadena JSON con el id de la sesión de sincronización
	 * @return id de la sesión
	 * @throws Exception 
	 */
	private String AccionIniciarSesion() {
		WifiManager wifi=(WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		String macaddress= wifi.getConnectionInfo().getMacAddress();
		macaddress = macaddress.replace(":", "");
		Log.d(TAG, "mac es:"+macaddress);
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_INICIAR_SESION));
        parametros.add(new BasicNameValuePair(PARAMETRO_TAB, macaddress));
		
        Log.d(TAG,"Request Inicio de sesión");
		String json = RequestPost(VariablesEntorno.URL_SINCRONIZACION, parametros);
		
		try {
			JSONObject jo=new JSONObject(json);
			return jo.getString(PARAMETRO_SESION);
		} catch (JSONException e) {
			Log.e(TAG, "No se interpretó llave sesión en json:"+json+" "+e.toString());
		}
		return null;
	}
	
	/**
	 * Esta acción manda credenciales y recibe primeros catálogos a insertar en base de datos
	 */
	private void AccionPrimerosCatalogos(String id_sesion){
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, id_sesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_CATALOGOS));
		
        Log.d(TAG, "Request primeros catálogos");
		String json = RequestPost(VariablesEntorno.URL_SINCRONIZACION, parametros);
		
		try {
			JSONObject jo=new JSONObject(json);
			
			//ACTUALIZAMOS VARIABLES DE ENTORNO
			VariablesEntorno.setTipoCenso( jo.getInt("id_tipo_censo") );
			VariablesEntorno.setUnidadMedica( jo.getInt("id_asu_um") );
			
			
			//TABLAS GENERALES DEL SISTEMA
			ContentResolver cr = contexto.getContentResolver();
			ContentValues[] filas; //parámetros en filas para inserciones múltiples
			
			//TABLA GRUPOS
			JSONArray grupos = jo.getJSONArray("grupo");
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
			JSONArray usuarios = jo.getJSONArray("usuario");
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
			JSONArray permisos = jo.getJSONArray("permiso");
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
			JSONArray notificaciones = jo.getJSONArray("notificacion");
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
			
			//TABLA TIPO SANGUINEO
			JSONArray tiposSangre = jo.getJSONArray("tipo_sanguineo");
			filas=new ContentValues[tiposSangre.length()];
			for(int i=0;i<tiposSangre.length();i++){
				JSONObject sangre=tiposSangre.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(TipoSanguineo.ID, sangre.getInt(TipoSanguineo._REMOTO_ID));
				filas[i].put(TipoSanguineo.DESCRIPCION, sangre.getString(TipoSanguineo.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.TIPO_SANGUINEO_CONTENT_URI, filas);
			
			//TABLA VACUNAS
			JSONArray vacunas = jo.getJSONArray("vacunas");
			filas=new ContentValues[vacunas.length()];
			for(int i=0;i<vacunas.length();i++){
				JSONObject vacuna=vacunas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Vacuna.ID, vacuna.getInt(Vacuna._REMOTO_ID));
				filas[i].put(Vacuna.DESCRIPCION, vacuna.getString(Vacuna.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.VACUNA_CONTENT_URI, filas);
			
			//TABLA ACCIONES NUTRICIONALES
			JSONArray accionesNutricionales = jo.getJSONArray("accion_nutricional");
			filas=new ContentValues[accionesNutricionales.length()];
			for(int i=0;i<accionesNutricionales.length();i++){
				JSONObject accion=accionesNutricionales.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(AccionNutricional.ID, accion.getInt(AccionNutricional._REMOTO_ID));
				filas[i].put(AccionNutricional.DESCRIPCION, accion.getString(AccionNutricional.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.ACCION_NUTRICIONAL_CONTENT_URI, filas);
			
			//TABLA IRAS
			JSONArray iras = jo.getJSONArray("ira");
			filas=new ContentValues[iras.length()];
			for(int i=0;i<iras.length();i++){
				JSONObject ira=iras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Ira.ID, ira.getInt(Ira._REMOTO_ID));
				filas[i].put(Ira.ID_CIE10, ira.getInt(Ira.ID_CIE10));
				filas[i].put(Ira.DESCRIPCION, ira.getString(Ira.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.IRA_CONTENT_URI, filas);
			
			//TABLA EDAS
			JSONArray edas = jo.getJSONArray("eda");
			filas=new ContentValues[edas.length()];
			for(int i=0;i<edas.length();i++){
				JSONObject eda=iras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Eda.ID, eda.getInt(Eda._REMOTO_ID));
				filas[i].put(Eda.ID_CIE10, eda.getInt(Eda.ID_CIE10));
				filas[i].put(Eda.DESCRIPCION, eda.getString(Eda.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.EDA_CONTENT_URI, filas);
			
			//TABLA CONSULTAS
			JSONArray consultas = jo.getJSONArray("consulta");
			filas=new ContentValues[consultas.length()];
			for(int i=0;i<consultas.length();i++){
				JSONObject consulta=consultas.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Consulta.ID, consulta.getInt(Consulta._REMOTO_ID));
				filas[i].put(Consulta.ID_CIE10, consulta.getInt(Consulta.ID_CIE10));
				filas[i].put(Consulta.DESCRIPCION, consulta.getString(Consulta.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.CONSULTA_CONTENT_URI, filas);
			
			//TABLA ALERGIAS
			JSONArray alergias = jo.getJSONArray("alergia");
			filas=new ContentValues[alergias.length()];
			for(int i=0;i<alergias.length();i++){
				JSONObject alergia=alergias.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Alergia.ID, alergia.getInt(Alergia._REMOTO_ID));
				filas[i].put(Alergia.TIPO, alergia.getString(Alergia.TIPO));
				filas[i].put(Alergia.DESCRIPCION, alergia.getString(Alergia.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.ALERGIA_CONTENT_URI, filas);
			
			//TABLA AFILIACIONES
			JSONArray afiliaciones = jo.getJSONArray("afiliacion");
			filas=new ContentValues[afiliaciones.length()];
			for(int i=0;i<afiliaciones.length();i++){
				JSONObject afiliacion=afiliaciones.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Afiliacion.ID, afiliacion.getInt(Afiliacion._REMOTO_ID));
				filas[i].put(Afiliacion.DESCRIPCION, afiliacion.getString(Afiliacion.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.AFILIACION_CONTENT_URI, filas);
			
			//TABLA NACIONALIDADES
			JSONArray nacionalidades = jo.getJSONArray("nacionalidad");
			filas=new ContentValues[nacionalidades.length()];
			for(int i=0;i<nacionalidades.length();i++){
				JSONObject nacionalidad=nacionalidades.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(Nacionalidad.ID, nacionalidad.getInt(Nacionalidad._REMOTO_ID));
				filas[i].put(Nacionalidad.DESCRIPCION, nacionalidad.getString(Nacionalidad.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.NACIONALIDAD_CONTENT_URI, filas);
			
			//TABLA OPERADORAS CELULARES
			JSONArray operadoras = jo.getJSONArray("operadora_celular");
			filas=new ContentValues[operadoras.length()];
			for(int i=0;i<operadoras.length();i++){
				JSONObject operadora=operadoras.getJSONObject(i);
				filas[i]=new ContentValues();
				filas[i].put(OperadoraCelular.ID, operadora.getInt(OperadoraCelular._REMOTO_ID));
				filas[i].put(OperadoraCelular.DESCRIPCION, operadora.getString(OperadoraCelular.DESCRIPCION));
			}
			cr.bulkInsert(ProveedorContenido.OPERADORA_CELULAR_CONTENT_URI, filas);
			
			//TABLA PENDIENTES TARJETA
			JSONArray pendientes = jo.getJSONArray("tes_pendientes_tarjeta");
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
			
			//TABLA ARBOL SEGMENTACION
			JSONArray arboles = jo.getJSONArray("asu_arbol_segmentacion");
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
			
			
		} catch (JSONException e) {
			Log.e(TAG, "Error :"+json+" "+e.toString());
		}
	}
	
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
	
	
	
}//fin clase