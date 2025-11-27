package ui;
public class MapRecord {
    private int id;
    private String name;
    private String type;
    private double x;
    private double y;
    private String imagePath;
    private java.util.Date visitTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public java.util.Date getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(java.util.Date visitTime) {
        this.visitTime = visitTime;
    }
}
