/*
 * Copyright (C) 2018 Pixel Experience
 * Copyright (C) 2019 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.aoscp.miservices.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import co.aoscp.miservices.providers.database.HistoryDb;

public class AmbientHistoryProvider extends ContentProvider {

    private static final int SONGS = 1;
    private static final int SONG = 2;
    private static final String AUTHORITY = "co.aoscp.miservices.providers.ambient";
    private static final Uri HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/history");
    private static final String[] PROJECTION = {HistoryDb.KEY_ID, HistoryDb.KEY_TIMESTAMP, HistoryDb.KEY_SONG, HistoryDb.KEY_ARTIST};
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "history", SONGS);
        sUriMatcher.addURI(AUTHORITY, "history/#", SONG);
    }

    private HistoryDb mHistoryDb;

    @Override
    public boolean onCreate() {
        mHistoryDb = new HistoryDb(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHistoryDb.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case SONGS:
                //do nothing
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long isMatched = isMatched(values);
        long id = isMatched != -1 ? isMatched : db.insert(HistoryDb.SQLITE_TABLE, null, values);
        return Uri.parse(HISTORY_URI + "/" + id);
    }

    private int isMatched(ContentValues values) {
        try (Cursor c = query(AmbientHistoryProvider.HISTORY_URI, AmbientHistoryProvider.PROJECTION, null, null, null)) {
            if (c != null) {
                if (c.moveToFirst()) {
                    return values.get(HistoryDb.KEY_SONG).equals(c.getString(2)) 
					        && values.get(HistoryDb.KEY_ARTIST).equals(c.getString(3)) ? c.getInt(0) : -1;
                }
            }
        }
        return -1;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mHistoryDb.getWritableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(HistoryDb.SQLITE_TABLE);

        switch (sUriMatcher.match(uri)) {
            case SONGS:
                break;
            case SONG:
                String id = uri.getPathSegments().get(1);
                qBuilder.appendWhere(HistoryDb.KEY_ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return qBuilder.query(db, projection, selection,
                selectionArgs, null, null, HistoryDb.KEY_ID + " DESC");

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mHistoryDb.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case SONGS:
                break;
            case SONG:
                String id = uri.getPathSegments().get(1);
                selection = HistoryDb.KEY_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return db.delete(HistoryDb.SQLITE_TABLE, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

}