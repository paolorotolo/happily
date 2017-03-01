package com.github.paolorotolo.happily;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private CameraSource mCameraSource;
    private int backgroundColor;
    private RelativeLayout background;

    private int colorAlpha = 0;

    private int mInterval = 500;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = (RelativeLayout) findViewById(R.id.background);
        int[] backgroundColors = new int[]{
                HappilyColors.MATERIAL_BLUE,
                HappilyColors.MATERIAL_DEEP_PURPLE,
                HappilyColors.MATERIAL_INDIGO,
                HappilyColors.MATERIAL_PINK,
                HappilyColors.MATERIAL_PURPLE,
                HappilyColors.MATERIAL_BLUE,
                HappilyColors.MATERIAL_RED,
                HappilyColors.MATERIAL_TEAL
        };

        backgroundColor = backgroundColors[new Random().nextInt(backgroundColors.length)];
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(true)
                .setProminentFaceOnly(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new LargestFaceFocusingProcessor(
                        detector,
                        new FaceTracker()));

        createCameraSource(detector);
        try {
            mCameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();
        startRepeatingTask();
    }


    //==============================================================================================
    // Camera Source
    //==============================================================================================

    /**
     * Creates the face detector and the camera.
     */
    private void createCameraSource(FaceDetector detector) {
        Context context = getApplicationContext();

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(24f)
                .build();
    }


    private class FaceTracker extends Tracker<Face> {
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, final Face face){
            if (face.getIsSmilingProbability() != -1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float alpha = face.getIsSmilingProbability()*255;
                        alpha = alpha + 70;
                        if (alpha > 255) {
                            alpha = 255;
                        }
                        colorAlpha = (int) alpha;
                    }
                });

            }
        }
    }

    private int previousColor = backgroundColor;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                // set to coloralpha
                Log.e("happily", "Setting alpha to "+ colorAlpha);
                int newColor = ColorUtils.setAlphaComponent(backgroundColor, colorAlpha);
                changeBackground(previousColor, newColor);
                previousColor = newColor;
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void changeBackground(int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                background.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
}
