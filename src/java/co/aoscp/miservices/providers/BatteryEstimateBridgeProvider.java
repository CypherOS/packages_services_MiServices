/*
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

import co.aoscp.miservices.providers.database.EstimatesDb;

public class BatteryEstimateBridgeProvider extends ContentProvider {

    private static final int ESTIMATES = 1;
    private static final int ESTIMATE = 2;
    private static final String AUTHORITY = "co.aoscp.miservices.providers.batterybridge";
    private static final Uri ESTIMATE_URI = Uri.parse("content://" + AUTHORITY + "/estimate");
    private static final String[] PROJECTION = {EstimatesDb.KEY_ID, EstimatesDb.KEY_ESTIMATE};
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "estimate", ESTIMATES);
        sUriMatcher.addURI(AUTHORITY, "estimate/#", ESTIMATE);
    }

    private EstimatesDb mEstimatesDb;

    @Override
    public boolean onCreate() {
        mEstimatesDb = new EstimatesDb(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mEstimatesDb.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ESTIMATES:
                //do nothing
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return Uri.parse(ESTIMATE_URI + "/" + db.insert(EstimatesDb.SQLITE_TABLE, null, values));
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mEstimatesDb.getWritableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(EstimatesDb.SQLITE_TABLE);

        switch (sUriMatcher.match(uri)) {
            case ESTIMATES:
                break;
            case ESTIMATE:
                String id = uri.getPathSegments().get(1);
                qBuilder.appendWhere(EstimatesDb.KEY_ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return qBuilder.query(db, projection, selection,
                selectionArgs, null, null, EstimatesDb.KEY_ID + " DESC");

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mEstimatesDb.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ESTIMATES:
                break;
            case ESTIMATE:
                String id = uri.getPathSegments().get(1);
                selection = EstimatesDb.KEY_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return db.delete(EstimatesDb.SQLITE_TABLE, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

}