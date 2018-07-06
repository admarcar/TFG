package clases;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;

public class Transformacion {
	public String item;
	public double coste;
	public List<Componente> componentes;
	
	public Transformacion(String item, double coste, List<Componente> componentes) {
		this.item = item;
		this.coste = coste;
		this.componentes = componentes;
	}
	
	public String getItem() {
		return item;
	}
	
	public String getCoste() {
		return coste+"";
	}
	
	public String getComponentes() {
		String res = "";
		for(Componente c : componentes) {
			res += c.cantidad + " unidades de " + c.material + "\n";
		}
		return res;
	}
	
	public String toString() {
		return item;
	}
}
