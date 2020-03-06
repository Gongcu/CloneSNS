package com.example.healthtagram.messaging;

import android.util.Log;

import androidx.annotation.NonNull;
import com.example.healthtagram.database.PushData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMpush {
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static final String uri = "https://fcm.googleapis.com/fcm/send";
    private static final String serverKey = "AIzaSyD0BdAzyTCf7w-E9TiNCdmwHMJEt0rW3Ck";
    private Gson gson;
    private OkHttpClient httpClient;

    private FCMpush(){
        gson = new Gson();
        httpClient = new OkHttpClient();
    }

    public void sendMessage(String destinationUid, final String title, final String message){
        FirebaseFirestore.getInstance().collection("tokens").document(destinationUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String token = task.getResult().get("token").toString();
                    PushData pushData = new PushData(token);
                    pushData.getNotification().setBody(message);
                    pushData.getNotification().setTitle(title);

                    //okhttp를 이용한 http 통신
                    RequestBody body = RequestBody.create(mediaType,gson.toJson(pushData));
                    Request request = new Request.Builder().addHeader("Content-Type","application/json")
                            .addHeader("Authorization","key="+serverKey)
                            .url(uri).post(body).build();
                    httpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.e("DD",response.body().string());
                        }
                    });
                }
            }
        });
    }

    //싱글톤 패턴
    public static FCMpush getInstance(){
        return new FCMpush();
    }
}
