package com.siigs.tes;

import java.util.Calendar;
import java.util.List;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Clase tipo Application invocada autom�ticamente al cargar la app.
 * Implementa funciones b�sicas que son acceseibles desde varios m�dulos.
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
	private final static String REQUIERE_ACTUALIZAR_VERSION_APK = "requiere_actualizar_version_apk";
	private final static String URL_ACTUALIZACION_APK = "url_actualizacion_apk";

	private Sesion sesion = null; //La sesi�n de uso
	
	private ProgressDialog pdProgreso = null; //Contenedor para di�logos de progreso
	
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
	 * Preferencia disponible tambi�n para el usuario administrador
	 * @return
	 */
	public String getUrlSincronizacion(){
		return preferencias.getString(URL_SINCRONIZACION, "http://www.sm2015.com.mx/tes/servicios/prueba");
	}
	public void setUrlSincronizacion(String url){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(URL_SINCRONIZACION, url);
		editor.apply();
		Log.d(TAG, "Cambiado url sincronizaci�n a:"+url);
	}
	
	public int getReintentosConexion(){
		return preferencias.getInt(REINTENTOS_CONEXION, 3);
	}
	public void setReintentosConexion(int cantidad){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(REINTENTOS_CONEXION, cantidad);
		editor.apply();
		Log.d(TAG, "Cambiado reintentos conexi�n a:"+cantidad);
	}
	
	public int getTiempoEsperaReintento(){
		return preferencias.getInt(TIEMPO_ESPERA_REINTENTO, 2000);
	}
	public void setTiempoEsperaReintento(int milisegundos){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(TIEMPO_ESPERA_REINTENTO, milisegundos);
		editor.apply();
		Log.d(TAG, "Cambiado tiempo espera reintento a:"+milisegundos);
	}
	
	public int getTipoCenso(){
		return preferencias.getInt(TIPO_CENSO, -1);
	}
	public void setTipoCenso(int tipo){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(TIPO_CENSO, tipo);
		editor.apply();
		Log.d(TAG, "Cambiado tipo censo a:"+tipo);
	}
	
	public int getUnidadMedica(){
		return preferencias.getInt(UNIDAD_MEDICA, -1);
	}
	public void setUnidadMedica(int um){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(UNIDAD_MEDICA, um);
		editor.apply();
		Log.d(TAG, "Cambiada unidad m�dica a:"+um);
	}
	
	public boolean getEsInstalacionNueva(){
		return preferencias.getBoolean(ES_INSTALACION_NUEVA, true);
	}
	public void setEsInstalacionNueva(boolean valor){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putBoolean(ES_INSTALACION_NUEVA, valor);
		editor.apply();
		Log.d(TAG, "Cambiado instalaci�n nueva a:"+valor);
	}
	
	public boolean getRequiereActualizarApk(){
		return preferencias.getBoolean(REQUIERE_ACTUALIZAR_VERSION_APK, false);
	}
	public void setRequiereActualizarApk(boolean valor){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putBoolean(REQUIERE_ACTUALIZAR_VERSION_APK, valor);
		editor.apply();
		Log.i(TAG, "Cambiado requiere actualizar APK a:"+valor);
	}
	
	public String getUrlActualizacionApk(){
		return preferencias.getString(URL_ACTUALIZACION_APK, "http://www.sm2015.com.mx/tes/servicios/prueba");
	}
	public void setUrlActualizacionApk(String url){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(URL_ACTUALIZACION_APK, url);
		editor.apply();
		Log.d(TAG, "Cambiado url actualizaci�n APK a:"+url);
	}
	
	/**
	 * Valida si se requiere actualizar el apk y en tal caso muestra el mensaje apropiado
	 * usando Builder dialogo.
	 * Inicia 2 Intent, el primero para mandar un mensaje a la actividad principal pidi�ndole
	 * cerrar la sesi�n de usuario. Dicha actividad valida este mensaje usando getIntent()
	 * 
	 * El segundo Intent inicia el navegador para ir al URL de actualizaci�n.
	 * @param dialogo
	 */
	public void ValidarRequiereActualizarApk(AlertDialog dialogo){
		if(getRequiereActualizarApk()){
			dialogo.setMessage("Esta aplicaci�n requiere actualizarse. " +
					"Puede presionar 'Actualizar' para ser enviado a la p�gina de actualizaci�n");
			dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.actualizar), new DialogInterface.OnClickListener() {
			//dialogo.setPositiveButton(R.string.actualizar, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = getBaseContext().getPackageManager()
				             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra(PrincipalActivity.FORZAR_CIERRE_SESION_USUARIO, true);
					startActivity(i);
					
					// Env�a a la p�gina de actualizaci�n
					i = new Intent(Intent.ACTION_VIEW);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setData(Uri.parse(getUrlActualizacionApk()));
					startActivity(i);
				}
			});
		}
		
		dialogo.show();
	}
	
	public String getVersionApk(){
		String version="";
		try {
			version += getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e1) {
			version="imposible determinar version";
		}
		return version;
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
		String salida= DatosUtil.getAhora();
		editor.putString(FECHA_ULTIMA_SINCRONIZACION, salida);
		editor.apply();
		Log.d(TAG, "Cambiada �ltima sincronizaci�n a:"+salida);
	}
	
	/**
	 * Genera una nueva sesi�n para usuario normal
	 * @param idUsuario
	 */
	public void IniciarSesion(int idUsuario){
		Usuario usuario = Usuario.getUsuarioConId(this, idUsuario);
		Cursor cur = Permiso.getPermisosGrupo(this, usuario.id_grupo);
		List<Permiso> permisos = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		this.sesion = new Sesion(usuario, null, permisos); //Asigna nueva sesi�n
		
		//Actualiza las listas usadas para crear los submen�s izquierdos y men� superior
		ContenidoControles.RecargarControles(permisos);
	}
	/**
	 * Genera una nueva nueva sesi�n para usuario invitado 
	 * @param idInvitado
	 */
	public void IniciarSesionInvitado(int idInvitado){
		UsuarioInvitado invitado = UsuarioInvitado.getUsuarioInvitadoConId(this, idInvitado);
		Usuario usuario = Usuario.getUsuarioConId(this, invitado.id_usuario_creador);
		Cursor cur = Permiso.getPermisosGrupo(this, UsuarioInvitado.ID_GRUPO);
		List<Permiso> permisos = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		this.sesion = new Sesion(usuario, invitado, permisos);
		
		//Actualiza las listas usadas para crear los submen�s izquierdos y men� superior
		ContenidoControles.RecargarControles(permisos);
	}
	/**
	 * Cierra la sesi�n del usuario actual
	 */
	public void CerrarSesion(){
		//Registrar status relevantes
		this.sesion = null;
	}
	/**
	 * Indica si existe una sesi�n de usuario
	 * @return
	 */
	public boolean haySesion(){return this.sesion != null;}
	/**
	 * Devuelve la sesi�n de usuario actual
	 * @return
	 */
	public Sesion getSesion(){return this.sesion;}
	
	
	public void onPausa(Activity llamador){
		if(pdProgreso !=null)
			pdProgreso.dismiss();
	}
	public void onResumir(Activity llamador){
		if(pdProgreso!=null){ // && sinctask running
			destruirDialogoProgreso();
			crearDialogoProgreso(llamador);
		}
	}
	
	public void crearDialogoProgreso(Activity llamador){
		String mensaje= "El dispositivo se est� sincronizando en "+getUrlSincronizacion()
				+"\nPor favor espere.";
		if(getEsInstalacionNueva())
			mensaje="("+getUrlSincronizacion()+")"+"\nEsta acci�n puede tardar 30 minutos. Por favor espere.";
		boolean indeterminado=true, cancelable=false;
		this.pdProgreso = ProgressDialog.show(llamador, "Sincronizando", 
				mensaje, indeterminado, cancelable);
	}
	public void destruirDialogoProgreso(){this.pdProgreso = null;}
	public ProgressDialog getDialogoProgreso(){return pdProgreso;}

	
}