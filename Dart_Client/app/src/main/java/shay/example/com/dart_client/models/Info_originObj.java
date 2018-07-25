package shay.example.com.dart_client.models;

public class Info_originObj {

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
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

    private String origin;
    private Boolean man_icon;

    public Info_originObj(String origin, Boolean man_icon, Boolean zone_active, Integer color) {
        this.origin = origin;
        this.man_icon = man_icon;
        this.zone_active = zone_active;
        this.color = color;
    }

    public Boolean getZone_active() {
        return zone_active;
    }

    public void setZone_active(Boolean zone_active) {
        this.zone_active = zone_active;
    }

    private Boolean zone_active;
    private Integer color;


    public Info_originObj() {
    }

}
