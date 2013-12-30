/**
 * NOTA DESARROLLO: Si es posible, procurar que esta clase controle
 * no solo el layout de menú superior, sino el de menú principal.
 * En tal caso, hacer refactor y cambiar el nombre MenuSuperior a uno
 * más genérico como MenuPrincipal
 */
package com.siigs.tes;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.controles.ContenidoControles.ItemControl;


/**
 * Esta clase despliega un menú de opciones que se visualizan según los
 * privilegios del usuario.
 * La actividad que contiene este fragmento debe llamar al método
 * setOnSeleccionarMenuListener() para recibir aviso cuando el usuario
 * escoge una opción.
 * @author Axel
 * 
 */
public class MenuSuperior extends Fragment{
	
	/**
	 * Interface para avisar a la actividad contenedora de este fragmento
	 * que el usuario ha escogido una opción del menú.
	 * @author Axel
	 *
	 */
	public interface OnAccionMenuListener{
		public void onSeleccionarMenu(List<ItemControl> lista);
		public void onClickCerrarSesionUsuario();
		public void onIniciarSesionUsuario(int idUsuario, boolean esInvitado);
	}//fin OnAccionMenuListener
	private OnAccionMenuListener miListenerVacio=new OnAccionMenuListener() {		
		@Override public void onSeleccionarMenu(List<ItemControl> lista) {}
		@Override public void onClickCerrarSesionUsuario(){}
		@Override public void onIniciarSesionUsuario(int idUsuario, boolean esInvitado){}
	};
	private OnAccionMenuListener miListener=miListenerVacio;
	//Setter para que usuario de esta clase asigne un listener y escuche lo que aquí se hace
	public void setOnAccionMenuListener(OnAccionMenuListener lis){
		this.miListener=lis;
	}
	
	private View mnAtencion=null;
	private View mnCenso=null;
	private View mnNotificaciones=null;
	private View mnEsquemas=null;
	private View mnSincronizacion=null;
	private View mnConfiguracion=null;
	private View mnInvitados=null;
	
	private TextView lblTitulo=null;
	
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

		mnAtencion = rootView.findViewById(R.id.mnAtencion);
		mnAtencion.setOnClickListener(listener);
		
		mnCenso = rootView.findViewById(R.id.mnCenso);
		mnCenso.setOnClickListener(listener);
		
		mnNotificaciones = rootView.findViewById(R.id.mnNotificaciones);
		mnNotificaciones.setOnClickListener(listener);
		
		mnEsquemas= rootView.findViewById(R.id.mnEsquemas);
		mnEsquemas.setOnClickListener(listener);
		
		mnSincronizacion = rootView.findViewById(R.id.mnSincronizacion);
		mnSincronizacion.setOnClickListener(listener);
		
		mnConfiguracion = rootView.findViewById(R.id.mnConfiguracion);
		mnConfiguracion.setOnClickListener(listener);
		
		mnInvitados= rootView.findViewById(R.id.mnInvitados);
		mnInvitados.setOnClickListener(listener);
		
		lblTitulo = (TextView)rootView.findViewById(R.id.lblTitulo);
		if(savedInstanceState!=null && savedInstanceState.containsKey(lblTitulo.getId()+""))
			lblTitulo.setText(savedInstanceState.getCharSequence(lblTitulo.getId()+""));
		
		rootView.findViewById(R.id.mnCerrar).setOnClickListener(listener);
	
