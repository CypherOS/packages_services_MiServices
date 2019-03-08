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

import static co.aoscp.miservices.weather.utils.Constants.DEBUG;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import co.aoscp.miservices.MiManager;
import co.aoscp.miservices.quickspace.EventsController.EventListener;
import co.aoscp.miservices.quickspace.QuickspaceCard;
import co.aoscp.miservices.weather.MiApi;
import co.aoscp.miservices.weather.WeatherController.UpdateListener;

public class QuickspaceProvider extends ContentProvider implements UpdateListener, EventListener {

    private static final String TAG = "QuickspaceProvider";

    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CONDITIONS = "conditions";
    private static final String COLUMN_TEMPERATURE_METRIC = "temperatureMetric";
    private static final String COLUMN_TEMPERATURE_IMPERIAL = "temperatureImperial";
    private static final String COLUMN_EVENT_TYPE = "eventType";
    private static final String COLUMN_EVENT_TITLE = "eventTitle";
    private static final String COLUMN_EVENT_ACTION = "eventAction";

    private static final Uri QUICKSPACE_URI = Uri.parse("content://co.aoscp.miservices.providers.quickspace/card");
    private static final String[] PROJECTION = new String[]{
            COLUMN_STATUS,
            COLUMN_CONDITIONS,
            COLUMN_TEMPERATURE_METRIC,
            COLUMN_TEMPERATURE_IMPERIAL,
            COLUMN_EVENT_TYPE,
            COLUMN_EVENT_TITLE,
            COLUMN_EVENT_ACTION
    };

    private MiApi mMiApi;
	private MiManager mMiManager;
    protected ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        mContentResolver = getContext().getContentResolver();
		mMiManager = new MiManager(getContext().getApplicationContext());
		mMiManager.getQsEvents().addEventListener(this);
		mMiManager.getQsWeather().addUpdateListener(this);
        return true;
    }

    @Override
    public void onPostUpdate() {
        mContentResolver.notifyChange(QUICKSPACE_URI, null /* observer */);
    }

    @Override
    public void onNewEvent() {
        mContentResolver.notifyChange(QUICKSPACE_URI, null /* observer */);
    }

    @Override
    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        if (DEBUG) Log.i(TAG, "query: " + uri.toString());
        if (mMiApi == null) {
            mMiApi = new MiApi(getContext());
        }
        mMiApi.queryLocation();
        while (mMiApi.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        QuickspaceCard card = mMiApi.getResult();
        if (DEBUG) Log.d(TAG, card.toString());
        final MatrixCursor result = new MatrixCursor(PROJECTION);
        if (card != null) {
            result.newRow()
                    .add(COLUMN_STATUS, card.getStatus())
                    .add(COLUMN_CONDITIONS, card.getConditions())
                    .add(COLUMN_TEMPERATURE_METRIC, card.getTemperature(true))
                    .add(COLUMN_TEMPERATURE_IMPERIAL, card.getTemperature(false))
                    .add(COLUMN_EVENT_TYPE, card.getEventType())
                    .add(COLUMN_EVENT_TITLE, card.getEventTitle())
                    .add(COLUMN_EVENT_ACTION, card.getEventAction());
            return result;
        }

        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}