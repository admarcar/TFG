// Internal action code for project proyectoTFG.mas2j

package sort;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import entidades.*;
import java.util.*;

public class por_distancia extends DefaultInternalAction {
	
	HashMap<String,Posicion> map; // Map: nombre_comprador -> posicion
	Posicion yo; // posicion del vendedor que prioriza
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		Structure o = (Structure) args[0]; // Creencia de la oferta
		ListTerm p = (ListTerm)args[1]; // Creencia de las peticiones con la que existe una coincidencia
		ListTerm pos = (ListTerm)args[2]; // Creencia de las posiciones de los compradores
		NumberTerm x = (NumberTerm)args[3]; // Creencia de la posicion x del vendedor
		NumberTerm y = (NumberTerm)args[4]; // Creencia de la posicion y del vendedor
		Term solucion = args[5];
		yo = new Posicion(x.solve(), y.solve());
		
		
		// Extraemos todas las peticiones
		ArrayList<Peticion> peticiones = new ArrayList<Peticion>();
		for(Term pet : p.getAsList()){
			Structure s = (Structure) pet;
			// Obtenemos una peticion
			String Id = s.getTerm(0).toString();
			int cantidad = Integer.parseInt(s.getTerm(1).toString());  
			String material = s.getTerm(2).toString();
			int max_price = Integer.parseInt(s.getTerm(3).toString());  
			String comprador = s.getTerm(4).toString();
			Peticion to_add = new Peticion(Id, cantidad, material, max_price, comprador);
			peticiones.add(to_add);
		}
		
		// Extraemos las posiciones de los compradores
		map = new HashMap<String,Posicion>();
		for(Term po : pos.getAsList()){
				Structure s = (Structure) po;
				String comprador = s.getTerm(0).toString();
				double xp = Double.parseDouble(s.getTerm(1).toString());
				double yp = Double.parseDouble(s.getTerm(2).toString());
				map.put(comprador, new Posicion(xp,yp));
		}
				
		// Ordenamos
		Collections.sort(peticiones, new Comparador());
		
		// Creamos el resultado
		ListTermImpl result = new ListTermImpl();
		for(Peticion pet : peticiones){
			Structure s = new Structure("peticion");
			s.addTerm(new Atom(pet.Id));
			s.addTerm(new NumberTermImpl(pet.cantidad));
			s.addTerm(new Atom(pet.material));
			s.addTerm(new NumberTermImpl(pet.max_price));
			s.addTerm(new Atom(pet.comprador));
			result.add(s);
		}
		
		// Devolvemos el resultado
        return un.unifies(result, solucion);
    }
	
	private class Comparador implements Comparator<Peticion>{
		public int compare(Peticion a, Peticion b){
			double x_yo = yo.x;
			double y_yo = yo.y;
			double x_a = map.get(a.comprador).x;
			double y_a = map.get(a.comprador).y;
			double x_b = map.get(b.comprador).x;
			double y_b = map.get(b.comprador).y;
			return (int) (manhattan(x_yo, y_yo, x_a, y_a) - manhattan(x_yo, y_yo, x_b, y_b) );
		}
		
		public double manhattan(double x1, double y1, double x2, double y2) {
			return Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
		}
		
	}
}




