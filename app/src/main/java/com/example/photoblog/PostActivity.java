package com.example.photoblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {


    private Toolbar postToolbar;
    private ImageView imageView;
    private EditText desc;
    private Button post;
    private Uri postUri;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Bitmap compressedImageFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postToolbar = findViewById(R.id.postTool);
        imageView = findViewById(R.id.imageView);
        desc = findViewById(R.id.editText);
        post = findViewById(R.id.button2);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Post");
        firestore = FirebaseFirestore.getInstance();
        setSupportActionBar(postToolbar);
        getSupportActionBar().setTitle("New Post");
        // BACK BUTTON
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(PostActivity.this);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String Desc = desc.getText().toString();
                if (Desc.isEmpty()) {
                    desc.setError("Enter some Description");
                    desc.requestFocus();
                    return;
                }
                if(postUri == null){
                    print("Please Select An Image");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                final String time = FieldValue.serverTimestamp().toString();
                StorageReference path = storageReference.child(postUri.getLastPathSegment());
                path.putFile(postUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<UploadTask.TaskSnapshot> task) {
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        if (task.isSuccessful()) {

                            // COMPRESS THE IMAGE
                            File actualImageFile = new File(postUri.getPath());
                            try {
                                 compressedImageFile = new Compressor(PostActivity.this)
                                         .setMaxHeight(100)
                                         .setMaxWidth(100)
                                         .setQuality(5)
                                         .compressToBitmap(actualImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // FOR THUMBNAILS
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask thumb = storageReference.child("thumbs")
                                    .child(postUri.getLastPathSegment()).putBytes(data);
                            thumb.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                               @Override
                               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                   String thumbUri = taskSnapshot.getDownloadUrl().toString();
                                   // DATABASE

                                   Map<String, Object> map = new HashMap<>();
                                   map.put("postImage", downloadUri);
                                   map.put("thumb",thumbUri);
                                   map.put("Desc", Desc);
                                   map.put("user", auth.getCurrentUser().getUid());
                                   map.put("time", FieldValue.serverTimestamp());
                                   firestore.collection("Post").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                       @Override
                                       public void onComplete(@NonNull Task<DocumentReference> task) {
                                           if(task.isSuccessful()){

                                               print("Posted");
                                               startActivity(new Intent(PostActivity.this,HomeActivity.class));
                                               finish();
                                           }else
                                               print(task.getException().getMessage());
                                           progressBar.setVisibility(View.INVISIBLE);
                                       }
                                   });
                               }
                            }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {

                                   print(e.getMessage());
                               }
                           });

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            print(task.getException().getMessage());
                        }
                    }
                });
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postUri = result.getUri();
                imageView.setImageURI(postUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                print(error.getMessage());
            }
        }

    }

    private void print(String message) {
        Toast.makeText(PostActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
