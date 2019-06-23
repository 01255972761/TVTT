package IR;

public class Evaluation {
	private float precision;
	private float recall;
	public float getPrecision() {
		return precision;
	}
	public void setPrecision(float precision) {
		this.precision = precision;
	}
	public float getRecall() {
		return recall;
	}
	public void setRecall(float recall) {
		this.recall = recall;
	}
	public Evaluation(float precision, float recall) {
		super();
		this.precision = precision;
		this.recall = recall;
	}
	public Evaluation() {
	}
}
