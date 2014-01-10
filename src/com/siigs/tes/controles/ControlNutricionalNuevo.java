/**
 * Muestra un di�logo modal para agregar una nueva vacuna al paciente
 */
package com.siigs.tes.controles;

import java.util.List;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Vacuna;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class ControlNutricionalNuevo extends DialogFragment {

	public static final String TAG=ControlNutricionalNuevo.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;	


	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	//Constructor requerido
	public ControlNutricionalNuevo(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Est�lo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//M�todo 2 para hacer la ventana modal 
		setCancelable(false);
		
		this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Di�logo sin t�tulo (pues setCancelable() deja t�tulo que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		dialogo.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista=inflater.inflate(R.layout.controles_atencion_control_nutricional_nuevo, container,false);			
		
		final Persona p = sesion.getDatosPacienteActual().persona;
		
		TextView titulo = (TextView)vista.findViewById(R.id.barra_titulo_nutricion).findViewById(R.id.txtTituloBarra);
		titulo.setText(R.string.agregar_nutricion);
		
		((TextView)vista.findViewById(R.id.txtNombre)).setText(p.getNombreCompleto());
		
		//Cancelaci�n manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(ControlNutricionalNuevo.RESULT_CANCELAR);
			}
		});
		
		final EditText txtEstatura = (EditText)vista.findViewById(R.id.txtEstatura);
		final EditText txtPeso = (EditText)vista.findViewById(R.id.txtPeso);
		
		//Agregar
		Button btnAgregar = (Button) vista.findViewById(R.id.btnAgregar);
		btnAgregar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Validar datos
				final double peso; //TODO Double peso = bueno or toast, return
				final int estatura; //TODO int estatura = bueno or toast, return
				final int talla; //TODO int talla = bueno or toast, return
				
				//Confirmaci�n
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("�En verdad desea registrar este control?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Guardamos cambios en memoria
						ControlNutricional controlNutricional = new ControlNutricional();
						controlNutricional.altura = estatura;
						controlNutricional.peso = peso;
						controlNutricional.talla = talla;
						controlNutricional.id_persona = p.id;
						controlNutricional.id_invitado = sesion.getUsuarioInvitado() != null ? 
								sesion.getUsuarioInvitado()._id : null;
						controlNutricional.id_asu_um = aplicacion.getUnidadMedica();
						controlNutricional.fecha = DatosUtil.getAhora();

						sesion.getDatosPacienteActual().controlesNutricionales.add(controlNutricional);
						//En bd
						int ICA = ContenidoControles.ICA_CONTROLNUTRICIONAL_INSERTAR;
						try {
							ControlNutricional.AgregarNuevoControlNutricional(getActivity(), controlNutricional);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id+", altura:"+estatura+", peso:"+peso+", talla:"+talla);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
						}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = ControlNutricional.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(controlNutricional);
						// Por default pedimos una TES al usuario en un di�logo modal
						DialogoTes.IniciarNuevo(ControlNutricionalNuevo.this,
								DialogoTes.ModoOperacion.GUARDAR, pendiente);
						//onActivityResult recibe respuesta del di�logo
					}
				} );
				dialogo.show();
				
			}
		});
	
		return vista;
	}
	
	/**
	 * Cierra este di�logo y notifica a fragmento padre los datos.
	 * @param resultado C�digo de resultado oficial de la ejecuci�n de este di�logo
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
			if(resultCode == DialogoTes.RESULT_CANCELAR)Cerrar(ControlNutricionalNuevo.RESULT_CANCELAR);
			else if(resultCode == DialogoTes.RESULT_OK)Cerrar(ControlNutricionalNuevo.RESULT_OK);
			break;
		}
	}
	
}//fin clase
