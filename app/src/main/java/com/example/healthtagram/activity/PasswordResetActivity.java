package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.healthtagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordResetActivity extends AppCompatActivity {
    private Button backBtn, sendBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth= FirebaseAuth.getInstance();

        backBtn = findViewById(R.id.backBtn);
        sendBtn = findViewById(R.id.sendBtn);
        backBtn.setOnClickListener(onClickListener);
        sendBtn.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.sendBtn:
                    resetMail();
                    break;
                case R.id.backBtn:
                    finish();
                    break;
            }
        }
    };

    private void resetMail(){
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        if (email.length() >0) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "메일을 보냈습니다.",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }else{
            Toast.makeText(this, "올바른 이메일을 입력해주세요.",Toast.LENGTH_SHORT).show();
        }
    }
}
