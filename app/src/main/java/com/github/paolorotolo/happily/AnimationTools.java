package com.github.paolorotolo.happily;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;

public class AnimationTools {
    public static void startCircularReveal(View view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // start from bottom - right (FAB position)
            int cx = view.getWidth();
            int cy = view.getHeight();

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.max(view.getWidth(), view.getHeight()) + 20;

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            // make the view visible and start the animation
            view.setVisibility(View.VISIBLE);
            anim.setDuration(800);
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000);
            view.startAnimation(anim);
        }
    }

    public static void startFadeOutAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(1000);
        view.startAnimation(anim);
        view.setVisibility(View.INVISIBLE);
    }

    public static void startFadeInAnimation(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        view.startAnimation(anim);
    }
}