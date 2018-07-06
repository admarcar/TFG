// Agent Vendedor in project proyectoTFG.mas2j

/* Creencias iniciales */

posicion(0.0, 0.0).
ofertas([oferta_vendedor(deschos,1,100,pet,3)]).
ordenacion(1).
beta(1.0).
coste_contaminacion(0.0).

/* Objetivos iniciales */

!start.

/* Planes */

/*
*  Inicio del agente, se mandan las ofertas al manager
*/
+!start <-
	?ofertas(L);
	!mandar_ofertas_iniciales(L).

/*
*  Mandar 1 oferta del vendedor al manager
*/
+!mandar_ofertas_iniciales([oferta_vendedor(Id,PMin,Cant,Mat, PrecioPartida)|T]) <-
	.send(manager,tell,oferta_vendedor(Id,PMin,Cant,Mat));
	// Seguir mandando
	.length(T,Length);
	if(Length > 0){!mandar_ofertas_iniciales(T);} // Si aun queda por mandar
	else{ // Si todas las ofertas enviadas
		//.print("He enviado todas mis ofertas");
		.send(manager,tell,vendedor_fin);
	}
	.

/*
*  Mandar 1 oferta compuesta del vendedor al manager
*/
+!mandar_ofertas_iniciales([oferta_compuesta_vendedor(Id, Item, Cantidad, DineroTrans, DineroPedir, CDineroEmpezar)|T]) <-
	.send(manager,tell,oferta_compuesta_vendedor(Id, Item, Cantidad, DineroTrans, DineroPedir));
	// Seguir mandando
	.length(T,Length);
	if(Length > 0){!mandar_ofertas_iniciales(T);} // Si aun queda por mandar
	else{ // Si todas las ofertas enviadas
		//.print("He enviado todas mis ofertas");
		.send(manager,tell,vendedor_fin);
	}
	.
	
/*
*  Recibe una coincidencia del manager
*/
@match[atomic]
+match(oferta(VId,VPMin,VCant,VMat), peticion(CId,CCant,CMat,CPMax,Rondas,Comp))[source(M)] <-
	-match(oferta(VId,VPMin,VCant,VMat),peticion(CId,CCant,CMat,CPMax,Comp))[source(M)];
	?ofertas(O);
	.member(oferta_vendedor(VId, VPMin, VCant, VMat, VPrecioPartida),O);
	// Almacenar coincidencia
	.count(matches(oferta(VId,_,_,_,_),_),N);
	if(N == 0){ //Primer match para dicha oferta
		+matches(oferta(VId,VPMin,VCant,VMat,VPrecioPartida),[peticion(CId,CCant,CMat,VPrecioPartida,Comp)]);
	}
	else{ // Ya hay otra coincidencia para esa oferta
		?matches(oferta(VId,VPMin,VCant,VMat,VPrecioPartida),L);
		-matches(oferta(VId,_,_,_,_),_);
		.concat(L,[peticion(CId,CCant,CMat,VPrecioPartida,Comp)],NewL);
		+matches(oferta(VId,VPMin,VCant,VMat,VPrecioPartida),NewL);
		//.print(NewL);
	}
	+peticion_comprador(CId,CCant,CMat,CPMax,1,Rondas,Comp);
	.

