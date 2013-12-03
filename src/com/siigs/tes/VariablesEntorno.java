package com.siigs.tes;

/**
 * Esta clase guarda informaci�n general usada en distintos puntos de la aplicaci�n
 * @author Axel
 *
 */
public class VariablesEntorno {

	/**
	 * URL donde sincronizar el sistema
	 */
	public static String URL_SINCRONIZACION = "http://www.google.com";
	
	/**
	 * Al haber un error de conexi�n hacia {@link URL_SINCRONIZACION}
	 * este valor indica la cantidad de reintentos para lograr conexi�n.
	 */
	public static int REINTENTOS_CONEXION = 3;
	
	/**
	 * El texto correspondiente a este valor se guarda en un arreglo en {@link strings.xml}
	 */
	public static int ID_TIPO_CENSO = 1;
	
	public static void setTipoCenso(int tipoCenso){
		
	}
	
	/**
	 * La unidad m�dica de este dispositivo
	 */
	public static int ID_UNIDAD_MEDICA = 0;
	
	public static void setUnidadMedica(int unidad){
		
	}
}
