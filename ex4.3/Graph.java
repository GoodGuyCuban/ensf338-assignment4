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
        // Example main method to test the Graph class.
        // Imports the given example graph and prints it.
        Graph graph = Graph.importFromFile("example.dot");
        if (graph == null) {
            System.out.println("Error importing graph.");
            return;
        }

        System.out.println("Graph imported successfully.");

        graph.printGraph();

        Graph mst = mst(graph);
        System.out.println("MST:");
        mst.printGraph();
    }

    /**
     * Computes the minimum spanning tree of the given graph using Kruskal's
     * algorithm.
     * 
     * @param graph The graph to compute the MST of.
     * @return The minimum spanning tree of the given graph.
     */
    public static Graph mst(Graph graph) {
        // Create a heap of edges sorted by weight
        PriorityQueue<Edge> edges = new PriorityQueue<>(Comparator.comparingInt(Edge::getWeight));
        // Add all edges to the heap
        for (GraphNode node : graph.adjacencyList.keySet()) {
            for (Edge edge : graph.adjacencyList.get(node)) {
                if (!edges.contains(edge)) { // Avoid duplicates
                    edges.add(edge);
                }
            }
        }

        // Create a new graph to store the MST
        Graph tree = new Graph();
        // Add all nodes to the MST
        for (GraphNode node : graph.adjacencyList.keySet()) {
            tree.addNode(node.getData());
        }

        while (!edges.isEmpty()) {
            Edge edge = edges.poll();

            // Add the edge to the MST
            tree.addEdge(edge);

            // If the MST now contains a cycle, remove the edge
            if (tree.detectCycles()) {
                tree.removeEdge(edge);
            }
        }

        return tree;
    }

    /**
     * Uses the UNION-FIND algorithm to detect cycles in the graph.
     * 
     * @return true if the graph contains a cycle, false otherwise.
     */
    private boolean detectCycles() {
        DisjointSet ds = new DisjointSet();
        ds.makeSet(adjacencyList.keySet());

        Set<Edge> seenEdges = new HashSet<>(); // Used to avoid duplicates

        for (GraphNode node : adjacencyList.keySet()) {
            for (Edge edge : adjacencyList.get(node)) {
                // Skip edges that have already been seen
                if (seenEdges.contains(edge)) {
                    continue;
                }
                seenEdges.add(edge);

                GraphNode other = edge.getOtherEndpoint(node);
                GraphNode root1 = ds.find(node);
                GraphNode root2 = ds.find(other);
                if (root1 == root2) {
                    return true;
                }
                ds.union(root1, root2);
            }
        }

        return false;
    }

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void printGraph() {
        for (GraphNode node : adjacencyList.keySet()) {
            System.out.print(node.getData() + " -> ");
            for (Edge edge : adjacencyList.get(node)) {
                GraphNode other = edge.getOtherEndpoint(node);
                System.out.print(other.getData() + " (" + edge.getWeight() + "), ");
            }
            System.out.println();
        }
        System.out.println();
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
     * Adds the given edge to the graph.
     * 
     * @param edge
     */
    public void addEdge(Edge edge) {
        adjacencyList.get(edge.getEndpoint1()).add(edge);
        adjacencyList.get(edge.getEndpoint2()).add(edge);
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
     * Removes the given edge from the graph.
     * 
     * @param edge
     */
    public void removeEdge(Edge edge) {
        adjacencyList.get(edge.getEndpoint1()).remove(edge);
        adjacencyList.get(edge.getEndpoint2()).remove(edge);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Edge other = (Edge) obj;

        boolean endpointsMatch = (endpoint1.equals(other.endpoint1) && endpoint2.equals(other.endpoint2))
                || (endpoint1.equals(other.endpoint2) && endpoint2.equals(other.endpoint1));
        boolean weightsMatch = weight == other.weight;

        return endpointsMatch && weightsMatch;
    }

    @Override
    public int hashCode() {
        return endpoint1.hashCode() + endpoint2.hashCode() + weight;
    }
}

class DisjointSet {
    private Map<GraphNode, GraphNode> parentMap = new HashMap<>();

    public void makeSet(Set<GraphNode> nodes) {
        // Create a set for each node
        for (GraphNode node : nodes) {
            parentMap.put(node, node);
        }
    }

    public GraphNode find(GraphNode node) {
        // If the node is the parent of itself, then it is the root of the set
        if (parentMap.get(node) == node) {
            return node;
        }

        // Recursively find the parent of the node
        return find(parentMap.get(node));
    }

    public void union(GraphNode node1, GraphNode node2) {
        // Find the root of each set
        GraphNode parent1 = find(node1);
        GraphNode parent2 = find(node2);
        parentMap.put(parent1, parent2);
    }
}