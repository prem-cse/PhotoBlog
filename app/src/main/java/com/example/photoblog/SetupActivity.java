package com.example.photoblog;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView circleImageView;
    private Uri imageUri = null;
    private EditText name;
    private static final int GALLERY_INTENT = 123;
    private Button finish;
    private FirebaseAuth setupAuth;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        toolbar = findViewById(R.id.setup_toolbar);
        circleImageView = findViewById(R.id.circleImageView);
        name = findViewById(R.id.name);
        finish = findViewById(R.id.finish);
        progressDialog = new ProgressDialog(this);
        setupAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("PhotoBlog");
        firestore = FirebaseFirestore.getInstance();


        // LOAD THE PREVIOUS DATA
        progressDialog.setMessage("Loading");
        progressDialog.show();
        finish.setBackgroundColor(getResources().getColor(R.color.grey));
        finish.setEnabled(false);
        firestore.collection("Users").document(setupAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    // IF DATA EXISTS THEN WE WANT TO RETRIEVE DATA
                    if (task.getResult().exists()) {

                        String Name = task.getResult().getString("name");
                        String Image = task.getResult().getString("userImage");
                        name.setText(Name);
                        // OFFLINE CAPABILITIES:  INITIALLY NULL THEN THE SELECTED URI
                        imageUri = Uri.parse(Image);
                        RequestOptions placeholders = new RequestOptions();
                        placeholders.placeholder(R.mipmap.dp);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholders).load(Image).into(circleImageView);


                    }
                } else {
                    print(task.getException().getMessage());
                }
                progressDialog.dismiss();
                finish.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                finish.setEnabled(true);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String Name = name.getText().toString();
                if (Name.isEmpty()) {
                    name.setError("Enter your name");
                    name.requestFocus();
                    return;
                }
                progressDialog.setMessage("Almost Done...");
                progressDialog.show();
                // IF IMAGE IS CHANGED THEN STORE BOTH ELSE ONLY NAME
                if (isChanged) {

                    if (imageUri != null) {

                        final StorageReference path = storageReference.child(imageUri.getLastPathSegment());
                        path.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    store(task, Name);
                                } else print(task.getException().getMessage());
                            }
                        });
                    }

                }else store(null,Name);
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Setup");

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if user runs on marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        //print("Access Denied");
                    } else {
                        //GREAT TOOL
                        //galleryPicker(); dont need a picker it will do for us
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(SetupActivity.this);
                    }
                } else {
                    // lower version
                    galleryPicker();

                }
            }
        });
    }

    private void galleryPicker() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                circleImageView.setImageURI(imageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                print(error.getMessage());
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // on allowing some permission
    }

    private void print(String message) {
        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
    }


    private void store(Task<UploadTask.TaskSnapshot> task, String Name) {
        Uri downloadUri;
        if(task != null) {
             downloadUri = task.getResult().getDownloadUrl();
        }else downloadUri = imageUri;

        Map<String,Object> map = new HashMap<>();
        map.put("name",Name);
        map.put("userImage",downloadUri.toString());
        firestore.collection("Users").document(setupAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    startActivity(new Intent(SetupActivity.this,HomeActivity.class));
                    finish();
                }else{
                    progressDialog.dismiss();
                    print(task.getException().getMessage());

                }
            }
        });
    }
}