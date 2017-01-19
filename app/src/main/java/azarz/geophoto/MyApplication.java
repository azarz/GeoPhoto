package azarz.geophoto;

import android.app.Application;

import azarz.geophoto.dataaccess.DataSource;

/**
 * Created by Amaury on 18/01/2017.
 */

public class MyApplication extends Application {

    // L'objet dataSource est le même dans toute l'application
    public DataSource dataSource;
    private static MyApplication singleton;

    public static MyApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        // L'objet dataSource est le même dans toute l'application
        dataSource = new DataSource(this);
    }
}
