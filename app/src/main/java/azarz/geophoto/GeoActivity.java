package azarz.geophoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Random;

import azarz.geophoto.dataaccess.DataSource;
import azarz.geophoto.dataaccess.PhotoDataAccessObject;
import azarz.geophoto.dataobject.Photo;

public class GeoActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Location lastLocation;
    private Button b_picture;
    private CheckBox chkbx_mail;
    boolean send_mail = false;
    LocationManager locationManager;
    DataSource dataSource;
    PhotoDataAccessObject mPhotoDataAccessObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo);
        dataSource = ((MyApplication)getApplication()).dataSource;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadComponents();
        setEventListeners();

        mPhotoDataAccessObject = new PhotoDataAccessObject(dataSource);
    }

    private void loadComponents(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        b_picture = (Button) findViewById(R.id.b_picture);
        chkbx_mail =  (CheckBox) findViewById(R.id.mail);
    }

    private void setEventListeners(){
        b_picture.setOnClickListener(photoEventListener);

        //Si le GPS est activé, tout est normal
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                // App is running
            } catch (SecurityException e) {
                // App does not has required permissions
                Toast.makeText(GeoActivity.this, "L'application n'a pas les autorisations nécessaires", Toast.LENGTH_LONG).show();
            }
        //Sinon, on choisit une position aléatoire, sur une emprise proche de MLV
        } else {
            lastLocation = new Location("dummyprovider");
            Random r = new Random();
            double rLong = 2.44 + 0.44*r.nextDouble();
            double rLat = 48.77 + 0.15*r.nextDouble();
            lastLocation.setLongitude(rLong);
            lastLocation.setLatitude(rLat);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        updateMap();
    }

    /**
     * Met à jour la carte en allant chercher les marqueurs dans la base de données
     */
    public void updateMap() {
        // Recuperation des photos de la BDD :
        List<Photo> listPhotos = mPhotoDataAccessObject.readAll();
        for (int i = 0; i < listPhotos.size(); i++) {
            // Ajoute un marqueur et bouge la caméra
            // Position du marqueur
            LatLng pos = listPhotos.get(i).getPosition();

            //Chemin de l'image correspondant au marqueur
            String path = listPhotos.get(i).getPath();

            // Icone du marqueur
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            //create the thumbnail to use as marker
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);

            mMap.addMarker(new MarkerOptions().position(pos).alpha(0.7f)
                    .snippet(path)
                    .icon(BitmapDescriptorFactory.fromBitmap(thumbnail)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10.0f));
        }
    }


    /**
     * Met à jour la carte en retournant sur l'activité
     */
    @Override
    public void onResume(){
        super.onResume();
        if(mMap != null) {
            updateMap();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Affiche la photo en cliquant dessus
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + marker.getSnippet()), "image/*");
        startActivity(intent);

        return false;
    }



    private View.OnClickListener photoEventListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            send_mail = chkbx_mail.isChecked();

            // Si on a accès à une localisation, on agit normalement
            if (lastLocation != null){
                double latit = 48.77;
                double longit = 2.44;

                //Si le GPS est actif, on agit normalement
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    latit = lastLocation.getLatitude();
                    longit = lastLocation.getLongitude();

                //Si pas de GPS, on change la position aléatoire à chaque photo, et ce sur une emprise proche de MLV
                } else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    //Génération aléatoire
                    Random r = new Random();
                    double rLong = 2.44 + 0.44 * r.nextDouble();
                    double rLat = 48.77 + 0.15 * r.nextDouble();

                    latit = rLat;
                    longit = rLong;
                }

                Intent intent = new Intent(GeoActivity.this, PhotoActivity.class);
                intent.putExtra("lat", latit);
                intent.putExtra("lon", longit);
                intent.putExtra("send_mail", send_mail);
                startActivity(intent);

            } else{
                //Sinon, on informe l'utilisateur que la photo est impossible (normalement, n'arrive jamais ou uniquement
                //Au tout début du lancement de l'application
                Toast.makeText(GeoActivity.this, "Photo impossible : pas de localisation, attendre un instant", Toast.LENGTH_SHORT).show();
            }
        }
    };


    // My private attribute which is the location listener
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }

    };
}
