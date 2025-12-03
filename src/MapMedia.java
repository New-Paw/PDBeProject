public class MapMedia {
    private int id;
    private String name;
    private String type;
    private double x;
    private double y;
    private java.util.Date tokenTime;
    private java.awt.Image image;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setTokenTime(java.util.Date tokenTime) {
        this.tokenTime = tokenTime;
    }

    public void setImage(java.awt.Image image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public java.util.Date getTokenTime() {
        return tokenTime;
    }
    
    public java.awt.Image getImage() {
        return image;
    }
}