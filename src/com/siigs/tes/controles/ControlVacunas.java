/**
 * 
 */
package com.siigs.tes.controles;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.Persona;
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
public class ControlVacunas extends Fragment {

	private static final String TAG = ControlVacunas.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private View tablaEsquema; //Contiene las celdas a marcar

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlVacunas() {
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
				R.layout.controles_atencion_control_vacunas, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		((TextView)rootView.findViewById(R.id.txtNombre)).setText(p.getNombreCompleto());
		((TextView)rootView.findViewById(R.id.txtSexoEdad)).setText(
				"Sexo: "+p.sexo+"   Edad: "+DatosUtil.calcularEdad(p.fecha_nacimiento));
		
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verEsquema = (LinearLayout)rootView.findViewById(R.id.accion_ver_esquema);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_VER))
			verEsquema.setVisibility(View.VISIBLE); else verEsquema.setVisibility(View.GONE);
		
		LinearLayout agregarVacuna = (LinearLayout)rootView.findViewById(R.id.accion_agregar_vacuna);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_INSERTAR))
			agregarVacuna.setVisibility(View.VISIBLE); else agregarVacuna.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_esquema, "Ver esquema de vacunación", 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());

		//Marcado de vacunas en tablaEsquema
		tablaEsquema = rootView.findViewById(R.id.tablaEsquema);
		MarcarVacunas();
		
		//AGREGAR VACUNA
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_vacuna, R.string.agregar_vacuna, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		Button btnAgregarVacuna = (Button)rootView.findViewById(R.id.btnAgregarVacuna);
		btnAgregarVacuna.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva vacuna
				ControlVacunasNuevo dialogo=new ControlVacunasNuevo();
				Bundle args = new Bundle();
				//args.putSerializable(ControlVacunasNuevo.PARAM_MODO_OPERACION, modoOperacion);
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlVacunas.this, ControlVacunasNuevo.REQUEST_CODE);
				dialogo.show(ControlVacunas.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlVacunasNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
			
		
		return rootView;
	}
	
	/**
	 * Marca en el esquema las vacunas aplicadas a la persona
	 */
	public void MarcarVacunas(){
		for(ControlVacuna vacuna : sesion.getDatosPacienteActual().vacunas){
			TextView celda = (TextView)tablaEsquema.findViewWithTag("id_vacuna_"+vacuna.id_vacuna);
			if(celda == null){
				//Vacuna inexistente en vista
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLVACUNA_VER, 
						"No hay celda en esquema para id_vacuna "+vacuna.id_vacuna);
				continue;
			}
			//La vacuna debe ser marcada
			celda.setText("X");
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlVacunasNuevo.REQUEST_CODE:
			if(resultCode==ControlVacunasNuevo.RESULT_OK){
				MarcarVacunas();
			}
			break;
		}
	}

	
	
	
}//fin clase
