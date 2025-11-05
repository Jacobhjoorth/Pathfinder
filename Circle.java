public abstract class Circle extends javafx.scene.shape.Circle {

    // Sets the center position (x, y) and the radius (size) of the circle
    public Circle(double x, double y, double size){
        super(x, y, size);
    }

    public abstract void paintRed();

    public abstract void paintBlue();
}

