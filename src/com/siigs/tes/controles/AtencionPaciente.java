/**
 * 
 */
package com.siigs.tes.controles;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.Alergia;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.TipoSanguineo;
import com.siigs.tes.datos.tablas.Tutor;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Axel
 *
 */
public class AtencionPaciente extends Fragment {

	private static final String TAG = AtencionPaciente.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private AutoCompleteTextView acLocalidad=null;
	private AutoCompleteTextView acUM = null;
	
	//Guardan selecciones de AutoCompleteTextView ó -1 si nada hay aún
	private int idLocalidadSeleccionada = -1;
	private int idUmSeleccionada = -1;
	private String textoLocalidadSeleccionada = "";
	private String textoUmSeleccionada = "";
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public AtencionPaciente() {
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
				R.layout.controles_atencion_paciente, container, false);

		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verDatos = (LinearLayout)rootView.findViewById(R.id.accion_ver_datos_paciente);
		if(sesion.tienePermiso(ContenidoControles.ICA_PACIENTE_VER))
			verDatos.setVisibility(View.VISIBLE); else verDatos.setVisibility(View.VISIBLE);
		
		LinearLayout editarDomicilio = (LinearLayout)rootView.findViewById(R.id.accion_editar_domicilio);
		if(sesion.tienePermiso(ContenidoControles.ICA_PACIENTE_EDITAR_DOMICILIO))
			editarDomicilio.setVisibility(View.VISIBLE); else editarDomicilio.setVisibility(View.GONE);
		
		LinearLayout editarUM = (LinearLayout)rootView.findViewById(R.id.accion_editar_um);
		if(sesion.tienePermiso(ContenidoControles.ICA_PACIENTE_ASIGNAR_UM))
			editarUM.setVisibility(View.VISIBLE); else editarUM.setVisibility(View.GONE);
		
		LinearLayout editarAlergias = (LinearLayout)rootView.findViewById(R.id.accion_editar_alergias);
		if(sesion.tienePermiso(ContenidoControles.ICA_PACIENTE_AGREGAR_ALERGIAS))
			editarAlergias.setVisibility(View.VISIBLE); else editarAlergias.setVisibility(View.GONE);
		
		final Persona p = sesion.getDatosPacienteActual().persona;
		
		//LECTURA DE DATOS PARA SECCIÓN VER
		TextView ayudaVer = 
				(TextView)rootView.findViewById(R.id.barra_titulo_ver).findViewById(R.id.txtTituloBarra);
		ayudaVer.setText(R.string.datos_paciente);
		((TextView)rootView.findViewById(R.id.txtNombre)).setText(p.nombre);
		((TextView)rootView.findViewById(R.id.txtCurp)).setText(p.curp);
		((TextView)rootView.findViewById(R.id.txtEdad)).setText(calcularEdad(p.fecha_nacimiento));
		((TextView)rootView.findViewById(R.id.txtSexo)).setText(p.sexo);
		((TextView)rootView.findViewById(R.id.txtSangre)).setText(
				TipoSanguineo.getTipoSanguineo(getActivity(), p.id_tipo_sanguineo));
		((TextView)rootView.findViewById(R.id.txtDireccion)).setText(p.calle_domicilio);
		((TextView)rootView.findViewById(R.id.txtCP)).setText(p.cp_domicilio+"");
		
		String valor = getString(R.string.desconocido);
		try{valor=ArbolSegmentacion.getDescripcion(getActivity(), p.id_asu_localidad_domicilio);}catch(Exception e){}
		((TextView)rootView.findViewById(R.id.txtLocalidad)).setText(valor);
		
		((TextView)rootView.findViewById(R.id.txtFechaRegistroCivil)).setText(p.fecha_registro);
		
		valor = getString(R.string.desconocido);
		try{valor = ArbolSegmentacion.getDescripcion(getActivity(), p.id_asu_localidad_nacimiento);}catch(Exception e){}
		((TextView)rootView.findViewById(R.id.txtLocalidadRegistroCivil)).setText(valor);
		
		valor = getString(R.string.desconocido);
		try{valor = ArbolSegmentacion.getDescripcion(getActivity(), p.id_asu_um_tratante);}catch(Exception e){}
		((TextView)rootView.findViewById(R.id.txtUnidadMedicaTratante)).setText(valor);
		
		Tutor tutor = sesion.getDatosPacienteActual().tutor;
		String nombreTutor = tutor.nombre+" "+tutor.apellido_paterno+" "+tutor.apellido_materno;
		((TextView)rootView.findViewById(R.id.txtTutor)).setText(nombreTutor);
				
