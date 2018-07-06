package entidades;

public class Oferta{
		public String Id;
		public int min_price;
		public int cantidad;
		public String material;
		
		public Oferta(String Id, int min_price, int cantidad, String material){
			this.Id = Id;
			this.min_price = min_price;
			this.cantidad = cantidad;
			this.material = material;
		}
		
		public String toString(){
			return "(ID: " + Id + 
			", min_price: " + min_price + 
			", cantidad: " + cantidad + 
			", material: " + material + ")";
		}
}
