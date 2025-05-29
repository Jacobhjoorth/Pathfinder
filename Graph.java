import java.util.Collection;
import java.util.List;
import java.util.Set;

// A generic interface for a graph data structure
public interface Graph<T> {

    // Adds a node to the graph
    void add(T node);

    // Connects two nodes with a named, weighted edge
    void connect(T node1, T node2, String name, int weight);

    // Changes the weight of an existing connection between two nodes
    void setConnectionWeight(T node1, T node2, int weight);

    // Returns all nodes in the graph
    Set<T> getNodes();

    // Returns all edges starting from a given node
    Collection<Edge<T>> getEdgesFrom(T node);

    // Returns the edge between two specific nodes, if it exists
    Edge<T> getEdgeBetween(T node1, T node2);

    // Removes the connection between two nodes
    void disconnect(T node1, T node2);

    // Removes a node and all its edges from the graph
    void remove(T node);

    // Checks if a path exists between two nodes
    boolean pathExists(T from, T to);

    // Returns a list of edges representing the path between two nodes
    List<Edge<T>> getPath(T from, T to);
}
