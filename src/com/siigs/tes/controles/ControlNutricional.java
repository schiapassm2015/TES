/**
 * 
 */
package com.siigs.tes.controles;


import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import com.example.chartlibrary.Line;
import com.example.chartlibrary.LineGraph;
import com.example.chartlibrary.LinePoint;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.NivelNutricion;
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
import android.widget.Toast;

/**
 * @author Axel
 *
 */
public class ControlNutricional extends Fragment {

	private static final String TAG = ControlNutricional.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	LineGraph miGrafica=null;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlNutricional() {
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
				R.layout.controles_atencion_control_nutricional, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEG�N PERMISOS
		LinearLayout verNutricion = (LinearLayout)rootView.findViewById(R.id.accion_ver_nutricion);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLNUTRICIONAL_VER))
			verNutricion.setVisibility(View.VISIBLE); else verNutricion.setVisibility(View.GONE);
		
		LinearLayout agregarNutricion = (LinearLayout)rootView.findViewById(R.id.accion_agregar_nutricion);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLNUTRICIONAL_INSERTAR))
			agregarNutricion.setVisibility(View.VISIBLE); else agregarNutricion.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_nutricion, "Ver Controles Nutricionales", 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());

		
		//VER CONTROL
		miGrafica = (LineGraph)rootView.findViewById(R.id.grafica_lineal);
		GenerarGrafica();
		
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_nutricion, R.string.agregar_nutricion, 
				R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		
		Button btnAgregarControlNutricional = (Button)rootView.findViewById(R.id.btnAgregarControl);
		btnAgregarControlNutricional.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Di�logo de nueva vacuna
				ControlNutricionalNuevo dialogo=new ControlNutricionalNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlNutricional.this, ControlNutricionalNuevo.REQUEST_CODE);
				dialogo.show(ControlNutricional.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlNutricionalNuevo.TAG);
				//Este di�logo avisar� su fin en onActivityResult() de llamador
			}
		});
		
		return rootView;
	}
	
	private void GenerarGrafica(){
		Persona persona = sesion.getDatosPacienteActual().persona;
		
		Period periodo;
		int mesesMinimo = 0, mesesMaximo = -1;
		//Calculamos los meses del paciente
		DateTime nacimiento = new DateTime(persona.fecha_nacimiento);
				
		//Sacamos muestras del paciente
		Line lineaPaciente = new Line();
		for(com.siigs.tes.datos.tablas.ControlNutricional control 
				: sesion.getDatosPacienteActual().controlesNutricionales){
			DateTime muestra;
			try{
				muestra = DatosUtil.parsearFechaHora(control.fecha);
				periodo = new Interval(nacimiento, muestra).toPeriod();
			}catch(Exception e){
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLNUTRICIONAL_VER, 
						"La fecha del control nutricional id_persona:"+control.id_persona
						+" fecha:"+control.fecha+" no puede ser procesado contra nacimiento:"
								+persona.fecha_nacimiento+". Ex:"+e.getMessage());
				Toast.makeText(getActivity(), 
						"Las fechas control:"+control.fecha+" y nacimiento:"+persona.fecha_nacimiento
						+" no se pueden procesar. Informar al administrador", 
						Toast.LENGTH_LONG).show();
				return;
			}
			int meses = periodo.getYears()*12+periodo.getMonths();
			if(meses>mesesMaximo)mesesMaximo=meses;
			lineaPaciente.addPoint(new LinePoint(meses, (float)control.peso));
		}
		lineaPaciente.setColor(getResources().getColor(R.color.grafica_linea_paciente));
		lineaPaciente.setShowingPoints(true);
		lineaPaciente.setRadioPoint(7f);
		
		/*try{
			DateTime hoy = new DateTime(System.currentTimeMillis());
			periodo = new Interval(nacimiento,hoy).toPeriod();
			int meses = periodo.getYears()*12+periodo.getMonths();
			if(meses>mesesMaximo) mesesMaximo = meses;
		}catch(Exception e){}*/
		
		mesesMaximo += 6; //solo para darle un colch�n de visibilidad
		
		//Genereaci�n de l�neas b�sicas
		int genero=persona.sexo.equals(getString(R.string.valor_masculino)) ? 
						NivelNutricion.MASCULINO : NivelNutricion.FEMENINO;
		
		miGrafica.removeAllLines();
		miGrafica.addLine(
				NivelNutricion.getLineaNivelPeso(genero, NivelNutricion.PESO_NORMAL, 
						mesesMinimo, mesesMaximo));
		miGrafica.addLine(
				NivelNutricion.getLineaNivelPeso(genero, NivelNutricion.PESO_BAJO, 
						mesesMinimo, mesesMaximo));
		miGrafica.addLine(
				NivelNutricion.getLineaNivelPeso(genero, NivelNutricion.PESO_ALTO, 
						mesesMinimo, mesesMaximo));
		miGrafica.addLine(
				NivelNutricion.getLineaNivelPeso(genero, NivelNutricion.PESO_EXCEDIDO,
						mesesMinimo, mesesMaximo));
		
		
		miGrafica.addLine(lineaPaciente);
		//miGrafica.setRangeY(0, 11);
		//miGrafica.setLineToFill(2);
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlNutricionalNuevo.REQUEST_CODE:
			//if(resultCode==ControlNutricionalNuevo.RESULT_OK){
				GenerarGrafica();
			//}
			break;
		}
	}

	
	
	
}//fin clase
