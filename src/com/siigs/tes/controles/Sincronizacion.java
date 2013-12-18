package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.SincronizacionTask;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
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
				// PENDIENTE VERIFICAR SI HAY CONDICIONES PARA SINCRONIZAR
				
				//Confirmaci�n
				String mensaje ="�En verdad desea sincronizar?";
				if(((TesAplicacion)getActivity().getApplication()).getEsInstalacionNueva())
					mensaje="Est� a punto de sincronizar por primera vez. " +
							"Esta acci�n puede tardar m�s de 30 minutos y requiere de " +
							"conexi�n a Internet y suficiente bater�a\n\n�Desea continuar?";
				AlertDialog.Builder dialogo=new AlertDialog.Builder(getActivity());
				dialogo.setMessage(mensaje)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog.Builder dlgResultado=new AlertDialog.Builder(getActivity());
						dlgResultado.create();
						
						SincronizacionTask sinc = new SincronizacionTask(getActivity(), dlgResultado);
						sinc.execute("");
					}
				} )
				.create().show();
			}
		});
		
		return rootView;
	}
		
	
}//fin clase
