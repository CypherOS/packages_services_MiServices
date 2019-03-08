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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import co.aoscp.miservices.Bits;

public class MiUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "MiUpdateReceiver";

	private final IControllers mControllers;
	private boolean mIsRegistered = false;

	public MiUpdateReceiver(Context context, IControllers controllers) {
        mControllers = controllers;
		if (mIsRegistered) {
			context.unregisterReceiver(this);
			mIsRegistered = false;
		}
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Bits.QUICKSPACE_UPDATE_ACTION);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        context.registerReceiver(this, filter);
		mIsRegistered = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
		if (intent == null) return;
        if (Bits.QUICKSPACE_UPDATE_ACTION.equals(intent.getAction())) {
            mControllers.onUpdate(false);
        } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            mControllers.onUpdate(true);
        }
    }
}