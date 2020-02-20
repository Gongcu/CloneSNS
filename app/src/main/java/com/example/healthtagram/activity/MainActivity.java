package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.healthtagram.R;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.fragment.HistoryFragment;
import com.example.healthtagram.fragment.HomeFragment;
import com.example.healthtagram.fragment.ProfileFragment;
import com.example.healthtagram.fragment.SearchFragment;
import com.example.healthtagram.fragment.UploadFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;


public class MainActivity extends AppCompatActivity {
    public static Context context;
    public static String TAG = "MAINACTIVITY";
    public static int UPLOAD = 100;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private SearchFragment fragmentSearch = new SearchFragment();
    private UploadFragment fragmentUpload = new UploadFragment();
    private HistoryFragment fragmentHistory = new HistoryFragment();
    private ProfileFragment fragmentProfile = new ProfileFragment();
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSION_CODE = 1000;
    public static final int FIRST_ACCCESS =10;

    /*
     * Hash Key: nhdcW85xtLEbd2HTEJW0p1Z9Z7I=
     * */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
        }
        user = FirebaseAuth.getInstance().getCurrentUser(); //현재 유저 가져오기
        db= FirebaseFirestore.getInstance();
        if (user == null) {
            startActivity(LoginActivity.class);
        } else {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    if(userData==null){
                        Intent intent = new Intent(getApplicationContext(),EditProfileActivity.class);
                        intent.putExtra("state",FIRST_ACCCESS);
                        startActivity(intent);
                    }
                }

            });
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }


    private void startActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
             FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.action_home:
                    transaction.replace(R.id.frameLayout, fragmentHome).commitAllowingStateLoss();
                    break;
                case R.id.action_search:
                    transaction.replace(R.id.frameLayout, fragmentSearch).commitAllowingStateLoss();
                    break;
                case R.id.action_exercise:
                    Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
                    startActivityForResult(intent, UPLOAD);
                    break;
                case R.id.action_heart:
                    transaction.replace(R.id.frameLayout, fragmentHistory).commitAllowingStateLoss();
                    break;
                case R.id.action_profile:
                    Bundle bundle = new Bundle();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    bundle.putString("destinationUid",uid);
                    fragmentProfile.setArguments(bundle);
                    transaction.replace(R.id.frameLayout, fragmentProfile).commitAllowingStateLoss();
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD && resultCode == RESULT_OK) {
            Log.e("성공", requestCode + "  " + resultCode);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameLayout, fragmentHome).commitAllowingStateLoss();
            bottomNavigationView.getMenu().getItem(0).setChecked(true); //클릭 표시

            //https://stackoverflow.com/questions/40236786/set-initially-selected-item-index-id-in-bottomnavigationview/43278541
            //bottomNavigationView.setSelectedItemId(R.id.action_exercise); 이 코드는 적용 안됨
        } else {
            Log.e("request, result", requestCode + "  " + resultCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                //허용됨
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "권한을 승인하셔야 앱이 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
                }
                return;
            }
        }

    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void callFragmentUpdateMethod(int state) {
        if(state!=FIRST_ACCCESS) {
            ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            fragment.updateProfile(user.getUid());
        }
    }
}
