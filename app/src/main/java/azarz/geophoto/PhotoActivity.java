package azarz.geophoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import azarz.geophoto.dataaccess.DataSource;
import azarz.geophoto.dataaccess.PhotoDataAccessObject;
import azarz.geophoto.dataobject.Photo;

public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    LatLng position;
    SurfaceView cameraView;
    boolean isPreview = false;
    Camera camera;
    int angleOrientation = 0;
    OrientationEventListener orientationEventListener;
    Camera.AutoFocusCallback autoFocusCallback;
    public int cameraId = 0;
    DataSource dataSource;
    PhotoDataAccessObject mPhotoDataAccessObject;
    boolean send_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        dataSource = ((MyApplication)getApplication()).dataSource;

        loadComponents();
        initEventListeners();

        // Recuperation of the position:
        Intent intent = getIntent();
        double lat = intent.getExtras().getDouble("lat");
        double lon = intent.getExtras().getDouble("lon");
        position = new LatLng(lat, lon);

        //Recupération du booléen d'envoi de courriel
        send_mail = intent.getExtras().getBoolean("send_mail");

        // Init cameraView
        cameraView.getHolder().addCallback(this);
        cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Init the photo data access object
        mPhotoDataAccessObject = new PhotoDataAccessObject(dataSource);
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
        orientationEventListener.enable();
    }

    @Override
    public void onPause() {
        super.onPause();

        orientationEventListener.disable();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void loadComponents() {
        cameraView = (SurfaceView)findViewById(R.id.camera_view);
    }

    private void initEventListeners() {
        cameraView.setOnClickListener(event_takePicture);

        orientationEventListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int orientation) {
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(cameraId, info);
                int rotation = PhotoActivity.this.getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;
                switch (rotation) {
                    case Surface.ROTATION_0:
                        degrees = 0;
                        break;
                    case Surface.ROTATION_90:
                        degrees = 90;
                        break;
                    case Surface.ROTATION_180:
                        degrees = 180;
                        break;
                    case Surface.ROTATION_270:
                        degrees = 270;
                        break;
                }

                int result;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (info.orientation + degrees) % 360;
                    result = (360 - result) % 360; // compensate the mirror
                } else { // back-facing
                    result = (info.orientation - degrees + 360) % 360;
                }
                if (camera != null) {camera.setDisplayOrientation(result);}
                angleOrientation = result;
            }
        };

        autoFocusCallback = new Camera.AutoFocusCallback(){
            @Override
            public void onAutoFocus(boolean arg0, Camera arg1) {
                try {
                    camera.takePicture(null, null, PhotoActivity.this);
                } catch (Exception e) {
                    Toast.makeText(PhotoActivity.this, "Impossible de sauvegarder la photo : " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public void onPictureTaken(byte[] bytes, Camera camera) {
        File pictureFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Toast.makeText(PhotoActivity.this, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(angleOrientation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            boolean result = rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.write(bytes);
            fos.close();
            if (result) {
                // Now we add exif information :
                ExifInterface exif = new ExifInterface(filename);
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, getLatTagGPS());
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, getLonTagGPS());
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, getLatRefTagGps());
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, getLonRefTagGps());
                exif.setAttribute(ExifInterface.TAG_FLASH, camera.getParameters().getFlashMode());
                exif.saveAttributes();
                Toast.makeText(PhotoActivity.this, "New Image saved with the EXIF information: " + photoFile, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(PhotoActivity.this, "Couldn't save image:" + photoFile, Toast.LENGTH_LONG).show();
                //camera.startPreview();
            }
        } catch (Exception error) {
            Toast.makeText(PhotoActivity.this, "Image could not be saved.", Toast.LENGTH_LONG).show();
        }



        // Saving the picture to the database
        Photo mPhoto1 = new Photo(-1, date, filename, this.position);
        //L'identifiant "-1" dit à SQLite de créer un nouvel identifiant en autoincrémentation
        //stockage des attributs de l'objet dans la base de données
        mPhotoDataAccessObject.insert(mPhoto1);

        // Envoi de l'image par mail le cas échéant
        if (send_mail){
            Intent mail_intent = new Intent();
            mail_intent.setAction(Intent.ACTION_SEND);
            mail_intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename));
            mail_intent.setType("image/*");
            startActivity(Intent.createChooser(mail_intent, "Envoyer courriel..."));
        }

        // Close activity
        finish();
    }

    private View.OnClickListener event_takePicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (camera != null) {
                camera.autoFocus(autoFocusCallback);
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // Stop preview mode if running
        if (isPreview) {
            camera.stopPreview();
        }

        // Get camera parameters
        Camera.Parameters parameters = camera.getParameters();

        // Nous changeons la taille
        parameters.setPreviewSize(parameters.getPreviewSize().width, parameters.getPreviewSize().height);
        parameters.setGpsLatitude(position.latitude);
        parameters.setGpsLongitude(position.longitude);

        // Get the best size for previsualisation
        Camera.Size size;
        size = getBestSize(parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(size.width, size.height);
        // Get the best picture size for the files :
        size = getBestSize(parameters.getSupportedPictureSizes());
        parameters.setPictureSize(size.width, size.height);

        // Apply new paramters
        camera.setParameters(parameters);

        try { // Make link between surfaceView and the camera
            camera.setPreviewDisplay(cameraView.getHolder());
        } catch (IOException e) {
            // TODO: handle exception
        }

        // Run preview
        camera.startPreview();

        isPreview = true;
    }

    public Camera.Size getBestSize(List<Camera.Size> sizes) {
        Camera.Size bestSize = sizes.get(0);
        for(int i = 1; i < sizes.size(); i++){
            if((sizes.get(i).width * sizes.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizes.get(i);
            }
        }
        return bestSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public String getLatTagGPS() {
        double latitude = Math.abs(this.position.latitude);
        int num1Lat = (int)Math.floor(latitude);
        int num2Lat = (int)Math.floor((latitude - num1Lat) * 60);
        double num3Lat = (latitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;
        return num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000";
    }

    public String getLonTagGPS() {
        double longitude = Math.abs(this.position.longitude);
        int num1Lon = (int)Math.floor(longitude);
        int num2Lon = (int)Math.floor((longitude - num1Lon) * 60);
        double num3Lon = (longitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;
        return num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000";
    }

    public String getLatRefTagGps() {
        if (this.position.latitude > 0) {return "N";} else {return "S";}
    }

    public String getLonRefTagGps() {
        if (this.position.longitude > 0) {return "E";} else {return "W";}
    }
}


