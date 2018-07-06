package controller;

import java.io.IOException;
import java.util.List;

import clases.Material;
import clases.Transformacion;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Materiales_controller{
	
	@FXML
	private TableView<Material> table;
	
	@FXML
	private TableColumn<Material, String> material;
	
	private ObservableList<Material> materiales;
	
	public void initialize() {
		material.setCellValueFactory(
				new PropertyValueFactory<Material,String>("material"));
	}

	public void anyadir() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/material.fxml"));
			Parent root = fxmlLoader.load();
			Material_controller controller = fxmlLoader.<Material_controller>getController();
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
	
	public void eliminar() {
		int aux = table.getSelectionModel().getSelectedIndex();
		if(aux != -1) materiales.remove(aux);
	}
	
	public void setTransformaciones(ObservableList<Material> materiales) {
		this.materiales = materiales;
		table.setItems(materiales);
	}
}
