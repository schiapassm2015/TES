package com.siigs.tes.datos.tablas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Alergia {

	public final static String NOMBRE_TABLA = "cns_alergia"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String TIPO = "tipo";
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		TIPO + " TEXT NOT NULL, "+
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String tipo;
	public String descripcion;
	public int activo;
	
	
	public static List<Alergia> getAlergias(Context context){
		Uri uri = ProveedorContenido.ALERGIA_CONTENT_URI;
		Cursor cur = context.getContentResolver().query(uri, null, ACTIVO+"=1", null, null);
		
		List<Alergia> salida = DatosUtil.ObjetosDesdeCursor(cur, Alergia.class);
		cur.close();
		return salida;
	}
	
	public static List<Alergia> getAlergiasEnLista(Context context, List<PersonaAlergia> alergiasDePersona){
		String whereIn = ACTIVO +"=1 and " + ID + " in (";
		for(PersonaAlergia pa : alergiasDePersona)
			whereIn += pa.id_alergia + ",";
		if(alergiasDePersona.size()>0)
			whereIn = whereIn.substring(0, whereIn.length()-1);
		whereIn += ")";
		
		Uri uri = ProveedorContenido.ALERGIA_CONTENT_URI;
		Cursor cur = context.getContentResolver().query(uri, null, whereIn, null, null);
		List<Alergia> salida = new ArrayList<Alergia>();
		salida = DatosUtil.ObjetosDesdeCursor(cur, Alergia.class);
		cur.close();
		return salida;
	}
}