/*
*  Recibe una coincidencia de un objeto compuesto del manager
*/
@match_compuesto[atomic]
+match(oferta_compuesta(VId,VItem,VCant,VDineroTrans,VDineroPedir),CosteTransformacion,Matches)[source(M)] <-
	-match(oferta_compuesta(VId,VItem,VCant,VDineroTrans,VDineroPedir),CosteTransformacion,Matches)[source(M)];
	// Se decide si lo acepto o no
	+descomponer(false);
	for(.member(Match,Matches)){
		if(.length(Match,L) & not(L == 0)){
			-+descomponer(true);

		}
	}
	?descomponer(B);
	if(B == true){
		//.print("Se ha descompuesto ", VId, " por ", CosteTransformacion);
		// Se informa al log de que se descompone
		.send(log, tell, descompuesto(VId, Matches));
		?ofertas(O);
		.member(oferta_compuesta_vendedor(VId, VItem, VCant, VDineroTrans, VDineroPedir, CDineroEmpezar), O);
		.delete(oferta_compuesta_vendedor(VId, VItem, VCant, VDineroTrans, VDineroPedir, CDineroEmpezar), O, NuevaO);
		-+ofertas(NuevaO);
		+posicionPrecio(0);
		for(.member(Match,Matches)){
			?posicionPrecio(PosicionPrecio);
			-+posicionPrecio(PosicionPrecio+1);
			if(.length(Match,L) & L > 0){
				// Se genera la nueva oferta
				?ofertas(Ofertas);
				.nth(0,Match,match(oferta(MVId,MVPMin,MVCant,MVMat), _));
				.nth(PosicionPrecio,CDineroEmpezar,Max);
				.concat(Ofertas,[oferta_vendedor(MVId,MVPMin,MVCant,MVMat,Max)],NuevaOfertas);
				-+ofertas(NuevaOfertas);
				// Se anyaden las coincidencias
				for(.member(match(oferta(MVId,MVPMin,MVCant,MVMat), peticion(MCId,MCCant,MCMat,MCPMax,Rondas,MComp)), Match)){
					+match(oferta(MVId,MVPMin,MVCant,MVMat), peticion(MCId,MCCant,MCMat,MCPMax,Rondas,MComp)); // Se lanza la regla asociada
				}
				// Se informa al manager
				.send(manager,tell,oferta_vendedor(MVId,MVPMin,MVCant,MVMat));
			}
		}
		-posicionPrecio(_);
	}
	-descomponer(_);
	.
	
/*
*  Recibe del manager la señal de que todas las coincidencias han sido enviados
*/
@empezar_negociar[atomic]
+empezar_negociacion[source(M)] <-
	-empezar_negociacion[source(M)];
	//.print("Empezar fase de negociación");
	!empezar_negociacion;
	.

/*
*  Empieza la fase de negociacion
*/
+!empezar_negociacion <-
	.findall(VId,matches(oferta(VId,VPMin,VCant,VMat,VPMax),Peticiones),L);
	for(.member(X,L)){
		// Se calcula el mejor comprador con el que negociar
		!determinar_peticion(X); // Crea la peticion ganadora en peticion_ganadora
		// Se negociamos con dicho comprador
		//?peticion_ganadora(CId,CCant,CMat,CPMax,Comp);
		!negociar(X);
	}
	.

/*
*	Objetivo que sirve para determinar que peticion de compra satisfacer para
*	una determinada oferta del vendedor
*/
@determinar[atomic]
+!determinar_peticion(VId) <-
	?matches(oferta(VId,VPMin,VCant,VMat,VPMax),Peticiones);
	?ordenacion(O);
	if(O == 1){
		sort.por_precio_maximo(oferta(VId,VPMin,VCant,VMat),Peticiones,Ordenada);
		//.print(Ordenada);
	}
	elif(O == 2){
		?posiciones(Pos);
		?posicion(X,Y);
		//.print(Peticiones)
		sort.por_distancia(oferta(VId,VPMin,VCant,VMat),Peticiones, Pos, X, Y, Ordenada);
		//.print(Ordenada);
	}
	elif(O == 3){
		?posiciones(Pos);
		?posicion(X,Y);
		?coste_contaminacion(C);
		sort.por_precio_y_distancia(oferta(VId,VPMin,VCant,VMat),Peticiones, Pos, X, Y, C,Ordenada);
	}
	if( .length(Ordenada,N) & N == 0) {+no_peticion_ganadora;}
	else{
		.nth(0,Ordenada, peticion(CId,CCant,CMat,CPMax,Comp));
		-+peticion_ganadora(CId,CCant,CMat,CPMax,Comp);
		.delete(peticion(CId,CCant,CMat,CPMax,Comp),Peticiones,PeticionesRestantes);
		-matches(oferta(VId,VPMin,VCant,VMat,VPMax),Peticiones);
		+matches(oferta(VId,VPMin,VCant,VMat,VPMax),PeticionesRestantes);
	}
	.

