package com.siigs.tes;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.datos.BaseDatos;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.SincronizacionTask;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Grupo;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Usuario;

/**
 * A list fragment representing a list of {@link ItemControl}. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link SeccionDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class SeccionListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	//Guarda la lista de controles enlistados
	private List<ContenidoControles.ItemControl> miListaControles;
	
	private TesAplicacion aplicacion;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public SeccionListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.aplicacion = (TesAplicacion)this.getActivity().getApplication();
//BaseDatos base =new BaseDatos(this.getActivity());
//base.getReadableDatabase();
/*getActivity().getContentResolver().query(ProveedorContenido.USUARIO_CONTENT_URI, new String[]{Usuario.ID,Usuario.NOMBRE}, null, null, null);
ContentValues valores=new ContentValues();valores.put(Usuario.ID, 5);valores.put(Usuario.NOMBRE_USUARIO, "tu123");
valores.put(Usuario.NOMBRE, "fulanito");valores.put(Usuario.CLAVE, "asdfasf");valores.put(Usuario.APELLIDO_PATERNO, "appat");
valores.put(Usuario.APELLIDO_MATERNO, "apmat");valores.put(Usuario.CORREO, "agc@google.com");
valores.put(Usuario.ACTIVO, 1);valores.put(Usuario.ID_GRUPO, 2);
getActivity().getContentResolver().insert(ProveedorContenido.USUARIO_CONTENT_URI, valores);*/
		
		GenerarDatosFalsos();
		
String uuid= UUID.randomUUID().toString().replace("-", "");
byte[] hex = hexStringToByteArray(uuid);
String dec = ByteArrayTohexString(hex);
Log.i("Lista", "uuid:"+uuid+" en bytes:"+hex.length+ " decodificado:"+dec+" son iguales?"+uuid.equalsIgnoreCase(dec) );

		//Lista vacía
		LlenarLista(new java.util.ArrayList<ContenidoControles.ItemControl>());
	}
	
	public static byte[] hexStringToByteArray(String cadena) {
	    int len = cadena.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	    	int hex1 = Character.digit(cadena.charAt(i), 16) << 4;
	    	int hex2 = Character.digit(cadena.charAt(i+1), 16);
	        data[i / 2] = (byte) (hex1+hex2);
	        //Log.d("a hex", "hex1="+hex1+", hex2="+hex2);
	    }
	    return data;
	}
	
	public static String ByteArrayTohexString(byte[] cadena) {
		int largo= cadena.length*2;
		char[] salida = new char[largo];

	    for (int i = 0; i < cadena.length; i++) {
	    	int hex1= cadena[i]>>4, hex2= (cadena[i]- ((cadena[i]>>4)<<4) );
	    	hex1= hex1<0? 16+hex1 : hex1;
	    	//char izquierdo = Integer.toHexString(hex1).charAt(0);// Character.forDigit(cadena[i]>>4, 16);
	    	//char derecho = Integer.toHexString(hex2).charAt(0); // Character.forDigit( cadena[i]-((cadena[i]>>4)<<4), 16);
	    	//Log.d("cod", "Decodificado hex1="+hex1+", hex2="+hex2);//new String(new char[]{izquierdo}) + "," + new String(new char[]{derecho}) );
	    	salida[i*2] = Integer.toHexString(hex1).charAt(0);
	    	salida[(i*2)+1] = Integer.toHexString(hex2).charAt(0);
	    }
	    return new String(salida);
	}
	
	private void GenerarDatosFalsos(){
		ContentResolver cr = this.getActivity().getContentResolver();
		ContentValues valores = new ContentValues();
		String where="";
		String[] args = null;
		
		//PERSONA
		where = Persona._ID + "=1";
		//valores.put(Persona.NOMBRE, "Nuevo nombre");String s=null;
		//valores.put(Persona.ID_OPERADORA_CELULAR, s);
		valores.put(Persona.CP_DOMICILIO, 29001);
		valores.put(Persona.ULTIMA_ACTUALIZACION, DatosUtil.getAhora());
		//valores.put(Persona.REFERENCIA_DOMICILIO, "Aplicación, apostrofe (')");
		int afectados=cr.update(ProveedorContenido.PERSONA_CONTENT_URI, valores, where, args);
		afectados++;
		
		valores.clear();
		valores.put(ControlVacuna.ID_PERSONA, "49a6cddb690074c1f8b5019ecaea25e7");
		valores.put(ControlVacuna.FECHA, DatosUtil.getAhora());
		valores.put(ControlVacuna.ID_VACUNA, 2);
		valores.put(ControlVacuna.ID_ASU_UM, aplicacion.getUnidadMedica());
		cr.insert(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, valores);
	}
	
	
	/**
	 * Actualiza la lista de opciones para el usuario.
	 * @param lista
	 * @return false (no se llena/actualiza la lista) debido a que:
	 * parámetro <strong>lista</strong> ya estaba visualizada Y
	 * no da controles para paciente. De esta forma en un cambio de
	 * paciente si se actualizará la lista aunque esté visualizada.
	 * Regresa true en caso contrario.
	 */
	public boolean LlenarLista(List<ContenidoControles.ItemControl> lista){
		if(miListaControles==lista 
				&& lista!=ContenidoControles.CONTROLES_ATENCION)return false;
		
		miListaControles=lista;
		setListAdapter(new ArrayAdapter<ContenidoControles.ItemControl>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, miListaControles));
		if(lista.size()>0){
			setActivatedPosition(0);
			AvisarItemSeleccionado(0);
		}
		return true;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		AvisarItemSeleccionado(position);
	}
	
	/**
	 * 
	 * @param index posición en la lista del item a avisar.
	 */
	private void AvisarItemSeleccionado(int index){
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(miListaControles.get(index).id);		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
