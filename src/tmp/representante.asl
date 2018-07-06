// Agent representante in project proyectoTFG.mas2j

/* Initial beliefs and rules */

contador(0).

/* Initial goals */

!start.

/* Plans */

+!start : true <- {}.

/*
*	Se recibe la peticion que se tiene que conseguir
*/
+peticion(CId,Comprador)[source(M)] <- 
	?contador(C);
	-+contador(C+1);
	if(C==1){
		!negociar;
	}
	.

/*
*	Se recibe la lista de aliados a los que se representa
*/
@aliados[atomic]
+aliados(Alianza)[source(M)] <-
	+pedir(0);
	for(.member(aliado(VId,Vendedor,Pide),Alianza)){
		?pedir(Dinero);
		-+pedir(Dinero + Pide);
	}
	?contador(C);
	-+contador(C+1);
	if(C==1){
		!negociar;
	}
	.

/*
*	Ejecutado cuando se ha recibido la oferta y la lista de aliados
*	Se le hace al comprador la oferta
*/
@negociar[atomic]
+!negociar <-
	-contador(_);
	?peticion(CId,Comp);
	?pedir(Dinero);
	.my_name(Nombre); //Como el representante solo vende un unico producto, se utiliza su nombre tambien como identificador del producto
	.send(Comp,tell,oferta(CId,Nombre,Dinero));
	.

/*
*	El comprador notifica que hay una oferta mejor a la realizada
*	Se disuelve la alianza (el manager notificara a los aliados y podran probar con menos cantidad)
*/
@oferta_actual[atomic]
+oferta_actual(CId,Comprador,Precio,VId)[source(S)] <-
	?aliados(Alianza);
	.send(manager,tell,pide_menos(peticion(CId,Comprador),Alianza));
	.my_name(Name);
	.kill_agent(Name);
	.
	
/*
*	El comprador notifica que la peticion ya ha sido satisfecha
*	Se disuelve la alianza (el manager lo anatorá e informará a los vendedores)
*/
@comprado[atomic]
+comprado(CId,Comprador,VId)[source(S)] <-
	?aliados(Alianza);
	.send(manager,tell,ya_comprado(peticion(CId,Comprador),Alianza));
	.my_name(Name);
	.kill_agent(Name);
	.

/*
*	El comprador compra el producto al que representa
*	Se disuelve la alianza y le informa al manager (lo anatorá e informará a los vendedores)
*/
@comprar[atomic]
+comprar(CId,Comprador,VId,Precio)[source(S)] <-
	?aliados(Alianza);
	.send(manager,tell,conseguido(peticion(CId,Comprador),Alianza));
	.my_name(Name);
	.kill_agent(Name);
	.
	
	
	
	
	
	
	
	
	
