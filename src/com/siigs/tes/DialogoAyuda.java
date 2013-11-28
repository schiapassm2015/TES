/**
 * Esta es una clase gen�rica para desplegar un cuadro de di�logo simple
 * que carga din�micamente el layout indicado a trav�s de de argumentos
 * de tipo Bundle.
 * Por convenci�n, los layouts usados tienen la nomenclatura: ayuda_XXX.xml
 */

package com.siigs.tes;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class DialogoAyuda extends DialogFragment {


	public static final String TAG=DialogoAyuda.class.toString();
	//Nombre del argumento/par�metro le�do en getArguments()
	//que indica el layout a visualizar
	public static final String ARG_LAYOUT="layout";

	/**
	 * Crea un nuevo di�logo de ayuda con el fragment manager y layout
	 * especificados.
	 * @param fm
	 * @param layout
	 */
	public static void CrearNuevo(FragmentManager fm, int layout){
		DialogoAyuda dialogo=new DialogoAyuda();
		Bundle args=new Bundle();
		args.putInt(DialogoAyuda.ARG_LAYOUT, layout);
		dialogo.setArguments(args);
		dialogo.show(fm, DialogoAyuda.TAG);
	}
	
	//Constructor requerido
	public DialogoAyuda(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Di�logo sin t�tulo
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Visualizamos el layout indicado en los argumentos
		View view=inflater.inflate(getArguments().getInt(ARG_LAYOUT), 
				container,false);
		
		//Bot�n cerrar
		Button btnCerrar=(Button)view.findViewById(R.id.btnCerrar);
		btnCerrar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		return view;
	}
	

}//fin clase