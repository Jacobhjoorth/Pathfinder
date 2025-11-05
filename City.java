import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class City extends Circle {

    private String name;

    private boolean marked = false;

    // Sets name and position, and paints the city blue
    public City(String name, double x, double y) {
        super(x, y, 10);       // Set centerX, centerY, and radius (10)
        this.name = name;
        paintBlue();           // Default color is blue
        this.setId(name);      // Set ID 
    }

    public String getName() {
        return name;
    }

    public void paintRed() {
        setFill(Color.RED);
    }

    public void paintBlue() {
        setFill(Color.BLUE);
    }

    public boolean isMarked() {
        return this.marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    // Returns the city's name and coordinates (used for saving)
    public String saveInfo() {
        return name + ";" + getCenterX() + ";" + getCenterY();
    }

    @Override
    public String toString() {
        return name;
    }
}
