package com.sciapps.model;

import java.util.Comparator;


public class NodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node lhs, Node rhs) {
        if (Double.parseDouble(((Node) rhs).getIntensity()) > Double
                .parseDouble(((Node) lhs).getIntensity())) {
            return 1;
        } else
            return -1;
    }
}
