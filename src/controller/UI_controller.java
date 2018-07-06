package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import clases.Componente;
import clases.Transformacion;
import clases.Material;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UI_controller{
	
	@FXML
	private VBox sellers; 
	
	@FXML
	private VBox buyers; 
	
	@FXML
	private Spinner<String> tiempo_espera;
	
	private ObservableList<Transformacion> transformaciones;
	
	private ObservableList<Material> materiales;
	
	public void initialize() {
		transformaciones = FXCollections.observableArrayList();
		materiales = FXCollections.observableArrayList();
		configurar_spinner(tiempo_espera, "10");
	}
	
	public void abrir_transformaciones(){
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/transformaciones.fxml"));
			Parent root = fxmlLoader.load();
			Transformaciones_controller controller = fxmlLoader.<Transformaciones_controller>getController();
			controller.setTransformaciones(transformaciones, materiales);
			Stage stage = new Stage();
	        Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../css/application.css").toExternalForm());
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
	        stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void abrir_materiales(){
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/materiales.fxml"));
			Parent root = fxmlLoader.load();
			Materiales_controller controller = fxmlLoader.<Materiales_controller>getController();
			controller.setTransformaciones(materiales);
			Stage stage = new Stage();
	        Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../css/application.css").toExternalForm());
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
	        stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){		
		int compradores = 0;
		int vendedores = 0;
		String agents = "";
		
		// Limpiamos cualquier posible ejecucion previa
		limpiar();
		
		// Copiamos todo el codigo java de funciones internas adicionales usadas por los agentes
		copiarDir("entidades");
		copiarDir("sort");
		copiarDir("precio");
		
		// Cargamos en memoria el comprador, vendedor y representante
		String comprador = cargar_agente(new File("src/agents/comprador.asl"));
		String vendedor = cargar_agente(new File("src/agents/vendedor.asl"));
		
		// Obtenemos el tiempo de espera
		int espera = Integer.parseInt(tiempo_espera.getEditor().getText());
		
		// Generar los sellers
		for(Node s : sellers.getChildrenUnmodifiable()) {
			VBox seller = (VBox) s;
			String name = ((TextField) ((HBox) seller.getChildren().get(0)).getChildren().get(1)).getText();
			double x = Double.parseDouble(((TextField)((HBox)((VBox) ((HBox) seller.getChildren().get(0)).getChildren().get(2)).getChildren().get(0)).getChildren().get(1)).getText());
			double y = Double.parseDouble(((TextField)((HBox)((VBox) ((HBox) seller.getChildren().get(0)).getChildren().get(2)).getChildren().get(1)).getChildren().get(1)).getText());
			int ordenacion = 1+((ComboBox) ((VBox)((HBox)seller.getChildren().get(0)).getChildren().get(5)).getChildren().get(1)).getSelectionModel().getSelectedIndex(); // indice del metodo de ordenacion
			double beta = Double.parseDouble(((TextField) ((VBox)((HBox)seller.getChildren().get(0)).getChildren().get(7)).getChildren().get(1)).getText());
			int numero = Integer.parseInt(((Spinner) ((VBox)((HBox)seller.getChildren().get(0)).getChildren().get(8)).getChildren().get(1)).getEditor().getText()); // numero agentes
			List<String> beliefs = new ArrayList<String>();
			
			double contaminacion = 0;
			if(((ComboBox) ((VBox)((HBox)seller.getChildren().get(0)).getChildren().get(5)).getChildren().get(1)).getSelectionModel().getSelectedIndex() == 2) {
				contaminacion = Double.parseDouble(((TextField) ((VBox)((HBox)seller.getChildren().get(0)).getChildren().get(6)).getChildren().get(1)).getText());
			}
			
			// Extraemos los atributos
			VBox items = ((VBox) seller.getChildren().get(1));
			for(Node i : items.getChildren()) {
				if(i.getId().equals("item")) {
					HBox item = (HBox) i;
					String oferta = "oferta_vendedor(";
					oferta += ((TextField)((VBox) item.getChildrenUnmodifiable().get(0)).getChildrenUnmodifiable().get(1)).getText() + ","; // id
					oferta += ((Spinner)((VBox) item.getChildrenUnmodifiable().get(1)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ","; // precio min
					oferta += ((Spinner)((VBox) item.getChildrenUnmodifiable().get(2)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ","; // cantidad
					oferta += ((ComboBox<Material>)((VBox) item.getChildrenUnmodifiable().get(3)).getChildrenUnmodifiable().get(1)).getValue().material + ","; // material
					oferta += ((Spinner)((VBox) item.getChildrenUnmodifiable().get(4)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ")"; // precio partida
					beliefs.add(oferta);
				}
				else {
					HBox compuesto = ((HBox)((VBox) i).getChildren().get(0));
					String oferta = "oferta_compuesta_vendedor(";
					oferta += ((TextField)((VBox) compuesto.getChildrenUnmodifiable().get(0)).getChildrenUnmodifiable().get(1)).getText() + ","; // id
					oferta += ((ComboBox<Transformacion>)((VBox) compuesto.getChildrenUnmodifiable().get(1)).getChildrenUnmodifiable().get(1)).getValue().item + ","; // item
					oferta += ((Spinner)((VBox) compuesto.getChildrenUnmodifiable().get(2)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ","; // cantidad
					oferta += ((Spinner)((VBox) compuesto.getChildrenUnmodifiable().get(3)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ","; // precio trans
					// PRECIO MIN COMPONENTES
					oferta += "[";
					FlowPane flow = ((FlowPane)((VBox) i).getChildren().get(1));
					boolean first = true;
					for(Node n : flow.getChildren()) {
						if(first) first = false;
						else oferta += ",";
						oferta += ((Spinner)((HBox) n).getChildren().get(2)).getEditor().getText();
					}
					oferta += "],";
					// PRECIO PARTIDA COMPONENTES
					oferta += "[";
					first = true;
					for(Node n : flow.getChildren()) {
						if(first) first = false;
						else oferta += ",";
						oferta += ((Spinner)((HBox) n).getChildren().get(4)).getEditor().getText();
					}
					oferta += "])";
					beliefs.add(oferta);
				}
			}
			
			// Obtenemos la descripcion del agente vendedor anyadir en el mas2j
			agents += generar_vendedor(vendedor, beliefs, name, numero, x, y, ordenacion, beta, contaminacion);
			vendedores += numero;
		}
		
		// Generar los buyers
		for(Node s : buyers.getChildrenUnmodifiable()) {
			VBox seller = (VBox) s;
			String name = ((TextField) ((HBox) seller.getChildren().get(0)).getChildren().get(1)).getText();
			double x = Double.parseDouble(((TextField)((HBox)((VBox) ((HBox) seller.getChildren().get(0)).getChildren().get(2)).getChildren().get(0)).getChildren().get(1)).getText());
			double y = Double.parseDouble(((TextField)((HBox)((VBox) ((HBox) seller.getChildren().get(0)).getChildren().get(2)).getChildren().get(1)).getChildren().get(1)).getText());
			int numero = Integer.parseInt(((Spinner) ((VBox)((HBox)seller.getChildren().get(0)).getChildren().get(5)).getChildren().get(1)).getEditor().getText()); // numero agentes
			List<String> beliefs = new ArrayList<String>();
			
			// Extraemos los atributos
			VBox items = ((VBox) seller.getChildren().get(1));
			for(Node i : items.getChildren()) {
				HBox item = (HBox) i;
				String peticion = "peticion_comprador(";
				peticion += ((TextField)((VBox) item.getChildrenUnmodifiable().get(0)).getChildrenUnmodifiable().get(1)).getText() + ",";// id
				peticion += ((Spinner)((VBox) item.getChildrenUnmodifiable().get(1)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ","; // cantidad
				peticion += ((ComboBox<Material>)((VBox) item.getChildrenUnmodifiable().get(2)).getChildrenUnmodifiable().get(1)).getValue().material + ",";// material
				peticion += ((Spinner)((VBox) item.getChildrenUnmodifiable().get(3)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ","; // rondas
				peticion += ((Spinner)((VBox) item.getChildrenUnmodifiable().get(4)).getChildrenUnmodifiable().get(1)).getEditor().getText() + ")"; // precio max
				beliefs.add(peticion);
			}
			
			// Obtenemos la descripcion del agente comprador anyadir en el mas2j
			agents += generar_comprador(comprador, beliefs, name, numero, x, y, espera); // -1 no usado 
			compradores += numero;
		}

		// Copiar el representante y log
		copiar_representante(cargar_agente(new File("src/agents/representante.asl")));
		copiar_log(cargar_agente(new File("src/agents/log.asl")));
		
		// Generamos al manager
		copiar_manager(cargar_agente(new File("src/agents/manager.asl")), vendedores, compradores, espera, transformaciones);
		
		// Generar el mas2j
		generar_mas2j(cargar_agente(new File("src/agents/proyectoTFG.mas2j")), agents);
		
		// Ejecutamos
		ejecutar();
	}
	
	public void add_seller_item(Parent vendedor) {
		VBox pane = ((VBox) ((VBox) vendedor).getChildren().get(1)); // Contenedor de items
		try {
			Parent item = FXMLLoader.load(getClass().getResource("../view/item_vendedor.fxml"));
			
			// Configuramos el spinner del precio minimo
			configurar_spinner( ((Spinner<String>)((VBox)((HBox)item).getChildren().get(1)).getChildren().get(1)),"1");
			
			// Configuramos el spinner de la cantidad
			configurar_spinner( ((Spinner<String>)((VBox)((HBox)item).getChildren().get(2)).getChildren().get(1)),"1");
			
			//Rellenamos el contenido del ComboBox
			((ComboBox<Material>)((VBox)((HBox)item).getChildren().get(3)).getChildren().get(1)).setItems(this.materiales);
			
			// Configuramos el spinner del precio de partida
			configurar_spinner( ((Spinner<String>)((VBox)((HBox)item).getChildren().get(4)).getChildren().get(1)),"1");
			
			// Boton de eliminar
			((Button) ((HBox) item).getChildren().get(5) ).setOnAction((event) ->{
				pane.getChildren().remove(item);
				if(pane.getChildren().size() == 0) { // Si no hay items, borramos al vendedor
					sellers.getChildren().remove(vendedor);
				}
			});
			pane.getChildren().add(item);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add_seller_compuesto(Parent vendedor) {
		VBox pane = ((VBox) ((VBox) vendedor).getChildren().get(1)); // Contenedor de items
		try {
			Parent item = FXMLLoader.load(getClass().getResource("../view/compuesto_vendedor.fxml"));
			
			// Configuramos los spiners
			UI_controller.configurar_spinner(((Spinner<String>)((VBox) ((HBox) ((VBox)item).getChildren().get(0)).getChildren().get(2)).getChildren().get(1)), "1"); // Cantidad
			UI_controller.configurar_spinner(((Spinner<String>)((VBox) ((HBox) ((VBox)item).getChildren().get(0)).getChildren().get(3)).getChildren().get(1)), "1"); // Precio transformacio
			
			// Configuramos el desplegable del item
			// Valor
			((ComboBox<Transformacion>)((VBox) ((HBox) ((VBox)item).getChildren().get(0)).getChildren().get(1)).getChildren().get(1)).setItems(this.transformaciones);
			// Lo que debe mostrar al clickar
			((ComboBox<Transformacion>)((VBox) ((HBox) ((VBox)item).getChildren().get(0)).getChildren().get(1)).getChildren().get(1)).setCellFactory(
					new Callback<ListView<Transformacion>, ListCell<Transformacion>>(){
						@Override
						public ListCell<Transformacion> call(ListView<Transformacion> param) {
							final ListCell<Transformacion> cell = new ListCell<Transformacion>() {
								{
									super.setPrefWidth(200);
								}
								public void updateItem(Transformacion item, boolean empty) {
									super.updateItem(item, empty);
									if(item != null) {
										String componentes = "";
										for(Componente c : item.componentes) {
											componentes+= "\t" + c.cantidad + " de " + c.material + "\n";
										}
										setText(item.item + " por un coste de " + item.coste + "\n"
												 + "se producirá por cada unidad:\n" + componentes);
									}
								}
							};
							return cell;
						}
					});
			// El comportamiento
			FlowPane flow = ((FlowPane) ((VBox)item).getChildren().get(1));
			((ComboBox<Transformacion>)((VBox) ((HBox) ((VBox)item).getChildren().get(0)).getChildren().get(1)).getChildren().get(1)).getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
				flow.getChildren().clear();
				for(Componente c : newValue.componentes) {
					try {
						Parent comp = FXMLLoader.load(getClass().getResource("../view/precio_componente.fxml"));
						((Label) ((HBox) (comp)).getChildren().get(0)).setText(c.cantidad + " de " + c.material + " por cada unidad");
						UI_controller.configurar_spinner(((Spinner<String>) ((HBox) (comp)).getChildren().get(2)), "1");
						UI_controller.configurar_spinner(((Spinner<String>) ((HBox) (comp)).getChildren().get(4)), "1");
						flow.getChildren().add(comp);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			// Boton de eliminar
			((Button) ((HBox) ((VBox)item).getChildren().get(0)).getChildren().get(4) ).setOnAction((event) ->{
				pane.getChildren().remove(item);
				if(pane.getChildren().size() == 0) { // Si no hay items, borramos al vendedor
					sellers.getChildren().remove(vendedor);
				}
			});
			pane.getChildren().add(item);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add_seller() throws IOException {
		Parent vendedor = FXMLLoader.load(getClass().getResource("../view/vendedor.fxml"));
		
		// Boton de añadir item
		((Button) ((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(3)).getChildren().get(0)).setOnAction((event) ->{
			add_seller_item(vendedor);		
		});
		
		// Boton de añadir compuesto
			((Button) ((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(3)).getChildren().get(1)).setOnAction((event) ->{
				add_seller_compuesto(vendedor);		
			});
		
		// Boton de eliminar vendedor
		((Button) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(4)).setOnAction((event) ->{
			sellers.getChildren().remove(vendedor);
		});
		
		// Populamos el desplegable de los metodos de ordenacion
		ObservableList<String> items = FXCollections.observableArrayList();
		items.addAll("por precio maximo","por distancia", "por precio y distancia");
		((ComboBox)((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(5)).getChildren().get(1)).setItems(items);
		((ComboBox)((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(5)).getChildren().get(1)).getSelectionModel().selectFirst();
		
		// Comportamiento del desplegable
		((ComboBox<String>)((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(5)).getChildren().get(1)).getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) ->{
			if(newValue.equals("por precio y distancia")) {
				((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(6)).setManaged(true);
				for(Node e : ((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(6)).getChildren()) {
					e.setVisible(true);
				}
			}
			else{
				((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(6)).setManaged(false);
				for(Node e : ((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(6)).getChildren()) {
					e.setVisible(false);
				}
			}
		});
		
		// Configuramos el spinner de agentes
		configurar_spinner((Spinner<String>)((VBox) ((HBox) ((VBox) vendedor).getChildren().get(0)).getChildren().get(8)).getChildren().get(1), "1");
		
		sellers.getChildren().add(vendedor);
	}
	
	public void add_buyer_item(Parent comprador) {
		VBox pane = ((VBox) ((VBox) comprador).getChildren().get(1)); // Contenedor de items
		try {
			Parent item = FXMLLoader.load(getClass().getResource("../view/item_comprador.fxml"));
								
			// Configuramos el spinner de la cantidad
			configurar_spinner( ((Spinner<String>)((VBox)((HBox)item).getChildren().get(1)).getChildren().get(1)),"1");
			
			//Rellenamos el contenido del ComboBox
			((ComboBox<Material>)((VBox)((HBox)item).getChildren().get(2)).getChildren().get(1)).setItems(this.materiales);
			
			// Configuramos el spinner del precio minimo
			configurar_spinner( ((Spinner<String>)((VBox)((HBox)item).getChildren().get(3)).getChildren().get(1)),"1");
						
			// Configuramos el spinner de las rondas
			configurar_spinner( ((Spinner<String>)((VBox)((HBox)item).getChildren().get(4)).getChildren().get(1)),"1");
			
			// Boton de eliminar
			((Button) ((HBox) item).getChildren().get(5) ).setOnAction((event) ->{
				pane.getChildren().remove(item);
				if(pane.getChildren().size() == 0) { // Si no hay items, borramos al comprador
					buyers.getChildren().remove(comprador);
				}
			});
			pane.getChildren().add(item);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add_buyer() throws IOException {
		Parent comprador = FXMLLoader.load(getClass().getResource("../view/comprador.fxml"));
		
		// Boton de añadir item
		((Button) ((HBox) ((VBox) comprador).getChildren().get(0)).getChildren().get(3)).setOnAction((event) ->{
			add_buyer_item(comprador);		
		});
		
		// Boton de eliminar vendedor
		((Button) ((HBox) ((VBox) comprador).getChildren().get(0)).getChildren().get(4)).setOnAction((event) ->{
			buyers.getChildren().remove(comprador);
		});
		
		// Configuramos el spinner de agentes
		configurar_spinner((Spinner<String>)((VBox) ((HBox) ((VBox) comprador).getChildren().get(0)).getChildren().get(5)).getChildren().get(1), "1");
		
		add_buyer_item(comprador); // Por defecto añadimos uno
		buyers.getChildren().add(comprador);
	}
	
	public static void configurar_spinner(Spinner<String> spinner, String ini) {
		SpinnerValueFactory<String> aux = new SpinnerValueFactory<String>(){
			
			@Override
			public void decrement(int steps) {
				int val = Integer.valueOf(this.getValue()).intValue()-steps;
				if(val >= 0) this.setValue(val+"");
				else this.setValue("0");
			}

			@Override
			public void increment(int steps) {
				this.setValue((Integer.valueOf(this.getValue()).intValue()+steps)+"");
			}
			
		};
		aux.setValue(ini);
		spinner.setValueFactory(aux);
	}
	
	void copiarDir(String nombre) {
		File java = new File("src/agents/" + nombre + "/");
		try {
			Files.createDirectories(new File("src/tmp/" + nombre + "/").toPath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(File f : java.listFiles()) {
			try {
				Files.copy(f.toPath(), new File("src/tmp/" + nombre + "/" + f.getName()).toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	// Devuelve los vendedores que deben ir en el mas2j
	private String generar_vendedor(String codigo, List<String> beliefs, String identificador, int cant, double x, double y, int ordenacion, double beta, double contaminacion) {
		String res = "";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/"+identificador+".asl"));
			Scanner read = new Scanner(codigo);
			while(read.hasNextLine()) {
				String line = read.nextLine();
				if(line.startsWith("ofertas().")) {
					line = "ofertas([";
					for(String belief : beliefs) {
						line += belief + ",";
					}
					line = line.substring(0,line.length()-1);
					line += "]).";
				}
				else if(line.startsWith("ordenacion().")) {
					line = "ordenacion(" + ordenacion + ").";
				}
				else if(line.startsWith("posicion().")){
					line = "posicion(" + x + ", " + y + ").";
				}
				else if(line.startsWith("beta().")){
					line = "beta(" + beta + ").";
				}
				else if(line.startsWith("coste_contaminacion().")){
					line = "coste_contaminacion(" + contaminacion + ").";
				}
				writer.write(line + "\n");
			}
			writer.close();
			res += "        " +  identificador + " #" + cant + ";\n"; 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	// Devuelve los agentes que deben ir en el mas2j
	private String generar_comprador(String codigo, List<String> beliefs, String identificador, int cant, double x, double y, int espera) {
		String res = "";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/"+identificador+".asl"));
			Scanner read = new Scanner(codigo);
			while(read.hasNextLine()) {
				String line = read.nextLine();
				boolean add_beliefs = false;
				if(line.startsWith("peticiones().")) {
					line = "peticiones([";
					for(String belief : beliefs) {
						line += belief + ",";
					}
					line = line.substring(0,line.length()-1);
					line += "]).";
				}
				else if(line.startsWith("waiting_time().")) {
					line = "waiting_time(" + espera + ").";
				}
				else if(line.startsWith("posicion().")){
					line = "posicion(" + x + ", " + y + ").";
				}
				writer.write(line + "\n");
			}
			writer.close();
			res += "        " +  identificador + " #" + cant + ";\n"; 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	private void copiar_representante(String codigo) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/representante.asl"));
			writer.write(codigo);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void copiar_log(String codigo) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/log.asl"));
			writer.write(codigo);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void copiar_manager(String codigo, int vendedores, int compradores, int espera, List<Transformacion> transformaciones) {
		String aux;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/manager.asl"));
			Scanner read = new Scanner(codigo);
			while(read.hasNextLine()) {
				aux = read.nextLine();
				if(aux.startsWith("numero_vendedores().")) {
					writer.write("numero_vendedores("+vendedores+").");
				}
				else if(aux.startsWith("numero_compradores().")) {
					writer.write("numero_compradores("+compradores+").");
				}
				else if(aux.startsWith("waiting_time().")) {
					writer.write("waiting_time(" + espera + ").");
				}
				else if(aux.startsWith("transformaciones().")) {
					String t = "[";
					for(Transformacion trans : transformaciones) {
						String comp = "[";
						for(Componente c : trans.componentes) {
							comp += "componente(" + c.material + "," + c.cantidad + "),";
						}
						if(comp.charAt(comp.length()-1) == ',') comp = comp.substring(0, comp.length()-1); 
						comp += "]";
						t += "transformacion(" + trans.item + "," + comp  + "," + trans.coste + "),";
					}
					if(t.charAt(t.length()-1) == ',') t = t.substring(0, t.length()-1); 
					t += "]";
					writer.write("transformaciones(" + t + ").");
				}
				else writer.write(aux);
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void limpiar() {
		File folder = new File("src/tmp");
		if(folder.exists()) {
			deleteDir(folder);
		}
		folder.mkdir();
	}
	
	private void ejecutar() {
		execute_command("src/scripts/mas2j src/tmp/proyecto.mas2j");
		new Thread() {
			public void run() {
				execute_command("ant -f src/tmp/bin/build.xml");
			}
		}.start();
	}
	
	private void generar_mas2j(String plantilla, String agentes) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/proyecto.mas2j"));
			//TODO: añadir las creencias
			writer.write(plantilla);
			writer.write(agentes);
			writer.write("        " + "manager #1;\n");
			writer.write("}");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void execute_command(String command) {
		Runtime rt = Runtime.getRuntime();
		try {
			Process pc = rt.exec(command);			
			BufferedReader stdInput = new BufferedReader(new 
				     InputStreamReader(pc.getInputStream()));

				BufferedReader stdError = new BufferedReader(new 
				     InputStreamReader(pc.getErrorStream()));

				// read the output from the command
				System.out.println("Here is the standard output of the command:\n");
				String s = null;
				while ((s = stdInput.readLine()) != null) {
				    System.out.println(s);
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String cargar_agente(File agente) {
		String res = "";
		try {
			Scanner read = new Scanner(agente);
			while(read.hasNextLine()) {
				res += read.nextLine() + "\n";
			}
			read.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
}
