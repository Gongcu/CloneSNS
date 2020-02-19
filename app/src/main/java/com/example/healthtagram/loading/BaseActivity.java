package com.example.healthtagram.loading;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by TedPark on 2017. 3. 18..
 */

public class BaseActivity extends AppCompatActivity {


    public void progressON() {
        BaseApplication.getInstance().progressON(this);
    }

    public void progressOFF() {
        BaseApplication.getInstance().progressOFF();
    }

}