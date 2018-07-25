package shay.example.com.dart_client.models;

public class Info_destObj {


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Boolean getMan_icon() {
        return man_icon;
    }

    public void setMan_icon(Boolean man_icon) {
        this.man_icon = man_icon;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }


    private Boolean zone_active;
    private String destination;
    private Boolean man_icon;
    private Integer color;


    public Boolean getZone_active() {
        return zone_active;
    }

    public void setZone_active(Boolean zone_active) {
        this.zone_active = zone_active;
    }


    public Info_destObj(String destination, Boolean man_icon, Boolean zone_active, Integer color) {
        this.destination = destination;
        this.man_icon = man_icon;
        this.color = color;
        this.zone_active = zone_active;
    }

    public Info_destObj() {
    }

}
