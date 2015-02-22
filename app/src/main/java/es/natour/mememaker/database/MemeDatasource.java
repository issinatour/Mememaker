package es.natour.mememaker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.Date;
import es.natour.mememaker.models.Meme;
import es.natour.mememaker.models.MemeAnnotation;


public class MemeDatasource {
    private Context mContext;
    private MemeSQLiteHelper mMemeSqlLiteHelper;

    public MemeDatasource(Context context) {
        mContext = context;
        mMemeSqlLiteHelper = new MemeSQLiteHelper(mContext);
    }

    public SQLiteDatabase openReadable() {
        return mMemeSqlLiteHelper.getReadableDatabase();
    }

    public SQLiteDatabase openWriteable() {
        return mMemeSqlLiteHelper.getWritableDatabase();
    }

    public void close(SQLiteDatabase database) {
        database.close();
    }



    public ArrayList<Meme> read() {
        ArrayList<Meme> memes = readMemes();
        addMemeAnnotations(memes);
        return memes;
    }
    public ArrayList<Meme> readMemes() {
        SQLiteDatabase database = openReadable();
        Cursor cursor = database.query(
                MemeContract.MemesEntry.TABLE_NAME, // Nombre de la tabla
                new String[]{MemeContract.MemesEntry.COLUMN_NAME, BaseColumns._ID, MemeContract.MemesEntry.COLUMN_ASSET}, // Nombre de columnas a devolver
                null, // selection
                null, // selection Args
                null, //Group by
                null, // Having
                MemeContract.MemesEntry.COLUMN_CREATE_DATE + " DESC" // Ordenados por fecha descendiente
        );
        ArrayList<Meme> memes = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Meme meme = new Meme(getIntFromColumnName(cursor, BaseColumns._ID),
                            getStringFromColumnName(cursor, MemeContract.MemesEntry.COLUMN_ASSET),
                            getStringFromColumnName(cursor, MemeContract.MemesEntry.COLUMN_NAME),
                            null);
                memes.add(meme);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return memes;
    }

    public void addMemeAnnotations(ArrayList<Meme> memes) {
        SQLiteDatabase database = openReadable();
        ArrayList<MemeAnnotation> annotations;
        Cursor cursorMeme;
        MemeAnnotation annotation;

        for (Meme meme : memes) {
            annotations = new ArrayList<>();

            cursorMeme = database.rawQuery("SELECT * " +
                                       " FROM " + MemeContract.AnnotationsEntry.TABLE_NAME +
                                       " WHERE " + MemeContract.AnnotationsEntry.COLUMN_FK_MEME + " = " + meme.getId(), null);

            if (cursorMeme.moveToFirst()) {
                do {
                    annotation = new MemeAnnotation(getIntFromColumnName(cursorMeme, BaseColumns._ID),
                                 getStringFromColumnName(cursorMeme, MemeContract.AnnotationsEntry.COLUMN_COLOR),
                                 getStringFromColumnName(cursorMeme, MemeContract.AnnotationsEntry.COLUMN_TITLE),
                                 getIntFromColumnName(cursorMeme, MemeContract.AnnotationsEntry.COLUMN_Y),
                                 getIntFromColumnName(cursorMeme, MemeContract.AnnotationsEntry.COLUMN_X)
                    );
                    annotations.add(annotation);
                } while (cursorMeme.moveToNext());
                meme.setAnnotations(annotations);
                cursorMeme.close();
            }
        }
        database.close();
    }
    private int getIntFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }


    private String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }


    public void creatememe(Meme meme) {
        SQLiteDatabase database = openWriteable();
        database.beginTransaction();
        ContentValues memeValues = new ContentValues();
        memeValues.put(MemeContract.MemesEntry.COLUMN_NAME, meme.getName());
        memeValues.put(MemeContract.MemesEntry.COLUMN_ASSET, meme.getAssetLocation());
        memeValues.put(MemeContract.MemesEntry.COLUMN_CREATE_DATE, new Date().getTime());
        long memeId = database.insert(MemeContract.MemesEntry.TABLE_NAME, null, memeValues);
        for (MemeAnnotation memeAnnotation : meme.getAnnotations()) {
            ContentValues annotationValues = new ContentValues();
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_TITLE, memeAnnotation.getTitle());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_X, memeAnnotation.getLocationX());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_Y, memeAnnotation.getLocationY());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_COLOR, memeAnnotation.getColor());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_FK_MEME, memeId);
            database.insert(MemeContract.AnnotationsEntry.TABLE_NAME, null, annotationValues);
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }

    public void update(Meme meme) {
        SQLiteDatabase database = openWriteable();
        database.beginTransaction();

        ContentValues updateMemeValues = new ContentValues();
        updateMemeValues.put(MemeContract.MemesEntry.COLUMN_NAME, meme.getName());

        database.update(MemeContract.MemesEntry.TABLE_NAME,
                updateMemeValues,
                String.format("%s=%d", BaseColumns._ID, meme.getId()),
                null);
        for (MemeAnnotation memeAnnotation : meme.getAnnotations()) {
            ContentValues updateAnnotation = new ContentValues();
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_TITLE, memeAnnotation.getTitle());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_X, memeAnnotation.getLocationX());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_Y, memeAnnotation.getLocationY());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_COLOR, memeAnnotation.getColor());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_FK_MEME, meme.getId());
            if(memeAnnotation.hasBeenSaved()) {
                database.update(MemeContract.AnnotationsEntry.TABLE_NAME,
                        updateAnnotation,
                        String.format("%s=%d", MemeContract.AnnotationsEntry.COLUMN_FK_MEME, memeAnnotation.getId()),
                        null);
            }
            else{
                database.insert(MemeContract.AnnotationsEntry.TABLE_NAME, null, updateAnnotation);
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }

    public void delete(int memeId){
        SQLiteDatabase database = openWriteable();
        database.beginTransaction();
        database.delete(MemeContract.AnnotationsEntry.TABLE_NAME,
                String.format("%s=%d", MemeContract.AnnotationsEntry.COLUMN_FK_MEME, memeId),
                null);
        database.delete(MemeContract.MemesEntry.TABLE_NAME,
                String.format("%s=%d", BaseColumns._ID, memeId),
                null);

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }
}