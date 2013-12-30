package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.SincronizacionTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class Sincronizacion extends Fragment {
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Sincronizacion(){}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_sincronizacion, container, false);
		//Bot�n ayuda
		ImageButton btnAyuda=(ImageButton)rootView.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), 
						R.layout.ayuda_controles_sincronizacion);	
			}
		});
		
		//Bot�n sincronizar
		Button btnSincronizar=(Button)rootView.findViewById(R.id.btnSincronizar);
		btnSincronizar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!((TesAplicacion)getActivity().getApplication()).hayInternet() ){
					String msg="No existe conexi�n a la red en este momento";
					Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
					return;
				}
				
				//Confirmaci�n
				String mensaje ="�En verdad desea sincronizar?";
				if(((TesAplicacion)getActivity().getApplication()).getEsInstalacionNueva())
					mensaje="Est� a punto de sincronizar por primera vez. " +
							"Esta acci�n puede tardar m�s de 30 minutos y requiere de " +
							"conexi�n a Internet y suficiente bater�a\n\n�Desea continuar?";
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage(mensaje);
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Un dialogo vac�o que la sincronizaci�n puede usar para mostrar resultados
						AlertDialog dlgResultado=new AlertDialog.Builder(getActivity()).create();
						dlgResultado.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface arg0) {
								ActualizarInformacion();
							}
						});

						SincronizacionTask sinc = new SincronizacionTask(getActivity(), dlgResultado);
						sinc.execute("");
					}
				} );
				dialogo.show();
			}
		});
		
		ActualizarInformacion();
		
		return rootView;
	}
	
	/**
	 * Actualiza widgets con la informaci�n actual
	 */
	private void ActualizarInformacion(){
		//TODO crear variables privadas de cada widget actualizable en clase y poner actualizarlos aqu�
	}
		
	
}//fin clase
