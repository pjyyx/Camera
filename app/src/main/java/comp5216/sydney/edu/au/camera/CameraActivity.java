package comp5216.sydney.edu.au.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CameraActivity extends Activity {
    private Camera mCamera;
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        //Button
        Button takePhoto = findViewById(R.id.takePhoto);
        Button goBack = findViewById(R.id.goBack);

        //Open the camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        //Start Preview
        mCamera.startPreview();
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        //take photo
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        //save the photo
                        String path;
                        path = saveFile(bytes);
                        if (path != null) {
                            //Saved successfully,go to the PreviewActivity
                            Intent intent = new Intent(CameraActivity.this,
                                    PreviewActivity.class);
                            if (intent != null) {
                                intent.putExtra("path", path);
                                startActivityForResult(intent,1);
                            }
                        } else {
                            Toast.makeText(CameraActivity.this, "Fail",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //Cancel to take photo, go back to MainActivity
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //release camera
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            // attempt to get a Camera instance
            c = Camera.open();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    //Help PreviewActivity return to MainActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            setResult(-1, data);
            finish();
        }
    }

    // Save file
    private String saveFile(byte[] bytes) {
        if (!marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForExternalStorage();
            return null;
        } else {
            //filename
            String time = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + time + ".jpg";
            File file = new File(this.getExternalFilesDir(null)
                    .getAbsolutePath()+"/images/",fileName);

            // make directory if it does not exist
            if (!file.getParentFile().exists() &&
                    !file.getParentFile().mkdirs()) {
                Log.e("write photo", "failed to make the directory");
            }

            //turn back 90°
            Bitmap picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix mtx = new Matrix();
            mtx.postRotate(90);
            picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(),
                    picture.getHeight(), mtx, true);
            // save data locally
            try {
                //write into
                FileOutputStream fos = new FileOutputStream(file);
                picture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file.getAbsolutePath();
        }
    }


}