/*
*	Empieza la negociacion para vender VId a la peticion ganadora
*/
@negociar[atomic]
+!negociar(VId) <-
	if(no_peticion_ganadora){
		-no_peticion_ganadora;
	}
	else{
		?peticion_ganadora(CId,CCant,CMat,CPMax,Comp);
		-peticion_ganadora(CId,CCant,CMat,CPMax,Comp);
		?matches(oferta(VId,_,VCant,_,_),_);
		if(VCant == CCant){ // Se le pide directamente al comprador
			.send(Comp,tell,oferta(CId,VId,CPMax));
		}
		else{ // Se tiene que formar una alianza, se lo notifica al manager
			.send(manager,tell,formar_alianza(CId,Comp,VId,CPMax));
		}
	}
	.

/*
*	Un comprador/manager notifica que hay una oferta
*	Se guarda y se vuelve a intentar negociar con el producto que no se ha podido vender
*/
@oferta_actual[atomic]
+oferta_actual(CId,Comprador,Precio,VId)[source(S)] <-
	?matches(oferta(VId,VPMin,VCant,VMat,VPMax),Peticiones);
	-oferta_actual(CId,Comprador,Precio,VId)[source(S)]
	?peticion_comprador(CId,CCant,CMat,_,RondaActual,Rondas,Comprador);
	-peticion_comprador(CId,CCant,CMat,_,RondaActual,Rondas,Comprador);
	if(RondaActual <= Rondas-1){
		?beta(Beta);
		precio.calcular_nuevo_precio(VPMin,VPMax,RondaActual+1,Rondas,Beta,NuevoPrecio);
		//.print(NuevoPrecio);
		.concat(Peticiones,[peticion(CId,CCant,CMat,NuevoPrecio,Comprador)],NewPeticiones);
		-matches(oferta(VId,VPMin,_,_,_),Peticiones);
		+matches(oferta(VId,VPMin,VCant,VMat,VPMax),NewPeticiones);
		+peticion_comprador(CId,CCant,CMat,_,RondaActual+1,Rondas,Comprador);
	}
	// else -> nada, la coincidencia ya ha sido eliminada antes de comenzar
	//         la negociación por lo tanto, no se añade de nuevo
	//Negociar
	!determinar_peticion(VId); // Se crea la peticion ganadora en peticion_ganadora
	// Se negocia con dicho comprador
	!negociar(VId);
	.
	
/*
*	El comprador notifica que la peticion ya ha sido satisfecha
*/
@comprado[atomic]
+comprado(CId,Comprador,VId)[source(S)] <-
	-comprado(CId,Comprador,VId)[source(S)];
	// Negociar
	!determinar_peticion(VId); // Se crea la peticion ganadora en peticion_ganadora
	// Se negocia con dicho comprador
	!negociar(VId);
	.

/*
*	El comprador compra unq oferta
*/
@comprar[atomic]
+comprar(CId,Comprador,VId,Precio)[source(S)] <-
	-comprar(CId,Comprador,VId,Precio)[source(S)];
	-matches(oferta(VId,_,_,_),_);
	+vendido(VId,CId,Comprador,Precio);
	//.print("He vendido ", VId, " a ", Comprador, " para satisfacer ", CId, " por un precio de ", Precio);
	
	// Se informamos al log de que se ha vendido
	?ofertas(O);
	.member(oferta_vendedor(VId, PrecioMin,_,_,_),O);
	?posiciones(P);
	.member(posicion(Comprador,XC,YC),P);
	?posicion(XY,YY);
	.send(log, tell, vendido(VId, Precio, Precio-PrecioMin, posicion(XY,YY), posicion(XC,YC)));
	.
	
/*
*	El manager dice que no ha podido formar alianza con una oferta
*/
+alianza_no_posible(oferta(VId,Vendedor,Pide),peticion(CId,Comprador))[source(S)] <-
	-alianza_no_posible(oferta(VId,Vendedor,Pide),peticion(CId,Comprador))[source(S)];
	// Negociar
	!determinar_peticion(VId); // Se crea la peticion ganadora en peticion_ganadora
	// Se negocia con dicho comprador
	!negociar(VId);
	.
