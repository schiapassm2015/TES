package com.siigs.tes.controles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siigs.tes.SeccionDetailFragment;

/**
 * Helper class que provee contenido para listas de menús en panel izquierdo.
 */
public class ContenidoControles {

	//Todos los items.
	public static List<ItemControl> CONTROLES_TODOS = new ArrayList<ItemControl>();
	//Todos los items por ID.
	public static Map<String, ItemControl> CONTROLES_TODOS_MAP = new HashMap<String, ItemControl>();

	//items de atención.
	public static List<ItemControl> CONTROLES_ATENCION = new ArrayList<ItemControl>();
	//items de atención por ID.
	public static Map<String, ItemControl> CONTROLES_ATENCION_MAP = new HashMap<String, ItemControl>();

	//items de censo.
	public static List<ItemControl> CONTROLES_CENSO = new ArrayList<ItemControl>();
	//items de censo por ID.
	public static Map<String, ItemControl> CONTROLES_CENSO_MAP = new HashMap<String, ItemControl>();
	
	//items de notificaciones.
	public static List<ItemControl> CONTROLES_NOTIFICACIONES = new ArrayList<ItemControl>();
	//items de notificaciones por ID.
	public static Map<String, ItemControl> CONTROLES_NOTIFICACIONES_MAP = new HashMap<String, ItemControl>();
	
	//items de sincronización.
	public static List<ItemControl> CONTROLES_SINCRONIZACION = new ArrayList<ItemControl>();
	//items de sinronización por ID.
	public static Map<String, ItemControl> CONTROLES_SINCRONIZACION_MAP = new HashMap<String, ItemControl>();

	//items de configuración.
	public static List<ItemControl> CONTROLES_CONFIGURACION = new ArrayList<ItemControl>();
	//items de configuracióin por ID.
	public static Map<String, ItemControl> CONTROLES_CONFIGURACION_MAP = new HashMap<String, ItemControl>();
	
	static {		
		CrearControles();
	}

	/**
	 * @author Axel
	 * Crea los arreglos y mapas para los menús de navegación.
	 * Esta función es llamada al crear la aplicación pero debe ser
	 * llamada nuevamente en la funcióin de login y aquí se debe implementar
	 * verificación de roles para solo agregar los menús permitidos.
	 */
	private static void CrearControles(){
		ItemControl item;
		
		//Controles de Atención
		CONTROLES_ATENCION.clear();CONTROLES_ATENCION_MAP.clear();
		item=new ItemControl("101", "Paciente", AtencionPaciente.class);
		addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		item=new ItemControl("102", "Control de Vacunación", SeccionDetailFragment.class);
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
		item=new ItemControl("303", "Reporte de Desempeño", SeccionDetailFragment.class);
		addItem(item, CONTROLES_NOTIFICACIONES, CONTROLES_NOTIFICACIONES_MAP);
		
		//Controles de Sincronización
		CONTROLES_SINCRONIZACION.clear();CONTROLES_SINCRONIZACION_MAP.clear();
		item=new ItemControl("401", "Sincronización", Sincronizacion.class);
		addItem(item, CONTROLES_SINCRONIZACION, CONTROLES_SINCRONIZACION_MAP);
		
		//Controles de Configuracion
		CONTROLES_CONFIGURACION.clear();CONTROLES_CONFIGURACION_MAP.clear();
		item=new ItemControl("501", "Configuración", SeccionDetailFragment.class);
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
	 * A item que representa un Control (elemento de lista) de un menú.
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
