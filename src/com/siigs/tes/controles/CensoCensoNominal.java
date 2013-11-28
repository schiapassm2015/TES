/**
 * 
 */
package com.siigs.tes.controles;

import com.siigs.tes.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * @author Axel
 * Por seguir nomenclatura "GrupoSubgrupo" el nombre de esta clase
 * se compone de Grupo:Censo, Control:CensoNominal
 */
public class CensoCensoNominal extends Fragment {
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CensoCensoNominal() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_censo_censo_nominal, container, false);

		//Sexo
		Spinner spSexo=(Spinner)rootView.findViewById(R.id.spSexo);
		ArrayAdapter<CharSequence> adaptadorSexo=
				ArrayAdapter.createFromResource(getActivity(), 
						R.array.opciones_sexo, android.R.layout.simple_spinner_item);
		adaptadorSexo.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spSexo.setAdapter(adaptadorSexo);
		
		//Edad
		Spinner spEdad=(Spinner)rootView.findViewById(R.id.spEdad);
		ArrayAdapter<CharSequence> adaptadorEdad=
				ArrayAdapter.createFromResource(getActivity(), 
						R.array.opciones_edad, android.R.layout.simple_spinner_item);
		adaptadorEdad.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spEdad.setAdapter(adaptadorEdad);
		
		return rootView;
	}
	
}//fin clase
