package com.example.photoblog;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton add;
    private FirebaseAuth homeAuth;
    private FirebaseFirestore firestore;
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.tool);
        add = findViewById(R.id.floatingActionButton);
        homeAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PhotoBlog");
        if(homeAuth.getCurrentUser()!=null) {
            bottomNavigationView = findViewById(R.id.bottomNavigationView2);

            // FRAGMENTS
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            ChangeFragment(homeFragment);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.home:
                            ChangeFragment(homeFragment);
                            return true;
                        case R.id.notification:
                            ChangeFragment(notificationFragment);
                            return true;
                        case R.id.account:
                            ChangeFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }

                }
            });

        }
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivity(new Intent(HomeActivity.this,PostActivity.class));
            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
            finish();
        }else{
            firestore.collection("Users").document(homeAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            startActivity(new Intent(HomeActivity.this,SetupActivity.class));
                            finish();
                        }
                    }else  print(task.getException().getMessage());
                }
            });
        }
    }

    private void print(String message) {

        Toast.makeText(HomeActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(item.getItemId() == R.id.logOut){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
            finish();
        }else  if(item.getItemId() == R.id.setting){
            startActivity(new Intent(HomeActivity.this,SetupActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void ChangeFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frames,fragment);
        fragmentTransaction.commit();
    }
}
