package com.github.paolorotolo.happily;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private CameraSource mCameraSource;
    private int backgroundColor;
    RelativeLayout background;

    private int colorSaturation = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = (RelativeLayout) findViewById(R.id.background);

        backgroundColor = Color.RED;
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

        changeBackgroundColor();
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
        int backgroundColorPrevious = Color.RED;

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, final Face face){
            if (face.getIsSmilingProbability() != -1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float alpha = face.getIsSmilingProbability()*255;
                        colorSaturation = (int) alpha;
                    }
                });

            }
        }
    }

    private int previousColor = Color.RED;

    private void changeBackgroundColor(){
        new Thread() {
            int hue = 0 ;
            int saturation = 200;
            int value = 120; //adjust as per your need

            public void run() {
                for ( hue = 0; hue < 255; hue++) {
                    try {
                        sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Now form a hsv float array from the 3 variables
                                float[] hsv = {hue , saturation, value};

                                //make color from that float array
                                int cl = Color.HSVToColor(hsv);

                                changeBackground(previousColor, ColorUtils.setAlphaComponent(cl, colorSaturation));
                                previousColor = ColorUtils.setAlphaComponent(cl, colorSaturation);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

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
}
