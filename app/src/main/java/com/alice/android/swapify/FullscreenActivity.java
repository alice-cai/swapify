package com.alice.android.swapify;

import android.annotation.SuppressLint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    //private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    // TODO: move these global declarations
    public static final int NUM_ROWS = 5;
    public static final int NUM_COLS = 4;
    private ImageView[] imageViews = new ImageView[NUM_ROWS*NUM_COLS];
    private ImageCard[] imageCards = new ImageCard[NUM_ROWS*NUM_COLS];

    private void initializeImageViews() {
        String[] imageViewIds = new String[NUM_ROWS*NUM_COLS];

        int index = 0;
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                imageViewIds[index] = "image_" + i + "_" + j;
                index++;
            }
        }

        for (int i = 0; i < (NUM_ROWS*NUM_COLS); i++) {
            int resID = getResources().getIdentifier(imageViewIds[i], "id", getPackageName());
            ImageView imageView = (ImageView) findViewById(resID);

            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    if (v.equals(imageView)) {
                        // Flip the card.
                        Picasso.get()
                                // TODO: reverse this
                                .load(R.drawable.shopify_cardback)
                                .resize(200, 200)
                                .centerCrop()
                                .into(imageView);
                    }
                }
            };
            imageView.setOnClickListener(clickListener);
            imageView.setVisibility(View.GONE);

            imageViews[i] = imageView;shuffleImageCards();
            imageCards[i] = new ImageCard();
        }
    }

    private void shuffleImageCards() {
        // implement the Fisher–Yates shuffling algorithm
        for (int i = 0; i < imageCards.length - 1; i++) {
            int randomIndex = (int) (Math.random() * (imageCards.length - i) + i);

            if (randomIndex == i) continue;

            // Swap the current ImageCard with the one at the randomly generated index.
            ImageCard temp = imageCards[i];
            imageCards[i] = imageCards[randomIndex];
            imageCards[randomIndex] = temp;

            Log.d("SHUFFLER", "swapping " + i + " and " + randomIndex);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        mVisible = true;

        initializeImageViews();

        ImageView shopifyLogoImageView = findViewById(R.id.shopify_logo);
        ProgressBar loadingProgress = findViewById(R.id.indeterminateBar);

        // Set the accent colour for the indeterminate loading bar
        loadingProgress.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        Runnable runnable = () -> {
            loadingProgress.setVisibility(View.GONE);
            shopifyLogoImageView.setVisibility(View.GONE);
            findViewById(R.id.swapify_header).setVisibility(View.VISIBLE);

            shuffleImageCards();

            // Update the grid of ImageView objects with the shuffled Shopify images.
            for (int i = 0; i < (NUM_ROWS*NUM_COLS); i++) {
                ImageView imageView = imageViews[i];
                Picasso.get()
                        // offset
                        .load(imageCards[i].getImageSource())
                        .resize(200, 200)
                        .centerCrop()
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
                imageCards[i].setImageView(imageView);
            }
        };

        TaskDelegateForShopifyRequest taskDelegate = new TaskDelegateForShopifyRequest(runnable);
        new ShopifyProductFetcher(taskDelegate, loadingProgress).execute();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public class TaskDelegateForShopifyRequest implements TaskDelegate {
        private Runnable callback;

        public TaskDelegateForShopifyRequest(Runnable callback) {
            this.callback = callback;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void taskCompletionResult(String[] result) {
            //String[] imageUrls = new String[NUM_ROWS*NUM_COLS];
            int index = (int) ((Math.random() * 25) + 15);

            for (int i = 0; i < (NUM_ROWS*NUM_COLS); i+=2) {
                imageCards[i].setImageSource(result[index]);
                imageCards[i+1].setImageSource(result[index]);

                imageCards[i].setCardId(i);
                imageCards[i+1].setCardId(i);
                index++;
            }

            callback.run();
        }
    }
}
