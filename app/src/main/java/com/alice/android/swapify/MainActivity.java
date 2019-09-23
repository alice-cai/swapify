package com.alice.android.swapify;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.alice.android.swapify.TileAnimationUtil.*;

public class MainActivity extends AppCompatActivity {
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = () -> { };
    private final Runnable mShowPart2Runnable = () -> {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };
    private final Runnable mHideRunnable = () -> hide();


    // Global constants and variables for gameplay.
    public static final int NUM_ROWS = 5;
    public static final int NUM_COLS = 4;

    private TaskDelegateForShopifyRequest taskDelegate = new TaskDelegateForShopifyRequest();

    private ImageView[] imageViews = new ImageView[NUM_ROWS*NUM_COLS];
    private SwapifyTile[] swapifyTiles = new SwapifyTile[NUM_ROWS*NUM_COLS];
    private SwapifyTile firstSelection;

    private String matchCountLabel;
    private int matchCount = 0;
    private TextView mMatchCountTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delayedHideActionBar(0);
        showLoadingSplashScreen(true);

        // Start fetching Shopify images.
        new ShopifyImageFetcher(taskDelegate).execute();
    }

    private void initializeImageViews() {
        String[] imageViewIds = new String[NUM_ROWS*NUM_COLS];

        // Initialize imageViewIds with the resource IDs of every Swapify image tile.
        int index = 0;
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                imageViewIds[index] = "image_" + i + "_" + j;
                index++;
            }
        }

        // For each image tile, add a listener to handle game logic.
        for (int i = 0; i < (NUM_ROWS*NUM_COLS); i++) {
            int resID = getResources().getIdentifier(imageViewIds[i], "id", getPackageName());
            ImageView imageView = findViewById(resID);

            View.OnClickListener cardFlipListener = v -> {
                if (imageView.equals(v)) {
                    SwapifyTile thisSelection = null;

                    // Find the SwapifyTile object that the selected ImageView represents.
                    for (SwapifyTile imageCard: swapifyTiles) {
                        if (imageCard.getImageView().equals(imageView)) {
                            thisSelection = imageCard;
                            break;
                        }
                    }

                    if (thisSelection == null) {
                        Log.d("ERROR", "Unable to match card object. Aborting.");
                        return;
                    }

                    handleTileSelection(thisSelection);
                }
            };
            imageView.setOnClickListener(cardFlipListener);

            imageViews[i] = imageView;
            swapifyTiles[i] = new SwapifyTile();
        }
    }


    // Handles matching logic for the selected tile and updates the UI accordingly.
    private void handleTileSelection(SwapifyTile thisSelection) {
        if (firstSelection  == null) {
            // This is the first selection.
            flipFaceUp(thisSelection);
            firstSelection = thisSelection;
            return;
        } else if (firstSelection.equals(thisSelection)) {
            // Do not allow the user to de-select tiles.
            return;
        } else if (firstSelection.getCardId() == thisSelection.getCardId()) {
            // Match! Remove matched cards.
            flipFaceUp(thisSelection);

            // Disable touch events.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            matchCount++;
            removeTilesAfterDelay(firstSelection, thisSelection);

            // Update match count and re-enable touch events.
            new Handler().postDelayed(() -> {
                mMatchCountTextView.setText(String.format("%s: %d", matchCountLabel, matchCount));
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }, 500);

            if (matchCount == (NUM_ROWS*NUM_COLS)/2) {
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_win_game, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(dialogView).setCancelable(false);

                AlertDialog winnerAlert = builder.create();
                winnerAlert.show();

                new Handler().postDelayed(() -> {
                    winnerAlert.dismiss();
                    resetGame();
                }, 3000);
            }
        } else {
            // Incorrect pairing. Reflip both cards.
            flipFaceUp(thisSelection);

            // Disable touch events.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            reflipTilesAfterDelay(firstSelection, thisSelection);

            // Update match count and re-enable touch events.
            new Handler().postDelayed(() -> {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }, 500);
        }
        firstSelection = null;
    }

    private void showLoadingSplashScreen(boolean firstShuffle) {
        setContentView(R.layout.activity_loading_spash_screen);

        // Set the accent colour for the indeterminate loading bar
        ProgressBar loadingProgress = findViewById(R.id.loading_progress);
        loadingProgress.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        if (!firstShuffle) {
            ImageView shopifyLogoImageView = findViewById(R.id.loading_screen_img);
            shopifyLogoImageView.setImageResource(R.drawable.re_swapify);

            // change image size to scale for re-swapify logo
            shopifyLogoImageView.getLayoutParams().height = 670;
            shopifyLogoImageView.requestLayout();
        }
    }

    private void resetGame() {
        showLoadingSplashScreen(false);
        new ShopifyImageFetcher(taskDelegate).execute();
    }

    private void initializeGameScreen() {
        setContentView(R.layout.activity_main);

        // Reset match count and update UI.
        matchCount = 0;
        mMatchCountTextView = findViewById(R.id.match_count);
        matchCountLabel = getResources().getString(R.string.match_count_label);
        mMatchCountTextView.setText(String.format("%s: %d", matchCountLabel, matchCount));

        // Set listener for the reshuffling button.
        ImageView reshuffleButton = findViewById(R.id.reshuffle_button);
        View.OnClickListener reshuffleListener = v -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Do you want to reshuffle? All game progress will be lost.")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (DialogInterface dialog, int id) -> resetGame())
                    .setNegativeButton("No", (DialogInterface dialog, int id) -> dialog.cancel())
                    .setTitle("Restart Game")
                    .setIcon(R.drawable.shopify_logo)
                    .create().show();
        };
        reshuffleButton.setOnClickListener(reshuffleListener);

        initializeImageViews();
    }

    private void shuffleImageCards() {
        // implement the Fisher–Yates shuffling algorithm
        for (int i = 0; i < swapifyTiles.length - 1; i++) {
            int randomIndex = (int) (Math.random() * (swapifyTiles.length - i) + i);

            if (randomIndex == i) continue;

            // Swap the current SwapifyTile with the one at the randomly generated index.
            SwapifyTile temp = swapifyTiles[i];
            swapifyTiles[i] = swapifyTiles[randomIndex];
            swapifyTiles[randomIndex] = temp;
        }
    }

    private void displayImageCardsAndFlip() {
        // Update the grid of ImageView objects with the shuffled Shopify images.
        for (int i = 0; i < (NUM_ROWS*NUM_COLS); i++) {
            ImageView imageView = imageViews[i];
            Picasso.get()
                    // offset
                    .load(swapifyTiles[i].getImageSource())
                    .resize(200, 200)
                    .centerCrop()
                    .into(imageView);
            swapifyTiles[i].setImageView(imageView);
        }

        // Disable touch events when showing the cards for 2 seconds.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        // Flip cards face down after delay.
        new Handler().postDelayed(() -> {
            for (SwapifyTile tile: swapifyTiles) {
                flipFaceDown(tile);
            }

            // Re-enable touch events.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }, 2000);
    }

    private void startGame() {
        shuffleImageCards();
        displayImageCardsAndFlip();
    }



    // Hide action bar.
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHideActionBar(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // Task delegate to be notified when the ShopifyImageFetcher task has completed.
    public class TaskDelegateForShopifyRequest implements TaskDelegate {
        //private Runnable callback;

//        public TaskDelegateForShopifyRequest(Runnable callback) {
//            this.callback = callback;
//        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void taskCompletionResult(String[] result) {
            initializeGameScreen();

            // Initialize the image views and randomly assign a range of 10 images
            // from the Shopify product list (2 of each image).
            int index = (int) ((Math.random() * 25) + 15);
            for (int i = 0; i < (NUM_ROWS*NUM_COLS); i+=2) {
                swapifyTiles[i].setImageSource(result[index]);
                swapifyTiles[i+1].setImageSource(result[index]);

                swapifyTiles[i].setCardId(i);
                swapifyTiles[i+1].setCardId(i);
                index++;
            }

            startGame();
            //callback.run();
        }
    }
}
