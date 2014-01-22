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
public class ControlVacunas extends Fragment {

	private static final String TAG = ControlVacunas.class.getSimpleName();
	
	private static final String PREFIJO_VACUNA = "id_vacuna_"; //Para localizar celdas en tabla
	
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
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verEsquema = (LinearLayout)rootView.findViewById(R.id.accion_ver_esquema);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_VER))
			verEsquema.setVisibility(View.VISIBLE); else verEsquema.setVisibility(View.GONE);
		
		LinearLayout agregarVacuna = (LinearLayout)rootView.findViewById(R.id.accion_agregar_vacuna);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_INSERTAR))
			agregarVacuna.setVisibility(View.VISIBLE); else agregarVacuna.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_esquema, "Ver Esquema de Vacunación", 
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
				AplicarVacuna(null);
			}
		});

		
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_INSERTAR)){
			//Asignación de click a cada celda de vacuna
			List<Vacuna> vacunas = Vacuna.getVacunasActivas(getActivity());
			for(final Vacuna vacuna : vacunas){
				View celda = tablaEsquema.findViewWithTag(PREFIJO_VACUNA+vacuna._id);
				if(celda != null)
					celda.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							TextView opcion =(TextView)view;
							//Si no está marcada ya
							if(!opcion.getText().toString().equals(getString(R.string.MARCA_VACUNA)))
								AplicarVacuna(vacuna._id);
						}
					});
			}
		}
		
		return rootView;
	}
	
	
	private void AplicarVacuna(Integer idVacuna){
		//Diálogo de nueva vacuna
		ControlVacunasNuevo dialogo=new ControlVacunasNuevo();
		Bundle args = new Bundle();
		if(idVacuna != null)args.putInt(ControlVacunasNuevo.PARAM_ID_VACUNA, idVacuna);
		dialogo.setArguments(args);
		dialogo.setTargetFragment(ControlVacunas.this, ControlVacunasNuevo.REQUEST_CODE);
		dialogo.show(ControlVacunas.this.getFragmentManager(),
				//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
				ControlVacunasNuevo.TAG);
		//Este diálogo avisará su fin en onActivityResult() de llamador
	}
	
	
	/**
	 * Marca en el esquema las vacunas aplicadas a la persona
	 */
	private void MarcarVacunas(){
		for(ControlVacuna vacuna : sesion.getDatosPacienteActual().vacunas){
			TextView celda = (TextView)tablaEsquema.findViewWithTag(PREFIJO_VACUNA+vacuna.id_vacuna);
			if(celda == null){
				//Vacuna inexistente en vista
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLVACUNA_VER, 
						"No hay celda en esquema para id_vacuna "+vacuna.id_vacuna);
				continue;
			}
			//La vacuna debe ser marcada
			celda.setBackgroundColor(getResources().getColor(R.color.vacuna_aplicable));
			celda.setText(getText(R.string.MARCA_VACUNA));
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlVacunasNuevo.REQUEST_CODE:
			//if(resultCode==ControlVacunasNuevo.RESULT_OK){
				MarcarVacunas();
			//}
			break;
		}
	}

	
	
	
}//fin clase
