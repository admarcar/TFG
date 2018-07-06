package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import clases.Componente;
import clases.Material;
import clases.Transformacion;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Transformacion_controller{

	@FXML
	private VBox componentes; 
	
	@FXML
	private TextField item;
	
	@FXML
	private TextField coste;
	
	private ObservableList<Transformacion> transformaciones;
	private ObservableList<Material> materiales;
	
	public void anyadir_componente() {
		try {
			Parent componente = FXMLLoader.load(getClass().getResource("../view/componente.fxml"));
			
			// Rellenamos el contenido del ComboBox
			((ComboBox<Material>)((HBox)((HBox)componente).getChildren().get(0)).getChildren().get(1)).setItems(this.materiales);
			
			// Configuramos el spinner
			UI_controller.configurar_spinner(((Spinner<String>)((HBox)((HBox)componente).getChildren().get(1)).getChildren().get(1)), "1");
			
			// Boton de eliminar
			((Button) ((HBox) componente).getChildren().get(2) ).setOnAction((event) ->{
				componentes.getChildren().remove(componente);
			});
			componentes.getChildren().add(componente);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void aceptar() {
		String item = this.item.getText();
		double coste = Double.valueOf(this.coste.getText()).doubleValue();
		List<Componente> componentes = new ArrayList<Componente>();
		for(Node c : this.componentes.getChildren()) {
			String material = ((ComboBox<Material>)((HBox) ((HBox) c).getChildren().get(0)).getChildren().get(1)).getValue().material;
			int cantidad = Integer.valueOf(((Spinner)((HBox) ((HBox) c).getChildren().get(1)).getChildren().get(1)).getEditor().getText()).intValue();
			componentes.add(new Componente(material, cantidad));
		}
		transformaciones.add(new Transformacion(item, coste, componentes));
		((Stage) this.componentes.getScene().getWindow()).close();
	}
	
	public void cancelar() {
		((Stage) componentes.getScene().getWindow()).close();
	}
	
	public void setTransformaciones(ObservableList<Transformacion> transformaciones, ObservableList<Material> materiales) {
		this.transformaciones = transformaciones;
		this.materiales = materiales;
		anyadir_componente();
	}
}
