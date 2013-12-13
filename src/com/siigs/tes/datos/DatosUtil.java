package com.siigs.tes.datos;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class DatosUtil {

	/**
	 * Genera una estructura ContentValues a partir de los atributos NO estáticos de
	 * la instancia del objeto o. Las tuplas se forman como (nombreAtributo, valorAtributo)
	 * @param obj objeto cuyos atributos se convierten a entradas para ContentValues
	 * @return Estructura ContentValues
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static ContentValues ContentValuesDesdeObjeto(Object obj) throws IllegalArgumentException, IllegalAccessException  {
	    ContentValues cv = new ContentValues();

	    for (Field field : obj.getClass().getFields()) {
	    	if( (Modifier.STATIC & field.getModifiers())== Modifier.STATIC)
	    		continue;
	    	
	        Object value = field.get(obj);
	        //check if compatible with contentvalues
	        if( value == null){
	        	cv.put(field.getName(), (String)value);
	        }else if (value instanceof Double || value instanceof Integer || value instanceof String || value instanceof Boolean
	                || value instanceof Long || value instanceof Float || value instanceof Short) {
	            cv.put(field.getName(), value.toString());
	            //Log.d("CVLOOP", field.getName() + ":" + value.toString());
	        } else if (value instanceof Date) {
	            cv.put(field.getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)value));
	        }
	    }
	    return cv;
	}
	
	/**
	 * Crea un arreglo de objetos JSON a partir de los datos del cursor.
	 * Cada objeto en el arreglo representa una fila de resultado del cursor.
	 * @param cur
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray CrearJsonArray(Cursor cur) throws JSONException{
		return CrearJsonArray(cur, new String[]{});
	}
	public static JSONArray CrearJsonArray(Cursor cur, String[] excepciones) throws JSONException{
		return CrearJsonArray(cur, excepciones, new HashMap<String,String>());
	}
	public static JSONArray CrearJsonArray(Cursor cur, String[] excepciones, HashMap<String,String> renombres) throws JSONException{
		if(excepciones == null)excepciones = new String[]{};
		if(renombres == null)renombres = new HashMap<String,String>();
		
		JSONArray salida = new JSONArray();
		while(cur.moveToNext()){
			JSONObject fila = new JSONObject();
			for(String col : cur.getColumnNames()){
				if(Arrays.asList(excepciones).contains(col))
					continue;
				Object nuleable = cur.getString(cur.getColumnIndex(col));
				col = renombres.containsKey(col)? renombres.get(col) : col;
				fila.put(col, nuleable==null? JSONObject.NULL : nuleable);
			}
			salida.put(fila);
		}
		return salida;
	}
	
	
	/**
	 * Regresa la fecha y hora actual en formato 24 horas.
	 * @return String que representa la fecha y hora.
	 */
	public static String getAhora(){
		Calendar cal = Calendar.getInstance();
		String salida= cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+
				"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+
				":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
		return salida;
	}
	
	/**
	 * Regresa la fecha a 0 horas 0 minutos 0 segundos.
	 * @return String que representa la fecha y hora.
	 */
	public static String getHoy(){
		Calendar cal = Calendar.getInstance();
		String salida= cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+
				"-"+cal.get(Calendar.DAY_OF_MONTH)+" 00:00:00";
		return salida;
	}
	
}//fin clase
