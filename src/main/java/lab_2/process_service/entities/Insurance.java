package lab_2.process_service.entities;




public class Insurance {

    private int id;

    private int userId;

    private String type;

    private float cost;
    public Insurance() {

    }

    public int getId() {
        return id;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return String.format("UserId{to=%s, Cost=%s}", getUserId(), getCost());
    }
}
