package com.siigs.tes.datos;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;


public class DatosUtil {
	private static String TAG = DatosUtil.class.getSimpleName();

	/**
	 * Genera una estructura ContentValues a partir de los atributos NO estáticos de
	 * la instancia del objeto obj. Las tuplas se forman como (nombreAtributo, valorAtributo)
	 * Esta función es útil para generar datos de inserción en base de datos desde la instancia de un objeto
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
	 * Construye una instancia de la Clase {@clase} y asigna sus campos públicos NO estáticos
	 * con los valores contenidos en el Cursor {@cur}. Para esto tanto los campos a asignar
	 * en clase como los campos en el registro de cur deben tener los mismos nombres.  
	 * @param cur Cursor que ya debe apuntar al registro que se desea convertir
	 * @param clase
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> T ObjetoDesdeCursor(Cursor cur, Class clase) throws InstantiationException, IllegalAccessException{
		T salida = (T)clase.newInstance();
		
		for (Field field : salida.getClass().getFields()) {
	    	if( (Modifier.STATIC & field.getModifiers())== Modifier.STATIC)
	    		continue;

	    	int index = cur.getColumnIndex(field.getName());
	    	switch(cur.getType(index)){
	    	case Cursor.FIELD_TYPE_STRING:
	    		field.set(salida, cur.getString(index));
	    		break;
	    	case Cursor.FIELD_TYPE_INTEGER:
	    		field.set(salida, cur.getInt(index));
	    		break;
	    	case Cursor.FIELD_TYPE_NULL:
	    		field.set(salida, null);
	    		break;
	    	case Cursor.FIELD_TYPE_FLOAT: //NO EXISTÍA DOUBLE ASÍ QUE USAMOS FLOAT
	    		field.setDouble(salida, cur.getDouble(index));
	    		break;
	    	default:
	    		throw new IllegalAccessException("No se reconoce el tipo de columna "+cur.getType(index)+" para el campo "+field.getName()+" de la clase "+clase.getName());
	    	}
	    }//fin ciclo
		
		return salida;
	}
	
	/**
	 * Genera dinámicamente una lista de instancias de Clase {@clase} a partir
	 * del Cursor cur.
	 * @param cur
	 * @param clase
	 * @return
	 */
	public static <T> List<T> ObjetosDesdeCursor(Cursor cur, Class<?> clase){
		List<T> salida = new ArrayList<T>();
		try {
			while(cur.moveToNext())
				salida.add((T) ObjetoDesdeCursor(cur,clase));
		} catch (InstantiationException e) {
			Log.d(TAG, "No fue posible inicializar dinámicamente variable de tipo "+clase.getName()+":"+e);
		} catch (IllegalAccessException e) {
			Log.d(TAG, "No fue posible accesar a propiedades dinámicas en clase tipo "+clase.getName()+":"+e);
		}
		return salida;
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
	 * Convierte obj en su representación JSON.
	 * @param obj
	 * @return
	 */
	public static String CrearStringJson(Object obj){
		Gson gson = new Gson();
		return gson.toJson(obj);
	}
	
	
	/**
	 * Regresa la fecha y hora actual en formato 24 horas.
	 * @return String que representa la fecha y hora.
	 */
	public static String getAhora(){
		Calendar cal = Calendar.getInstance();
		
		String mes = (cal.get(Calendar.MONTH)+1)+"";
		if(mes.length()==1)mes="0"+mes;
		
		String dia = cal.get(Calendar.DAY_OF_MONTH)+"";
		if(dia.length()==1)dia="0"+dia;
		
		String hora = cal.get(Calendar.HOUR_OF_DAY)+"";
		if(hora.length()==1)hora="0"+hora;
		
		String minuto= cal.get(Calendar.MINUTE)+"";
		if(minuto.length()==1)minuto="0"+minuto;
		
		String segundo= cal.get(Calendar.SECOND)+"";
		if(segundo.length()==1)segundo="0"+segundo;
		
		String salida= cal.get(Calendar.YEAR)+"-" + mes +
				"-" + dia + " " + hora + ":" + minuto + ":" + segundo;
		return salida;
	}
	
	/**
	 * Regresa la fecha a 0 horas 0 minutos 0 segundos.
	 * @return String que representa la fecha y hora.
	 */
	public static String getHoy(){
		Calendar cal = Calendar.getInstance();
		
		String mes = (cal.get(Calendar.MONTH)+1)+"";
		if(mes.length()==1)mes="0"+mes;
		
		String dia = cal.get(Calendar.DAY_OF_MONTH)+"";
		if(dia.length()==1)dia="0"+dia;
		
		String salida= cal.get(Calendar.YEAR) + "-" + mes +
				"-" + dia + " 00:00:00";
		return salida;
	}
	
	/**
	 * Calcula la edad basado en la fecha ingresada
	 * @param fechaNacimiento Fecha en formato aaaa-mm-dd
	 * @return
	 */
	public static String calcularEdad(String fechaNacimiento){
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
	
	/**
	 * Confierte la fecha recibida al estílo día(numérico)-Mes(3 letras)-Año(numérico)
	 * @param fecha Fecha en formato numérico aaaa-mm-dd
	 * @return Fecha legible en formato dd-MMM-aaaa
	 */
	public static String fechaCorta(String fecha){
		//TODO implementar
		return "";
	}
	
}//fin clase
