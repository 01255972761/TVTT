package IR;

public class Dictionary {
	private int indexDocuments;
	private int frequency;
	public int getIndexDocuments() {
		return indexDocuments;
	}
	public void setIndexDocuments(int indexDocuments) {
		this.indexDocuments = indexDocuments;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public Dictionary(int indexDocuments, int frequency) {
		super();
		this.indexDocuments = indexDocuments;
		this.frequency = frequency;
	}
	public Dictionary() {
	}
	
	
}
