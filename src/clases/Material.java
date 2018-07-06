package clases;


public class Material {
	public String material;
	
	public Material(String material) {
		this.material = material;
	}
	
	public String getMaterial() {
		return material;
	}
	
	@Override
	public String toString(){
		return material;
	}
}
