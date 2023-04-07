import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * A Graph class used to describe an undirected graph.
 * Internally stores edges as an adjacency list.
 */
public class Graph {
    private Map<GraphNode, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    /**
     * Creates a new GraphNode with the given data and adds it to the graph.
     * 
     * @param data The data to store in the node.
     * @return The newly created node.
     */
    public GraphNode addNode(String data) {
        GraphNode node = new GraphNode(data);

        // If the node already exists, return the existing node.
        if (adjacencyList.containsKey(node)) {
            return node;
        }

        // Otherwise, add the node to the adjacency list.
        adjacencyList.put(node, new ArrayList<>());
        return node;
    }

    /**
     * Removes the given node from the graph.
     * 
     * @param node
     */
    public void removeNode(GraphNode node) {
        adjacencyList.remove(node); // Remove the node from the adjacency list.

        // Remove all edges that contain the node.
        for (GraphNode other : adjacencyList.keySet()) {
            List<Edge> edges = adjacencyList.get(other);
            edges.removeIf(e -> e.hasEndpoint(node));
        }
    }

    /**
     * Adds an edge between the two given nodes, with given weight.
     * 
     * @param n1
     * @param n2
     * @param weight
     */
    public void addEdge(GraphNode n1, GraphNode n2, int weight) {
        Edge edge = new Edge(n1, n2, weight);
        adjacencyList.get(n1).add(edge);
        adjacencyList.get(n2).add(edge);
    }

    /**
     * Removes the edge between the two given nodes.
     * 
     * @param n1
     * @param n2
     */
    public void removeEdge(GraphNode n1, GraphNode n2) {
        List<Edge> edges = adjacencyList.get(n1);
        edges.removeIf(e -> e.hasEndpoint(n2));

        edges = adjacencyList.get(n2);
        edges.removeIf(e -> e.hasEndpoint(n1));
    }

    /**
     * Imports a graph description from a GraphViz file.
     * 
     * @param filename The filename to import from.
     * @return The imported graph.
     */
    public static Graph importFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line == null || !line.startsWith("strict graph")) {
                // GraphViz files must begin with "strict graph" for undirected graphs.
                return null;
            }

            Graph graph = new Graph();
            while ((line = reader.readLine()) != null) {
                if (line.equals("}")) {
                    // End of definition.
                    return graph;
                }

                // Regex to match a line of the form "node1 -- node2 [weight = 1];"
                // or "node1 -- node2;"
                if (!line.matches("\\s*\\w+\\s+--\\s+\\w+(\\s*\\[\\s*weight\\s*=\\s*\\d+\\s*\\])?\\s*;\\s*")) {
                    // Invalid line.
                    return null;
                }

                // Split the line into tokens.
                String[] tokens = line.split("\\s+--\\s+");
                String node1Data = tokens[0].trim();
                String node2Data = tokens[1].trim().replaceAll("\\s*\\[\\s*weight\\s*=\\s*\\d+\\s*\\]\\s*", "");
                node2Data = node2Data.replaceAll(";", "");
                int weight = 1;
                if (line.contains("weight")) {
                    // Extract the weight from the line.
                    weight = Integer.parseInt(line.replaceAll(".*weight\\s*=\\s*(\\d+).*", "$1"));
                }

                // Add the nodes and edge to the graph.
                GraphNode node1 = graph.addNode(node1Data);
                GraphNode node2 = graph.addNode(node2Data);
                graph.addEdge(node1, node2, weight);
            }
        } catch (IOException | NumberFormatException e) {
            // If there is an error reading or parsing the file, return null.
            return null;
        }

        // If the file is empty, return null.
        return null;
    }

    public static void main(String[] args) {
        // Example main method to test the Graph class.
        // Imports the given example graph and prints it.
        Graph graph = Graph.importFromFile("example.dot");
        if (graph == null) {
            System.out.println("Error importing graph.");
            return;
        }

        System.out.println("Graph imported successfully.");

        // Print the graph.
        for (GraphNode node : graph.adjacencyList.keySet()) {
            System.out.print(node.getData() + " -> ");
            for (Edge edge : graph.adjacencyList.get(node)) {
                GraphNode other = edge.getOtherEndpoint(node);
                System.out.print(other.getData() + " (" + edge.getWeight() + "), ");
            }
            System.out.println();
        }
    }
}

class GraphNode {
    private String data;

    public GraphNode(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GraphNode other = (GraphNode) obj;
        return data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}

class Edge {
    private GraphNode endpoint1;
    private GraphNode endpoint2;
    private int weight;

    public Edge(GraphNode endpoint1, GraphNode endpoint2, int weight) {
        this.endpoint1 = endpoint1;
        this.endpoint2 = endpoint2;
        this.weight = weight;
    }

    public GraphNode getEndpoint1() {
        return endpoint1;
    }

    public GraphNode getEndpoint2() {
        return endpoint2;
    }

    public int getWeight() {
        return weight;
    }

    public boolean hasEndpoint(GraphNode node) {
        return endpoint1.equals(node) || endpoint2.equals(node);
    }

    public GraphNode getOtherEndpoint(GraphNode node) {
        if (endpoint1.equals(node)) {
            return endpoint2;
        } else if (endpoint2.equals(node)) {
            return endpoint1;
        } else {
            return null;
        }
    }
}