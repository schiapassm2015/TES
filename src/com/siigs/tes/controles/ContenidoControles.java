package com.siigs.tes.controles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siigs.tes.SeccionDetailFragment;

/**
 * Helper class que provee contenido para listas de men�s en panel izquierdo.
 */
public class ContenidoControles {

	//Todos los items.
	public static List<ItemControl> CONTROLES_TODOS = new ArrayList<ItemControl>();
	//Todos los items por ID.
	public static Map<String, ItemControl> CONTROLES_TODOS_MAP = new HashMap<String, ItemControl>();

	//items de atenci�n.
	public static List<ItemControl> CONTROLES_ATENCION = new ArrayList<ItemControl>();
	//items de atenci�n por ID.
	public static Map<String, ItemControl> CONTROLES_ATENCION_MAP = new HashMap<String, ItemControl>();

	//items de censo.
	public static List<ItemControl> CONTROLES_CENSO = new ArrayList<ItemControl>();
	//items de censo por ID.
	public static Map<String, ItemControl> CONTROLES_CENSO_MAP = new HashMap<String, ItemControl>();
	
	//items de notificaciones.
	public static List<ItemControl> CONTROLES_NOTIFICACIONES = new ArrayList<ItemControl>();
	//items de notificaciones por ID.
	public static Map<String, ItemControl> CONTROLES_NOTIFICACIONES_MAP = new HashMap<String, ItemControl>();
	
	//items de sincronizaci�n.
	public static List<ItemControl> CONTROLES_SINCRONIZACION = new ArrayList<ItemControl>();
	//items de sinronizaci�n por ID.
	public static Map<String, ItemControl> CONTROLES_SINCRONIZACION_MAP = new HashMap<String, ItemControl>();

	//items de configuraci�n.
	public static List<ItemControl> CONTROLES_CONFIGURACION = new ArrayList<ItemControl>();
	//items de configuraci�in por ID.
	public static Map<String, ItemControl> CONTROLES_CONFIGURACION_MAP = new HashMap<String, ItemControl>();
	
	static {		
		CrearControles();
	}

	/**
	 * @author Axel
	 * Crea los arreglos y mapas para los men�s de navegaci�n.
	 * Esta funci�n es llamada al crear la aplicaci�n pero debe ser
	 * llamada nuevamente en la funci�in de login y aqu� se debe implementar
	 * verificaci�n de roles para solo agregar los men�s permitidos.
	 */
	private static void CrearControles(){
		ItemControl item;
		
		//Controles de Atenci�n
		CONTROLES_ATENCION.clear();CONTROLES_ATENCION_MAP.clear();
		item=new ItemControl("101", "Paciente", AtencionPaciente.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("102", "Control de Vacunaci�n", SeccionDetailFragment.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("103", "Control Nutricional", SeccionDetailFragment.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("104", "Acciones Nutricionales", SeccionDetailFragment.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("105", "Control IDAs", SeccionDetailFragment.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("106", "Control ERAs", SeccionDetailFragment.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("107", "Control Consultas", SeccionDetailFragment.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		
		//Controles de Censo
		CONTROLES_CENSO.clear();CONTROLES_CENSO_MAP.clear();
		item=new ItemControl("201", "Censo", CensoCensoNominal.class);
		addItem(item, CONTROLES_CENSO, CONTROLES_CENSO_MAP);
		
		//Controles de Notificaciones
		CONTROLES_NOTIFICACIONES.clear();CONTROLES_NOTIFICACIONES_MAP.clear();
		item=new ItemControl("301", "Notificaciones", Notificaciones.class);
		addItem(item, CONTROLES_NOTIFICACIONES, CONTROLES_NOTIFICACIONES_MAP);
		item=new ItemControl("302", "Esquema Incompleto", SeccionDetailFragment.class);
		addItem(item, CONTROLES_NOTIFICACIONES, CONTROLES_NOTIFICACIONES_MAP);
		item=new ItemControl("303", "Reporte de Desempe�o", SeccionDetailFragment.class);
		addItem(item, CONTROLES_NOTIFICACIONES, CONTROLES_NOTIFICACIONES_MAP);
		
		//Controles de Sincronizaci�n
		CONTROLES_SINCRONIZACION.clear();CONTROLES_SINCRONIZACION_MAP.clear();
		item=new ItemControl("401", "Sincronizaci�n", Sincronizacion.class);
		addItem(item, CONTROLES_SINCRONIZACION, CONTROLES_SINCRONIZACION_MAP);
		
		//Controles de Configuracion
		CONTROLES_CONFIGURACION.clear();CONTROLES_CONFIGURACION_MAP.clear();
		item=new ItemControl("501", "Configuraci�n", SeccionDetailFragment.class);
		addItem(item, CONTROLES_CONFIGURACION, CONTROLES_CONFIGURACION_MAP);
	}
	
	private static void addItem(ItemControl item, List<ItemControl> listaDestino, 
			Map<String, ItemControl> mapaDestino) {
		listaDestino.add(item);
		mapaDestino.put(item.id, item);
		
		CONTROLES_TODOS.add(item);
		CONTROLES_TODOS_MAP.put(item.id, item);
	}

	/**
	 * A item que representa un Control (elemento de lista) de un men�.
	 */
	public static class ItemControl {
		public String id;
		public String titulo;
		public Class clase;
		public List<Integer> acciones=new ArrayList<Integer>(); //pendiente a implementar

		public ItemControl(String id, String content, Class clase) {
			this.id = id;
			this.titulo = content;
			this.clase=clase;
		}

		@Override
		public String toString() {
			return titulo;
		}
	}
}
