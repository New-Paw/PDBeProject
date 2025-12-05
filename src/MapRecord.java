import java.util.Date;

public class MapRecord {
    private int id;
    private String name;
    private String type;
    private double x;
    private double y;
    private String imagePath;
    private Date visitTime;

    public MapRecord(int id, String name, String type,
                     double x, double y, String imagePath, Date visitTime) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.visitTime = visitTime;
    }

    public MapRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Date getVisitTime() { return visitTime; }
    public void setVisitTime(Date visitTime) { this.visitTime = visitTime; }
}
