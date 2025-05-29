import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class City extends Circle {

    // Name of the city
    private String name;

    // True if the city is marked, false otherwise
    private boolean marked = false;

    // Constructor: sets name and position, and paints the city blue
    public City(String name, double x, double y) {
        super(x, y, 10);       // Set centerX, centerY, and radius (10)
        this.name = name;
        paintBlue();           // Default color is blue
        this.setId(name);      // Set ID for JavaFX (can be used in CSS or lookups)
    }

    // Returns the name of the city
    public String getName() {
        return name;
    }

    // Changes color to red
    public void paintRed() {
        setFill(Color.RED);
    }

    // Changes color to blue
    public void paintBlue() {
        setFill(Color.BLUE);
    }

    // Returns true if the city is marked
    public boolean isMarked() {
        return this.marked;
    }

    // Sets the marked status
    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    // Returns a string with the city's name and coordinates (used for saving)
    public String saveInfo() {
        return name + ";" + getCenterX() + ";" + getCenterY();
    }

    // Returns the city's name when printed
    @Override
    public String toString() {
        return name;
    }
}
