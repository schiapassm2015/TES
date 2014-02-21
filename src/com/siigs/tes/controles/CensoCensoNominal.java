/**
 * 
 */
package com.siigs.tes.controles;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.vistas.Censo;
import com.siigs.tes.datos.vistas.EsquemasIncompletos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Axel
 * Hace una consulta masiva en el censo pudiendo regresar desde pocos datos hasta miles dependiendo
 * del censo manejado en la unidad m�dica. Por tal motivo se implementa un LoaderManager para
 * no bloquear la aplicaci�n mientras se sacan los resultados.
 * 
 * Por seguir nomenclatura "GrupoSubgrupo" el nombre de esta clase
 * se compone de Grupo:Censo, Control:CensoNominal
 */
public class CensoCensoNominal extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = CensoCensoNominal.class.getSimpleName();
	
	//Datos para comunicaci�n externa
	public static final int REQUEST_CODE = 54; //Se usa en caso de avisar que usuario quiere atender un paciente
	public static final int RESULT_ATENDER_PACIENTE_SIN_TES = 111;
	public static final String PARAM_ID_PERSONA = "id_persona";
	
	//Implementamos m�todo para guardar scroll de lista seg�n http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
	private static final String ESTADO_SCROLL_LISTA = "estado_lista";
	private Parcelable estadoListaResultados;
	
	//Datos de filtro para consulta
	private static final String FILTRO_NOMBRE = "filtro_nombre";
	private static final String FILTRO_SEXO = "filtro_sexo";
	private static final String FILTRO_ANO_NACIMIENTO = "filtro_ano_nacimiento";
	private static final String FILTRO_AGEB = "filtro_ageb";
	private static final String FILTRO = "filtro"; //usado para salvar estado de filtroBusqueda
	private Bundle filtroBusqueda=null;
	
	private static final int ID_LOADER_CENSO = 1; //identifica el loader de datos
	
	private ListView lvResultados = null;
	private SimpleCursorAdapter adaptadorCenso = null;
	private ProgressBar pbProgreso = null;
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private boolean esVistaCenso = true; //Puede cambiar a falso si el usuario ve Esquemas Incompletos 
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CensoCensoNominal() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.setRetainInstance(true);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
		
		if(getArguments()!=null && getArguments().containsKey(ContenidoControles.ARG_ICA)
				&& getArguments().getString(ContenidoControles.ARG_ICA).equals(ContenidoControles.ICA_ESQUEMAS_LISTAR+""))
					esVistaCenso = false;
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
		
		//Filtros
		final EditText txtNombre = (EditText)rootView.findViewById(R.id.txtNombre);
		final EditText txtAno = (EditText)rootView.findViewById(R.id.txtAnoNacimiento);
		final EditText txtAgeb = (EditText)rootView.findViewById(R.id.txtAGEB);
	
		//CARGA DE DATOS
		if(savedInstanceState != null && savedInstanceState.containsKey(FILTRO))
			filtroBusqueda = savedInstanceState.getBundle(FILTRO);
		lvResultados = (ListView) rootView.findViewById(R.id.lvResultados);
		lvResultados.setEmptyView(rootView.findViewById(R.id.txtSinResultados));
		pbProgreso = (ProgressBar)rootView.findViewById(R.id.pbProgreso);
		CrearAdaptador();
		LlenarResultados(filtroBusqueda);
		if(savedInstanceState != null && savedInstanceState.containsKey(ESTADO_SCROLL_LISTA))
			estadoListaResultados = savedInstanceState.getParcelable(ESTADO_SCROLL_LISTA);
		
		Button btnFiltrar = (Button) rootView.findViewById(R.id.btnFiltrar);
		btnFiltrar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Datos del filtro
				
				Integer anoNacimiento = null; 
				//Intentamos conseguir n�mero, pero si falla no usamos el dato
				try{ anoNacimiento = Integer.parseInt(txtAno.getText().toString());}catch(Exception e){}
				
				String nombre = txtNombre.getText().toString().trim();
				if(nombre.equals(""))nombre = null;
				
				String sexo = spSexo.getSelectedItem().toString();
				if(sexo.equals(getString(R.string.indistinto)))sexo=null;
				else if(sexo.equals(getString(R.string.masculino)))sexo = Censo.MASCULINO;
				else sexo = Censo.FEMENINO;
				
				String ageb = txtAgeb.getText().toString().trim();
				if(ageb.equals(""))ageb = null;
				
				Bundle filtro = new Bundle();
				if(nombre!=null)filtro.putString(FILTRO_NOMBRE, nombre);
				if(sexo!=null)filtro.putString(FILTRO_SEXO, sexo);
				if(anoNacimiento!=null)filtro.putInt(FILTRO_ANO_NACIMIENTO, anoNacimiento);
				if(ageb != null) filtro.putString(FILTRO_AGEB, ageb);
				
				LlenarResultados(filtro);
			}
		});
		
		Button btnVerTodos = (Button)rootView.findViewById(R.id.btnVerTodos);
		btnVerTodos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Bundle filtroVacio = new Bundle();
				LlenarResultados(filtroVacio);
			}
		});
		
		
		return rootView;
	}
	
	
	private void CrearAdaptador(){
		//Definici�n del adaptador para resultados del censo con Cursor nulo
		String[] bindDeColumna;
		//if(esVistaCenso){ //Este if se comenta pues actualmente Censo y Esquemas usan las mismas columnas ...
			bindDeColumna = new String[]{Censo.NOMBRE_PACIENTE, Censo.APPAT_PACIENTE, Censo.APMAT_PACIENTE,
					Censo.NOMBRE_TUTOR, Censo.APPAT_TUTOR, Censo.APMAT_TUTOR,
					Censo.CALLE_DOMICILIO, Censo.AGEB, Censo.CURP, Censo.FECHA_NACIMIENTO, Censo.SEXO, Censo.BCG,
					Censo.HEPATITIS_1, Censo.HEPATITIS_2, Censo.HEPATITIS_3, Censo.PENTAVALENTE_1,
					Censo.PENTAVALENTE_2, Censo.PENTAVALENTE_3, Censo.PENTAVALENTE_4, Censo.DPT_R, 
					Censo.SRP_1, Censo.SRP_2, Censo.ROTAVIRUS_1, Censo.ROTAVIRUS_2,
					Censo.ROTAVIRUS_3, Censo.NEUMOCOCO_1, Censo.NEUMOCOCO_2, Censo.NEUMOCOCO_3,
					Censo.INFLUENZA_1, Censo.INFLUENZA_2, Censo.INFLUENZA_R};			
		//} //de lo contrario aqu� se deber�a agregar un else if para customizar columnas seg�n caso
			
		int[] bindAview = new int[]			{R.id.txtNombre, R.id.txtPaterno, R.id.txtMaterno,
				R.id.txtNombreTutor, R.id.txtPaternoTutor, R.id.txtMaternoTutor,
				R.id.txtDomicilio, R.id.txtAGEB, R.id.txtCurp, R.id.txtFechaNacimiento, R.id.txtSexo, R.id.txtBCG_U,
				R.id.txtHepatitis_1, R.id.txtHepatitis_2, R.id.txtHepatitis_3, R.id.txtPentavalente_1,
				R.id.txtPentavalente_2, R.id.txtPentavalente_3, R.id.txtPentavalente_4, R.id.txtDPT_R, 
				R.id.txtSRP_1, R.id.txtSRP_2, R.id.txtROTAVIRUS_1, R.id.txtROTAVIRUS_2,
				R.id.txtROTAVIRUS_3, R.id.txtNEUMOCOCO_1, R.id.txtNEUMOCOCO_2, R.id.txtNEUMOCOCO_3,
				R.id.txtINFLUENZA_1, R.id.txtINFLUENZA_2, R.id.txtINFLUENZA_R};
		
		if(adaptadorCenso==null){
			adaptadorCenso = new SimpleCursorAdapter(getActivity(), 
					R.layout.fila_censo_nominal, null, bindDeColumna, bindAview, 0);
			adaptadorCenso.setViewBinder(binderFilaCenso);
		}
		lvResultados.setAdapter(adaptadorCenso);
	}
	
	
	/**
	 * Crear consulta en otro thread con los par�metros recibidos.
	 * Los par�metros tener un Bundle vac�o (sin filtro) � contener uno o m�s criterios de filtro.
	 * @param filtro Par�metros de filtrado encapsulados
	 */
	private void LlenarResultados(Bundle filtro){
		if(filtro == null)return; //Esto solo suceder�a mientras no se soliciten b�squedas
		
		filtroBusqueda = filtro; //As�, incluso en cambio de orientaci�n, sabremos lo �ltimo que se busc�
		
		//Mandamos a crear el cursor en otro thread llamando a onCreateLoader()
		this.getLoaderManager().restartLoader(ID_LOADER_CENSO, filtro, this);
	}
	
	/**
	 * Creamos un binder para capturar/modificar datos al insertarse en UI de una fila de resultado
	 */
	private ViewBinder binderFilaCenso = new ViewBinder(){
		@Override
		public boolean setViewValue(View view, final Cursor cur, int col) {
			//Marcaje de views tipo vacuna
			if(view.getTag()!=null && view.getTag().equals("vacuna")){
				TextView celda = (TextView)view;
				if(cur.isNull(col)){
					celda.setText("");
					//Prevenimos extra�o bug en reuso de views que android hace y desordena los colores en scroll
					if(!esVistaCenso)celda.setBackgroundResource(R.drawable.gradiente_amarillo_borde);
				}else{
					if(esVistaCenso){
						celda.setText(getString(R.string.MARCA_VACUNA));
					}else{
						//Vamos a colorear seg�n la prioridad
						int prioridad = cur.getInt(col);
						switch(prioridad){
						case EsquemaIncompleto.PRIORIDAD_0:
							celda.setBackgroundResource(R.drawable.gradiente_rosa_borde);
							break;
						case EsquemaIncompleto.PRIORIDAD_1:
							celda.setBackgroundResource(R.drawable.gradiente_rojo_borde);
							break;
							//default:
							//	celda.setBackgroundResource(R.drawable.gradiente_naranja);
						}
						celda.setText("");
					}
				}
				return true;
			}

			//Fecha nacimiento
			if(view.getId()==R.id.txtFechaNacimiento){
				((TextView)view).setText(DatosUtil.fechaCorta(cur.getString(col)));
				//Usamos el bind de fecha para asignar color de fondo pero pudo ser otro objeto
				int fondo;
				if(cur.getPosition() % 2 ==0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)view.getParent()).setBackgroundResource(fondo);
				return true;
			}
			
			//Curp
			if(view.getId()==R.id.txtCurp && cur.isNull(col)){
				((TextView)view).setText("");
				return true;
			}
			
			//Domicilio
			if(view.getId()==R.id.txtDomicilio){
				String domicilio = cur.getString(cur.getColumnIndex(Censo.CALLE_DOMICILIO));
				if(!cur.isNull(cur.getColumnIndex(Censo.NUMERO_DOMICILIO)))
					domicilio+=", #"+ cur.getString(cur.getColumnIndex(Censo.NUMERO_DOMICILIO));
				if(!cur.isNull(cur.getColumnIndex(Censo.COLONIA_DOMICILIO)))
					domicilio+=", "+ cur.getString(cur.getColumnIndex(Censo.COLONIA_DOMICILIO));
				if(!cur.isNull(cur.getColumnIndex(Censo.REFERENCIA_DOMICILIO)))
					domicilio+=", "+ cur.getString(cur.getColumnIndex(Censo.REFERENCIA_DOMICILIO));
				((TextView)view).setText(domicilio);
				return true;
			}
			
			//BOT�N PARA DAR CAPACIDAD DE ATENDER A UN PACIENTE SIN TES
			if(view.getId()==R.id.txtPaterno || view.getId()==R.id.txtMaterno || view.getId()==R.id.txtNombre){
				//NOTA: Esta extracci�n de ID_PERSONA se hace aqu� porque en el evento click
				// cur tendr� un contexto distinto y devolver� cualquier valor
				int nColId = esVistaCenso ? 
						cur.getColumnIndex(Censo._ID_PACIENTE) : 
							cur.getColumnIndex(EsquemasIncompletos._ID_PACIENTE);
				final int idPersona = cur.getInt(nColId);
				((LinearLayout)view.getParent()).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//Confirmaci�n
						AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
						dialogo.setMessage("�Desea darle atenci�n al paciente seleccionado?");
						dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
							@Override public void onClick(DialogInterface arg0, int arg1) {}
						});
						dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//Encapsulamos id de la persona
								//int col = cur.
								Intent datos = new Intent();
								datos.putExtra(PARAM_ID_PERSONA, idPersona);
								//Avisamos al target que hemos realizado acci�n de solicitar atenci�n para alguien
								getTargetFragment().onActivityResult(getTargetRequestCode(), 
										RESULT_ATENDER_PACIENTE_SIN_TES, datos);
							}
						} );
						dialogo.show();
					}
				});
			}
			
			return false; //false para que binder siga haciendo su chamba autom�tica
		}
	};
	
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("censo", "guarda");
		outState.putBundle(FILTRO, filtroBusqueda);
		outState.putParcelable(ESTADO_SCROLL_LISTA, lvResultados.onSaveInstanceState());
	}	

	@Override
	public void onResume() {
		super.onResume();
		if(estadoListaResultados!=null){
			lvResultados.onRestoreInstanceState(estadoListaResultados);
			Log.d("censo", "restaura");
		}
	}
	
	
	
	//FUNCIONES HEREDADAS DE LoaderManager para el cursor de la consulta

	@Override
	public Loader<Cursor> onCreateLoader(int idLoader, Bundle args) {    	
		String nombre = args.containsKey(FILTRO_NOMBRE) ? args.getString(FILTRO_NOMBRE) : null;
		Integer ano = args.containsKey(FILTRO_ANO_NACIMIENTO) ? args.getInt(FILTRO_ANO_NACIMIENTO) : null;
		String sexo = args.containsKey(FILTRO_SEXO) ? args.getString(FILTRO_SEXO) : null;
		String ageb = args.containsKey(FILTRO_AGEB) ? args.getString(FILTRO_AGEB) : null;
		
    	switch(idLoader){
    	case ID_LOADER_CENSO:
    		if(pbProgreso!=null)pbProgreso.setVisibility(View.VISIBLE);
    		if(esVistaCenso){
    			return Censo.getCenso(getActivity(), nombre, ano, sexo, ageb);
    		}else{
    			return EsquemasIncompletos.getEsquemasIncompletos(getActivity(), nombre, ano, sexo, ageb);
    		}
    	}
    	return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(loader.getId()== ID_LOADER_CENSO){
			//El thread ha terminado de crear un cursor con los datos,
        	//ahora el adaptador que actualmente contiene 0 datos actualiza
        	//su cursor lo cual escuchar� el ListView y 
        	//visualizar� los datos.
			this.adaptadorCenso.swapCursor(cursor);
			if(pbProgreso!=null)pbProgreso.setVisibility(View.GONE);
			Toast.makeText(getActivity(), "Se encontraron "+cursor.getCount()+" coincidencias", Toast.LENGTH_LONG).show();
			if(estadoListaResultados!=null){
				try{lvResultados.onRestoreInstanceState(estadoListaResultados);}catch(Exception e){}
				estadoListaResultados=null;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if(loader.getId() == ID_LOADER_CENSO)
			this.adaptadorCenso.swapCursor(null);
		
	}
}//fin clase
