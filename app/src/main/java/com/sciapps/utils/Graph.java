package com.sciapps.utils;

import com.sciapps.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/*
 * 1. Changed the adjacency list to a hashMap that contains arrayLists and strings for keys. This allows
 *   for direct access to any value by way of the key and is faster as a result of less iterating. Less loops
 *   also makes for shorter, neater classes.
 *
 * 2. Added a method called percentDifference which takes doubles as parameters and determines if a wavelength
 *   is <= a specified value. With this method I was able to parse all of the strings to doubles once at the
 *   beginning as opposed to converting back and forth between double and string. This reduces processing time
 *   and makes for cleaner code. In addition it is now possible to specify the amount of variation that's allowed
 *   between a value and the reference value for the two to still be considered a match.
 *
 *
 * */
public class Graph {
    private Map<String, List<Node>> adjacencyList = new HashMap<String, List<Node>>();
    private List<Node> elementList = null;
    private String referenceElement = "";
    private double referenceIntensity = 0.0;


    public void buildGraph(String element, String w, String i) {
        double wavelength = parse(w);
        double intensity = parse(i);

        addNode(element, new Node(wavelength, intensity));
    }

    public void addNode(String element, Node node) {
        if (!adjacencyList.containsKey(element)) {
            elementList = new ArrayList<Node>();
            elementList.add(node);
            adjacencyList.put(element, elementList);
        } else {
            elementList = adjacencyList.get(element);
            elementList.add(node);
        }
    }

    public boolean containsWavelength(double allowedPercentDifference, double wavelength) {
        boolean containsWavelength = false;
        Iterator iterator = adjacencyList.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry elementData = (Map.Entry) iterator.next();
            elementList = (List<Node>) elementData.getValue();

            for (int i = 0; i < elementList.size(); i++) {
                if (percentDifference(allowedPercentDifference, wavelength, elementList.get(i).getWavelength())) {
                    containsWavelength = true;
                    referenceIntensity = elementList.get(i).getIntensity();
                    referenceElement = (String) elementData.getKey();
                    break;
                }
            }
        }

        return containsWavelength;
    }

    public boolean percentDifference(double difference, double value, double referenceValue) {
        boolean withInRange = false;
        double actualDifference = 0.0;

        if (value > referenceValue) {
            actualDifference = (((referenceValue - value) / value));
            if (value > 1)
                actualDifference = actualDifference * 100;
            if (actualDifference <= difference)
                withInRange = true;
        } else {
            actualDifference = (((referenceValue - value) / referenceValue));
            if (value > 1)
                actualDifference = actualDifference * 100;
            if (actualDifference <= difference)
                withInRange = true;
        }

        return withInRange;
    }

    public double normalize(double value, double minimumValue, double maximumValue) {
        double normalizedValue = 0.0;

        normalizedValue = ((value - minimumValue) / (maximumValue - minimumValue));

        return normalizedValue;
    }

    private double parse(String value) {
        return Math.round(Double.parseDouble(value) * 100.0) / 100.0;
    }

    public List<Node> getAllElements() {
        List<Node> elements = new ArrayList<Node>();
        Iterator iterator = adjacencyList.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry elementData = (Map.Entry) iterator.next();
            elementList = (List<Node>) elementData.getValue();

            for (int i = 0; i < elementList.size(); i++) {
                elements.add(elementList.get(i));
            }
        }

        return elements;
    }

    public List<Node> getResults() {
        List<Node> elements = new ArrayList<Node>();
        Iterator iterator = adjacencyList.entrySet().iterator();
        double intensityTotal = 0.0;

        while (iterator.hasNext()) {
            Map.Entry elementData = (Map.Entry) iterator.next();
            Node node = new Node();
            elementList = (List<Node>) elementData.getValue();
            node.setIcon(elementList.get(0).getIcon());
            node.setLines(elementList.size());
            node.setElement(elementList.get(0).getElement());
            for (int i = 0; i < elementList.size(); i++) {
               intensityTotal+= elementList.get(i).getIntensity();
            }
            node.setIntensity(Math.round(intensityTotal/elementList.size() * 100.0) / 100.0);
            elements.add(node);
        }

        return elements;
    }

    public String getReferenceElement() {
        return referenceElement;
    }

    public double getReferenceIntensity() {
        return referenceIntensity;
    }

    public Map<String, List<Node>> getAdjacencyList() {
        return adjacencyList;
    }

    public String toString() {
        String element = "";

        Iterator iterator = adjacencyList.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry spectrum = (Map.Entry) iterator.next();
            elementList = (List<Node>) spectrum.getValue();

            for (int i = 0; i < elementList.size(); i++) {
                element = element + (String) spectrum.getKey() + " " + elementList.get(i)
                        .getWavelength() + " " + elementList.get(i).getIntensity() + " ";
            }

            element = element + "\n\n";
        }

        return element;
    }
}
