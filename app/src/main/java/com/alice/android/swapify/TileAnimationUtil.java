package com.alice.android.swapify;

import android.os.Handler;
import android.view.View;

import com.squareup.picasso.Picasso;

public class TileAnimationUtil {
    public static void flipFaceUp(SwapifyTile card) {
        Picasso.get()
                .load(card.getImageSource())
                .resize(200, 200)
                .centerCrop()
                .into(card.getImageView());
    }

    public static void flipFaceDown(SwapifyTile card) {
        Picasso.get()
                .load(R.drawable.shopify_cardback)
                .resize(200, 200)
                .centerCrop()
                .into(card.getImageView());
    }

    public static void removeTilesAfterDelay(SwapifyTile card1, SwapifyTile card2) {
        new Handler().postDelayed(() -> {
            card1.getImageView().setVisibility(View.INVISIBLE);
            card2.getImageView().setVisibility(View.INVISIBLE);
        }, 500);
    }

    public static void reflipTilesAfterDelay(SwapifyTile card1, SwapifyTile card2) {
        new Handler().postDelayed(() -> {
            flipFaceDown(card1);
            flipFaceDown(card2);
        }, 500);
    }
}
