package es.natour.mememaker.models;

/**
 * Created by Carlos Tessier on 30/12/14.
 */
import android.graphics.Bitmap;

public class ImageGridItem {
    private Bitmap image;
    private String filename;
    private String fullPath;

    public ImageGridItem(Bitmap image, String filename, String fullpath) {
        super();
        this.image = image;
        this.filename = filename;
        this.fullPath = fullpath;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFullPath() { return this.fullPath; }

    public void setFullPath(String fullPath) { this.fullPath = fullPath; }
}