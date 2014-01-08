/**
 * 
 */
package com.siigs.tes.controles;


import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.ProveedorContenido;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

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
	
	private static final String TAG = AtencionPaciente.class.getSimpleName();
	
	//Datos para comunicación externa
	public static final int REQUEST_CODE = 54; //Se usa en caso de avisar que usuario quiere atender un paciente
	public static final int RESULT_ATENDER_PACIENTE_SIN_TES = 111;
	public static final String PARAM_ID_PERSONA = "id_persona";
	
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
		String[] bindDeColumna = new String[]{"p.nombre"};
		int[] bindAview = new int[]{R.id.txtNombre};
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
		Spinner spSexo=(Spinner)rootView.findViewById(R.id.spSexo);
		ArrayAdapter<CharSequence> adaptadorSexo=
				ArrayAdapter.createFromResource(getActivity(), 
						R.array.opciones_sexo, android.R.layout.simple_spinner_item);
		adaptadorSexo.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spSexo.setAdapter(adaptadorSexo);
		
		//Edad
		Spinner spEdad=(Spinner)rootView.findViewById(R.id.spEdad);
		ArrayAdapter<CharSequence> adaptadorEdad=
				ArrayAdapter.createFromResource(getActivity(), 
						R.array.opciones_edad, android.R.layout.simple_spinner_item);
		adaptadorEdad.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spEdad.setAdapter(adaptadorEdad);
	
		lvResultados = (ListView) rootView.findViewById(R.id.lvResultados);
		
		Button btnFiltrar = (Button) rootView.findViewById(R.id.btnFiltrar);
		btnFiltrar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO poner filtros reales
				LlenarResultados(null,null,null);
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
	
	
	private void LlenarResultados(String nombre, String sexo, Integer edad){
		if(nombre != null){
			//TODO implementar filtrado
		}
		
		//Mandamos a crear el cursor en otro thread llamando a onCreateLoader()
		this.getLoaderManager().initLoader(ID_LOADER_CENSO, null, this);
		
		lvResultados.setAdapter(this.adaptadorCenso);
	}

	/**
	 * Creamos un binder para capturar/modificar datos al insertarse en UI de una fila de resultado
	 */
	private ViewBinder binderFilaCenso = new ViewBinder(){

		@Override
		public boolean setViewValue(View view, final Cursor cur, int col) {
			// TODO selección de vacunas
			//método con tag()
			/**
			 * String[] tagViewVacuna = new String[]{"vacXX","vacYY", "vacZZ"};
			 * if(view.getTag() IN tagViewVacuna){ //Si es vacuna
			 * 		if(!cur.isnull(col)) //Y existe
			 * 			((TextView)view).setText(getString(R.strings.MARCA_VACUNA)); //la marcamos
			 * 		return true;
			 * }
			 */

			//BOTÓN PARA DAR CAPACIDAD DE ATENDER A UN PACIENTE SIN TES
			if(view.getId()==R.id.txtPaterno || view.getId()==R.id.txtMaterno || view.getId()==R.id.txtNombre)
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO preguntar si en verdad quiere atender a la persona
						Intent datos = new Intent();
						datos.putExtra(PARAM_ID_PERSONA, "temporal");
						getTargetFragment().onActivityResult(getTargetRequestCode(), 
								RESULT_ATENDER_PACIENTE_SIN_TES, datos);
					}
				});

			return false;
		}
		
	};
	
	//FUNCIONES HEREDADAS DE LoaderManager para el cursor de la consulta
	
	@Override
	public Loader<Cursor> onCreateLoader(int idLoader, Bundle args) {
		String[] projection = null;
    	Uri uri=null;
    	
    	switch(idLoader){
    	case ID_LOADER_CENSO:
    		uri = ProveedorContenido.CENSO_CONTENT_URI;
    		break;
    	}
    	
    	return new CursorLoader(this.getActivity(), uri, projection, null, null, null);
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
