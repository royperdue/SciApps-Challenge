package com.sciapps.utils;

import com.sciapps.model.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Graph {
    private List<LinkedList<Node>> adjacencyList = new ArrayList<LinkedList<Node>>();
    private LinkedList<Node> elementList = null;
    private List<String> elements = new ArrayList<String>();
    private String referenceElement = "";
    private double maxWaveLength = 0.0;
    private double maxIntensity = 0.0;

    public void buildGraph(String line) {
        String[] data = line.split(",");

        if (!elements.contains(data[0])) {
            elementList = new LinkedList<Node>();

            if (elementList != null)
                adjacencyList.add(elementList);

            elements.add(data[0]);
            elementList.add(new Node(data[0], data[1], data[2]));
        } else {
            elementList = findElementList(data[0]);
            elementList.add(new Node(data[0], data[1], data[2]));
        }
    }

    private LinkedList<Node> findElementList(String element) {
        for (int i = 0; i < adjacencyList.size(); i++) {
            if (adjacencyList.get(i).get(0).equals(element)) {
                elementList = adjacencyList.get(i);
                break;
            }
        }
        return elementList;
    }

    public List<Double> getReferenceIntensities(String element) {
        List<Double> referenceIntensities = new ArrayList<Double>();
        elementList = findElementList(element);

        if (elementList != null) {
            for (int i = 0; i < elementList.size(); i++) {
                if (elementList.get(i).getElement().equals(element)) {
                    referenceIntensities.add(Double.parseDouble(elementList.get(i).getIntensity()));
                }
            }
        }

        return referenceIntensities;
    }

    public boolean containsWaveLength(String wavelength) {
        boolean containsWaveLength = false;

        for (int l = 0; l < adjacencyList.size(); l++) {
            elementList = adjacencyList.get(l);
            for (int i = 0; i < elementList.size(); i++) {
                if (elementList.get(i).getWavelength()
                        .contains(wavelength.substring(0, wavelength.indexOf(".")))) {
                    double roundedWavelength = Math
                            .round(Double.parseDouble(wavelength) * 100.0) / 100.0;
                    double d1 = Double.parseDouble(elementList.get(i).getWavelength());
                    double d2 = roundedWavelength;

                    if (Math.abs(Math.abs(d1) - Math.abs(d2)) < .5) {
                        containsWaveLength = true;
                        referenceElement = elementList.get(i).getElement();
                        break;
                    }
                }
            }
        }

        return containsWaveLength;
    }

    public void addNode(String element, String wavelength, String intensity) {
        System.out.print(element + " " + wavelength + " " + intensity);
        if (Double.parseDouble(wavelength) > maxWaveLength)
            maxWaveLength = Double.parseDouble(wavelength);
        if (Double.parseDouble(wavelength) > maxIntensity)
            maxIntensity = Double.parseDouble(intensity);

        if (!elements.contains(element)) {
            elementList = new LinkedList<Node>();

            if (elementList != null)
                adjacencyList.add(elementList);

            elements.add(element);
            elementList.add(new Node(element, wavelength, intensity));
        } else {
            elementList = findElementList(element);
            elementList.add(new Node(element, wavelength, intensity));
        }
    }

    public int getElementCount(String element) {
        int elements = 0;

        elementList = findElementList(element);
        for (int i = 0; i < elementList.size(); i++) {
            if (elementList.get(i).getElement().equals(element))
                elements++;
        }

        return elements;
    }

    public double getMaxWaveLength() {
        return maxWaveLength;
    }

    public double getMaxIntensity() {
        return maxIntensity;
    }

    public String getReferenceElement() {
        return referenceElement;
    }

    public int getAdjacencyListSize() {
        return adjacencyList.size();
    }

    public List<LinkedList<Node>> getAdjacencyList() {
        return adjacencyList;
    }

    public String toString() {
        String element = "";

        for (int l = 0; l < adjacencyList.size(); l++) {
            elementList = adjacencyList.get(l);
            for (int i = 0; i < elementList.size(); i++) {
                element = element + elementList.get(i).getElement() + " " + elementList.get(i)
                        .getWavelength() + " " + elementList.get(i).getIntensity() + " ";
            }

            element = element + "\n\n";
        }
        return element;
    }
}
