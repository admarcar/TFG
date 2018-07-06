// Agent manager in project proyectoTFG.mas2j

/* Creencias iniciales */

waiting_time().

peticiones_comprador([]).
ofertas_vendedor([]).
numero_vendedores().
numero_compradores().
alianza_id(0).
posiciones([]).

transformaciones().

/* Objetivos iniciales */

!start.

/* Planes */

/*
*  Inicio del agente, no hace nada
*/
+!start <- {}.

/*
*  Se recibe la posicion de un comprador, la almacenamos
*/
@posicion[atomic]
+posicion(X,Y)[source(Comp)] <-
	?posiciones(P);
	.concat(P, [posicion(Comp,X,Y)], NuevaP);
	-+posiciones(NuevaP);
	-posicion(X,Y)[source(Comp)];
	.
	
	
/*
*  Se recibe una petición inicial de un comprador, la almacenamos
*/
@peticion_comprador[atomic]
+peticion_comprador(Id,Cant,Mat,PMax,Rondas)[source(Comp)] <-
	// Se añade la nueva petición a la lista
	?peticiones_comprador(L);
	.concat(L,[peticion(Id,Cant,Mat,PMax,Rondas,Comp)],NuevaL);
	-+peticiones_comprador(NuevaL);
	-peticion_comprador(Id,Cant,Mat,PMax,Rondas)[source(Comp)];
	+peticiones_alianza(peticion(Id,Comp),[]); //Creamos la estructura donde se añadiran los posibles aliados
	.

/*
*  El comprador indica al manager que ya ha mandado todas sus peticiones
*/
@comprador_fin[atomic]
+comprador_fin[source(Comp)] <-
	?numero_compradores(N);
	-+numero_compradores(N-1);
	-comprador_fin[source(Comp)];
	if(not .desire(comprobar_todo_recibido)){!comprobar_todo_recibido}; // Se añadimos el deseo de comprobar si no se ha añadido ya
	.


/*
*  Se recibe una oferta inicial de un vendedor, la almacenamos
*/	
@oferta_vendedor[atomic]
+oferta_vendedor(Id,PMin,Cant,Mat)[source(Vend)] <-
	// Se añade la nueva oferta a la lista
	?ofertas_vendedor(L);
	.concat(L,[oferta(Id,PMin,Cant,Mat,Vend)],NuevaL);
	-+ofertas_vendedor(NuevaL);
	-oferta_vendedor(Id,PMin,Cant,Mat)[source(Vend)];
	.

/*
*  Se recibe una oferta compuesta inicial de un vendedor, la almacenamos
*/	
@oferta_compuesta_vendedor[atomic]
+oferta_compuesta_vendedor(Id, Item, Cantidad, DineroTrans, DineroPedir)[source(Vend)] <-
	//Añadimos la nueva oferta a la lista
	?ofertas_vendedor(L);
	.concat(L,[oferta_compuesta(Id,Item,Cantidad,DineroTrans,DineroPedir,Vend)],NuevaL);
	-+ofertas_vendedor(NuevaL);
	-oferta_compuesta_vendedor(Id, Item, Cantidad, DineroTrans, DineroPedir)[source(Vend)];
	.
	
/*
*  El comprador indica al manager que ya ha mandado todas sus peticiones
*/
@vendedor_fin[atomic]
+vendedor_fin[source(Vend)] <-
	?numero_vendedores(N);
	-+numero_vendedores(N-1);
	-vendedor_fin[source(Vend)];
	if(not .desire(comprobar_todo_recibido)){!comprobar_todo_recibido}; // Se añadimos el deseo de comprobar si no se ha añadido ya
	.

/*
*  Cuando ya se ha recibido todo de los vendedores y compradores, se empieza a mandar los matches
*/
+!comprobar_todo_recibido <-
	?numero_vendedores(V);
	?numero_compradores(C);
	if(V == 0 & C == 0){
		-numero_vendedores(_);
		-numero_compradores(_);
		//.print("Calculando matches");
		!mandar_macthes;
	}
	.
	
