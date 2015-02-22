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






    // Devuelve un Arraylist con todos los meses y todas las anotaciones puestas
    public ArrayList<Meme> read() {
        // Recojo todos los memes de la base de datos
        ArrayList<Meme> memes = readMemes();
        // Añado las anotaciones a estos
        addMemeAnnotations(memes);
        // Los devuelvo con las anotaciones puestas
        return memes;
    }






    // Devuelve un Arraylist con todos los memes ordenados por fecha descendiente
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

        // En caso de que se haya devuelto al menos un valor
        if (cursor.moveToFirst()) {
            do {
                // Recojo todas las columnas del meme
                Meme meme = new Meme(getIntFromColumnName(cursor, BaseColumns._ID),
                            getStringFromColumnName(cursor, MemeContract.MemesEntry.COLUMN_ASSET),
                            getStringFromColumnName(cursor, MemeContract.MemesEntry.COLUMN_NAME),
                            null);
                // Añado el meme al ArrayList
                memes.add(meme);
            }
            // Mientras haya siguiente
            while (cursor.moveToNext());
        }

        // Cierro cursor y database
        cursor.close();
        database.close();

        return memes;
    }






    // Añade las anotaciones a todos los memes
    public void addMemeAnnotations(ArrayList<Meme> memes) {
        SQLiteDatabase database = openReadable();
        ArrayList<MemeAnnotation> annotations;
        Cursor cursor;
        MemeAnnotation annotation;

        // Recorro el Arraylist de memes
        for (Meme meme : memes) {
            annotations = new ArrayList<>();

            // Busco las anotaciones de cada meme en la tabla ANNOTATIONS a traves del ID
            cursor = database.rawQuery("SELECT * " +
                                       "FROM " + MemeContract.AnnotationsEntry.TABLE_NAME +
                                       "WHERE " + MemeContract.AnnotationsEntry.COLUMN_FK_MEME + " = " + meme.getId(), null);

            // En caso de que se haya devuelto al menos un valor
            if (cursor.moveToFirst()) {
                // Recojo todas las columnas de la anotacion
                do {
                    annotation = new MemeAnnotation(getIntFromColumnName(cursor, BaseColumns._ID),
                                 getStringFromColumnName(cursor, MemeContract.AnnotationsEntry.COLUMN_COLOR),
                                 getStringFromColumnName(cursor, MemeContract.AnnotationsEntry.COLUMN_TITLE),
                                 getIntFromColumnName(cursor, MemeContract.AnnotationsEntry.COLUMN_Y),
                                 getIntFromColumnName(cursor, MemeContract.AnnotationsEntry.COLUMN_X)
                    );
                    // Y la añado al ArrayList donde voy a guardar cada una
                    annotations.add(annotation);
                // Mientras haya siguiente
                } while (cursor.moveToNext());

                // Ahora asigno todas las anotaciones correspondientes al meme
                meme.setAnnotations(annotations);

                // Y cierro el cursor
                cursor.close();
            }
        }
        // Cierro la base de datos
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








    // Inserta en la base de datos un meme
    public void create(Meme meme) {
        SQLiteDatabase database = openWriteable();
        database.beginTransaction();

        // Asigno los valores del meme a un ContentValues
        ContentValues memeValues = new ContentValues();
        memeValues.put(MemeContract.MemesEntry.COLUMN_NAME, meme.getName());
        memeValues.put(MemeContract.MemesEntry.COLUMN_ASSET, meme.getAssetLocation());
        memeValues.put(MemeContract.MemesEntry.COLUMN_CREATE_DATE, new Date().getTime());

        // Recojo el ID del meme recien insertado para asignarlo a la Foreign Key de la anotacion mas adelante
        long memeId = database.insert(MemeContract.MemesEntry.TABLE_NAME, null, memeValues);

        // Asigno por cada anotacion del meme
        for (MemeAnnotation memeAnnotation : meme.getAnnotations()) {
            // los valores de las anotaciones del meme a un ContentValues
            ContentValues annotationValues = new ContentValues();
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_TITLE, memeAnnotation.getTitle());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_X, memeAnnotation.getLocationX());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_Y, memeAnnotation.getLocationY());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_COLOR, memeAnnotation.getColor());
            annotationValues.put(MemeContract.AnnotationsEntry.COLUMN_FK_MEME, memeId);

            // Inserto en la base de datos
            database.insert(MemeContract.AnnotationsEntry.TABLE_NAME, null, annotationValues);
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }








    // Actualiza un meme de la base de datos
    public void update(Meme meme) {
        SQLiteDatabase database = openWriteable();
        database.beginTransaction();

        // Recojo el meme a actualizar
        ContentValues updateMemeValues = new ContentValues();
        updateMemeValues.put(MemeContract.MemesEntry.COLUMN_NAME, meme.getName());

        // Actualizo los valores del meme
        database.update(MemeContract.MemesEntry.TABLE_NAME,
                updateMemeValues,
                String.format("%s=%d", BaseColumns._ID, meme.getId()),
                null);

        // Asigno por cada anotacion del meme
        for (MemeAnnotation memeAnnotation : meme.getAnnotations()) {
            // los valores de las anotaciones del meme a un ContentValues
            ContentValues updateAnnotation = new ContentValues();
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_TITLE, memeAnnotation.getTitle());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_X, memeAnnotation.getLocationX());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_Y, memeAnnotation.getLocationY());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_COLOR, memeAnnotation.getColor());
            updateAnnotation.put(MemeContract.AnnotationsEntry.COLUMN_FK_MEME, meme.getId());
            // En caso de que tenga ya anotaciones uso el update
            if(memeAnnotation.hasBeenSaved()) {
                database.update(MemeContract.AnnotationsEntry.TABLE_NAME,
                        updateAnnotation,
                        String.format("%s=%d", MemeContract.AnnotationsEntry.COLUMN_FK_MEME, memeAnnotation.getId()),
                        null);
            }
            // Y si no existen entonces hago un insert
            else{
                database.insert(MemeContract.AnnotationsEntry.TABLE_NAME, null, updateAnnotation);
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }








    // Borra un meme de la base de datos
    public void delete(int memeId){
        SQLiteDatabase database = openWriteable();
        database.beginTransaction();

        // Borro las anotaciones del meme
        database.delete(MemeContract.AnnotationsEntry.TABLE_NAME,
                String.format("%s=%d", MemeContract.AnnotationsEntry.COLUMN_FK_MEME, memeId),
                null);

        // Borro el meme
        database.delete(MemeContract.MemesEntry.TABLE_NAME,
                String.format("%s=%d", BaseColumns._ID, memeId),
                null);

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }
}