package entidades;
public class Peticion{
		public String Id;
		public int cantidad;
		public String material;
		public int max_price;
		public String comprador;
		
		public Peticion(String Id, int cantidad, String material, int max_price, String comprador){
			this.Id = Id;
			this.cantidad = cantidad;
			this.material = material;
			this.max_price = max_price;
			this.comprador = comprador;
		}
		
		public String toString(){
			return "(Id: " + Id +
			", cantidad: " + cantidad +
			", material: " + material + 
			", max_price: " + max_price + ")";
		}
}
