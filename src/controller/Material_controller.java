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
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Material_controller{
	
	@FXML
	private TextField material;
	
	private ObservableList<Material> materiales;
	
	public void initialize() {
		
	}
	
	public void aceptar() {
		String material = this.material.getText();
		materiales.add(new Material(material));
		((Stage) this.material.getScene().getWindow()).close();
	}
	
	public void cancelar() {
		((Stage) material.getScene().getWindow()).close();
	}
	
	public void setTransformaciones(ObservableList<Material> materiales) {
		this.materiales = materiales;
	}
}
