/**
 * Muestra un di�logo modal que intenta escuchar la presencia de una TES
 * y realiza las acciones necesarias seg�n el caso
 */
package com.siigs.tes;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * @author Axel
 *
 */
public class DialogoTesLogin extends DialogFragment {

	public static final String TAG=DialogoTesLogin.class.toString();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;
	
	//Constructor requerido
	public DialogoTesLogin(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Est�lo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//M�todo 2 para hacer la ventana modal 
		setCancelable(false);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Di�logo sin t�tulo (pues setCancelable() deja t�tulo que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View view=inflater.inflate(R.layout.dialogo_tes_login, 
				container,false);
		
		//Cancelaci�n manual
		Button btnCancelar=(Button)view.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(DialogoTesLogin.RESULT_CANCELAR);
			}
		});
		
		//Bot�n ayuda
		ImageButton btnAyuda=(ImageButton)view.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), R.layout.ayuda_dialogo_tes_login);
			}
		});
		
		return view;
	}
	
	/**
	 * Cierra este di�logo y notifica a fragmento padre los datos.
	 * @param c�digo de resultado oficial de la ejecuci�n de este di�logo
	 */
	private void Cerrar(int resultado){
		Intent datos=null;//new Intent();
		//resultado.putExtra("dato", "valor");
		getTargetFragment().onActivityResult(getTargetRequestCode(), 
				resultado, datos);
		dismiss();
	}

}//fin clase
