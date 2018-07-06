// Agent log in project proyectoTFG.mas2j

/* Initial beliefs and rules */

ofertas([]).
peticiones([]).

numero_ofertas_satisfechas(0).
numero_peticiones_satisfechas(0).

ganancias_compradores(0).
ganancias_vendedores(0).
contaminacion(0).

/* Initial goals */

!start.

/* Plans */

+!start : true <- {}.

/*
*   Recibe que el manager ha encontrado una coincidencia entre una oferta
*   de un vendedor y una peticion de un comprador
*/
@match[atomic]
+match(oferta(VId, Vend),peticion(CId,Comp))[source(S)] <- 
	-match(oferta(VId, Vend),peticion(CId,Comp))[source(S)]
	?ofertas(O);
	.union(O,[oferta(VId,Vend)], NewO);
	-+ofertas(NewO);
	?peticiones(P);
	.union(P,[peticion(CId,Comp)], NewP);
	-+peticiones(NewP);
	.

/*
*	Recibe que el manager ha terminado de calcular las coincidencias y 
*	que lo agentes empiezan a negociar
*/
@negociar[atomic]
+empezar_negociar[source(So)] <-
	-empezar_negociar[source(So)];
	.print("Inicio de las negociaciones")
	!informar;
	.

/*
*	Recibe que un vendedor ha decidido transformar uno de sus productos
*	por lo tanto, se modifican las ofertas y peticiones con coincidencia
*/
@descompuesto[atomic]
+descompuesto(Id, Matches)[source(V)] <-
	-descompuesto(Matches)[source(V)]
	for(.member(Lista, Matches)){
		for( .member(match(oferta(VId,_,_,_), peticion(CId,_,_,_,_,Comp)), Lista)){
			+match(oferta(VId,V), peticion(CId, Comp));
		}
	}
	.print("El vendedor ", V, " ha decidido descomponer su producto ", Id);
	!informar;
	.
	
/*
*	Recibe que un comprador ha comprado un objeto, modifica las estadisticas
*/
@comprado[atomic]
+comprado(CId, Precio, Ganancias)[source(C)] <-
	-comprado(CId, Precio, Ganancias)[source(C)];
	?numero_peticiones_satisfechas(P);
	-+numero_peticiones_satisfechas(P+1);
	?ganancias_compradores(G);
	-+ganancias_compradores(G+Ganancias);
	.print("El comprador ", C, " ha satisfecho su petición de ", CId, " por un precio de ", Precio);
	!informar;
	.

/*
*	Recibe que un vendedor ha vendido una oferta, modifica las estadisticas
*/
@vendido[atomic]
+vendido(VId, Precio, Ganancias, posicion(XY,YY), posicion(XC,YC))[source(V)] <-
	-vendido(VId, Precio, Ganancias, posicion(XY,YY), posicion(XC,YC))[source(V)];
	?numero_ofertas_satisfechas(O);
	-+numero_ofertas_satisfechas(O+1);
	?ganancias_vendedores(G);
	-+ganancias_vendedores(G+Ganancias);
	?contaminacion(C);
	+x((XC-XY)*(XC-XY));
	+y((YC-YY)*(YC-YY));
	?x(X);
	?y(Y);
	+cont(math.sqrt(X+Y));
	?cont(Cont);
	-+contaminacion(C+Cont);
	-x(_);
	-y(_);
	-cont(_);
	.print("El vendedor ", V, " ha vendido su oferta de ", VId, " por un precio de ", Precio);
	!informar
	.

/*
*	Muestra por pantalla las estadisticas de la situacion actual
*/
+!informar <- 
	?ofertas(O);
	?numero_ofertas_satisfechas(OS);
	.length(O,LO);
	?peticiones(P);
	?numero_peticiones_satisfechas(PS);
	.length(P,LP);
	?ganancias_compradores(GC);
	?ganancias_vendedores(GV);
	?contaminacion(C);
	.print("ofertas vendedores: ", OS, "/", LO,"     peticiones compradores: ", PS, "/", LP,"      ganancias compradores: ", GC, "    ganancias vendedores: ", GV, "    contaminacion: ", C, "\n");                                    
	.	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	