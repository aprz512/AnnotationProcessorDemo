package com.aprz.annotationprocessor;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aprz.annotataion.NewIntent;

/**
 * @author by liyunlei
 * <p>
 * write on 2019/7/16
 * <p>
 * Class desc:
 */
@NewIntent
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }
}
