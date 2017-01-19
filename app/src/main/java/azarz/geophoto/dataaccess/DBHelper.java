package azarz.geophoto.dataaccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Amaury on 17/01/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    //utilisation du modèle de données
    public static final String DB_NAME = "base_de_donnees_photos.db";
    public static final int DB_VERSION = 1;

    //constructor
    public DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    public static String getQueryCreate(){
        return "CREATE TABLE Photos(" +
                "id Integer PRIMARY KEY AUTOINCREMENT, " +
                "date Text NOT NULL," +
                "path Text NOT NULL," +
                "lat Double NOT NULL," +
                "lng Double NOT NULL" +
                ");"
                ;
    }

    public static String getQueryDrop(){
        return "DROP TABLE IF EXISTS Photos;";
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        //ceci est automatiquement géré par SQLite
        db.execSQL(getQueryCreate());
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(getQueryDrop());
        db.execSQL(getQueryCreate());
    }
}
