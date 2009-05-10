/**
 *  MicroEmulator
 *  Copyright (C) 2009 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 *
 *  @version $Id: MicroEmulatorActivity.java 1918 2009-01-21 12:56:43Z barteo $
 */

package org.microemu.android;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.device.AndroidDeviceDisplay;
import org.microemu.device.DeviceFactory;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;

public abstract class MicroEmulatorActivity extends Activity {

	private Handler handler = new Handler();
	
	private Thread activityThread;
	
	private View contentView;
	
	public boolean post(Runnable r) {
		if (activityThread == Thread.currentThread()) {
			r.run();
			return true;
		} else {
			return handler.post(r);
		}
	}
	
	public boolean isActivityThread() {
		return (activityThread == Thread.currentThread());
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		activityThread = Thread.currentThread();
	}
	
	public View getContentView() {
		return contentView;
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		
		contentView = view;
	}
		
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
        Display display = getWindowManager().getDefaultDisplay();
		AndroidDeviceDisplay deviceDisplay = (AndroidDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay();
		deviceDisplay.displayRectangleWidth = display.getWidth();
		deviceDisplay.displayRectangleHeight = display.getHeight() - 25;
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return;
		}
		DisplayAccess da = ma.getDisplayAccess();
		if (da != null) {
			da.sizeChanged();
			deviceDisplay.repaint(0, 0, deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight());
		}
	}
	
}