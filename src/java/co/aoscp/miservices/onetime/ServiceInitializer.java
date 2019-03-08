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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import co.aoscp.miservices.MiServices;

public class ServiceInitializer extends Service {

    private static final String TAG = "ServiceInitializer";

	private final IBinder mBinder = new LocalBinder();
    private MiServices mMiServices;

    public interface ServiceCallback {
        void sendResults(int resultCode, Bundle b);
    }

    public class LocalBinder extends Binder {
        ServiceInitializer getService() {
            return ServiceInitializer.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mMiServices = new MiServices(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    public void setCallback(ServiceCallback callback) {
    }

    public void start() {
        Log.d(TAG, "start");
    }

    public void stop() {
    }
}