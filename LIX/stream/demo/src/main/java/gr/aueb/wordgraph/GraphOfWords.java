/*
 * Copyright 2016 Midas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.aueb.wordgraph;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import static java.lang.Integer.min;

/**
 *
 * @author Midas
 */
public class GraphOfWords {

    private WeightedGraph g;
    private Integer direction;
    private Integer windowSize = 4;

    public GraphOfWords(String text) {
        this.g= new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
        String[] tokens = text.split(" ");
        int size = tokens.length;
        for (int ii = 0; ii < size; ii++) {
            String v1 = tokens[ii];
            if (!g.containsVertex(v1)) {
                g.addVertex(v1);
            }
            for (int jj = min(ii + this.windowSize,size-1); jj > ii; jj--) {
                String v2 = tokens[jj];
                if (v2.equals(v1)) {
                    continue;
                }
                if (!g.containsVertex(v2)) {
                    g.addVertex(v2);
                }
                String temp = "";
                if (v1.compareTo(v2) < 0) {
                    temp = v1;
                    v1 = v2;
                    v2 = temp;
                }
                if (g.containsEdge(v1, v2)) {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) g.getEdge(v1, v2);
                    g.setEdgeWeight(e, g.getEdgeWeight(e) + 1.0);
                } else {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) g.addEdge(v1, v2);
                    g.setEdgeWeight(e, 1.0);
                }

            }
        }
    }

    public WeightedGraph getGraph() {
        return g;
    }

}
