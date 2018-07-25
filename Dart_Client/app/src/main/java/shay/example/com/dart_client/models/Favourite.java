package shay.example.com.dart_client.models;

/**
 * Created by swapnilparashar on 01/07/2017.
 * Modified by Shay de Barra on 20/03/2018 migrated to Favourite Model
 */

public class Favourite {
    private int id;
    private String origin_name;
    private String dest_name;

    public Favourite() {
    }

    public Favourite(String origin_name, String dest_name) {
        this.origin_name = origin_name;
        this.dest_name = dest_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrigin_name() {
        return origin_name;
    }

    public void setOrigin_name(String origin_name) {
        this.origin_name = origin_name;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
    }

    @Override
    public String toString() {
        return "Favourite{" +
                "id=" + id +
                ", origin_name='" + origin_name + '\'' +
                ", dest_name='" + dest_name + '\'' +
                '}';
    }
}
