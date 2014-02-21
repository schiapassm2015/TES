package com.siigs.tes.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Este es un adaptador gen�rico para mapear una lista de objetos gen�ricos contra 
 * widgets TextView que visualizar�n los atributos definidos.
 * @author Axel
 *
 * @param <T>
 */
public class AdaptadorArrayMultiTextView<T> extends ArrayAdapter<T> {

	private Context contexto;
	private int layoutId;
	private List<T> datos;
	
	private String[] bindDeAtributo; //Atributos de <T> que se mapear�n
	private int[] bindIdView; //Id's de objetos de UI que recibir�n los datos de bindDeAtributo[] de T
	
	private ObjectViewBinder<T> miBinder = null;
	
	/**
	 * Crea un adaptador que mapea los atributos bindDeAtributo[] al TextView bindHaciaIdView[] por lo que
	 * ambos arreglos deben tener el mismo tama�o y los atributos bindDeAtributo[] deben existir en
	 * la clase T para ser extra�dos de la lista datos.
	 * 
	 * @param c Contexto donde se ejecuta este adaptador
	 * @param layout Identificador del layout XML a cargar como visualizador de cada elemento
	 * @param datos Origen de los datos a representar
	 * @param bindDeAtributo Lista de atributos de la clase T que se extraer�n para visualizar en 
	 * los view definidos en la lista bindHaciaIdView
	 * @param bindHaciaIdView Lista de Id's de los View en el xml de layout que recibir�n datos desde bindDeAtributo
	 */
	public AdaptadorArrayMultiTextView(Context c, int layout, List<T> datos, String[] bindDeAtributo, int[] bindHaciaIdView ){
		super(c, layout, datos);
		this.contexto = c;
		this.layoutId = layout;
		this.datos = datos;
		this.bindDeAtributo = bindDeAtributo;
		this.bindIdView = bindHaciaIdView;
	}

	public void setViewBinder(ObjectViewBinder<T> binder){this.miBinder = binder;}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View salida = convertView;
		if(salida == null){
			LayoutInflater inflater =LayoutInflater.from(contexto);
			salida = inflater.inflate(this.layoutId, parent, false);	
		}
		
		T elemento = datos.get(position);
		
		for(int i=0; i < this.bindIdView.length; i++){
			String valor="";
			try {
				//Extraemos el valor del atributo requerido
				valor = elemento.getClass().getField(this.bindDeAtributo[i]).get(elemento).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			TextView destino = (TextView) salida.findViewById(this.bindIdView[i]);
			if(miBinder == null || !miBinder.setViewValue(destino, "setText", elemento, bindDeAtributo[i], valor, position))
				destino.setText(valor);
		}

		return salida; //super.getView(position, convertView, parent);
	}

	@Override
	/**
	 * Regresamos lo mismo que getView() pues queremos mostrar mismos datos en caso de un spinner
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
		//return super.getDropDownView(position, convertView, parent);
	}

	@Override
	public T getItem(int position) {
		return datos.get(position);
		//return super.getItem(position);
	}

	@Override
	public int getCount() {
		return datos.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	
	
	
}
