package com.sciapps.model;

public class Node
{
	private String element = "";
	private double wavelength;
	private double intensity;
	private double referenceIntensity;
	private double normalizedIntensity;
	private int icon = 0;
	private int lines = 0;

	public Node() {
	}

	public Node(double wavelength, double intensity) {
		this.wavelength = wavelength;
		this.intensity = intensity;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public double getWavelength() {
		return wavelength;
	}

	public void setWavelength(double wavelength) {
		this.wavelength = wavelength;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public double getReferenceIntensity() {
		return referenceIntensity;
	}

	public void setReferenceIntensity(double referenceIntensity) {
		this.referenceIntensity = referenceIntensity;
	}

	public double getNormalizedIntensity() {
		return normalizedIntensity;
	}

	public void setNormalizedIntensity(double normalizedIntensity) {
		this.normalizedIntensity = normalizedIntensity;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}
	
	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}
}
