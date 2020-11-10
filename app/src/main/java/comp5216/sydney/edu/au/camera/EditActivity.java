package comp5216.sydney.edu.au.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class EditActivity extends Activity {
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_view);

        //Get the id from MainActivity
        Intent intent = getIntent();
        id = intent.getIntExtra("id",-1);

        //Show the photo which is need to edit
        ImageView imgview = findViewById(R.id.editimage);
        String image_url = intent.getStringExtra("imagepath");
        if(image_url != null){
            imgview.setImageURI(Uri.parse(image_url));
        }
    }

    //Delete the photo
    public void delete(View view){
        Intent intent = new Intent();
        //return the photo id to the MainActivity
        intent.putExtra("id", id);
        setResult(-1, intent);
        finish();
    }

    //Back to the MainActivity
    public void back(View view){
        Intent intent = new Intent();
        setResult(0, intent);
        finish();
    }
}
