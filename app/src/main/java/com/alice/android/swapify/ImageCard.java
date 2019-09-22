package com.alice.android.swapify;

import android.widget.ImageView;

public class ImageCard {
    private ImageView imageView;
    private int cardId;
    private String imageSource; // URL

    public ImageCard() {
        this.imageView = null;
        this.cardId = -1;
        this.imageSource = "";
    }

    public ImageCard(ImageView imageView, int cardId, String imageSource) {
        this.imageView = imageView;
        this.cardId = cardId;
        this.imageSource = imageSource;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView newImageView) {
        imageView = newImageView;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int newCardId) {
        cardId = newCardId;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String newImageSource) {
        imageSource = newImageSource;
    }

//    public boolean equals(ImageCard other) {
//        return other.getImageView() == imageView;
//    }

    public boolean checkMatch(ImageCard other) {
        return other.getCardId() == cardId;
    }
}
