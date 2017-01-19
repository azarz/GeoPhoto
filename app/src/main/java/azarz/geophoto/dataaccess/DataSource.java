package azarz.geophoto.dataaccess;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Amaury on 17/01/2017.
 */

public class DataSource {
    //connexion à la base de données
    private SQLiteDatabase db;
    private final DBHelper helper;
    public DataSource(Context context) {
        helper = new DBHelper(context);
    }
    public SQLiteDatabase getDB(){
        if (db == null) open ();
        return db;
    }
    public void open() throws SQLException{
        db = helper.getWritableDatabase();
    }
    public void close(){
        helper.close();
    }

    //factory
    public PhotoDataAccessObject newPhotoDataAccessObject(){
        return new PhotoDataAccessObject(this);
    }
}
