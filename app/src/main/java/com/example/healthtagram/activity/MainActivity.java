package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.healthtagram.R;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.fragment.HistoryFragment;
import com.example.healthtagram.fragment.HomeFragment;
import com.example.healthtagram.fragment.ProfileFragment;
import com.example.healthtagram.fragment.SearchFragment;
import com.example.healthtagram.messaging.NotificationGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static Context context;
    public static int UPLOAD = 100;
    private Button logoutBtn;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private LinearLayout mainLayout;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private SearchFragment fragmentSearch = new SearchFragment();
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
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        if (!hasPermissions(this, permissions))
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);

        user = FirebaseAuth.getInstance().getCurrentUser(); //현재 유저 가져오기
        firestore= FirebaseFirestore.getInstance();
        if (user == null) {
            startActivity(LoginActivity.class);
            finish();
        } else {
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    if(userData==null){
                        Intent intent = new Intent(getApplicationContext(),EditProfileActivity.class);
                        intent.putExtra("state",FIRST_ACCCESS);
                        startActivity(intent);
                    }
                    registerPushToken();
                }

            });
        }
        mainLayout = findViewById(R.id.main_layout);
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(onClickListener);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private void registerPushToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult().getToken();
                        Map<String,Object> map = new HashMap<>();
                        map.put("token",token);
                        String uid = user.getUid();
                        FirebaseFirestore.getInstance().collection("tokens").document(uid).set(map);
                    }
                });
    }

    private void startActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.logoutBtn:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(LoginActivity.class);
                    finish();
                    break;
            }
        }
    };

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
            fragmentHome=new HomeFragment();
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
        Log.e("grans.size",grantResults.length+"");
        if(requestCode==PERMISSION_CODE && grantResults.length ==permissions.length ) {
            boolean check_result = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if(!check_result){
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[1])||ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[2])) {
                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    NotificationGenerator generator = new NotificationGenerator(this,"권한을 허용해야 앱 사용이 가능합니다.");

                }else {
                    // “다시 묻지 않음”을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    NotificationGenerator generator = new NotificationGenerator(this,"설정에서 앱 권한을 허용해야 앱 사용이 가능합니다.");
                }
                finish();
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
    public void changeBottomNavigationItem(int position){
        bottomNavigationView.getMenu().getItem(position).setChecked(true); //클릭 표시
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        int selectedItemId = bottomNavigationView.getSelectedItemId();
        if (R.id.action_home != selectedItemId) {
            setHomeItem(MainActivity.this); //home이 아닐 경우 home으로 이동
        } else {
            super.onBackPressed();//home일 경우 일반 종료
            finish();
        }
    }

    public static void setHomeItem(Activity activity) {
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                activity.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }


}
