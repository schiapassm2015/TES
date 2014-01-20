package com.siigs.tes.ui;

import android.view.View;

/**
 * Proporciona un método para modificar el proceso de asignación de un atributo de T a un View
 * en un {@link AdaptadorArrayMultiTextView} y un {@link AdaptadorArrayMultiView}
 * @author Axel
 *
 */
public interface ObjectViewBinder<T> {

	/**
	 * Le comunica al implementador que el adaptador está a punto de asignar {@link valor]
	 * en {@link viewDestino} para tomar decisión de permitirlo, o alterar el resultado de la asignación.
	 * El implementador puede usar <b>viewDestino.getId()</b> para identificar el destino.
	 * 
	 * @param viewDestino El View en UI al que le será asignado {@link valor}
	 * @param metodoInvocarDestino El método que adaptador invocará en {@link viewDestino} para asignarle {@link valor}.
	 * En el caso de un {@link AdaptadorArrayMultiTextView} siempre será <b>setText</b> 
	 * @param origen El objeto en la colección de datos del adaptador de donde se extrae {@link valor}
	 * @param atributoOrigen Atributo de {@link origen} que se extrae cuyo valor es el denominado por {@link valor}
	 * @param valor Valor del atributo {@link atributoOrigen} contenido en {@link origen} usado para ser asignado a {@link viewDestino}
	 * @return El implementador debe regresar <b>true</b> para avisar al adaptador que NO debe
	 * asignar él mismo {@link valor} a {@link viewDestino} pues el implementador lo ha hecho por su cuenta.
	 * Debe regresar <b>false</b> en caso contrario para que el adaptador continúe con la asignación.
	 */
	public boolean setViewValue(View viewDestino, String metodoInvocarDestino, T origen, String atributoOrigen, Object valor );

}
