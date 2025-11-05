public class Edge<T> {

    // The destination node this edge points to
    private T destination;

    // The weight or cost of the edge
    private int weight;

    private String name;

    public Edge(T destination, int weight, String name) {
        this.destination = destination;
        this.weight = weight;
        this.name = name;
    }

    public Edge() {
    }

    public T getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Error: The weight cannot be negative");
        } else {
            this.weight = weight;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "to " + destination + " by " + name + " takes " + weight;
    }
}
