package com.siigs.tes.ui;

import android.view.View;

/**
 * Proporciona un m�todo para modificar el proceso de asignaci�n de un atributo de T a un View
 * en un {@link AdaptadorArrayMultiTextView} y un {@link AdaptadorArrayMultiView}
 * @author Axel
 *
 */
public interface ObjectViewBinder<T> {

	/**
	 * Le comunica al implementador que el adaptador est� a punto de asignar {@link valor]
	 * en {@link viewDestino} para tomar decisi�n de permitirlo, o alterar el resultado de la asignaci�n.
	 * El implementador puede usar <b>viewDestino.getId()</b> para identificar el destino.
	 * 
	 * @param viewDestino El View en UI al que le ser� asignado {@link valor}
	 * @param metodoInvocarDestino El m�todo que adaptador invocar� en {@link viewDestino} para asignarle {@link valor}.
	 * En el caso de un {@link AdaptadorArrayMultiTextView} siempre ser� <b>setText</b> 
	 * @param origen El objeto en la colecci�n de datos del adaptador de donde se extrae {@link valor}
	 * @param atributoOrigen Atributo de {@link origen} que se extrae cuyo valor es el denominado por {@link valor}
	 * @param valor Valor del atributo {@link atributoOrigen} contenido en {@link origen} usado para ser asignado a {@link viewDestino}
	 * @return El implementador debe regresar <b>true</b> para avisar al adaptador que NO debe
	 * asignar �l mismo {@link valor} a {@link viewDestino} pues el implementador lo ha hecho por su cuenta.
	 * Debe regresar <b>false</b> en caso contrario para que el adaptador contin�e con la asignaci�n.
	 */
	public boolean setViewValue(View viewDestino, String metodoInvocarDestino, T origen, String atributoOrigen, Object valor );

}
