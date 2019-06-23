package IR;

import java.util.HashMap;

public class Model {
	private int tf;
	private int numOfDocs;
	private float idf;
	private HashMap<Integer , Posting> postingList;
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public int getNumOfDocs() {
		return numOfDocs;
	}
	public void setNumOfDocs(int numOfDocs) {
		this.numOfDocs = numOfDocs;
	}
	public float getIdf() {
		return idf;
	}
	public void setIdf(float idf) {
		this.idf = idf;
	}
	public HashMap<Integer, Posting> getPostingList() {
		return postingList;
	}
	public void setPostingList(HashMap<Integer, Posting> postingList) {
		this.postingList = postingList;
	}
	public Model(int tf, int numOfDocs, float idf, HashMap<Integer, Posting> postingList) {
		super();
		this.tf = tf;
		this.numOfDocs = numOfDocs;
		this.idf = idf;
		this.postingList = postingList;
	}
	public Model() {
	}
	
	
}
