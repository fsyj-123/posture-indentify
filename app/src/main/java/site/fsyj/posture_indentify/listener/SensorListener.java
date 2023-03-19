package site.fsyj.posture_indentify.listener;

import static site.fsyj.posture_indentify.util.SystemConst.COLLECTION_INTERVAL;
import static site.fsyj.posture_indentify.util.SystemConst.COLLECTION_TIME;
import static site.fsyj.posture_indentify.util.SystemConst.LISTENING_DURATION;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import site.fsyj.posture_indentify.R;
import site.fsyj.posture_indentify.util.ServiceStateEnum;
import site.fsyj.posture_indentify.util.SystemConst;

public class SensorListener extends Service implements SensorEventListener {

    private static final String TAG = "SensorService";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;

    private float[] accValues = new float[3];
    private float[] gyroscopeValues = new float[3];

    private Timer timer;
    private TimerTask timerTask;


    /**
     * 旋转矩阵，用来保存磁场和加速度的数据
     */
    private float r[] = new float[9];
    // 模拟方向传感器的数据（原始数据为弧度）
    private float values[] = new float[3];

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: start");
        String channelId = createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(SystemConst.CONTENT_TITLE)
                .setContentText(ServiceStateEnum.RUNNING.getState())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.favicon_16x16);

        startForeground(1, builder.build());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Start the timer and schedule the timer task
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "task run");
                stopListening();
                startListening();
            }
        };
        timer.schedule(timerTask, COLLECTION_TIME * 1000, COLLECTION_INTERVAL * 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 获取手机触发event的传感器的类型
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                gyroscopeValues = event.values.clone();
                break;
        }

        SensorManager.getRotationMatrix(r, null, accValues, gyroscopeValues);
        SensorManager.getOrientation(r, values);

        // 获取　沿着Z轴转过的角度
        float azimuth = values[0];

        // 获取　沿着X轴倾斜时　与Y轴的夹角
        float pitchAngle = values[1];

        // 获取　沿着Y轴的滚动时　与X轴的角度
        //此处与官方文档描述不一致，所在加了符号（https://developer.android.google.cn/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])）
        float rollAngle = -values[2];

        String data = "角度： azimuth：" + azimuth + ", pitchAngle" + pitchAngle + ", rollAngle" + rollAngle;

        Log.i("TAG", data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel the timer and the timer task
        timer.cancel();
        timerTask.cancel();

        // Unregister the sensor listeners
        stopListening();
    }

    private void startListening() {
        // Register the sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);

        // Schedule a timer task to stop listening after COLLECTION_DURATION
        try {
            TimeUnit.SECONDS.sleep(LISTENING_DURATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        stopListening();
    }

    private void stopListening() {
        // Unregister the sensor listeners
        sensorManager.unregisterListener(this);
    }


    /**
     * 创建Channel， 并返回channel ID
     * @return
     */
    private String createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = getString(R.string.chanelName);
        String description = getString(R.string.chanelDescription);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        String chanelId = UUID.randomUUID().toString();

        NotificationChannel channel = new NotificationChannel(chanelId, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        return chanelId;
    }

}
