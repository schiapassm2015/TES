/**
 * 
 */
package com.siigs.tes.controles;


import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.Eda;
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
public class ControlEdas extends Fragment {

	private static final String TAG = ControlEdas.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private ListaSimple lsEdas = null;
		
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlEdas() {
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
				R.layout.controles_atencion_control_edas, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verEdas = (LinearLayout)rootView.findViewById(R.id.accion_ver_edas);
		if(sesion.tienePermiso(ContenidoControles.ICA_EDA_VER))
			verEdas.setVisibility(View.VISIBLE); else verEdas.setVisibility(View.GONE);
		
		LinearLayout agregarEda = (LinearLayout)rootView.findViewById(R.id.accion_agregar_eda);
		if(sesion.tienePermiso(ContenidoControles.ICA_EDA_INSERTAR))
			agregarEda.setVisibility(View.VISIBLE); else agregarEda.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_edas, "Ver Control de EDAs", 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());

		
		//VER CONTROL
		lsEdas = (ListaSimple)rootView.findViewById(R.id.lsEdas); 
		GenerarEdas();
		
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_eda, R.string.agregar_eda, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		Button btnAgregarEda = (Button)rootView.findViewById(R.id.btnAgregarEda);
		btnAgregarEda.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva eda
				ControlEdasNuevo dialogo=new ControlEdasNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlEdas.this, ControlEdasNuevo.REQUEST_CODE);
				dialogo.show(ControlEdas.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlEdasNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		return rootView;
	}
	
	private void GenerarEdas(){
		AdaptadorArrayMultiTextView<ControlEda> adaptador = new AdaptadorArrayMultiTextView<ControlEda>(
				getActivity(), R.layout.fila_comun_para_ira_eda_accion_consulta,
				sesion.getDatosPacienteActual().edas, 
				new String[]{ControlEda.FECHA, ControlEda.ID_ASU_UM, ControlEda.ID_EDA},
				new int[]{R.id.txtFecha, R.id.txtUM, R.id.txtDetalle});
		adaptador.setViewBinder(binderFila);
		lsEdas.setAdaptador(adaptador);
	}

	private ObjectViewBinder<ControlEda> binderFila = new ObjectViewBinder<ControlEda>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, ControlEda origen,
				String atributoOrigen, Object valor) {
			TextView destino = (TextView)viewDestino;
			if(atributoOrigen.equals(ControlEda.FECHA)){
				destino.setText(DatosUtil.fechaHoraCorta(valor.toString()));
				return true;
			}else if(atributoOrigen.equals(ControlEda.ID_ASU_UM)){
				destino.setText(ArbolSegmentacion.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				return true;
			}else if(atributoOrigen.equals(ControlEda.ID_EDA)){
				destino.setText(Eda.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				TextView clave = (TextView)destino.getRootView().findViewById(R.id.txtClave);
				if(clave!=null)
					clave.setText(Eda.getClave(getActivity(), origen.id_eda));
				return true;
			}
			return false;
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlEdasNuevo.REQUEST_CODE:
			//if(resultCode==ControlEdasNuevo.RESULT_OK){
				GenerarEdas();
			//}
			break;
		}
	}

	
	
	
}//fin clase
