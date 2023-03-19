package site.fsyj.posture_indentify;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;

import site.fsyj.posture_indentify.listener.SensorListener;

public class MainActivity extends AppCompatActivity {
    private volatile boolean running = false;
    private final static String START_LOCK = "startLock";

    private PeriodicWorkRequest detectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.open_btn).setOnClickListener((view) -> {
            // Start the SensorService
            synchronized (START_LOCK) {
                if (!running) {
                    Intent intent = new Intent(this, SensorListener.class);
                    startService(intent);
                    running = true;
                }
            }
        });

        findViewById(R.id.close_btn).setOnClickListener((view) -> {
            Intent intent = new Intent(this, SensorListener.class);
            stopService(intent);
            running = false;
        });
    }
}