package azarz.geophoto.dataobject;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Amaury on 17/01/2017.
 */

public class Photo extends Pojo {
    private String date;
    private String path;
    private LatLng position;

    //Getters / setters
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getPath(){
        return path;
    }
    public void setPath(String path){
        this.path = path;
    }
    public LatLng getPosition(){
        return position;
    }
    public void setPosition(LatLng position){
        this.position = position;
    }

    //Constructeurs
    public Photo(int id){
        super(id);
    }
    public Photo(int id, String date){
        super(id);
        this.date = date;
    }
    public Photo(int id, String date, LatLng position){
        super(id);
        this.date = date;
        this.position = position;
    }
    public Photo(int id, String date, String path, LatLng position){
        super(id);
        this.date = date;
        this.path = path;
        this.position = position;
    }
}
