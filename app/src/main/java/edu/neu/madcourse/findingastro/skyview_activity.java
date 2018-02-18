package edu.neu.madcourse.findingastro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by adityaponnada on 04/04/16.
 */
public class skyview_activity extends AppCompatActivity implements SensorEventListener {

    //Use this GCM: AIzaSyA0MLjpLA0T0XElMFvJRn-34RSeuIx6Vrc
    Float azimut;
    Float roll;
    Float pitch;

    MediaPlayer mMediaPlayer;

    FrameLayout frameLayout;
    CustomDrawableView mCustomDrawableView;

    Vibrator vibrator;

    boolean button_left = false;
    boolean button_right = false;

    // View to draw a compass
    public class CustomDrawableView extends View {
        Paint paint = new Paint();
        public CustomDrawableView(Context context) {
            super(context);
            paint.setColor(0xff00ff00);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
        };

        public CustomDrawableView(Context context, AttributeSet attributeSet){
             super(context, attributeSet);
            paint.setColor(0xff00ff00);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);

         };

        public CustomDrawableView(Context context, AttributeSet attributeSet, int default_style){
            super(context, attributeSet, default_style);
            paint.setColor(0xff00ff00);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
            //empty constructor
        };


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();
            int centerx = width/2;
            int centery = height/2;
            canvas.drawLine(centerx, 0, centerx, height, paint);
            canvas.drawLine(0, centery, width, centery, paint);
            // Rotate the canvas with the azimut
            if (azimut != null)
                canvas.rotate(-azimut*360/(2*3.14159f), centerx, centery);
            paint.setColor(0xffff0000);
            canvas.drawLine(centerx, -1000, centerx, +1000, paint);
            canvas.drawLine(-1000, centery, 1000, centery, paint);
            paint.setColor(0xff00ff00);
        }
    }
   //CustomDrawableView mCustomDrawableView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    //ImageView arrow_Right;
    ImageView arrow_feedback;

    Timer timer;

    ImageButton holdButton_left;
    ImageButton holdButton_right;
    ImageView target;

    public ArrayList<Animation> animationArray = new ArrayList<>();
    public ArrayList<Integer> direction_feedback = new ArrayList<>();

    ImageView astroImage;
    Animation left_to_right_animation, botton_to_up_animation, right_angle_top_right;

    Random random;
    int AnimArraySize;
    int min = 0;
    int randomNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // mCustomDrawableView = new CustomDrawableView(getApplicationContext());
        mCustomDrawableView = new CustomDrawableView(this);
        setContentView(R.layout.skyview);



        //********UI elements **********
        astroImage = (ImageView)findViewById(R.id.astro);

        holdButton_left = (ImageButton)findViewById(R.id.left_hold);
        holdButton_right = (ImageButton)findViewById(R.id.right_hold);
       // arrow_Right = (ImageView)findViewById(R.id.move_right);
        arrow_feedback = (ImageView)findViewById(R.id.arrow_feedback);

        target = (ImageView)findViewById(R.id.target);

        frameLayout = (FrameLayout)findViewById(R.id.frame_layout);
        frameLayout.addView(mCustomDrawableView);
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bells);

        //*****************************

        //************ Animation Declarations ******************
        botton_to_up_animation = AnimationUtils.loadAnimation(this, R.anim.botton_to_up);
        left_to_right_animation = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
        right_angle_top_right = AnimationUtils.loadAnimation(this, R.anim.right_angle_top_right);
        //*****************************************************

        //************* ALL ANIMATIONS HERE ***************
        animationArray.add(botton_to_up_animation);
        animationArray.add(left_to_right_animation);
        animationArray.add(right_angle_top_right);
        //*************************************************

        //******** Misc Declarations*********
        random = new Random();
        //***********************************

        //****** SENSING*******
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        //****************

        //******Add direction feedback here******
        direction_feedback.add(R.drawable.up_left_arrow);


        holdButton_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        button_left = true;
                        if (button_right == true) {
                            startTimer_main();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        button_left = false;
                        startTimer();
                        break;
                    }
                }
                return true;
            }
        });

        holdButton_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        button_right = true;

                        if (button_left == true){
                            startTimer_main();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("ACTION_UP", "LIFTED");
                        button_right = true;
                        startTimer();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stoptimertask();
    }

    @Override
    public void onPause() {
        super.onPause();

        stoptimertask();
        mSensorManager.unregisterListener(this);

        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();

    }

    public void startTimer_main(){
        timer = new Timer();
        AnimArraySize = animationArray.size();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        randomNumber = random.nextInt(AnimArraySize - 1);
                        astroImage.startAnimation(animationArray.get(randomNumber));
                        Log.d("RANDOMIZED", String.valueOf(randomNumber));
                        Log.d("RANDOMIZED_SIZE", String.valueOf(AnimArraySize));
                        arrow_feedback.setImageResource(direction_feedback.get(0));
                        arrow_feedback.setVisibility(View.VISIBLE);
                        Log.d("TIMER", "RUNNING");
                        stoptimertask();
                        startTimer_moveTime();
                    }
                });
                Log.d("TIMER", "STOPPED");

            }
        }, 2000, 10000);
        arrow_feedback.setVisibility(View.GONE);
    }
    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            Log.d("STOPPED", "TIMER STOPPED");
            timer = null;
        }
    }

    public void startTimer(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        astroImage.startAnimation(left_to_right_animation);
                        arrow_feedback.setImageResource(direction_feedback.get(0));
                        arrow_feedback.setVisibility(View.VISIBLE);


                        Log.d("TIMER", "RUNNING");
                        stoptimertask();
                    }
                });

                Log.d("TIMER", "STOPPED");
            }
        }, 2000, 100000000);
        arrow_feedback.setVisibility(View.GONE);
    }


    public void startTimer_moveTime(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Time's up for the move", Toast.LENGTH_LONG).show();
                        //astroImage.setVisibility(View.VISIBLE);
                        stoptimertask();
                    }
                });
                Log.d("MOVE TIMER", "STOPPED");
            }
        }, 5000, 10000);
    }

    float[] mGravity;
    float[] mGeomagnetic;


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[1];
                if (azimut >= 0.1 || azimut <= -0.1){
                    vibrator.vibrate(500);
                    Log.d("AZIMUT DETECTED", String.valueOf(azimut));
                }

                roll = orientation[0];

                if (roll >=3.0 || roll <= -3.0){
                    vibrator.vibrate(500);
                    Log.d("ROLL DETECTED", String.valueOf(roll));
                }
                pitch = orientation[2];
                if(pitch >= 3.0 || pitch <= -3.0){
                    vibrator.vibrate(500);
                    Log.d("PITCH DETECTED", String.valueOf(pitch));

                    mMediaPlayer.setVolume(0.5f, 0.5f);
                    mMediaPlayer.start();
                }
            }
        }
       mCustomDrawableView.invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}