/*
*  Se empiazan a mandar los matches conforme se calculen
*/
+!mandar_macthes <-
	?peticiones_comprador(ListaComprador);
	?ofertas_vendedor(ListaVendedor);
	// Listas auxiliares para comunicar a los agentes que ya pueden empezar a negociar
	+vendedores([]);
	+compradores([]);
	// Se calculan los matches de productos simples
	for( .member(peticion(CId,CCant,CMat,CPMax,Rondas,Comp), ListaComprador) ){
		for( .member(oferta(VId,VPMin,VCant,VMat,Vend), ListaVendedor) ){
			//Productos simples
			if(VMat == CMat){ //Si la oferta y la petición es del mismo material
				if(VCant <= CCant){ //Si el vendedor tiene justo lo que pide o menos
					//Mandar al vendedor
					.send(Vend,tell,match(oferta(VId,VPMin,VCant,VMat), peticion(CId,CCant,CMat,CPMax,Rondas,Comp)));
					.send(log, tell, match(oferta(VId, Vend),peticion(CId,Comp)));
				}
				else{//El comprador pide menos de lo que se oferta
					//CASO NO CONTEMPLADO 
				}
			}
			// Se guarda al vendedor
			?vendedores(LVend);
			.union(LVend,[Vend],NLVend);
			-+vendedores(NLVend);
		}
		// Se guarda al comprador
		?compradores(LComp);
		.union(LComp,[Comp],NLComp);
		-+compradores(NLComp);
	}
	// Se le indica al log muestre los matches hasta el momento
	.send(log,tell,empezar_negociar);
	// Se calculan los matches de los compuestos
	?transformaciones(T);
	for( .member(oferta_compuesta(VId,VItem,VCant,VDineroTrans,VDineroPedir,Vend), ListaVendedor) ){ // nSe recorre todas las ofertas compuestas
		if(.member(transformacion(VItem,Componentes,TDineroTrans),T) & VDineroTrans >= TDineroTrans*VCant){ // Por cada una, se comprueba si existe transformacion y si se puede pagar
			+matches([]);
			.length(Componentes, ComponentesL);
			for(.range(I,0,ComponentesL-1)){ // Se recorre el indice de los componentes
				+matches_componente([]);
				.nth(I,Componentes,componente(Material,Cantidad)); // Se obtiene el componente
				.nth(I,VDineroPedir,DineroMinimoComponente); // Se obtene el precio a pedir
				for(.member(peticion(CId,CCant,CMat,CPMax,Rondas,Comp), ListaComprador) ){ // Se calculan los matches para cada componente
					if(Material == CMat & CCant >= Cantidad*VCant){
						.concat(VId,Material,NombreS); // Se genera el nombre del nuevo componente
						.term2string(Nombre, NombreS);
						?matches_componente(MatchesComponentes);
						.concat(MatchesComponentes,
						[match(oferta(Nombre,DineroMinimoComponente,Cantidad*VCant,Material), peticion(CId,CCant,CMat,CPMax,Rondas,Comp))]
						,MC);
						-+matches_componente(MC);
					}
				}
				?matches(Matches);
				?matches_componente(MatchesComponentes);
				.concat(Matches,[MatchesComponentes],M);
				-+matches(M);
				-matches_componente(_);
			}
			?matches(Matches);
			// Se le informa al vendedor
			.send(Vend,tell,
				match(oferta_compuesta(VId,VItem,VCant,VDineroTrans,VDineroPedir), // oferta original
				TDineroTrans*VCant, // Precio de la transformacion
				Matches // Matches por cada componente
			));
			-matches(_);
		}
		?vendedores(LVend);
		.union(LVend,[Vend],NLVend);
		-+vendedores(NLVend);
	}
	?vendedores(LVend);
	?compradores(LComp);
	?posiciones(Pos);
	// Se mandan la posicion de los compradores a todos los vendedores
	.send(LVend, tell, posiciones(Pos));
	// Se le indica a los compradores y vendedores para que pasen a la fase de negociacion
	.send(LVend,tell,empezar_negociacion);
	.send(LComp,tell,empezar_negociacion);
	-vendedores(_);
	-compradores(_);
	//.print("Empezar a negociar");
	.
	
	
