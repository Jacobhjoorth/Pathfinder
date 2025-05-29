public class Edge<T> {

    // The destination node this edge points to
    private T destination;

    // The weight or cost of the edge
    private int weight;

    // The name of the edge (e.g., road name)
    private String name;

    // Constructor with parameters
    public Edge(T destination, int weight, String name) {
        this.destination = destination;
        this.weight = weight;
        this.name = name;
    }

    // Default constructor
    public Edge() {
    }

    // Returns the destination node
    public T getDestination() {
        return destination;
    }

    // Returns the weight of the edge
    public int getWeight() {
        return weight;
    }

    // Sets the weight of the edge
    public void setWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Error: The weight cannot be negative");
        } else {
            this.weight = weight;
        }
    }

    // Returns the name of the edge
    public String getName() {
        return name;
    }

    // Returns a simple description of the edge
    @Override
    public String toString() {
        return "to " + destination + " by " + name + " takes " + weight;
    }
}