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

public class Transformaciones_controller{
	
	@FXML
	private TableView<Transformacion> table;
	
	@FXML
	private TableColumn<Transformacion, String> item;
	
	@FXML
	private TableColumn<Transformacion, String> precio;
	
	@FXML
	private TableColumn<Transformacion, String> componentes;
	
	private ObservableList<Transformacion> transformaciones;
	private ObservableList<Material> materiales;

	public void initialize() {
		item.setCellValueFactory(
				new PropertyValueFactory<Transformacion,String>("item"));
		precio.setCellValueFactory(
				new PropertyValueFactory<Transformacion,String>("coste"));
		componentes.setCellValueFactory(
				new PropertyValueFactory<Transformacion,String>("componentes"));
		
	}
	
	public void anyadir() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/transformacion.fxml"));
			Parent root = fxmlLoader.load();
			Transformacion_controller controller = fxmlLoader.<Transformacion_controller>getController();
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
	
	public void eliminar() {
		int aux = table.getSelectionModel().getSelectedIndex();
		if(aux != -1) transformaciones.remove(aux);
	}
	
	public void setTransformaciones(ObservableList<Transformacion> transformaciones, ObservableList<Material> materiales) {
		this.transformaciones = transformaciones;
		this.materiales = materiales;
		table.setItems(transformaciones);
	}
}
