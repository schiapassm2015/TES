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

import android.app.Activity;
import android.app.ProgressDialog;
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
	
	//Variable NO final para que pueda ser cambiada por fuera
	public static String URL_SINCRONIZACION = "http://192.168.1.27:8080/siigs/index.php/tes/servicios/";
	
	private final static String PARAMETRO_PASO ="paso";
	private final static String VALOR_PASO_PRIMERAVEZ = "1";
	private final static int HTTP_STATUS_OK = 200;
	
	private static byte[] sBuffer = new byte[1024]; 
	
	
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
	 */
	private static synchronized String RequestGet(String url){
		String salida="";
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
			while( (bytesLeidos=ist.read(sBuffer)) != -1 )
				contenido.write(sBuffer, 0, bytesLeidos);
			
			salida= new String( contenido.toByteArray());
			
			Log.d(TAG, "En GET se descargó: "+salida);
		}catch(Exception ex){
			Log.d(TAG, "Error en request tipo GET: "+ ex.toString());
		}
		return salida;
	}
	
	/**
	 * Función helper para mandar request tipo GET y regresar la respuesta del servidor.
	 * @param url Dirección a conectar
	 * @param parametros Valores a incluir en request
	 * @return String con resultado del request
	 */
	private static synchronized String RequestPost(String url, List<NameValuePair> parametros){
		String salida="";
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
			while( (bytesLeidos=ist.read(sBuffer)) != -1 )
				contenido.write(sBuffer, 0, bytesLeidos);
			
			salida= new String( contenido.toByteArray());
			
			Log.d(TAG, "En POST se descargó: "+salida);
		}catch(Exception ex){
			Log.d(TAG, "Error en request tipo POST: "+ ex.toString());
		}
		return salida;
	}
	
	private final static String ACCION_ENVIAR_MAC = "1";
	protected synchronized void SincronizacionTotal(){
		
		//identificar contexto de tableta:
		//es nueva??
		//última sincronización no completó??
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>(2);
        parametros.add(new BasicNameValuePair("param", "12345"));
        parametros.add(new BasicNameValuePair("stringdata", "Este texto tiene"));
        //RequestPost(URL_SINCRONIZACION + "receive", parametros);
        GenerarAccionX();
		//String resultado = RequestGet(URL_SINCRONIZACION+ACCION_ENVIAR_MAC+"/123456789");
		
		//JSONObject ojb=new JSONObject();ojb.
		//JsonWriter jWriter = new JsonWriter()
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
			
			WifiManager wifi=(WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
			Log.d(TAG, "mac es:"+wifi.getConnectionInfo().getMacAddress());

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
