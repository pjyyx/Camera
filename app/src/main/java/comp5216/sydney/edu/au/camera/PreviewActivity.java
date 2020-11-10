package comp5216.sydney.edu.au.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PreviewActivity extends Activity {

    String path;
    public Uri imageUri;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_preview);

        ImageView imageView = (ImageView) findViewById(R.id.photopreview);

        //get path
        path = getIntent().getStringExtra("path");
        imageUri = Uri.parse(path);

        //display the photo
        imageView.setImageURI(Uri.fromFile(new File(path)));

        //Create a firebase reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }


    public void sendToCloud(View view) throws FileNotFoundException {

    }

    //Synchronisation of photos should happen automatically without user interaction.
    //Automatically uploads the current photo to the cloud whenever the user wants to save it locally
    // And then Back to the MainActivity through CameraActivity
    public void quit(View view) throws FileNotFoundException {
        //mkdir
        StorageReference Ref = mStorageRef.child("images/" + imageUri);

        //use stream to send
        InputStream stream = new FileInputStream(new File(String.valueOf(imageUri)));
        UploadTask uploadTask = Ref.putStream(stream);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //No Success report
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //No Failure report
            }
        });
        Intent intent = new Intent();
        intent.putExtra("path", path);
        setResult(-1, intent);
        finish();
    }
}
