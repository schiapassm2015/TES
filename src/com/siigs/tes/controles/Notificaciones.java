package com.siigs.tes.controles;

import com.siigs.tes.R;
import com.siigs.tes.ui.WidgetUtil;

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
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver, R.string.notificaciones, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		
		return rootView;
	}
		
	
}//fin clase
