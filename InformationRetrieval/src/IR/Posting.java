package IR;

public class Posting {
	private int tf;
	private float w;
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public float getW() {
		return w;
	}
	public void setW(float w) {
		this.w = w;
	}
	public Posting(int tf, float w) {
		super();
		this.tf = tf;
		this.w = w;
	}
	public Posting() {
	}
	
	
}
