/**
 * Muestra un diálogo modal para agregar una nueva ira al paciente
 */
package com.siigs.tes.controles;

import java.util.List;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.Ira;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.WidgetUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class ControlIrasNuevo extends DialogFragment {

	public static final String TAG= ControlIrasNuevo.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;	

	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	//Constructor requerido
	public ControlIrasNuevo(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		setCancelable(false);
		
		//this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título (pues setCancelable() deja título que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		dialogo.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista=inflater.inflate(R.layout.controles_atencion_control_iras_nuevo, container,false);			
		
		final Persona p = sesion.getDatosPacienteActual().persona;
		
		WidgetUtil.setBarraTitulo(vista, R.id.barra_titulo_ira, R.string.agregar_ira, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		((TextView)vista.findViewById(R.id.txtNombre)).setText(p.getNombreCompleto());
		
		//Lista de iras
		final Spinner spIras = (Spinner)vista.findViewById(R.id.spIras);
		List<Ira> iras = Ira.getIrasActivas(getActivity());
		AdaptadorArrayMultiTextView<Ira> adaptador = new AdaptadorArrayMultiTextView<Ira>(
				getActivity(), android.R.layout.simple_dropdown_item_1line, iras, 
				new String[]{Ira.DESCRIPCION}, new int[]{android.R.id.text1});
		spIras.setAdapter(adaptador);
		
		//Cancelación manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(ControlIrasNuevo.RESULT_CANCELAR);
			}
		});
		
		//Agregar
		Button btnAgregar = (Button) vista.findViewById(R.id.btnAgregar);
		btnAgregar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(spIras.getSelectedItem() == null)return;
				
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea aplicar este control?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Guardamos cambios en memoria
						ControlIra ira = new ControlIra();
						ira.id_persona = p.id;
						ira.id_ira = ((Ira)spIras.getSelectedItem())._id;
						/*ira.id_invitado = sesion.getUsuarioInvitado() != null ? 
								sesion.getUsuarioInvitado()._id : null;*/
						ira.id_asu_um = aplicacion.getUnidadMedica();
						ira.fecha = DatosUtil.getAhora();
						sesion.getDatosPacienteActual().iras.add(ira);
						//En bd
						int ICA = ContenidoControles.ICA_IRA_INSERTAR;
						try {
							ControlIra.AgregarNuevoControlIra(getActivity(), ira);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id+", ira:"+ira.id_ira);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
						}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = ControlIra.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(ira);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(ControlIrasNuevo.this,
								DialogoTes.ModoOperacion.GUARDAR, pendiente);
						//onActivityResult recibe respuesta del diálogo
					}
				} );
				dialogo.show();
				
			}
		});
	
		return vista;
	}
	
	/**
	 * Cierra este diálogo y notifica a fragmento padre los datos.
	 * @param resultado Código de resultado oficial de la ejecución de este diálogo
	 */
	private void Cerrar(int resultado){
		Intent datos=null;//new Intent();
		//resultado.putExtra("dato", "valor");
		getTargetFragment().onActivityResult(getTargetRequestCode(), 
				resultado, datos);
		dismiss();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case DialogoTes.REQUEST_CODE:
			if(resultCode == DialogoTes.RESULT_CANCELAR)Cerrar(ControlIrasNuevo.RESULT_CANCELAR);
			else if(resultCode == DialogoTes.RESULT_OK)Cerrar(ControlIrasNuevo.RESULT_OK);
			break;
		}
	}
	
}//fin clase
