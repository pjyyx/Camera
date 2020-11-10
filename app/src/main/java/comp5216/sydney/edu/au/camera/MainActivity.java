package comp5216.sydney.edu.au.camera;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    ArrayList<File> allPhoto = new ArrayList<>();

    private GridView photodisplay;
    private Button camera;
    private GridAdapter gridadapter;
    public Uri uri;
    private StorageReference mStorageRef;
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find all the photos taken
        File photo = new File(this.getExternalFilesDir(null)
                .getAbsolutePath()+"/images/");
        if(photo.exists()){
            findAllImage(photo);
        }

        //Create a firebase reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Show the taken photos in a custom adapter
        photodisplay = findViewById(R.id.photodisplay);
        gridadapter = new GridAdapter();
        photodisplay.setAdapter(gridadapter);

        //Click to view the photos that have been taken
        photodisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                if (intent != null) {
                    //Pass parameters
                    intent.putExtra("imagepath", allPhoto.get(i).toString());
                    intent.putExtra("id", i);
                }
                startActivityForResult(intent,2);
            }
        });

        //Enter the photo page
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTakePhotoClick();
            }
        });
    }

    //a button to allow user to immediately
    // synchronise the local photo library with the cloud server by stream
    public void synchroniseStream(View view) throws FileNotFoundException {
        for (int i=0;i<allPhoto.size();i++){
            uri = Uri.parse(allPhoto.get(i).toString());
            //mkdir
            StorageReference Ref = mStorageRef.child("images/" + uri);
            String filepath = String.valueOf(uri);

            //use stream to send
            InputStream stream = new FileInputStream(new File(filepath));
            UploadTask uploadTask = Ref.putStream(stream);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Success report
                    Toast.makeText(getApplicationContext(),"Success to upload",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Failure report
                    Toast.makeText(getApplicationContext(),"Fail to upload",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //a button to allow user to immediately
    // synchronise the local photo library with the cloud server by file
    public void synchroniseFile(View view) throws FileNotFoundException {
        for (int i=0;i<allPhoto.size();i++){
            uri = Uri.fromFile(allPhoto.get(i));
            //mkdir
            StorageReference Ref = mStorageRef.child("images/" + uri);

            //use file to send
            UploadTask uploadTask = Ref.putFile(uri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Success report
                    Toast.makeText(getApplicationContext(),"Success to upload",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Failure report
                    Toast.makeText(getApplicationContext(),"Fail to upload",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    //Find all photos under this path
    public void findAllImage(File photo) {
        try {
            String[] file01 = photo.list();
            for (int i = 0; i < file01.length; i++) {
                File file02 = new File(photo + "/" + file01[i]);

                if (file02.isDirectory()) {
                    //Iteration
                    findAllImage(file02);
                } else if (file02.getName().endsWith(".jpg")) {
                    allPhoto.add(file02);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    //Custom gridAdapter
    class GridAdapter extends BaseAdapter {
        @Override
        public Object getItem(int i) {
            return allPhoto.get(i);
        }

        @Override
        public int getCount() {
            return allPhoto.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        //Custom gridView
        @Override
        public View getView(int i, View view, ViewGroup viewgroup) {
            view = getLayoutInflater().inflate(R.layout.photodisplay,
                    viewgroup, false);
            ImageView imageView = view.findViewById(R.id.photoImg);
            imageView.setImageURI(Uri.parse(getItem(i).toString()));
            return view;
        }
    }

    //Enter the photo page
    public void onTakePhotoClick() {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()
                || !marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent,1);
        }
    }

    //
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode=1 means the new photo is taken
        if(requestCode==1){
            if (resultCode == -1) {
                File path = new File(data.getStringExtra("path"));
                allPhoto.add(path);
                gridadapter.notifyDataSetChanged();
            }
        }
        //requestCode=2 means the old photo will be deleted
        else if(requestCode==2){
            if(resultCode ==-1){
                allPhoto.get(data.getIntExtra("id",-1)).delete();
                allPhoto.remove(data.getIntExtra("id",-1));
                gridadapter.notifyDataSetChanged();
            }
        }
    }
}







