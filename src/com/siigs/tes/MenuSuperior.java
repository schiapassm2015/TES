/**
 * NOTA DESARROLLO: Si es posible, procurar que esta clase controle
 * no solo el layout de men� superior, sino el de men� principal.
 * En tal caso, hacer refactor y cambiar el nombre MenuSuperior a uno
 * m�s gen�rico como MenuPrincipal
 */
package com.siigs.tes;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.controles.ContenidoControles.ItemControl;


/**
 * Esta clase despliega un men� de opciones que se visualizan seg�n los
 * privilegios del usuario.
 * La actividad que contiene este fragmento debe llamar al m�todo
 * setOnSeleccionarMenuListener() para recibir aviso cuando el usuario
 * escoge una opci�n.
 * @author Axel
 * 
 */
public class MenuSuperior extends Fragment{
	
	/**
	 * Interface para avisar a la actividad contenedora de este fragmento
	 * que el usuario ha escogido una opci�n del men�.
	 * @author Axel
	 *
	 */
	public interface OnSeleccionarMenuListener{
		public void onSeleccionarMenu(List<ItemControl> lista);
	}//fin OnSeleccionarMenuListener
	private OnSeleccionarMenuListener miListenerVacio=new OnSeleccionarMenuListener() {		
		@Override
		public void onSeleccionarMenu(List<ItemControl> lista) {}
	};
	private OnSeleccionarMenuListener miListener=miListenerVacio;
	public void setOnSeleccionarMenuListener(OnSeleccionarMenuListener lis){
		this.miListener=lis;
	}
	
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MenuSuperior() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.menu_superior,
				container, false);

		//Definir clicks para botones
		View.OnClickListener listener= new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				OpcionMenuClick(view);
			}
		};

		rootView.findViewById(R.id.mnAtencion).setOnClickListener(listener);
		rootView.findViewById(R.id.mnCenso).setOnClickListener(listener);
		rootView.findViewById(R.id.mnNotificaciones).setOnClickListener(listener);
		rootView.findViewById(R.id.mnSincronizacion).setOnClickListener(listener);
		rootView.findViewById(R.id.mnConfiguracion).setOnClickListener(listener);
		rootView.findViewById(R.id.mnCerrar).setOnClickListener(listener);
	
		//ESCONDER BOTONES SEG�N PERMISOS DE ACCESO

		return rootView;
	}
	
	private void OpcionMenuClick(View view){
		switch(view.getId()){
		case R.id.mnAtencion:
			//Por default pedimos una TES al usuario en un di�logo modal
			DialogoTesLogin dialogo=new DialogoTesLogin();
			dialogo.setTargetFragment(this, DialogoTesLogin.REQUEST_CODE);
			dialogo.show(getFragmentManager(),
					//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
					DialogoTesLogin.TAG);
			//Este di�logo avisar� su fin en onActivityResult()
			break;
		case R.id.mnCenso:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_CENSO);
			break;
		case R.id.mnNotificaciones:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_NOTIFICACIONES);
			break;
		case R.id.mnSincronizacion:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_SINCRONIZACION);
			break;
		case R.id.mnConfiguracion:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_CONFIGURACION);
			break;
		case R.id.mnCerrar:
			Toast.makeText(getActivity(), "Click en Cerrar",Toast.LENGTH_SHORT).show();
			break;
		default:
			//
		}
		//Toast.makeText(getActivity(), "Click boton "+view.getId(),Toast.LENGTH_SHORT).show();
	}

	
	@Override
	/**
	 * Recibe notificaci�n de status de una ventana de di�logo
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case DialogoTesLogin.REQUEST_CODE:			
			if(resultCode==DialogoTesLogin.RESULT_OK){
				//procedimiento login CORRECTO
				miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_ATENCION);
			}else if(resultCode==DialogoTesLogin.RESULT_CANCELAR){/*NADA*/}
			//String dato=data.getStringExtra("dato");
			//Toast.makeText(getActivity(), "Tes recibido req:"+requestCode+" res:"+resultCode, Toast.LENGTH_SHORT).show();
			break;
		}//fin switch
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		/** Esta t�cnica usada por {@link SeccionListFragment} no se usa
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		mCallbacks = (Callbacks) activity;
		*/
	}

	@Override
	public void onDetach() {
		super.onDetach();

		/** Esta t�cnica usada por {@link SeccionListFragment} no se usa
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
		*/
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/** Esta t�cnica usada por {@link SeccionListFragment} no se usa
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
		*/
	}
	
}
