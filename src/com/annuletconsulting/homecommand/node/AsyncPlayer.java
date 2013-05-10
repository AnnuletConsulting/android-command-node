/*
 * Copyright (C) 2013 Annulet Consulting, LLC
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

package com.annuletconsulting.homecommand.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.media.MediaPlayer;
import android.os.Bundle;

public class AsyncPlayer implements LoaderCallbacks<Void> {
	private Activity activity;
	private String ipAddr;
	private int port;
	MediaPlayer mediaPlayer = new MediaPlayer();

	public AsyncPlayer(Activity activity, String ipAddr, int port) {
		this.activity = activity;
		this.ipAddr = ipAddr;
		this.port = port;
	}

	@Override
	public Loader<Void> onCreateLoader(int id, Bundle args) {
		AsyncTaskLoader<Void> loader = new AsyncTaskLoader<Void>(activity) {
			@Override
			public Void loadInBackground() {
				try {
					Socket connection = new Socket(ipAddr, port);
					InputStream stream = connection.getInputStream();
					File tmpMedia = File.createTempFile("tmpMedia", ".mp3");
					FileOutputStream out = new FileOutputStream(tmpMedia);
					byte buf[] = new byte[connection.getReceiveBufferSize()];
					int read = stream.read(buf);
					do {
						out.write(buf, 0, read);
						read = stream.read(buf);
					} while (read > 0);
					stream.close();
					mediaPlayer.reset();
					mediaPlayer.setDataSource(new FileInputStream(tmpMedia).getFD());
					mediaPlayer.prepare();
					mediaPlayer.start();
					connection.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Void> loader, Void data) {
	}

	@Override
	public void onLoaderReset(Loader<Void> loader) {	
	}
}