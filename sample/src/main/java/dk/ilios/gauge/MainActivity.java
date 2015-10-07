package dk.ilios.gauge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import dk.ilios.gauge.gauge.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(dk.ilios.gauge.gauge.R.layout.activity_main);
    }
}
