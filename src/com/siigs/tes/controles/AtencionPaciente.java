/**
 * 
 */
package com.siigs.tes.controles;

import java.util.List;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Alergia;
import com.siigs.tes.datos.tablas.AntiguoDomicilio;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.TipoSanguineo;
import com.siigs.tes.datos.tablas.Tutor;
import com.siigs.tes.ui.AdaptadorArrayMultiView;
import com.siigs.tes.ui.WidgetUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
	
	//Guardan selecciones de AutoCompleteTextView ó -1 si nada hay aún
	private int idLocalidadSeleccionada = -1;
	private String textoLocalidadSeleccionada = "";
	
	private TextView lblSinAlergiasVer = null;
	
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
			verDatos.setVisibility(View.VISIBLE); else verDatos.setVisibility(View.GONE);
		
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
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver, R.string.datos_paciente);

		((TextView)rootView.findViewById(R.id.txtNombre)).setText(p.nombre);
		((TextView)rootView.findViewById(R.id.txtCurp)).setText(p.curp);
		((TextView)rootView.findViewById(R.id.txtEdad)).setText(DatosUtil.calcularEdad(p.fecha_nacimiento));
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
		final com.siigs.tes.ui.ListaSimple lsAlergiasActuales = (com.siigs.tes.ui.ListaSimple)
				rootView.findViewById(R.id.lsAlergiasActuales);
		lblSinAlergiasVer = (TextView) rootView.findViewById(R.id.lblSinAlergiasVer);
		GenerarAlergiasVer(lsAlergiasActuales);
		
		
		//LECTURA DE DATOS PARA SECCIÓN DOMICILIO
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_domicilio, R.string.actualizar_domicilio);

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
				
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea actualizar el domicilio?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String fechaCambio = DatosUtil.getAhora();
						//Guardamos cambios en memoria
						AntiguoDomicilio antiguoDom = AntiguoDomicilio.DePersona(p, fechaCambio);
						
						p.ultima_actualizacion = fechaCambio;
						p.id_asu_localidad_domicilio = idLocalidadSeleccionada;
						p.calle_domicilio = txtCalle.getText().toString();
						p.colonia_domicilio = txtColonia.getText().toString();
						p.numero_domicilio = txtNumero.getText().toString();
						
						//En bd
						try {
							AntiguoDomicilio.AgregarAntiguoDomicilio(getActivity(), antiguoDom);
							Persona.AgregarEditar(getActivity(), p);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ContenidoControles.ICA_PACIENTE_EDITAR_DOMICILIO, "paciente:"+p.id);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ContenidoControles.ICA_PACIENTE_EDITAR_DOMICILIO, e.toString());
							e.printStackTrace();
						}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = Persona.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(p);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(AtencionPaciente.this,
								DialogoTes.ModoOperacion.GUARDAR, pendiente);
					}
				} );
				dialogo.show();
			}
		});
		
		
		// LECTURA DE DATOS PARA SECCIÓN UNIDAD MÉDICA
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_um, R.string.actualizar_um);

		Button btnActualizarUM = (Button) rootView.findViewById(R.id.btnActualizarUM);
		btnActualizarUM.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea asignar paciente a esta unidad médica?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Guardamos cambios en memoria
						p.ultima_actualizacion = DatosUtil.getAhora();
						p.id_asu_um_tratante = aplicacion.getUnidadMedica();						
						// En bd
						int ICA = ContenidoControles.ICA_PACIENTE_ASIGNAR_UM;
						try {
							Persona.AgregarEditar(getActivity(), p);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
						}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = Persona.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(p);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(AtencionPaciente.this,
								DialogoTes.ModoOperacion.GUARDAR, pendiente);
					}
				} );
				dialogo.show();
			}
		});
		
		
		//LECTURA DE DATOS PARA SECCIÓN AGREGAR ALERGIAS
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_alergias, 
				R.string.agregar_alergia, R.layout.ayuda_dialogo_tes_login, getFragmentManager());
		TextView ayudaAlergia = (TextView) rootView.findViewById(
				R.id.barra_titulo_alergias).findViewById(R.id.txtTituloBarra);
		ayudaAlergia.setText(R.string.agregar_alergia);
		
		final Spinner spAlergiaAgregar = (Spinner) rootView.findViewById(R.id.spAlergiaAgregar);
		GenerarAlergiasAgregables(spAlergiaAgregar);
		
		Button btnAgregarAlergia = (Button)rootView.findViewById(R.id.btnAgregarAlergia);
		btnAgregarAlergia.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea registrar esta alergia para el paciente? \nEsta acción no se puede deshacer");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(spAlergiaAgregar.getAdapter().getCount()<=0)return;
						
						// Guardamos cambios en memoria
						PersonaAlergia alergia = new PersonaAlergia();
						alergia.id_alergia = ((Alergia)spAlergiaAgregar.getSelectedItem())._id;
						alergia.id_persona = p.id;
						alergia.ultima_actualizacion = DatosUtil.getAhora();
						sesion.getDatosPacienteActual().alergias.add(alergia);
						//En bd
						PersonaAlergia.AgregarNuevaAlergia(getActivity(), alergia);
						Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
								ContenidoControles.ICA_PACIENTE_AGREGAR_ALERGIAS, "paciente:"+p.id+", alergia:"+alergia.id_alergia);
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = PersonaAlergia.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(alergia);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(AtencionPaciente.this,
								DialogoTes.ModoOperacion.GUARDAR, pendiente);
						//Refrescamos controles
						GenerarAlergiasAgregables(spAlergiaAgregar);
						GenerarAlergiasVer(lsAlergiasActuales);
					}
				} );
				dialogo.show();
			}
		});
		
		
		return rootView;
	}
	
	/**
	 * Genera adaptador para el texto sugerido de arbol de segmentación
	 * @param asu View de autocompletado a configurar
	 * @param id_asu Hoja base del arbol de segmentación para generar sugerencias y opción default 
	 */
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
	
	
	private void GenerarAlergiasAgregables(Spinner spAlergias){
		spAlergias.setAdapter(crearAdaptadorAlergias(false));
	}
	
	private void GenerarAlergiasVer(com.siigs.tes.ui.ListaSimple lista){
		AdaptadorArrayMultiView<Alergia> adaptador = crearAdaptadorAlergias(true);
		lista.setAdaptador(adaptador);
		if(adaptador.getCount()>0)lblSinAlergiasVer.setVisibility(View.GONE);
		else lblSinAlergiasVer.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Crea un adaptador con alergias que TIENE ó NO TIENE el paciente según {@link enAlergiasPaciente}
	 * @param enAlergiasPaciente
	 * @return
	 */
	private AdaptadorArrayMultiView<Alergia> crearAdaptadorAlergias(boolean enAlergiasPaciente){
		//Consultamos las alergias agregables (aún no existen en el paciente actual
		List<Alergia> alergias = Alergia.getAlergiasConLista(getActivity(),
				sesion.getDatosPacienteActual().alergias, enAlergiasPaciente);

		//Aquí hacemos un "hack" en clase Alergia pues usaremos su campo activo (int) para guardar
		//el id de la imagen que se visualizará con el adaptador en el layout. Estas instancias de
		//Alergia NO deberían ser usadas para algo más, pues están "hackeadas"
		for(Alergia alergia : alergias)
			alergia.activo = Alergia.getResourceImagenTipoAlergia(alergia.tipo);
		
		// Construimos el adaptador que mapeará texto e imagen
		AdaptadorArrayMultiView.Mapeo[] mapeo = new AdaptadorArrayMultiView.Mapeo[] {
				new AdaptadorArrayMultiView.Mapeo(Alergia.DESCRIPCION,
						android.R.id.text1, "setText", CharSequence.class),
				new AdaptadorArrayMultiView.Mapeo(Alergia.ACTIVO, R.id.imgIcono,
						"setBackgroundResource", int.class) };
		return new AdaptadorArrayMultiView<Alergia>(getActivity(), 
				R.layout.fila_principalfragment, alergias, mapeo);
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

	
}//fin clase
