package de.lmu.ifi.dbs.elki.algorithm.clustering.ipeaks;

public class Sample {
	private int id;
	private double[] attributes;
	private String label;
	private String predictLabel;
	public Sample(double[] attributes, String label) {
		this.attributes = attributes;
		this.label = label;
	}
	public double[] getAttributes() {
		return attributes;
	}
	public String getLabel() {
		return label;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPredictLabel() {
		return predictLabel;
	}
	public void setPredictLabel(String predictLabel) {
		this.predictLabel = predictLabel;
	}
	
}
