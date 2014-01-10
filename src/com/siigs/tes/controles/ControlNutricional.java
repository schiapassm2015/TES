/**
 * 
 */
package com.siigs.tes.controles;

import java.util.List;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Vacuna;
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
import android.widget.Toast;

/**
 * @author Axel
 *
 */
public class ControlNutricional extends Fragment {

	private static final String TAG = ControlNutricional.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlNutricional() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_atencion_control_nutricional, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verNutricion = (LinearLayout)rootView.findViewById(R.id.accion_ver_nutricion);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLNUTRICIONAL_VER))
			verNutricion.setVisibility(View.VISIBLE); else verNutricion.setVisibility(View.GONE);
		
		LinearLayout agregarNutricion = (LinearLayout)rootView.findViewById(R.id.accion_agregar_nutricion);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLNUTRICIONAL_INSERTAR))
			agregarNutricion.setVisibility(View.VISIBLE); else agregarNutricion.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_nutricion, "Ver Controles Nutricionales", 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());

		GenerarGrafica();
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_nutricion, R.string.agregar_nutricion, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		Button btnAgregarControlNutricional = (Button)rootView.findViewById(R.id.btnAgregarVacuna);
		btnAgregarControlNutricional.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva vacuna
				ControlNutricionalNuevo dialogo=new ControlNutricionalNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlNutricional.this, ControlNutricionalNuevo.REQUEST_CODE);
				dialogo.show(ControlNutricional.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlNutricionalNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		return rootView;
	}
	
	private void GenerarGrafica(){
		//TODO
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlNutricionalNuevo.REQUEST_CODE:
			if(resultCode==ControlNutricionalNuevo.RESULT_OK){
				GenerarGrafica();
			}
			break;
		}
	}

	
	
	
}//fin clase
