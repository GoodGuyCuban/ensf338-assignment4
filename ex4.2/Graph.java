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

    public static void main(String[] args) {
        Graph graph = Graph.importFromFile("random.dot");
        if (graph == null) {
            System.out.println("Error importing graph.");
            return;
        }
        System.out.println("Graph imported successfully.");

        GraphNode g = graph.getAdjacencyList().keySet().iterator().next();
        System.out.println("Finding shortest paths from node " + g.getData() + "...");
        graph.fastSP(g);
    }

    /**
     * Dijkstra's algorithm for finding the shortest path from the given node to all
     * other nodes.
     * Uses a slow implementation.
     * 
     * @param g
     */
    public void slowSP(GraphNode g) {
        // Create a set of all unvisted nodes in the graph
        Set<GraphNode> unvisited = new HashSet<>(adjacencyList.keySet());

        // Create a map of distances from the given node to all other nodes
        Map<GraphNode, Integer> distances = new HashMap<>();
        // Set the distance from the given node to all other nodes to infinity
        for (GraphNode node : adjacencyList.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        // Set the distance from the given node to itself to 0
        distances.put(g, 0);

        // Set the current node to the given node
        GraphNode current = g;
        int currentDistance = 0;
        while (!unvisited.isEmpty()) {
            // Check all unvisited neighbours of the current node
            for (Edge edge : adjacencyList.get(current)) {
                GraphNode other = edge.getOtherEndpoint(current);
                // If the neighbour is unvisited, update its distance
                if (unvisited.contains(other)) {
                    int weight = edge.getWeight() + currentDistance;
                    if (weight < distances.get(other)) {
                        distances.put(other, weight);
                    }
                }
            }
            // Remove the current node from the set of unvisited nodes
            unvisited.remove(current);

            // Find the unvisited node with the smallest distance from the given node
            // and set it as the current node
            currentDistance = Integer.MAX_VALUE; // Reset the current distance to infinity
            for (GraphNode node : unvisited) {
                int distance = distances.get(node);
                if (distance < currentDistance) {
                    current = node;
                    currentDistance = distance;
                }
            }

            if (currentDistance == Integer.MAX_VALUE) {
                // If the current distance is infinity, then there are no more unvisited
                // connected nodes, so we can stop.
                break;
            }
        }

        // Print the distances from the given node to all other nodes
        for (GraphNode node : distances.keySet()) {
            System.out.println("Distance from " + g.getData() + " to " + node.getData() +
                    ": " + distances.get(node));
        }
    }

    /**
     * Dijkstra's algorithm for finding the shortest path from the given node to all
     * other nodes.
     * Uses a fast implementation.
     * 
     * @param g
     */
    public void fastSP(GraphNode g) {
        // Create a map of distances from the given node to all other nodes
        Map<GraphNode, Integer> distances = new HashMap<>();
        // Set the distance from the given node to all other nodes to infinity
        for (GraphNode node : adjacencyList.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        // Set the distance from the given node to itself to 0
        distances.put(g, 0);

        // Create a priority queue of nodes sorted by distance from the given node
        PriorityQueue<GraphNode> queue = new PriorityQueue<>(new Comparator<GraphNode>() {
            @Override
            public int compare(GraphNode o1, GraphNode o2) {
                return distances.get(o1) - distances.get(o2);
            }
        });
        queue.addAll(adjacencyList.keySet()); // Add all nodes to the queue

        while (!queue.isEmpty()) {
            GraphNode current = queue.poll(); // Get the node with the smallest distance

            if (current == null || distances.get(current) == Integer.MAX_VALUE) {
                // If the current node is null or has an infinite distance, then there are
                // no more unvisited connected nodes, so we can stop.
                break;
            }

            // Check all unvisited neighbours of the current node
            for (Edge edge : adjacencyList.get(current)) {
                GraphNode other = edge.getOtherEndpoint(current);
                // If the neighbour is unvisited, update its distance
                if (queue.contains(other)) {
                    int weight = edge.getWeight() + distances.get(current);
                    if (weight < distances.get(other)) {
                        distances.put(other, weight);
                        queue.remove(other);
                        queue.add(other);
                    }
                }
            }
        }

        // Print the distances from the given node to all other nodes
        for (GraphNode node : distances.keySet()) {
            System.out.println("Distance from " + g.getData() + " to " + node.getData() +
                    ": " + distances.get(node));
        }
    }

    public Map<GraphNode, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

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