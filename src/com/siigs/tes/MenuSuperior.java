/**
 * NOTA DESARROLLO: Si es posible, procurar que esta clase controle
 * no solo el layout de men� superior, sino el de men� principal.
 * En tal caso, hacer refactor y cambiar el nombre MenuSuperior a uno
 * m�s gen�rico como MenuPrincipal
 */
package com.siigs.tes;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.siigs.tes.controles.CensoCensoNominal;
import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.controles.ContenidoControles.ItemControl;
import com.siigs.tes.datos.tablas.Persona;


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
	public interface OnAccionMenuListener{
		public void onSeleccionarMenu(List<ItemControl> lista);
		public void onAtenderPacienteSinTes(int _id);
		public void onClickCerrarSesionUsuario();
		public void onIniciarSesionUsuario(int idUsuario, boolean esInvitado);
	}//fin OnAccionMenuListener
	private OnAccionMenuListener miListenerVacio=new OnAccionMenuListener() {		
		@Override public void onSeleccionarMenu(List<ItemControl> lista) {}
		@Override public void onAtenderPacienteSinTes(int _id){}
		@Override public void onClickCerrarSesionUsuario(){}
		@Override public void onIniciarSesionUsuario(int idUsuario, boolean esInvitado){}
	};
	private OnAccionMenuListener miListener=miListenerVacio;
	//Setter para que usuario de esta clase asigne un listener y escuche lo que aqu� se hace
	public void setOnAccionMenuListener(OnAccionMenuListener lis){
		this.miListener=lis;
	}
	
	private static final String BOTON_ACTIVADO = "boton_menu_activado";
	
	private View mnAtencion=null;
	private View mnCenso=null;
	private View mnNotificaciones=null;
	private View mnEsquemas=null;
	private View mnSincronizacion=null;
	private View mnConfiguracion=null;
	private View mnInvitados=null;
	
	private TextView lblTitulo=null;
	private View ultimoActivado = null;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MenuSuperior() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setRetainInstance(true);
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
		
		rootView.findViewById(R.id.mnCerrar).setOnClickListener(listener);

		lblTitulo = (TextView)rootView.findViewById(R.id.lblTitulo);
		if(savedInstanceState!=null){
			if(savedInstanceState.containsKey(R.id.lblTitulo+""))
				lblTitulo.setText(savedInstanceState.getCharSequence(lblTitulo.getId()+""));
			
			if(savedInstanceState.containsKey(BOTON_ACTIVADO))
				rootView.findViewById(savedInstanceState.getInt(BOTON_ACTIVADO)).setActivated(true);
		}
		
		//ESCONDER BOTONES SEG�N PERMISOS DE ACCESO
		ActualizarBotonesMenu();
		return rootView;
	}
	
	/**
	 * Actualiza la visibilidad de los botones de men�
	 * �til al haber inicios de sesi�n para no mostrar opciones que no debe ver el usuario
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
			//Por default pedimos una TES al usuario en un di�logo modal
			DialogoTes.IniciarNuevo(this, DialogoTes.ModoOperacion.LOGIN);
			//Este di�logo avisar� su fin en onActivityResult()
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
			//Confirmaci�n
			AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
			dialogo.setMessage("�Desea cerrar sesi�n?");
			dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface arg0, int arg1) {}
			});
			dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					miListener.onClickCerrarSesionUsuario();
				}
			} );
			dialogo.show();
			break;
		default:
			//
		}
		
		if(view.getId()!=R.id.mnCerrar && view.getId()!=R.id.mnAtencion){
			if(ultimoActivado!=null)ultimoActivado.setActivated(false);
			view.setActivated(true);
			ultimoActivado=view;
		}
		
		//Toast.makeText(getActivity(), "Click boton "+view.getId(),Toast.LENGTH_SHORT).show();
	}

	/**
	 * Inicia ventana para que el usuario se identifique
	 */
	public void PedirLoginUsuario(){
		//Por default pedimos una TES al usuario en un di�logo modal
		Login login=new Login();
		login.setTargetFragment(this, Login.REQUEST_CODE);
		login.show(getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
				Login.TAG);
		//Este di�logo avisar� su fin en onActivityResult()	
	}
	
	public void setTitulo(String titulo){
		lblTitulo.setText(titulo);
	}
	
	@Override
	/**
	 * Recibe notificaci�n de status de una ventana de di�logo
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case Login.REQUEST_CODE:
			int idUsuario = data.getIntExtra(Login.PARAM_ID_ESCOGIDO, -1);
			miListener.onIniciarSesionUsuario(idUsuario, resultCode == Login.RESULT_USUARIO_INVITADO);
			ActualizarBotonesMenu(); //Quiz�s cambiaron permisos, as� que actualiza
			break;
			
		case DialogoTes.REQUEST_CODE:			
			if(resultCode==DialogoTes.RESULT_OK){
				//procedimiento tes CORRECTA
				miListener.onSeleccionarMenu(ContenidoControles.CONTROLES_ATENCION);
				if(ultimoActivado!=null)ultimoActivado.setActivated(false);
				mnAtencion.setActivated(true);
				ultimoActivado = mnAtencion;
			}else if(resultCode==DialogoTes.RESULT_CANCELAR){/*NADA*/}
			break;
			
			//Este case es especial, pues es PrincipalActivity desde onItemSelected() quien crea
			//la pantalla de Censo y le indica que al terminar debe avisar su resultado aqu�
		case CensoCensoNominal.REQUEST_CODE:
			if(resultCode==CensoCensoNominal.RESULT_ATENDER_PACIENTE_SIN_TES){
				int idPersona = data.getIntExtra(CensoCensoNominal.PARAM_ID_PERSONA, -1);
				if(idPersona == -1){
					//NUNCA DEBER�A SUCEDER
					Toast.makeText(getActivity(), "Se recibi� un _id -1 de persona que no es v�lido desde ventana Censo", Toast.LENGTH_LONG).show();
					return;
				}
				if(ultimoActivado!=null)ultimoActivado.setActivated(false);
				mnAtencion.setActivated(true);
				ultimoActivado = mnAtencion;
				miListener.onAtenderPacienteSinTes(idPersona);
			}
			break;
		}//fin switch
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		/** Esta t�cnica usada por {@link PrincipalFragment} no se usa
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

		/** Esta t�cnica usada por {@link PrincipalFragment} no se usa
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
		*/
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("salvado","salvando estado");
		if(lblTitulo!=null)
			outState.putCharSequence(R.id.lblTitulo+"", lblTitulo.getText());
		
		if(ultimoActivado!=null)
			outState.putInt(BOTON_ACTIVADO, ultimoActivado.getId());
		/** Esta t�cnica usada por {@link PrincipalFragment} no se usa
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
		*/
	}
	
}
