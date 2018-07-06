// Internal action code for project proyectoTFG.mas2j

package precio;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class calcular_nuevo_precio extends DefaultInternalAction {

	    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		NumberTerm precio_min = (NumberTerm) args[0];
		NumberTerm precio_max = (NumberTerm) args[1];
		NumberTerm t = (NumberTerm) args[2];
		NumberTerm T = (NumberTerm) args[3];
		NumberTerm beta = (NumberTerm) args[4];
		
		NumberTerm sol = (NumberTerm) args[5];
				
		// Creamos el resultado
		double s = Math.pow(t.solve()/T.solve(),1/beta.solve());
		double res = precio_min.solve() + (1-s)*(precio_max.solve() - precio_min.solve());
		
		// Devolvemos el resultado
        return un.unifies(new NumberTermImpl((int)res), sol);
    }
}
