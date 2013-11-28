package com.siigs.tes.controles;

import com.siigs.tes.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class Notificaciones extends Fragment {
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Notificaciones(){}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_notificaciones, container, false);
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)rootView.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(getActivity())
				.setMessage("Ayuda solo texto").create().show();	
			}
		});
		
		return rootView;
	}
		
	
}//fin clase
