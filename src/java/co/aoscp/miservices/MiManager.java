/*
 * Copyright 2019 CypherOS
 *
 * MiServices is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MiServices is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MiServices.  If not, see <http://www.gnu.org/licenses/>.
 */
package co.aoscp.miservices;

import android.content.Context;
import android.util.Log;

import co.aoscp.miservices.quickspace.EventsController;
import co.aoscp.miservices.weather.WeatherController;

public class MiManager {

    private static final String TAG = "MiManager";

    private Context mContext;
    private EventsController mEventsController;
    private WeatherController mWeatherController;

    public MiManager(Context context) {
        mContext = context;
    }

    public void addQsControllers() {
        mEventsController = new EventsController(mContext);
        mWeatherController = new WeatherController(mContext);
    }

    public EventsController getQsEvents() {
        if (mEventsController == null) {
            mEventsController = new EventsController(mContext);
        }
        return mEventsController;
    }

    public WeatherController getQsWeather() {
        if (mWeatherController == null) {
            mWeatherController = new WeatherController(mContext);
        }
        return mWeatherController;
    }
}
