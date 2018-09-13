package com.sciapps.model;

public class Node
{
	private String element;
	private String wavelength;
	private String intensity;

	public Node(String element, String wavelength, String intensity) {
		this.element = element;
		this.wavelength = wavelength;
		this.intensity = intensity;
	}

	public String getElement() {
		return element;
	}

	public String getWavelength() {
		return wavelength;
	}

	public String getIntensity() {
		return intensity;
	}
}
