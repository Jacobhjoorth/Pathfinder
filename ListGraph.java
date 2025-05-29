import java.util.*;

// A graph implementation using an adjacency list with HashMap and HashSet
public class ListGraph<T> implements Graph<T> {

    // Stores each node and its set of connected edges
    private HashMap<T, HashSet<Edge<T>>> nodes = new HashMap<>();

    // Adds a node to the graph if it doesn't already exist
    public void add(T node1) {
        nodes.putIfAbsent(node1, new HashSet<>());
    }

    // Removes a node and all edges connected to it
    public void remove(T node) {
        if (!nodes.containsKey(node)) {
            throw new NoSuchElementException("Node not found in graph");
        }

        // Remove references to this node from connected nodes
        for (Edge<T> edge : nodes.get(node)) {
            T nodeConnected = edge.getDestination();
            nodes.get(nodeConnected).removeIf(e -> e.getDestination().equals(node));
        }

        // Remove the node itself
        nodes.remove(node);
    }

    // Connects two nodes with an edge (both directions since this is an undirected graph)
    public void connect(T node1, T node2, String name, int weight) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("One or both of the nodes not found in graph");
        } else if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        } else if (getEdgeBetween(node1, node2) != null) {
            throw new IllegalStateException("Edge already exists between nodes");
        }

        nodes.get(node1).add(new Edge<>(node2, weight, name));
        nodes.get(node2).add(new Edge<>(node1, weight, name));
    }

    // Disconnects two nodes (removes the edge between them)
    public void disconnect(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("One or both of the nodes not found in graph");
        }

        if (getEdgeBetween(node1, node2) == null) {
            throw new IllegalStateException("No edge found between the two nodes");
        }

        nodes.get(node1).remove(getEdgeBetween(node1, node2));
        nodes.get(node2).remove(getEdgeBetween(node2, node1));
    }

    // Sets a new weight for the edge between two nodes
    public void setConnectionWeight(T node1, T node2, int newWeight) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("One or both of the nodes not found in graph");
        } else if (newWeight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }

        Edge<T> edge = getEdgeBetween(node1, node2);
        Edge<T> edge2 = getEdgeBetween(node2, node1);

        if (edge == null || edge2 == null) {
            throw new NoSuchElementException("No edge found between the two nodes");
        } else {
            edge.setWeight(newWeight);
            edge2.setWeight(newWeight);
        }
    }

    // Returns a set of all nodes in the graph
    public Set<T> getNodes() {
        return new HashSet<>(nodes.keySet());
    }

    // Returns all edges from a given node
    public Collection<Edge<T>> getEdgesFrom(T node) {
        if (!nodes.containsKey(node)) {
            throw new NoSuchElementException("Node not found in graph");
        }
        return Collections.unmodifiableCollection(nodes.get(node));
    }

    // Returns the edge between two nodes if it exists, otherwise null
    public Edge<T> getEdgeBetween(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("One or both of the nodes not found in graph");
        }
        for (Edge<T> edge : nodes.get(node1)) {
            if (edge.getDestination().equals(node2)) {
                return edge;
            }
        }
        return null;
    }

    // Returns a string showing each node and its edges
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T city : nodes.keySet()) {
            sb.append(city).append(":").append(nodes.get(city)).append("\n");
        }
        return sb.toString();
    }

    // Returns true if there is a path from 'from' to 'to'
    public boolean pathExists(T from, T to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            return false;
        }
        Set<T> visited = new HashSet<>();
        dfs(from, to, visited);
        return visited.contains(to);
    }

    // Returns the shortest path from 'from' to 'to' using BFS with weights
    public List<Edge<T>> getPath(T from, T to) {
        Map<T, T> connections = new HashMap<>();
        Map<T, Integer> weights = new HashMap<>();
        LinkedList<T> queue = new LinkedList<>();

        connections.put(from, null);
        weights.put(from, 0);
        queue.add(from);

        while (!queue.isEmpty()) {
            T t = queue.pollFirst();
            for (Edge<T> edge : nodes.get(t)) {
                T destination = edge.getDestination();
                int weightToDestination = weights.get(t) + edge.getWeight();
                if (!weights.containsKey(destination) || weightToDestination < weights.get(destination)) {
                    connections.put(destination, t);
                    weights.put(destination, weightToDestination);
                    queue.add(destination);
                }
            }
        }

        // No path found
        if (!connections.containsKey(to)) {
            return null;
        }

        // Build and return the path
        return gatherPath(from, to, connections);
    }

    // Builds the path by walking backward through the connections map
    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> connections) {
        LinkedList<Edge<T>> path = new LinkedList<>();
        T current = to;

        while (!current.equals(from)) {
            T next = connections.get(current);
            Edge<T> edge = getEdgeBetween(next, current);
            path.addFirst(edge);
            current = next;
        }
        return path;
    }

    // Depth-first search to find if a path exists
    private boolean dfs(T current, T searchedFor, Set<T> visited) {
        visited.add(current);
        if (current.equals(searchedFor)) {
            return true;
        }
        for (Edge<T> edge : nodes.get(current)) {
            if (!visited.contains(edge.getDestination())) {
                if (dfs(edge.getDestination(), searchedFor, visited)) {
                    return true;
                }
            }
        }
        return false;
    }
}