		//ESCONDER BOTONES SEGÚN PERMISOS DE ACCESO
		ActualizarBotonesMenu();
		return rootView;
	}
	
	/**
	 * Actualiza la visibilidad de los botones de menú
	 * Útil al haber inicios de sesión para no mostrar opciones que no debe ver el usuario
	 */
	public void ActualizarBotonesMenu(){
		int visibilidad;
		
		visibilidad = ContenidoControles.CONTROLES_ATENCION.size()>0? View.VISIBLE : View.GONE;
		mnAtencion.setVisibility(visibilidad);
		visibilidad = ContenidoControles.CONTROLES_CENSO.size()>0? View.VISIBLE : View.GONE;
		mnCenso.setVisibility(visibilidad);
		visibilidad = ContenidoControles.CONTROLES_ESQUEMAS.size()>0? View.VISIBLE : View.GONE;
		mnEsquemas.setVisibility(visibilidad);
		visibilidad = ContenidoControles.CONTROLES_NOTIFICACIONES.size()>0? View.VISIBLE : View.GONE;
		mnNotificaciones.setVisibility(visibilidad);
		visibilidad = ContenidoControles.CONTROLES_SINCRONIZACION.size()>0? View.VISIBLE : View.GONE;
		mnSincronizacion.setVisibility(visibilidad);
		visibilidad = ContenidoControles.CONTROLES_CONFIGURACION.size()>0? View.VISIBLE : View.GONE;
		mnConfiguracion.setVisibility(visibilidad);
		visibilidad = ContenidoControles.CONTROLES_INVITADOS.size()>0? View.VISIBLE : View.GONE;
		mnInvitados.setVisibility(visibilidad);
	}
	
	private void OpcionMenuClick(View view){
		switch(view.getId()){
		case R.id.mnAtencion:
			//Por default pedimos una TES al usuario en un diálogo modal
			DialogoTes.IniciarNuevo(this, DialogoTes.ModoOperacion.LOGIN);
			//Este diálogo avisará su fin en onActivityResult()
			break;
		case R.id.mnCenso:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_CENSO);
			break;
		case R.id.mnNotificaciones:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_NOTIFICACIONES);
			break;
		case R.id.mnEsquemas:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_ESQUEMAS);
			break;
		case R.id.mnSincronizacion:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_SINCRONIZACION);
			break;
		case R.id.mnConfiguracion:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_CONFIGURACION);
			break;
		case R.id.mnInvitados:
			miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_INVITADOS);
			break;
		case R.id.mnCerrar:
			miListener.onClickCerrarSesionUsuario();
			break;
		default:
			//
		}
		//Toast.makeText(getActivity(), "Click boton "+view.getId(),Toast.LENGTH_SHORT).show();
	}

	/**
	 * Inicia ventana para que el usuario se identifique
	 */
	public void PedirLoginUsuario(){
		//Por default pedimos una TES al usuario en un diálogo modal
		Login login=new Login();
		login.setTargetFragment(this, Login.REQUEST_CODE);
		login.show(getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
				Login.TAG);
		//Este diálogo avisará su fin en onActivityResult()	
	}
	
	public void setTitulo(String titulo){
		lblTitulo.setText(titulo);
	}
	
	@Override
	/**
	 * Recibe notificación de status de una ventana de diálogo
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case Login.REQUEST_CODE:
			int idUsuario = data.getIntExtra(Login.PARAM_ID_ESCOGIDO, -1);
			miListener.onIniciarSesionUsuario(idUsuario, resultCode == Login.RESULT_USUARIO_INVITADO);
			ActualizarBotonesMenu(); //Quizás cambiaron permisos, así que actualiza
			break;
			
		case DialogoTes.REQUEST_CODE:			
			if(resultCode==DialogoTes.RESULT_OK){
				//procedimiento tes CORRECTA
				miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_ATENCION);
			}else if(resultCode==DialogoTes.RESULT_CANCELAR){/*NADA*/}
			break;
		}//fin switch
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		/** Esta técnica usada por {@link PrincipalFragment} no se usa
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

		/** Esta técnica usada por {@link PrincipalFragment} no se usa
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
		*/
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(lblTitulo!=null)
			outState.putCharSequence(lblTitulo.getId()+"", lblTitulo.getText());
		
		/** Esta técnica usada por {@link PrincipalFragment} no se usa
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
		*/
	}
	
}