/*
*	Se crea una peticion para formar una alianza
*/
@formar_alianza[atomic]
+formar_alianza(CId,Comprador,VId,Pide)[source(Vendedor)] <-
	-formar_alianza(CId,Comprador,VId,Pide)[source(Vendedor)];
	//.print(CId,Comprador,VId,Pide,Vendedor);
	// Comprobar si ya satisfecha
	if(comprado(CId,Comprador)){
		.send(Vendedor,tell,comprado(CId,Comprador,VId));
	}
	else{
		// Se obtenienen todos los vendedores que han pedido formar alianza para ese (CId,Comprador)
		?peticiones_alianza(peticion(CId,Comprador),Aliados);
		-peticiones_alianza(peticion(CId,Comprador),_);
		// Se añade el nuevo vendedor a dicha lista
		.concat(Aliados,[aliado(VId,Vendedor,Pide)],NewAliados);
		+peticiones_alianza(peticion(CId,Comprador),NewAliados);
		// Se crea la estructura donde se guardaran todas las alianzas en las que participa
		+aliado_de(oferta(VId,Vendedor,Pide),peticion(CId,Comprador),[]);
		// Timeout, si en un tiempo no forma parte de ninguna alianza, no se esta interesado en participar en ninguna otra
		?waiting_time(T);
		.concat("+!comprobar_si_aliado(",CId,",",Comprador,",",VId,",",Vendedor,",",Pide,")",Event);
		.concat("now +", T, " seconds", Time)
		.at(Time,Event);
		
		// Formar alianza
		// devuelve la lista de combinaciones en alianzas([alianza,alianza...])
		// alianza -> [aliado(VId,Vendedor,Pide)]
		+alianzas([]);
		!backtracking_aliados(peticion(CId,Comprador),Aliados,[aliado(VId,Vendedor,Pide)]);
		?alianzas(Alianzas);
		-alianzas(_);
		for(.member(Alianza,Alianzas)){
			// Se crea el nombre de la alianza
			?alianza_id(Id);
			-+alianza_id(Id+1);
			.concat("Representante",Id,Name);
			// Se crea al representante
			.create_agent(Name,"representante.asl");
			.send(Name,tell,peticion(CId,Comprador)); // Peticion que tiene que conseguir
			.send(Name,tell,aliados(Alianza)); // Miembos
			// Se guarda la alianza en la que ahora forma parte cada aliado
			for(.member(aliado(AliadoVId,AliadoVendedor,AliadoVPrecio),Alianza)){
				?aliado_de(oferta(AliadoId,AliadoVendedor,AliadoVPrecio),peticion(CId,Comprador),FormandoParte);
				.concat(FormandoParte,[Name],NewFormandoParte);
				-aliado_de(oferta(AliadoId,AliadoVendedor,AliadoVPrecio),peticion(CId,Comprador),_);
				+aliado_de(oferta(AliadoId,AliadoVendedor,AliadoVPrecio),peticion(CId,Comprador),NewFormandoParte);
				if(not ha_sido_aliado(oferta(AliadoId,AliadoVendedor,AliadoVPrecio),peticion(CId,Comprador))){
					+ha_sido_aliado(oferta(AliadoId,AliadoVendedor,AliadoVPrecio),peticion(CId,Comprador));
				}
			}
		}
	}
	.
	
/*
*	Devuelve en alianzas([alianza,alianza...]) donde alianza -> [aliado(VId,Vendedor,Pide)]
*	todas las posibles combinaciones de alianzas en las que participe el aliado
*/
@backtracking[atomic]
+!backtracking_aliados(peticion(CId,Comprador),Aliados,Combinacion) <-
	// Se calcula la cantidad restante para que la combinacion sea una alianza
	+cant_aux(0);
	?ofertas_vendedor(OV);
	?peticiones_comprador(PC);
	.member(peticion(CId,Cant,Mat,PMax,Rondas,Comprador),PC);
	for(.member(aliado(CombId, CombVend, CombPide),Combinacion)){
		?cant_aux(Cantidad);
		.member(oferta(CombId,PMin,CombCant,Mat,CombVend),OV);
		-+cant_aux(Cantidad+CombCant);
	}
	?cant_aux(CantidadFinal);
	-cant_aux(_);
	if(CantidadFinal == Cant){ // La combinacion ya es final
		?alianzas(Alianzas);
		.concat(Alianzas,[Combinacion],NewAlianzas);
		-+alianzas(NewAlianzas);
	}
	else{ // Hay que seguir combinando
		.length(Aliados,Len);
		if(Len \== 0){ // Se coge al siguiente de la lista y se introduce si se puede
			.nth(0,Aliados,aliado(ProcesarId,ProcesarVendedor,ProcesarPide));
			.delete(0,Aliados,AliadosProcesados);
			
			.member(oferta(ProcesarId,_,ProcesarCant,ProcesarMat,ProcesarVendedor),OV);
			if(CantidadFinal + ProcesarCant <= Cant){//No nos pasamos de la cantidad deseada
				.concat(Combinacion,[aliado(ProcesarId,ProcesarVendedor,ProcesarPide)],NuevaCombinacion);
				// Se sigue combinando
				!backtracking_aliados(peticion(CId,Comprador),AliadosProcesados,NuevaCombinacion);
			}
			
			// El siguiente no se introduce y seguimos combinando
			!backtracking_aliados(peticion(CId,Comprador),AliadosProcesados,Combinacion);
		}
	}
	.
	
