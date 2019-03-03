/*
 * Copyright (C) 2018 Pixel Experience
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
package co.aoscp.miservices.weather;

import static co.aoscp.miservices.weather.utils.Constants.DEBUG;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import co.aoscp.miservices.weather.WeatherController.UpdateListener;

public class WeatherContentProvider extends ContentProvider implements UpdateListener {

    private static final String TAG = "WeatherContentProvider";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CONDITIONS = "conditions";
    private static final String COLUMN_TEMPERATURE_METRIC = "temperatureMetric";
    private static final String COLUMN_TEMPERATURE_IMPERIAL = "temperatureImperial";
	public static final Uri WEATHER_URI = Uri.parse("content://co.aoscp.miservices.weather.provider/weather");
    private static final String[] PROJECTION_DEFAULT_WEATHER = new String[]{
            COLUMN_STATUS,
            COLUMN_CONDITIONS,
            COLUMN_TEMPERATURE_METRIC,
            COLUMN_TEMPERATURE_IMPERIAL
    };

	private WeatherChannelApi mWeatherChannelApi;
	protected ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
		mContentResolver = getContext().getContentResolver();
		WeatherController.get(getContext()).addUpdateListener(this);
        return true;
    }

	@Override
    public void onPostUpdate() {
        mContentResolver.notifyChange(WEATHER_URI, null /* observer */);
    }

    @Override
    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        if (DEBUG) Log.i(TAG, "query: " + uri.toString());
        if (mWeatherChannelApi == null) {
            mWeatherChannelApi = new WeatherChannelApi(getContext());
        }
        mWeatherChannelApi.queryLocation();
        while (mWeatherChannelApi.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        WeatherProvider provider = mWeatherChannelApi.getResult();
        if (DEBUG) Log.d(TAG, provider.toString());
        final MatrixCursor result = new MatrixCursor(PROJECTION_DEFAULT_WEATHER);
        if (provider != null) {
            result.newRow()
                    .add(COLUMN_STATUS, provider.getStatus())
                    .add(COLUMN_CONDITIONS, provider.getConditions())
                    .add(COLUMN_TEMPERATURE_METRIC, provider.getTemperature(true))
                    .add(COLUMN_TEMPERATURE_IMPERIAL, provider.getTemperature(false));
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