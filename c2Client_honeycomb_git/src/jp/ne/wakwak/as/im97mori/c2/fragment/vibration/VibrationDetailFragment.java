/*
 * Copyright (c) 2011 im97mori.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package jp.ne.wakwak.as.im97mori.c2.fragment.vibration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class VibrationDetailFragment extends Fragment implements Callback,
		OnTouchListener, OnClickListener {

	private static final int MESSAGE_QUIT = 0;
	private static final int MESSAGE_DRAW = 1;
	private static final int MESSAGE_TOUCH = 2;
	private static final int MESSAGE_RESET = 3;

	private static final Object[] LOCK = new Object[0];

	private static final Paint WHITE;
	static {
		WHITE = new Paint();
		WHITE.setColor(Color.WHITE);
		WHITE.setTextSize(20f);
		WHITE.setAntiAlias(true);
	}

	private static final Paint RED;
	static {
		RED = new Paint();
		RED.setColor(Color.RED);
		RED.setStrokeWidth(2);
	}

	private static final Paint BLUE;
	static {
		BLUE = new Paint();
		BLUE.setColor(Color.BLUE);
		BLUE.setStrokeWidth(2);
	}

	private static final long tick = 1000L / 60L;

	private int width = -1;
	private int height = -1;
	private float max;
	private float min;

	private SurfaceHandler surfaceHandler;
	private AtomicLong next;
	private AtomicInteger downCount = new AtomicInteger(0);
	private SurfaceHolder holder;
	private Vibrator vibrator;

	private boolean isStart = false;
	private long startTime;

	private String newRecordString;
	private String reRecordString;
	private String recordingString;

	private List<Long> list = Collections
			.synchronizedList(new LinkedList<Long>());

	private List<Long> recordedList = Collections
			.synchronizedList(new LinkedList<Long>());

	public static interface OnVibrationDetailFragmentListener {
		void onEditName(String name);

		void onUpdateVibration();
	}

	private OnVibrationDetailFragmentListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnVibrationDetailFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnVibrationDetailFragmentListener");
		}
	}

	public static VibrationDetailFragment newInstance(String name) {
		VibrationDetailFragment fragment = new VibrationDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.ArgumentKey.NAME, name);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static VibrationDetailFragment newInstance(long id) {
		VibrationDetailFragment fragment = new VibrationDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vibration_detail, null);

		SurfaceView surfaceView = (SurfaceView) view
				.findViewById(R.id.vibrationDetailSurface);
		surfaceView.getHolder().addCallback(this);
		surfaceView.setOnTouchListener(this);

		Bundle bundle = this.getArguments();
		TextView textView = (TextView) view
				.findViewById(R.id.vibrationDetailNameText);
		if (bundle.containsKey(Constants.ArgumentKey.NAME)) {
			textView.setText(bundle.getString(Constants.ArgumentKey.NAME));
		} else {
			AlarmDb db = new AlarmDb(this.getActivity());
			VibrationVo vo = db.getVibration(bundle
					.getLong(Constants.ArgumentKey.ID));
			db.close();
			textView.setText(vo.getName());
		}

		view.findViewById(R.id.vibrationDetailNameLayout).setOnClickListener(
				this);

		return view;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
		this.max = height / 3f;
		this.min = height * 2f / 3f;
	}

	private class SurfaceHandler extends Handler {
		public SurfaceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_QUIT) {
				this.getLooper().quit();
			} else if (msg.what == MESSAGE_DRAW) {
				long now = System.currentTimeMillis();
				if (next.get() < now) {
					draw();
					next.addAndGet(tick);
				}

				if (isStart && now > startTime + 10000L) {
					synchronized (LOCK) {
						recordedList.clear();
						recordedList.addAll(list);
					}
					downCount.set(0);
					vibrator.cancel();
					isStart = false;

					VibrationDetailFragment.this.getActivity().runOnUiThread(
							new Runnable() {

								@Override
								public void run() {
									VibrationDetailFragment.this.getActivity()
											.invalidateOptionsMenu();
								}
							});
				}
				this.sendEmptyMessage(MESSAGE_DRAW);
			} else if (msg.what == MESSAGE_TOUCH) {
				int count = downCount.get();
				if (count == 0 && msg.arg1 == MotionEvent.ACTION_DOWN) {
					count = downCount.incrementAndGet();
					vibrator.vibrate(10000L);
				} else if (count == 1 && msg.arg1 == MotionEvent.ACTION_UP) {
					count = downCount.decrementAndGet();
					vibrator.cancel();
				}
				long now = System.currentTimeMillis();
				if (count == 1 && msg.arg1 == MotionEvent.ACTION_DOWN) {
					if (!isStart) {
						list.clear();
						list.add(now);
						startTime = now;
						isStart = true;
					}
					list.add(now);
				} else if (count == 0 && msg.arg1 == MotionEvent.ACTION_UP) {
					list.add(now);
				}
			} else if (msg.what == MESSAGE_RESET) {
				reset();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.holder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.width = -1;
		this.height = -1;
		this.holder = null;

		this.surfaceHandler.sendEmptyMessage(MESSAGE_RESET);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Message message = this.surfaceHandler.obtainMessage(MESSAGE_TOUCH);
		message.arg1 = event.getAction();
		message.sendToTarget();
		return true;
	}

	protected void draw() {
		long now = System.currentTimeMillis();

		SurfaceHolder currentHolder;
		List<Long> targetList = new LinkedList<Long>();
		long currentStartTime;
		boolean currentIsStart;
		int currentWidth;
		int currentHeight;
		float currentMax;
		float currentMin;

		currentHolder = this.holder;
		currentIsStart = this.isStart;
		currentWidth = this.width;
		currentHeight = this.height;
		currentMax = this.max;
		currentMin = this.min;
		if (currentIsStart) {
			targetList.addAll(this.list);
		} else {
			synchronized (LOCK) {
				targetList.addAll(this.recordedList);
			}
		}
		currentStartTime = this.startTime;

		if (currentHolder == null) {
			return;
		}

		int x = -1;
		float[] linesArray = null;

		long diff = now - currentStartTime;
		diff = diff * currentWidth;
		x = (int) (diff / 10000L);
		int listSize = targetList.size();
		int size = (listSize * 8) + 4;
		linesArray = new float[size];
		linesArray[0] = 0f;
		linesArray[1] = currentMax;
		if (listSize == 0) {
			linesArray[2] = x;
			linesArray[3] = currentMax;
		} else {
			boolean on = false;
			for (int i = 0; i < listSize; i++) {
				long target = targetList.get(i);
				diff = target - currentStartTime;
				diff = diff * currentWidth;
				float targetX = diff / 10000f;

				linesArray[i * 8 + 2] = targetX;
				linesArray[i * 8 + 4] = targetX;
				linesArray[i * 8 + 6] = targetX;
				linesArray[i * 8 + 8] = targetX;

				on = i % 2 == 0;
				if (on) {
					linesArray[i * 8 + 3] = currentMax;
					linesArray[i * 8 + 5] = currentMax;
					linesArray[i * 8 + 7] = currentMin;
					linesArray[i * 8 + 9] = currentMin;
				} else {
					linesArray[i * 8 + 3] = currentMin;
					linesArray[i * 8 + 5] = currentMin;
					linesArray[i * 8 + 7] = currentMax;
					linesArray[i * 8 + 9] = currentMax;
				}
			}
			if (on) {
				linesArray[size - 2] = x;
				linesArray[size - 1] = currentMin;
			} else {
				linesArray[size - 2] = x;
				linesArray[size - 1] = currentMax;
			}

			if (!currentIsStart && linesArray[size - 2] < currentWidth) {
				float[] tmpArray = new float[size + 4];
				System.arraycopy(linesArray, 0, tmpArray, 0, size);
				tmpArray[size - 4] = tmpArray[size - 6];
				tmpArray[size - 3] = tmpArray[size - 5];
				tmpArray[size - 2] = currentWidth;
				tmpArray[size - 1] = tmpArray[size - 5];
				linesArray = tmpArray;
			}
		}

		Canvas canvas = currentHolder.lockCanvas();
		if (canvas != null) {
			canvas.save();
			canvas.drawColor(Color.BLACK);

			String text = null;
			if (currentIsStart) {
				canvas.drawLine(x, 0, x, currentHeight, BLUE);

				text = this.recordingString;
			} else {
				if (linesArray.length == 4) {
					text = this.newRecordString;
				} else {
					text = this.reRecordString;
				}
			}

			float textWidth = WHITE.measureText(text);
			textWidth = textWidth / 2f;
			x = (int) (currentWidth / 2 - textWidth);
			canvas.drawText(text, x, WHITE.getTextSize(), WHITE);

			canvas.drawLines(linesArray, RED);

			canvas.restore();
			currentHolder.unlockCanvasAndPost(canvas);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		this.newRecordString = this
				.getString(R.string.vibrationDetailNewRecordString);
		this.reRecordString = this
				.getString(R.string.vibrationDetailReRecordString);
		this.recordingString = this
				.getString(R.string.vibrationDetailRecordingString);
		this.setRetainInstance(true);

		Bundle bundle = this.getArguments();
		if (bundle.containsKey(Constants.ArgumentKey.ID)) {
			AlarmDb db = new AlarmDb(this.getActivity());
			VibrationVo vo = db.getVibration(bundle
					.getLong(Constants.ArgumentKey.ID));
			db.close();

			synchronized (LOCK) {
				for (int i = 0; i < vo.getPattern().length; i++) {
					this.recordedList.add(vo.getPattern()[i]);
				}
				if (this.recordedList.size() > 0) {
					this.startTime = this.recordedList.get(0);
				} else {
					this.startTime = System.currentTimeMillis() - 10000L;
				}
			}
		}

		HandlerThread thread = new HandlerThread(this.getClass().getName());
		thread.start();
		this.surfaceHandler = new SurfaceHandler(thread.getLooper());
		this.vibrator = (Vibrator) this.getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);

		this.next = new AtomicLong(System.currentTimeMillis());
		this.surfaceHandler.sendEmptyMessage(MESSAGE_DRAW);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.vibration_detail_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		if (item.getItemId() == R.id.vibrationDetailSaveMenu) {
			List<Long> workList = new ArrayList<Long>(this.recordedList.size());
			synchronized (LOCK) {
				workList.addAll(this.recordedList);
			}
			if (workList.size() == 0) {
				Toast.makeText(this.getActivity(),
						R.string.vibrationDetailPatternEmptyString,
						Toast.LENGTH_SHORT).show();
			} else {
				long[] array = new long[workList.size()];
				for (int i = 0; i < workList.size(); i++) {
					array[i] = workList.get(i) - workList.get(0);
				}
				TextView textView = (TextView) this.getView().findViewById(
						R.id.vibrationDetailNameText);
				String name = textView.getText().toString();
				Bundle bundle = this.getArguments();
				AlarmDb db = new AlarmDb(this.getActivity());
				if (bundle.containsKey(Constants.ArgumentKey.ID)) {
					db.updateVibration(
							bundle.getLong(Constants.ArgumentKey.ID), name,
							array);
				} else {
					long id = db.addVibration(name, array);
					bundle.putLong(Constants.ArgumentKey.ID, id);
				}
				db.close();

				this.listener.onUpdateVibration();
			}
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.surfaceHandler.sendEmptyMessage(MESSAGE_QUIT);
		this.vibrator.cancel();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.surfaceHandler.sendEmptyMessage(MESSAGE_RESET);
	}

	private void reset() {
		int count = downCount.get();
		if (count > 0) {
			this.vibrator.cancel();
			this.downCount.set(0);
			this.list.add(System.currentTimeMillis());
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.vibrationDetailNameLayout) {
			if (this.listener != null && !this.isStart) {
				TextView textView = (TextView) this.getView().findViewById(
						R.id.vibrationDetailNameText);
				this.listener.onEditName(textView.getText().toString());
			}
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item = menu.findItem(R.id.vibrationDetailSaveMenu);
		if (this.isStart) {
			item.setVisible(false);
		} else {
			item.setVisible(true);
		}
	}

	public void onNameChange(String name) {
		TextView textView = (TextView) this.getView().findViewById(
				R.id.vibrationDetailNameText);
		textView.setText(name);

		Bundle bundle = this.getArguments();
		bundle.putString(Constants.ArgumentKey.NAME, name);

		if (bundle.containsKey(Constants.ArgumentKey.ID)) {
			AlarmDb db = new AlarmDb(this.getActivity());
			VibrationVo vo = db.getVibration(bundle
					.getLong(Constants.ArgumentKey.ID));
			vo.setName(name);
			db.updateVibration(vo.getId(), name, vo.getPattern());
			db.close();
		}

		this.listener.onUpdateVibration();
	}
}