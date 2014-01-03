/**
 * Muestra un diálogo modal que intenta escuchar la presencia de una TES
 * y realiza las acciones necesarias según el caso
 */
package com.siigs.tes;

import com.siigs.tes.datos.ManejadorNfc;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * @author Axel
 *
 */
public class DialogoTes extends DialogFragment {

	public static final String TAG=DialogoTes.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;
	private static final String PARAM_MODO_OPERACION= "modo_operacion";
	
	public static void IniciarNuevo(Fragment llamador, ModoOperacion modoOperacion){
		DialogoTes dialogo=new DialogoTes();
		Bundle args = new Bundle();
		args.putSerializable(DialogoTes.PARAM_MODO_OPERACION, modoOperacion);
		dialogo.setArguments(args);
		dialogo.setTargetFragment(llamador, DialogoTes.REQUEST_CODE);
		dialogo.show(llamador.getFragmentManager(),
				//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
				DialogoTes.TAG);
		//Este diálogo avisará su fin en onActivityResult() de llamador
	}
	
	//Modos de operación de este diálogo
	public static enum ModoOperacion {LOGIN, GUARDAR}
	private ModoOperacion modoOperacion;

	//NFC
	private NfcAdapter adaptadorNFC;
	private PendingIntent pendingIntentNFC;
	private IntentFilter writeTagFiltersNFC[];
	private Tag tagNFC;
	private boolean modoEscrituraNFC;
	
	/**
	 * Usado para comunicarse con actividad contenedora la cual DEBE implementar
	 * esta interface
	 * @author Axel
	 *
	 */
	public interface Callbacks {
		/**
		 * Usado para avisar que esta instancia de {@link DialogoTes} existe y que
		 * requiere ser avisada cuando actividad contenedora detecte un tag NFC
		 * @param llamador {@link DialogoTes} que solicita recibir tags NFC
		 */
		public void onIniciarDialogoTes(DialogoTes llamador);
		/**
		 * Usado apra avisar a actividad contenedora que ya no es necesario
		 * dar avisos de tags NFC
		 * @param llamador {@link DialogoTes} que hace la llamada
		 */
		public void onDetenerDialogoTes(DialogoTes llamador);
	}
	
	private Callbacks miCallback = null;
	
	//Constructor requerido
	public DialogoTes(){}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		miCallback = (Callbacks)activity;
		miCallback.onIniciarDialogoTes(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if(miCallback!=null)
			miCallback.onDetenerDialogoTes(this);
		miCallback = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		setCancelable(false);
		
		//INICIA MODO ESCUCHA REDIRIGIENDO A ACTIVIDAD PADRE EN CASO DE ENCONTRAR ALGO
		adaptadorNFC = NfcAdapter.getDefaultAdapter(getActivity());
		pendingIntentNFC = PendingIntent.getActivity(getActivity(), 0, 
				new Intent(getActivity(), getActivity().getClass())
					.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetectada = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetectada.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFiltersNFC = new IntentFilter[] { tagDetectada };
		
		this.modoOperacion = (ModoOperacion) getArguments().getSerializable(PARAM_MODO_OPERACION);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título (pues setCancelable() deja título que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista;
		
		//TODO si no cambian mucho los layout, fusionarlos en uno solo pues ahora son casi iguales
		if(modoOperacion == ModoOperacion.LOGIN){
			vista=inflater.inflate(R.layout.dialogo_tes_login, container,false);			
		}else{
			vista=inflater.inflate(R.layout.dialogo_tes_guardar, container,false);			
		}
		
		//Cancelación manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(DialogoTes.RESULT_CANCELAR);
			}
		});
		
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)vista.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int layout = modoOperacion == ModoOperacion.LOGIN //TODO CREAR LAYOUT ayuda_dialogo_tes_guardar
						? R.layout.ayuda_dialogo_tes_login : R.layout.ayuda_dialogo_tes_login;
				DialogoAyuda.CrearNuevo(getFragmentManager(), layout);
			}
		});
		
		return vista;
	}
	
	/**
	 * Llamado cuando el contenedor de este fragmento ha detectado un nuevo tag NFC
	 * @param tag El tag recibido por el dispositivo NFC
	 */
	public void onTagNfcDetectado(Tag tag){
		try {
			if(this.modoOperacion == ModoOperacion.LOGIN){
				ManejadorNfc.LeerDatosNFC(tag, getActivity());
				Cerrar(DialogoTes.RESULT_OK);
			}else if(this.modoOperacion == ModoOperacion.GUARDAR){
				Sesion.DatosPaciente datosPaciente = 
						((TesAplicacion)getActivity().getApplication()).getSesion().getDatosPacienteActual();
				if(!ManejadorNfc.nfcTagPerteneceApersona(datosPaciente.persona.id, tag)){
					Toast.makeText(getActivity(), "La TES presentada no pertenece al paciente "
							+ datosPaciente.persona.nombre + datosPaciente.persona.apellido_paterno
							+ datosPaciente.persona.apellido_materno, Toast.LENGTH_LONG).show();
					return;
				}
				ManejadorNfc.EscribirDatosNFC(tag, datosPaciente);
				Cerrar(DialogoTes.RESULT_OK);
			}
		} catch (Exception e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Cierra este diálogo y notifica a fragmento padre los datos.
	 * @param código de resultado oficial de la ejecución de este diálogo
	 */
	private void Cerrar(int resultado){
		Intent datos=null;//new Intent();
		//resultado.putExtra("dato", "valor");
		getTargetFragment().onActivityResult(getTargetRequestCode(), 
				resultado, datos);
		dismiss();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		ModoEscrituraNfcInactivo();
	}

	@Override
	public void onResume(){
		super.onResume();
		ModoEscrituraNfcActivo();
	}

	private void ModoEscrituraNfcActivo(){
		modoEscrituraNFC = true;
		adaptadorNFC.enableForegroundDispatch(getActivity(), pendingIntentNFC, writeTagFiltersNFC, null);
	}

	private void ModoEscrituraNfcInactivo(){
		modoEscrituraNFC = false;
		adaptadorNFC.disableForegroundDispatch(getActivity());
	}
}//fin clase
