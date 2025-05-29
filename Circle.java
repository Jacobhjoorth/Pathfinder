public abstract class Circle extends javafx.scene.shape.Circle {

    // Constructor that sets the center position (x, y) and the radius (size) of the circle
    public Circle(double x, double y, double size){
        super(x, y, size);
    }

    // Abstract method: paints the circle red
    public abstract void paintRed();

    // Abstract method: paints the circle blue
    public abstract void paintBlue();
}

