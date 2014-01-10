/**
 * 
 */
package com.siigs.tes.controles;


import java.io.Serializable;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.vistas.Censo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Axel
 * Hace una consulta masiva en el censo pudiendo regresar desde pocos datos hasta miles dependiendo
 * del censo manejado en la unidad médica. Por tal motivo se implementa un LoaderManager para
 * no bloquear la aplicación mientras se sacan los resultados.
 * 
 * Por seguir nomenclatura "GrupoSubgrupo" el nombre de esta clase
 * se compone de Grupo:Censo, Control:CensoNominal
 */
public class CensoCensoNominal extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = CensoCensoNominal.class.getSimpleName();
	
	//Datos para comunicación externa
	public static final int REQUEST_CODE = 54; //Se usa en caso de avisar que usuario quiere atender un paciente
	public static final int RESULT_ATENDER_PACIENTE_SIN_TES = 111;
	public static final String PARAM_ID_PERSONA = "id_persona";
	
	//Datos de filtro para consulta
	private static final String FILTRO_NOMBRE = "filtro_nombre";
	private static final String FILTRO_SEXO = "filtro_sexo";
	private static final String FILTRO_ANO_NACIMIENTO = "filtro_ano_nacimiento";
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private static final int ID_LOADER_CENSO = 1; //identifica el loader si hubiera varios
	private ListView lvResultados = null;
	private SimpleCursorAdapter adaptadorCenso = null;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CensoCensoNominal() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
		
		//Definición del adaptador para resultados del censo con Cursor nulo
		String[] bindDeColumna = new String[]{Censo.NOMBRE_PACIENTE, Censo.APPAT_PACIENTE, Censo.APMAT_PACIENTE,
				Censo.NOMBRE_TUTOR, Censo.APPAT_TUTOR, Censo.APMAT_TUTOR,
				Censo.CALLE_DOMICILIO, Censo.CURP, Censo.FECHA_NACIMIENTO, Censo.SEXO, Censo.BCG,
				Censo.HEPATITIS_1, Censo.HEPATITIS_2, Censo.HEPATITIS_3, Censo.PENTAVALENTE_1,
				Censo.PENTAVALENTE_2, Censo.PENTAVALENTE_3, Censo.PENTAVALENTE_4, Censo.DPT_R, 
				Censo.SRP_1, Censo.SRP_2, Censo.ROTAVIRUS_1, Censo.ROTAVIRUS_2,
				Censo.ROTAVIRUS_3, Censo.NEUMOCOCO_1, Censo.NEUMOCOCO_2, Censo.NEUMOCOCO_3,
				Censo.INFLUENZA_1, Censo.INFLUENZA_2, Censo.INFLUENZA_R};
		
		int[] bindAview = new int[]			{R.id.txtNombre, R.id.txtPaterno, R.id.txtMaterno,
				R.id.txtNombreTutor, R.id.txtPaternoTutor, R.id.txtMaternoTutor,
				R.id.txtDomicilio, R.id.txtCurp, R.id.txtFechaNacimiento, R.id.txtSexo, R.id.txtBCG_U,
				R.id.txtHepatitis_1, R.id.txtHepatitis_2, R.id.txtHepatitis_3, R.id.txtPentavalente_1,
				R.id.txtPentavalente_2, R.id.txtPentavalente_3, R.id.txtPentavalente_4, R.id.txtDPT_R, 
				R.id.txtSRP_1, R.id.txtSRP_2, R.id.txtROTAVIRUS_1, R.id.txtROTAVIRUS_2,
				R.id.txtROTAVIRUS_3, R.id.txtNEUMOCOCO_1, R.id.txtNEUMOCOCO_2, R.id.txtNEUMOCOCO_3,
				R.id.txtINFLUENZA_1, R.id.txtINFLUENZA_2, R.id.txtINFLUENZA_R};
		
		adaptadorCenso = new SimpleCursorAdapter(getActivity(), 
				R.layout.fila_censo_nominal, null, bindDeColumna, bindAview, 0);
		adaptadorCenso.setViewBinder(binderFilaCenso);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_censo_censo_nominal, container, false);

		//Sexo
		final Spinner spSexo=(Spinner)rootView.findViewById(R.id.spSexo);
		ArrayAdapter<CharSequence> adaptadorSexo=
				ArrayAdapter.createFromResource(getActivity(), 
						R.array.opciones_sexo, android.R.layout.simple_spinner_item);
		adaptadorSexo.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spSexo.setAdapter(adaptadorSexo);
		
		final EditText txtNombre = (EditText)rootView.findViewById(R.id.txtNombre);
		//Año nacimiento
		final EditText txtAno = (EditText)rootView.findViewById(R.id.txtAnoNacimiento);
	
		lvResultados = (ListView) rootView.findViewById(R.id.lvResultados);
		
		Button btnFiltrar = (Button) rootView.findViewById(R.id.btnFiltrar);
		btnFiltrar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Datos del filtro
				
				Integer anoNacimiento = null; 
				//Intentamos conseguir número, pero si falla no usamos el dato
				try{ anoNacimiento = Integer.parseInt(txtAno.getText().toString());}catch(Exception e){}
				
				String nombre = txtNombre.getText().toString().trim();
				if(nombre.equals(""))nombre = null;
				
				String sexo = spSexo.getSelectedItem().toString();
				if(sexo.equals(getString(R.string.indistinto)))sexo=null;
				else if(sexo.equals(getString(R.string.masculino)))sexo = Censo.MASCULINO;
				else sexo = Censo.FEMENINO;
				
				LlenarResultados(nombre,sexo,anoNacimiento);
			}
		});
		
		Button btnVerTodos = (Button)rootView.findViewById(R.id.btnVerTodos);
		btnVerTodos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LlenarResultados(null,null,null);
			}
		});
		
		return rootView;
	}
	
	
	/**
	 * Encapsula los parámetros de la consulta para crear consulta en otro thread.
	 * Los parámetros pueden ser nulos
	 * @param nombre
	 * @param sexo
	 * @param anoNacimiento
	 */
	private void LlenarResultados(String nombre, String sexo, Integer anoNacimiento){
		//Mandamos a crear el cursor en otro thread llamando a onCreateLoader()
		Bundle datos = new Bundle();
		if(nombre!=null)datos.putString(FILTRO_NOMBRE, nombre);
		if(sexo!=null)datos.putString(FILTRO_SEXO, sexo);
		if(anoNacimiento!=null)datos.putInt(FILTRO_ANO_NACIMIENTO, anoNacimiento);
		
		this.getLoaderManager().restartLoader(ID_LOADER_CENSO, datos, this);
		
		lvResultados.setAdapter(this.adaptadorCenso);
	}

	/**
	 * Creamos un binder para capturar/modificar datos al insertarse en UI de una fila de resultado
	 */
	private ViewBinder binderFilaCenso = new ViewBinder(){
		@Override
		public boolean setViewValue(View view, final Cursor cur, int col) {
			//Marcaje de views tipo vacuna
			if(view.getTag()!=null && view.getTag().equals("vacuna")){
				((TextView)view).setText(cur.isNull(col) ? "" : 
							getString(R.string.MARCA_VACUNA));
				return true;
			}

			//Fecha nacimiento
			if(view.getId()==R.id.txtFechaNacimiento){
				((TextView)view).setText(DatosUtil.fechaCorta(cur.getString(col)));
				return true;
			}
			
			//Domicilio
			if(view.getId()==R.id.txtDomicilio){
				String domicilio = cur.getString(cur.getColumnIndex(Censo.CALLE_DOMICILIO));
				if(!cur.isNull(cur.getColumnIndex(Censo.NUMERO_DOMICILIO)))
					domicilio+=", "+ cur.getString(cur.getColumnIndex(Censo.NUMERO_DOMICILIO));
				if(!cur.isNull(cur.getColumnIndex(Censo.COLONIA_DOMICILIO)))
					domicilio+=", "+ cur.getString(cur.getColumnIndex(Censo.COLONIA_DOMICILIO));
				if(!cur.isNull(cur.getColumnIndex(Censo.REFERENCIA_DOMICILIO)))
					domicilio+=", "+ cur.getString(cur.getColumnIndex(Censo.REFERENCIA_DOMICILIO));
				((TextView)view).setText(domicilio);
				return true;
			}
			
			//BOTÓN PARA DAR CAPACIDAD DE ATENDER A UN PACIENTE SIN TES
			if(view.getId()==R.id.txtPaterno || view.getId()==R.id.txtMaterno || view.getId()==R.id.txtNombre)
				((LinearLayout)view.getParent()).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//Confirmación
						AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
						dialogo.setMessage("¿Desea darle atención al paciente seleccionado?");
						dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
							@Override public void onClick(DialogInterface arg0, int arg1) {}
						});
						dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//Encapsulamos id de la persona
								int idPersona = cur.getInt(cur.getColumnIndex(Persona._ID));
								Intent datos = new Intent();
								datos.putExtra(PARAM_ID_PERSONA, idPersona);
								//Avisamos al target que hemos realizado acción de solicitar atención para alguien
								getTargetFragment().onActivityResult(getTargetRequestCode(), 
										RESULT_ATENDER_PACIENTE_SIN_TES, datos);
							}
						} );
						dialogo.show();
					}
				});
			
			return false; //false para que binder siga haciendo su chamba automática
		}
	};
	
	//FUNCIONES HEREDADAS DE LoaderManager para el cursor de la consulta
	
	@Override
	public Loader<Cursor> onCreateLoader(int idLoader, Bundle args) {    	
    	switch(idLoader){
    	case ID_LOADER_CENSO:
    		return Censo.getCenso(getActivity(), 
    				args.containsKey(FILTRO_NOMBRE) ? args.getString(FILTRO_NOMBRE) : null, 
    				args.containsKey(FILTRO_ANO_NACIMIENTO) ? args.getInt(FILTRO_ANO_NACIMIENTO) : null,
    				args.containsKey(FILTRO_SEXO) ? args.getString(FILTRO_SEXO) : null);
    	}
    	return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(loader.getId()== ID_LOADER_CENSO){
			//El thread ha terminado de crear un cursor con los datos,
        	//ahora el adaptador que actualmente contiene 0 datos actualiza
        	//su cursor lo cual escuchará esta clase Fragment y 
        	//visualizará los datos.
			this.adaptadorCenso.swapCursor(cursor);
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if(loader.getId() == ID_LOADER_CENSO)
			this.adaptadorCenso.swapCursor(null);
		
	}
	
}//fin clase
