package com.siigs.tes;

import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.UsuarioInvitado;
import com.siigs.tes.ui.AdaptadorArrayMultiView;
import com.siigs.tes.datos.tablas.Grupo;
import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Usuario;

/**
 * A list fragment representing a list of {@link ItemControl}. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ControlFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PrincipalFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String ESTADO_ACTIVATED_POSITION = "activated_position";
	private static final String ESTADO_ESCONDIDO = "escondido";
	
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
	
	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;



	//Guarda la lista de controles enlistados
	private List<ContenidoControles.ItemControl> miListaControles;
	
	private TesAplicacion aplicacion;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PrincipalFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Retendr� el estado del fragmento de forma que onCreate no ser� llamado
		//nuevamente si {@link PrincipalActivity} es creado de nuevo
		this.setRetainInstance(true);
		
		this.aplicacion = (TesAplicacion)this.getActivity().getApplication();
		
		//Lista vac�a
		LlenarLista(new java.util.ArrayList<ContenidoControles.ItemControl>());
		
		GenerarDatosFalsos(); //TODO eliminar esto
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
		String uuid= UUID.randomUUID().toString().replace("-", "");
		byte[] hex = hexStringToByteArray(uuid);
		String dec = ByteArrayTohexString(hex);
		Log.i("Lista", "uuid:"+uuid+" en bytes:"+hex.length+ " decodificado:"+dec+" son iguales?"+uuid.equalsIgnoreCase(dec) );
		
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
		//valores.put(Persona.REFERENCIA_DOMICILIO, "Aplicaci�n, apostrofe (')");
		int afectados=cr.update(ProveedorContenido.PERSONA_CONTENT_URI, valores, where, args);
		afectados++;
		
		try {
			UsuarioInvitado invitado = new UsuarioInvitado();
			invitado._id=1;invitado.nombre="unico";invitado.id_usuario_creador=4;
			invitado.fecha_creacion=aplicacion.getFechaUltimaSincronizacion();
			invitado.activo=1;
			ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(invitado);
			//cv.remove(UsuarioInvitado.ID_INVITADO);
			cr.insert(ProveedorContenido.USUARIO_INVITADO_CONTENT_URI, cv  );
			/*//TODO Borrar estas creaciones
			aplicacion.setEsInstalacionNueva(false);
			Usuario usuario = new Usuario();
			usuario._id=4; usuario.nombre="usuario";usuario.nombre_usuario="loginusuario";usuario.id_grupo=4;
			usuario.activo=1;usuario.apellido_materno="";usuario.apellido_paterno="";usuario.clave="";usuario.correo="";
			cv = DatosUtil.ContentValuesDesdeObjeto(usuario);
			cr.insert(ProveedorContenido.USUARIO_CONTENT_URI, cv);
			
			Grupo grupo = new Grupo();grupo._id=4;grupo.descripcion="fabricado"; grupo.nombre="fabricado";
			cv = DatosUtil.ContentValuesDesdeObjeto(grupo);
			cr.insert(ProveedorContenido.GRUPO_CONTENT_URI, cv);
			
			Permiso permiso = new Permiso();permiso._id=214;permiso.id_grupo=4;permiso.id_controlador_accion=96;permiso.fecha=DatosUtil.getAhora();
			cv = DatosUtil.ContentValuesDesdeObjeto(permiso);
			cr.insert(ProveedorContenido.PERMISO_CONTENT_URI, cv);
			*/
			
			Cursor res = cr.query(ProveedorContenido.PERSONA_CONTENT_URI, null, null, null, null);
			while(res.moveToNext()){
				Persona p =DatosUtil.ObjetoDesdeCursor( res, Persona.class);
				p.apellido_materno="";
			}
			res.close();
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Actualiza la lista de opciones para el usuario.
	 * @param lista
	 * @return false (no se llena/actualiza la lista) debido a que:
	 * par�metro <strong>lista</strong> ya estaba visualizada Y
	 * no da controles para paciente. De esta forma en un cambio de
	 * paciente si se actualizar� la lista aunque est� visualizada.
	 * Regresa true en caso contrario.
	 */
	public boolean LlenarLista(List<ContenidoControles.ItemControl> lista){
		if(miListaControles==lista 
				&& lista!=ContenidoControles.CONTROLES_ATENCION)return false;
		
		miListaControles=lista;
		//Reglas de mapeo entre atributos del item de men� y el layout del item
		AdaptadorArrayMultiView.Mapeo[] reglasMapeo = new AdaptadorArrayMultiView.Mapeo[]{
			new AdaptadorArrayMultiView.Mapeo("titulo", android.R.id.text1, "setText", CharSequence.class),
			new AdaptadorArrayMultiView.Mapeo("resIdIcono", R.id.imgIcono, "setBackgroundResource", int.class)
		};
		AdaptadorArrayMultiView<ContenidoControles.ItemControl> adaptador = 
				new AdaptadorArrayMultiView<ContenidoControles.ItemControl>(getActivity(), 
						R.layout.fila_principalfragment, miListaControles, reglasMapeo);
		
		setListAdapter(adaptador);

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
		if (savedInstanceState != null){
			if(savedInstanceState.containsKey(ESTADO_ACTIVATED_POSITION))
				setActivatedPosition(savedInstanceState.getInt(ESTADO_ACTIVATED_POSITION));
			
			if(savedInstanceState.containsKey(ESTADO_ESCONDIDO)){
				if(savedInstanceState.getBoolean(ESTADO_ESCONDIDO))
					getFragmentManager().beginTransaction().show(this).commit();
				else
					getFragmentManager().beginTransaction().hide(this).commit();
			}
		}
	}
	

	@Override
	/**
	 * Llamado al incrustar este fragmento en actividad contenedora durante su construcci�n
	 */
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
	 * @param index posici�n en la lista del item a avisar.
	 */
	private void AvisarItemSeleccionado(int index){
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(miListaControles.get(index).id);		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ESTADO_ESCONDIDO, this.isHidden());
		
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(ESTADO_ACTIVATED_POSITION, mActivatedPosition);
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
