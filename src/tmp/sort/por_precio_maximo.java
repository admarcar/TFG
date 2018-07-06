// Internal action code for project proyectoTFG.mas2j

package sort;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import entidades.*;
import java.util.*;

public class por_precio_maximo extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		Structure o = (Structure) args[0]; // Creencia de la oferta
		ListTerm p = (ListTerm)args[1]; // Creencia de las peticiones con la que existe una coincidencia
		Term solucion = args[2];
		
		// Extraemos la oferta
		Oferta oferta;
		{
		String id = o.getTerm(0).toString();
		int min_price = Integer.parseInt(o.getTerm(1).toString());  
		int cantidad = Integer.parseInt(o.getTerm(2).toString());
		String material = o.getTerm(3).toString();
		oferta = new Oferta(id, min_price, cantidad, material);
		}
		
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
			
			if(oferta.min_price <= max_price){
				Peticion to_add = new Peticion(Id, cantidad, material, max_price, comprador);
				peticiones.add(to_add);
			}
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
			return - (a.max_price - b.max_price);
		}
	}
}