/*
*	El representante ha conseguido vender para satisfacer CId de Comprador
*	notificar a los aliados si han conseguido o no vender
*/
@conseguido[atomic]
+conseguido(peticion(CId,Comprador),Alianza)[source(Representante)] <-
	-conseguido(peticion(CId,Comprador),Alianza)[source(Representante)];
	+comprado(CId,Comprador);
	// Se notifican a los miemrbos de la alianza
	for(.member(aliado(VId,Vendedor,Precio),Alianza)){
		.send(Vendedor,tell,comprar(CId,Comprador,VId,Precio));
	}
	
	// Se Notifica al resto de miembros
	?peticiones_alianza(peticion(CId,Comprador),Aliados);
	for( .member(aliado(VId,Vendedor,_),Aliados) & not .member(aliado(VId,Vendedor,_),Alianza) ){
		.send(Vendedor,tell,comprado(CId,Comprador,VId));
	}
	.

/*
*	Una alianza notifica al manager que se ha disuelto porque no puede rebajar
*	el precio que tiene fijado por sus aliados
*/
@pide_menos[atomic]
+pide_menos(peticion(CId,Comprador),Alianza)[source(Representante)] <-
	-pide_menos(peticion(CId,Comprador),Alianza)[source(Representante)];
	// Se recorren los aliados que forman la alianza rota
	for(.member(aliado(VId,Vendedor,VPrecio),Alianza)){
		// Se guarda que dicho vendedor no forma parte de la alianza
		?aliado_de(oferta(VId,Vendedor,VPrecio),peticion(CId,Comprador),FormandoParte);
		.delete(S,FormandoParte,NewFormandoParte);
		-aliado_de(oferta(VId,Vendedor,VPrecio),peticion(CId,Comprador),_);
		// Si una aliado no forma parte de ninguna otra alianza activa se le notifica
		.length(NewFormandoParte,N); 
		if(N == 0){
			.send(Vendedor,tell,oferta_actual(CId,Comprador,VPrecio,VId));
			// Se elimina tambien de la lista de posibles aliados
			?peticiones_alianza(peticion(CId,Comprador),PosiblesAliados);
			.delete(aliado(VId,Vendedor,_),PosiblesAliados,NewPosiblesAliados);
			-peticiones_alianza(peticion(CId,Comprador),_);
			+peticiones_alianza(peticion(CId,Comprador),NewPosiblesAliados);
		}
		else{
			+aliado_de(oferta(VId,Vendedor,VPrecio),peticion(CId,Comprador),NewFormandoParte);
		}
	}
	.
	
/*
*	Si un aliado no ha formado parte de ninguna alianza hasta el momento
*	se le notifica y ya no formara parte de ninguna mas
*/
@comprobar_si_aliado[atomic]
+!comprobar_si_aliado(CId,Comprador,VId,Vendedor,Pide) <-
	if(ha_sido_aliado(oferta(VId,Vendedor,Pide),peticion(CId,Comprador))){
		// Nada
	}
	else{ // No ha formado parte de ninguna alianza
		// Se borra como posible aliado
		?peticiones_alianza(peticion(CId,Comprador),Aliados);
		-peticiones_alianza(peticion(CId,Comprador),_);
		.delete(aliado(VId,Vendedor,Pide),Aliados,NewAliados);
		+peticiones_alianza(peticion(CId,Comprador),NewAliados);
		// Se borra la estructura para guardar las alianzas ya que nunca ha sido usada
		-aliado_de(oferta(VId,Vendedor,Pide),peticion(CId,Comprador),[])
		// Notificar al vendedor del fracaso
		.send(Vendedor,tell,alianza_no_posible(oferta(VId,Vendedor,Pide),peticion(CId,Comprador)));
	}
	.