package shay.example.com.dart_client.helper_classes;

/**
 * Created by Shay de Barra on 28,February,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class Stations {

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    Double lat;
    Double lon;
    String name;
    public Stations() {
    }


    public Stations(Double lat, Double lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;

    }
}