		//Lista de alergias a ver
		com.siigs.tes.ui.ListaSimple lsAlergiasActuales = (com.siigs.tes.ui.ListaSimple)
				rootView.findViewById(R.id.lsAlergiasActuales);
		if(GenerarAlergias(lsAlergiasActuales)>0)
			rootView.findViewById(R.id.lblSinAlergiasVer).setVisibility(View.GONE);
		else rootView.findViewById(R.id.lblSinAlergiasVer).setVisibility(View.VISIBLE);
		
		
		//LECTURA DE DATOS PARA SECCIÓN DOMICILIO
		TextView ayudaDomicilio = (TextView) rootView.findViewById(
				R.id.barra_titulo_domicilio).findViewById(R.id.txtTituloBarra);
		ayudaDomicilio.setText(R.string.actualizar_domicilio);
		final TextView txtCalle = (TextView)rootView.findViewById(R.id.txtCalle);
		txtCalle.setText(p.calle_domicilio);
		final TextView txtNumero = (TextView)rootView.findViewById(R.id.txtNumero);
		txtNumero.setText(p.numero_domicilio);
		final TextView txtColonia = (TextView)rootView.findViewById(R.id.txtColonia);
		txtColonia.setText(p.colonia_domicilio);
		
		//Autocomplete de localidad
		idLocalidadSeleccionada = p.id_asu_localidad_domicilio;
		acLocalidad = (AutoCompleteTextView) rootView.findViewById(R.id.acLocalidad);
		GenerarAutoCompleteASU(acLocalidad, idLocalidadSeleccionada);
		textoLocalidadSeleccionada = acLocalidad.getText().toString();
		acLocalidad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				idLocalidadSeleccionada = (int)id;
				textoLocalidadSeleccionada = acLocalidad.getText().toString();
			}
		});
		acLocalidad.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus)acLocalidad.setText(textoLocalidadSeleccionada);
			}
		});
		
		Button btnActualizarDomicilio = (Button)rootView.findViewById(R.id.btnActualizarDireccion);
		btnActualizarDomicilio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Validamos los datos
				if(txtCalle.getText().toString().length()==0 || txtColonia.getText().toString().length()==0
						|| txtNumero.getText().toString().length()==0){
					Toast.makeText(getActivity(), getString(R.string.aviso_llenar_campos), Toast.LENGTH_LONG).show();
					return;
				}
				//Guardamos cambios
				p.id_asu_localidad_domicilio = idLocalidadSeleccionada;
				p.calle_domicilio = txtCalle.getText().toString();
				p.colonia_domicilio = txtColonia.getText().toString();
				p.numero_domicilio = txtNumero.getText().toString();
				//TODO QUEDAMOS QUE SI ES DE LOCALIDAD FUERA Y EL CAMBIO ES A LA MÍA NO GUARDO EN BD, PUES
				//AL SINCRONIZAR DEBO RECIBIR EL REGISTRO. Y SI ES ACTUALMENTE MÍO Y LO CAMBIO FUERA DE MI??
				//QUÉ PASA EN CADA CASO SI NO PUEDO ESCRIBIR EN SU TES??? LO AGREGO A PENDIENTES???
				//Por default pedimos una TES al usuario en un diálogo modal
				DialogoTes.IniciarNuevo(AtencionPaciente.this, DialogoTes.ModoOperacion.GUARDAR);
				//Este diálogo avisará su fin en onActivityResult()
			}
		});
		
		
		// LECTURA DE DATOS PARA SECCIÓN UNIDAD MÉDICA
		TextView ayudaUnidadMedica = (TextView) rootView.findViewById(
				R.id.barra_titulo_um).findViewById(R.id.txtTituloBarra);
		ayudaUnidadMedica.setText(R.string.actualizar_um);
		//Autocomplete de localidad
		idUmSeleccionada = p.id_asu_um_tratante;
		acUM = (AutoCompleteTextView) rootView
				.findViewById(R.id.acUM);
		GenerarAutoCompleteASU(acUM, idUmSeleccionada);
		textoUmSeleccionada = acUM.getText().toString();
		acUM.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				idUmSeleccionada = (int) id;
				textoUmSeleccionada = acUM.getText().toString();
			}
		});
		acUM.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (!hasFocus)acUM.setText(textoUmSeleccionada);
			}
		});

		Button btnActualizarUM = (Button) rootView.findViewById(R.id.btnActualizarUM);
		btnActualizarUM.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Validamos los datos
				//TODO preguntar si quiere cambiar a lo seleccionadoo...
				// Guardamos cambios
				p.id_asu_um_tratante = idUmSeleccionada;
				// TODO QUEDAMOS QUE SI ES DE LOCALIDAD FUERA Y EL CAMBIO ES A
				// LA MÍA NO GUARDO EN BD, PUES
				// AL SINCRONIZAR DEBO RECIBIR EL REGISTRO. Y SI ES ACTUALMENTE
				// MÍO Y LO CAMBIO FUERA DE MI??
				// QUÉ PASA EN CADA CASO SI NO PUEDO ESCRIBIR EN SU TES??? LO
				// AGREGO A PENDIENTES???
				// Por default pedimos una TES al usuario en un diálogo modal
				DialogoTes.IniciarNuevo(AtencionPaciente.this,
						DialogoTes.ModoOperacion.GUARDAR);
				// Este diálogo avisará su fin en onActivityResult()
			}
		});
		
		
		//LECTURA DE DATOS PARA SECCIÓN AGREGAR ALERGIAS
		TextView ayudaAlergia = (TextView) rootView.findViewById(
				R.id.barra_titulo_alergias).findViewById(R.id.txtTituloBarra);
		ayudaAlergia.setText(R.string.actualizar_alergias);
		
		return rootView;
	}
	
	private void GenerarAutoCompleteASU(AutoCompleteTextView asu, int id_asu){
		final ArbolSegmentacion arbol = ArbolSegmentacion.getArbol(getActivity(), id_asu);
		asu.setText(arbol.descripcion);

		String[] de = new String[]{ArbolSegmentacion.DESCRIPCION};
		int[] hacia = new int[]{android.R.id.text1};
		SimpleCursorAdapter adaptador = new SimpleCursorAdapter(
				getActivity(), android.R.layout.simple_dropdown_item_1line,
				null, de, hacia,0);
		//Convertidor de lo legible
		adaptador.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cur) {
				return cur.getString(cur.getColumnIndex(ArbolSegmentacion.DESCRIPCION));
			}
		});
		
		adaptador.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence description) {
				return ArbolSegmentacion.buscar(getActivity(), description.toString(),
						arbol.grado_segmentacion, arbol.id_padre);
			}
		});
		
		asu.setAdapter(adaptador);
	}
	
	
	private int GenerarAlergias(com.siigs.tes.ui.ListaSimple lista){
		List<Alergia> alergias = Alergia.getAlergiasEnLista(getActivity(),
				sesion.getDatosPacienteActual().alergias);
		AdaptadorArrayMultiTextView<Alergia> adaptador = new AdaptadorArrayMultiTextView<Alergia>(
				getActivity(),android.R.layout.simple_list_item_checked, alergias, 
				new String[]{Alergia.DESCRIPCION}, new int[]{android.R.id.text1});
		/*ArrayAdapter<String> adaptador = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_list_item_checked, android.R.id.text1, alergias);*/
		lista.setAdaptador(adaptador);
		return alergias.size();
	}
	
	/**
	 * Calcula la edad basado en la fecha ingresada
	 * @param fechaNacimiento Fecha en formato aaaa/mm/dd
	 * @return
	 */
	private String calcularEdad(String fechaNacimiento){
		DateTime nacimiento = new DateTime(fechaNacimiento);
		DateTime hoy = new DateTime(System.currentTimeMillis());
		Period periodo;
		try{
			periodo = new Interval(nacimiento,hoy).toPeriod();
		}catch(Exception e){return fechaNacimiento;}
		
		if(periodo.getYears()<=0) {
			if(periodo.getMonths()<=0) return periodo.getWeeks()+" semanas, "+periodo.getDays()+" días";
			else return periodo.getMonths()+" meses, "+ periodo.toStandardDays().getDays();
		}else return periodo.getYears()+" años, "+ periodo.getMonths()+" meses";	
	}

	@Override
	public void onPause() {
		if(acLocalidad!=null && acLocalidad.getAdapter()!=null){
			try{
				SimpleCursorAdapter adaptador = (SimpleCursorAdapter) acLocalidad.getAdapter();
				if(adaptador.getCursor()!=null){
					Log.d(TAG,"cerrando cursor");
					adaptador.getCursor().close();
				}
			}catch(Exception e){}
		}
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case DialogoTes.REQUEST_CODE:
			if(resultCode==DialogoTes.RESULT_CANCELAR){
				//TODO implementar guardado de TES_PENDIENTES
			}else if(resultCode==DialogoTes.RESULT_OK){/*NADA*/}
			break;
		}
	}

	
	
	
}//fin clase
