package azarz.geophoto.dataaccess;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import azarz.geophoto.dataobject.Photo;

/**
 * Created by Amaury on 17/01/2017.
 */

public class PhotoDataAccessObject {

    public static final String COL_ID="id";
    public static final String COL_DATE="date";
    public static final String COL_PATH="path";
    public static final String COL_LAT="lat";
    public static final String COL_LNG="lng";
    public static final String TABLE_NAME="Photos";

    private final DataSource datasource;
    //constructor
    public PhotoDataAccessObject(DataSource datasource){
        this.datasource = datasource;
    }

    public synchronized Photo insert(Photo mObjet){
        //on copie les champs de l'objet dans les colonnes de la table.
        ContentValues values=new ContentValues();
        values.put(COL_DATE, mObjet.getDate());
        values.put(COL_PATH, mObjet.getPath());
        values.put(COL_LAT, mObjet.getPosition().latitude);
        values.put(COL_LNG, mObjet.getPosition().longitude);
        //insert query
        int id=(int)datasource.getDB().insert(TABLE_NAME,null,values);
        //mise à jour de l'ID dans l'objet
        mObjet.setId(id);
        return mObjet;
    }

    public synchronized Photo update(Photo mObjet){
        //on copie les champs de l'objet dans les colonnes de la table.
        ContentValues values=new ContentValues();
        values.put(COL_ID, mObjet.getId());
        values.put(COL_DATE, mObjet.getDate());
        values.put(COL_PATH, mObjet.getPath());
        values.put(COL_LAT, mObjet.getPosition().latitude);
        values.put(COL_LNG, mObjet.getPosition().longitude);
        //gestion de la clause "WHERE"
        String clause = COL_ID + " = ? ";
        String[] clauseArgs = new String[]{
                String.valueOf(mObjet.getId())
        };
        datasource.getDB().update(TABLE_NAME, values, clause, clauseArgs);
        //mise à jour de l'ID dans l'objet
        return mObjet;
    }

    public synchronized void delete(Photo mObjet){
        //gestion de la clause "WHERE"
        String clause = COL_ID + " = ? ";
        String[] clauseArgs = new String[]{
                String.valueOf(mObjet.getId())
        };
        datasource.getDB().delete(TABLE_NAME, clause, clauseArgs);
    }

    public Photo read(Photo mObjet){
        //columns
        String[] allColumns = new String[]{COL_ID,COL_DATE,COL_PATH,COL_LAT,COL_LNG};

        //clause
        String clause = COL_ID + " = ? ";
        String[] clauseArgs = new String[]{
                String.valueOf(mObjet.getId())
        };

        //select query
        Cursor cursor = datasource.getDB().query(TABLE_NAME,allColumns, "ID = ?", clauseArgs, null, null,null);

        //read cursor. On copie les valeurs de la table dans l'objet
        cursor.moveToFirst();
        mObjet.setId(cursor.getInt(0));
        mObjet.setDate(cursor.getString(1));
        mObjet.setPath(cursor.getString(2));
        mObjet.setPosition(new LatLng(cursor.getDouble(3),cursor.getDouble(4)));
        cursor.close();
        return mObjet;
    }

    public List<Photo> readAll(){
        //columns
        String[] allColumns = new String[]{COL_ID,COL_DATE,COL_PATH,COL_LAT,COL_LNG};
        //select query
        Cursor cursor = datasource.getDB().query(TABLE_NAME,allColumns, null, null, null, null,null);
        //Iterate on cursor and retrieve result
        List<Photo> liste_Photo = new ArrayList<Photo>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            //Vérifie l'existence de la photo, la supprime de la BDD le cas échéant.
            //Action normale sinon.
            File f = new File(cursor.getString(2));
            if(f.exists()) {
                liste_Photo.add(new Photo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), new LatLng(cursor.getDouble(3), cursor.getDouble(4))));
            } else{
                this.delete(new Photo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), new LatLng(cursor.getDouble(3), cursor.getDouble(4))));
            }
            cursor.moveToNext();
        }
        cursor.close();
        return liste_Photo;
    }
}
