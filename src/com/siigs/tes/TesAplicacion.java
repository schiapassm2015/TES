package com.siigs.tes;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CalendarView;

/**
 * Clase tipo Application invocada automáticamente al cargar la app.
 * Implementa funciones básicas que son acceseibles desde varios módulos.
 * @author Axel
 *
 */
public class TesAplicacion extends Application {

	private final static String TAG = "TesAplicacion";
	
	SharedPreferences preferencias;
	//Preferencias...
	private final static String URL_SINCRONIZACION = "url_sincronizacion";
	private final static String REINTENTOS_CONEXION = "reintentos_conexion";
	private final static String TIEMPO_ESPERA_REINTENTO = "tiempo_espera_reintento";
	private final static String TIPO_CENSO = "tipo_censo";
	private final static String UNIDAD_MEDICA = "unidad_medica";
	private final static String ES_INSTALACION_NUEVA = "es_instalacion_nueva";
	private final static String FECHA_ULTIMA_SINCRONIZACION = "fecha_ultima_sincronizacion";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//Cargamos preferencias para usarlas
		preferencias= PreferenceManager.getDefaultSharedPreferences(this);
//		CargarPreferencias();
//		preferencias.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
//			@Override
//			public void onSharedPreferenceChanged(SharedPreferences sp, String arg1) {
//				preferencias = sp;
//				CargarPreferencias();
//			}
//		});
	}
	
	/**
	 * Indica si hay conectividad en Wifi, aunque no garantiza que haya internet.
	 * @return
	 */
	public boolean hayInternet() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

	//FUNCIONES DE MANEJO DE PREFERENCIAS
	
	/**
	 * Preferencia disponible también para el usuario administrador
	 * @return
	 */
	public String getUrlSincronizacion(){
		return preferencias.getString(URL_SINCRONIZACION, "http://www.siigs.gob.mx");
	}
	public void setUrlSincronizacion(String url){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(URL_SINCRONIZACION, url);
		editor.apply();
	}
	
	public int getReintentosConexion(){
		return preferencias.getInt(REINTENTOS_CONEXION, 3);
	}
	public void setReintentosConexion(int cantidad){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(REINTENTOS_CONEXION, cantidad);
		editor.apply();
	}
	
	public int getTiempoEsperaReintento(){
		return preferencias.getInt(TIEMPO_ESPERA_REINTENTO, 2000);
	}
	public void setTiempoEsperaReintento(int milisegundos){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(TIEMPO_ESPERA_REINTENTO, milisegundos);
		editor.apply();
	}
	
	public int getTipoCenso(){
		return preferencias.getInt(TIPO_CENSO, -1);
	}
	public void setTipoCenso(int tipo){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(TIPO_CENSO, tipo);
		editor.apply();
	}
	
	public int getUnidadMedica(){
		return preferencias.getInt(UNIDAD_MEDICA, -1);
	}
	public void setUnidadMedica(int um){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(UNIDAD_MEDICA, um);
		editor.apply();
	}
	
	public boolean getEsInstalacionNueva(){
		return preferencias.getBoolean(ES_INSTALACION_NUEVA, true);
	}
	public void setEsInstalacionNueva(boolean valor){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putBoolean(ES_INSTALACION_NUEVA, valor);
		editor.apply();
	}
	
	public String getFechaUltimaSincronizacion(){
		String fecha = preferencias.getString(FECHA_ULTIMA_SINCRONIZACION, "2000-01-01 00:00:00");
		return fecha;
/*		try {
			return DateFormat.getDateTimeInstance().parse(fecha);
		} catch (ParseException e) {
			Log.e(TAG, "No se pudo parsear fecha:"+fecha);
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.set(2000, Calendar.JANUARY, 1);
			return cal.getTime();
		}*/
	}
	public void setFechaUltimaSincronizacion(){
		//setFechaUltimaSincronizacion(new Date(System.currentTimeMillis()));
		setFechaUltimaSincronizacion(Calendar.getInstance());
	}
	public void setFechaUltimaSincronizacion(Calendar cal){
		SharedPreferences.Editor editor = preferencias.edit();
		//editor.putString(FECHA_ULTIMA_SINCRONIZACION, valor.toString());
		String salida= cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+
				"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+
				":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
		editor.putString(FECHA_ULTIMA_SINCRONIZACION, salida);
		editor.apply();
	}
	

	
}