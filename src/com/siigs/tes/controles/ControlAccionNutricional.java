/**
 * 
 */
package com.siigs.tes.controles;


import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.AccionNutricional;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ListaSimple;
import com.siigs.tes.ui.ObjectViewBinder;
import com.siigs.tes.ui.WidgetUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class ControlAccionNutricional extends Fragment {

	private static final String TAG = ControlAccionNutricional.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private ListaSimple lsAccionNutricional = null;
		
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlAccionNutricional() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_atencion_control_accion_nutricional, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEG�N PERMISOS
		LinearLayout verAcciones = (LinearLayout)rootView.findViewById(R.id.accion_ver_acciones);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLACCIONNUTRICIONAL_VER))
			verAcciones.setVisibility(View.VISIBLE); else verAcciones.setVisibility(View.GONE);
		
		LinearLayout agregarAccion = (LinearLayout)rootView.findViewById(R.id.accion_agregar_accion);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLACCIONNUTRICIONAL_INSERTAR))
			agregarAccion.setVisibility(View.VISIBLE); else agregarAccion.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_acciones, "Ver Acciones Nutricionales", 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());

		
		//VER CONTROL
		lsAccionNutricional = (ListaSimple)rootView.findViewById(R.id.lsAccionNutricional); 
		GenerarAcciones();
		
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_accion, R.string.agregar_accion, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		Button btnAgregarAccion = (Button)rootView.findViewById(R.id.btnAgregarAccion);
		btnAgregarAccion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Di�logo de nueva acci�n
				ControlAccionNutricionalNuevo dialogo=new ControlAccionNutricionalNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlAccionNutricional.this, ControlAccionNutricionalNuevo.REQUEST_CODE);
				dialogo.show(ControlAccionNutricional.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlAccionNutricionalNuevo.TAG);
				//Este di�logo avisar� su fin en onActivityResult() de llamador
			}
		});
		
		return rootView;
	}
	
	private void GenerarAcciones(){
		AdaptadorArrayMultiTextView<com.siigs.tes.datos.tablas.ControlAccionNutricional> adaptador = 
				new AdaptadorArrayMultiTextView<com.siigs.tes.datos.tablas.ControlAccionNutricional>(
						
				getActivity(), R.layout.fila_comun_para_ira_eda_accion_consulta,
				sesion.getDatosPacienteActual().accionesNutricionales, 
				new String[]{com.siigs.tes.datos.tablas.ControlAccionNutricional.FECHA, 
						com.siigs.tes.datos.tablas.ControlAccionNutricional.ID_ASU_UM, 
						com.siigs.tes.datos.tablas.ControlAccionNutricional.ID_ACCION_NUTRICIONAL},
				new int[]{R.id.txtFecha, R.id.txtUM, R.id.txtDetalle});
		adaptador.setViewBinder(binderFila);
		lsAccionNutricional.setAdaptador(adaptador);
	}

	private ObjectViewBinder<com.siigs.tes.datos.tablas.ControlAccionNutricional> binderFila = 
			new ObjectViewBinder<com.siigs.tes.datos.tablas.ControlAccionNutricional>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, 
				com.siigs.tes.datos.tablas.ControlAccionNutricional origen,
				String atributoOrigen, Object valor) {
			
			TextView destino = (TextView)viewDestino;
			if(atributoOrigen.equals(com.siigs.tes.datos.tablas.ControlAccionNutricional.FECHA)){
				destino.setText(DatosUtil.fechaHoraCorta(valor.toString()));
				return true;
			}else if(atributoOrigen.equals(com.siigs.tes.datos.tablas.ControlAccionNutricional.ID_ASU_UM)){
				destino.setText(ArbolSegmentacion.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				return true;
			}else if(atributoOrigen.equals(com.siigs.tes.datos.tablas.ControlAccionNutricional.ID_ACCION_NUTRICIONAL)){
				destino.setText(AccionNutricional.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				View v = destino.getRootView().findViewById(R.id.txtClave);
				if(v!=null)v.setVisibility(View.GONE); //No se maneja clave en acciones nutricionales
				return true;
			}
			return false;
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlAccionNutricionalNuevo.REQUEST_CODE:
			//if(resultCode==ControlAccionNutricionalNuevo.RESULT_OK){
				GenerarAcciones();
			//}
			break;
		}
	}

	
	
	
}//fin clase
