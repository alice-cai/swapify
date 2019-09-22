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

    public static void removeCardsAfterDelay(SwapifyTile card1, SwapifyTile card2) {
        new Handler().postDelayed(() -> {
            card1.getImageView().setVisibility(View.INVISIBLE);
            card2.getImageView().setVisibility(View.INVISIBLE);
        }, 500);
    }

    public static void reflipCardsAfterDelay(SwapifyTile card1, SwapifyTile card2) {
        new Handler().postDelayed(() -> {
            flipFaceUp(card1); // TODO: reverse
            flipFaceUp(card2); // TODO: reverse
        }, 500);
    }
}
