package com.github.paolorotolo.happily;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GetHappyQuote.AsyncResponse  {
    private static final int PERMISSIONS_REQUEST_CAMERA = 01;
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

        background = (RelativeLayout) findViewById(R.id.activity_main_relative_background);
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Snackbar.make((RelativeLayout) findViewById(R.id.activity_main_relative_main), "We need camera permission to detect your smile :)", Snackbar.LENGTH_LONG);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.INTERNET},
                            PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                mCameraSource.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();
        startRepeatingTask();
        new GetHappyQuote(this).execute();
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

    @Override
    public void processFinish(String output) {
        // onFinish here
        TextView quoteTextView = (TextView) findViewById(R.id.activity_main_textview_quote);
        quoteTextView.setText(output);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mCameraSource.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You can't use Happily without camera. Please restart the app and allow camera permission.", Toast.LENGTH_LONG);
                    this.finish();

                }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
}
