package es.natour.mememaker.database;

import android.provider.BaseColumns;

public class MemeContract {

    public MemeContract(){

    }

    public static abstract class MemesEntry implements BaseColumns {
        public static final String TABLE_NAME = "MEMES";
        public static final String COLUMN_ASSET = "asset";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CREATE_DATE = "create_date";
        public static final String COLUMN_ID = BaseColumns._ID;
    }

    public static abstract class AnnotationsEntry implements BaseColumns {
        public static final String TABLE_NAME = "ANNOTATIONS";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_X = "x";
        public static final String COLUMN_Y = "y";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_FK_MEME = MemesEntry.COLUMN_ID;
    }
}