package es.natour.mememaker.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Carlos Tessier on 30/12/14.
 */
public class ImageItem {
    private Bitmap mBitmap;
    private String mText;

    public ImageItem(String filePath, String text) {
        mBitmap = BitmapFactory.decodeFile(filePath);
        mText = text;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public String getText() {
        return mText;
    }
}
