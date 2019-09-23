# Swapify

Swapify is a memory matching game for Android.

You will be shown a grid containing 10 pairs of images (randomly shuffled). You will have 2 seconds to look at the images before they are hidden. Test your memory by seeing how long it takes you to find all 10 matching pairs!

<p align="center">
    <img src="https://user-images.githubusercontent.com/34670205/65397663-e2f80180-dd7f-11e9-81ea-6febbcacbdc6.png" width="250"> &nbsp;
    <img src="https://user-images.githubusercontent.com/34670205/65397664-e2f80180-dd7f-11e9-908a-50cdb4b98885.png" width="250">
<p>

Each time the grid is reshuffled, a random set of 10 images will be selected from [Shopify's JSON endpoint](https://shopicruit.myshopify.com/admin/products.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6). The images are loaded into the grid using Square's [Picasso](https://github.com/square/picasso) library.

This project was created in Sept 2019 for [Shopify's mobile developer challenge](https://docs.google.com/document/d/1M2VsBSZr8696HU6mO3MWveSB7p3Do9lOkMrjT5nKiEg/edit).

