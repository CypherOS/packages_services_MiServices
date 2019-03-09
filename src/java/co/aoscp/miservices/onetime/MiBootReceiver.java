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
package co.aoscp.miservices.onetime;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import co.aoscp.miservices.onetime.ServiceInitializer.LocalBinder;

public class MiBootReceiver extends BroadcastReceiver {

    private static final String TAG = "MiBootReceiver";

    private ServiceInitializer mInitializer;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "Boot completed");
        context.startService(new Intent(context, ServiceInitializer.class));
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mInitializer = binder.getService();
            mInitializer.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mInitializer = null;
        }
    };
}