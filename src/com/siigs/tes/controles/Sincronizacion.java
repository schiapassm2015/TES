package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
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
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)rootView.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), 
						R.layout.ayuda_controles_sincronizacion);	
			}
		});
		
		//Botón sincronizar
		Button btnSincronizar=(Button)rootView.findViewById(R.id.btnSincronizar);
		btnSincronizar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// PENDIENTE VERIFICAR SI HAY CONDICIONES PARA SINCRONIZAR
				
				//Confirmación
				AlertDialog.Builder dialogo=new AlertDialog.Builder(getActivity());
				dialogo.setMessage("¿En verdad desea sincronizar?")
				.setNegativeButton("Cancelar", null)
				.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SincronizacionTask sinc = new SincronizacionTask(getActivity());
						sinc.execute("lalala");
					}
				} )
				.create().show();
			}
		});
		
		return rootView;
	}
		
	
}//fin clase
