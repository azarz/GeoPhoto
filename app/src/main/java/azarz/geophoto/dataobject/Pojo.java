package azarz.geophoto.dataobject;

/**
 * Created by Amaury on 17/01/2017.
 */

public abstract class Pojo {
    //Atributs
    private int id;

    //Getters / setters
    public int getId(){
        return id;
    }
    public void setId(int id) {
    }

    //constructeur
    public Pojo(int id){
        this.id = id;
    }
}