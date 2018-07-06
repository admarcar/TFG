// Agent comprador in project proyectoTFG.mas2j

/* Creencias iniciales */

waiting_time(2).

posicion(0.0, 0.0).
peticiones([peticion_comprador(plastico,500,pet,20,5)]).

/* Objetivos iniciales */

!start.

/* Planes */

/*
*  Inicio del agente, empezamos a mandar las peticiones al manager
*/
+!start <-
	?peticiones(L);
	?posicion(X,Y);
	.send(manager,tell,posicion(X,Y));
	!mandar_peticiones_iniciales(L).

/*
*  Mandar 1 petición del comprador al manager
*/
+!mandar_peticiones_iniciales([H|T]) <-
	.send(manager,tell,H);
	.length(T,Length);
	if(Length > 0){!mandar_peticiones_iniciales(T);} //Si aun queda por mandar
	else{ //Si todas las peticiones enviadas
		//.print("He enviado todas mis peticiones");
		.send(manager,tell,comprador_fin);
	}
	.
	
/*
*  Recibe del manager la señal de que todos los matches han sido enviados
*/
+empezar_negociacion[source(M)] <-
	-empezar_negociacion[source(M)];
	//.print("Empezar fase de negociación");
	// No se realizada nada ya que las negociaciones las inicia el vendedor
	.
	
/*
*  Recibe oferta para que el comprador compre el producto VId, que satisfacerá la peticion GId,
*  pagando Precio
*/
@oferta[atomic]
+oferta(CId,VId,Precio)[source(Vendedor)] <-
	-oferta(CId,VId,Precio)[source(Vendedor)];
	.my_name(Name);
	if(comprado(CId,_)){
		//Notificar que el producto ha sido vendido
		.send(Vendedor, tell, comprado(CId,Name,VId));
	}
	else{
		//.print(CId,VId,Precio);
		.findall(oferta_actual(CId,OtroVId,OtroVendedor,OtroPrecio),oferta_actual(CId,OtroVId,OtroVendedor,OtroPrecio),L);
		//.print(L);
		.length(L,Num);
		if(Num == 0){//Primera oferta
			?peticiones(Pet);
			.member(peticion_comprador(CId,_,_,MaxDinero,_),Pet);
			if(MaxDinero < Precio){ // El comprador no puede pagar tanto
				// se leinforma al vendedor del fracaso
				.send(Vendedor,tell,oferta_actual(CId,Name,Precio,VId));
			}
			else{ // El comprador si que se puede permitir pagar eso
				+oferta_actual(CId,VId,Vendedor,Precio);
				//Lanzar timeout para comprar
				.concat("+!timeout(",CId,",",Precio,",",VId,",",Vendedor,")",Event);
				?waiting_time(Time);
				.concat("now +",Time," seconds",Timing);
				.at(Timing,Event);
			}
		}
		else{//Ya existe otra oferta
			.nth(0,L,oferta_actual(CId,OtroVId,OtroVendedor,OtroPrecio));
			if(Precio < OtroPrecio){// El comprador prefiere la nueva oferta
				-oferta_actual(CId,_,_,_);
				+oferta_actual(CId,VId,Vendedor,Precio);
				// Se le comunica al vendedor de antes que hay una oferta mejor
				.send(OtroVendedor,tell,oferta_actual(CId,Name,Precio,OtroVId));
				// Lanzar timeout para comprar
				.concat("+!timeout(",CId,",",Precio,",",VId,",",Vendedor,")",Event);
				?waiting_time(Time);
				.concat("now +",Time," seconds",Timing);
				.at(Timing,Event);
			}
			else{// Se manteniene la oferta previa
				// Se le comunica al vendedor que ha hecho la peticion que hay una oferta igual o mejor
				.send(Vendedor,tell,oferta_actual(CId,Name,Precio,VId));
			}
		}
	}
	.
	
/*
*   Timeout creado para si pasado un tiempo sin recibir ninguna oferta mejor, comprar la mejor oferta actual
*/
@timeout[atomic]
+!timeout(CId,Precio,VId,Vendedor) <-
	if(oferta_actual(CId,VId,Vendedor,Precio)){
		+comprado(CId, Vendedor);
		-oferta_actual(CId,VId,Vendedor,Precio);
		//Proceder a la compra
		.my_name(Name);
		.send(Vendedor,tell,comprar(CId,Name,VId,Precio));
		
		// Informamos al log de la compra
		?peticiones(P);
		.member(peticion_comprador(CId, _,_,PrecioMax,_),P);
		.send(log, tell, comprado(CId, Precio, PrecioMax-Precio));
	}
	.
	
	
	
	
	
	
	
	
	
