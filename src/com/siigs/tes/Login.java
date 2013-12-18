/**
 * Muestra un diálogo modal que intenta escuchar la presencia de una TES
 * y realiza las acciones necesarias según el caso
 */
package com.siigs.tes;

import java.util.List;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.ui.AdaptadorArrayGenerico;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class Login extends DialogFragment {

	public static final String TAG=Login.class.toString();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;
	
	private Usuario usuarioElegido; //El usuario escogido en spinner
	
	//Constructor requerido
	public Login(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		setCancelable(false);
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
		final View view=inflater.inflate(R.layout.login, container,false);
		
		//Lista de usuarios activos
		Cursor cur = Usuario.getUsuariosActivos(getActivity());
		List<Usuario> usuarios = DatosUtil.ObjetosDesdeCursor(cur, Usuario.class);
		cur.close();
		/*SimpleCursorAdapter adaptador = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_spinner_item, cur, 
				new String[]{Usuario.NOMBRE_USUARIO}, new int[]{android.R.id.text1},0);*/
		final AdaptadorArrayGenerico<Usuario> adaptador = 
				new AdaptadorArrayGenerico<Usuario>(getActivity(), android.R.layout.simple_spinner_item, 
						usuarios, new String[]{Usuario.NOMBRE_USUARIO}, new int[]{android.R.id.text1});
		adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spUsuario = (Spinner)view.findViewById(R.id.spUsuario);
		spUsuario.setAdapter(adaptador);
		//Manejo de click
		spUsuario.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view,
					int position, long id) {
				usuarioElegido = (Usuario) adapterView.getItemAtPosition(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		//Acceder normal
		Button btnAcceder=(Button)view.findViewById(R.id.btnAcceder);
		btnAcceder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String clave = ((TextView)view.findViewById(R.id.txtPassword)).getText().toString();
				Log.d(TAG, "comparando clave usuario "+ usuarioElegido.clave + " con "+ clave+" iguales?"+(usuarioElegido.clave.equals(clave)));
			}
		});
		
		//Acceder invitado
		Button btnInvitado=(Button)view.findViewById(R.id.btnInvitado);
		btnInvitado.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Cerrar(Login.RESULT_CANCELAR);
			}
		});
		
		
		//Botón ayuda
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

}//fin clase
