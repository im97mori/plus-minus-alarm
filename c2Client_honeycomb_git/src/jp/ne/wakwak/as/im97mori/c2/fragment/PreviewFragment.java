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

package jp.ne.wakwak.as.im97mori.c2.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.util.Util;
import jp.ne.wakwak.as.im97mori.c2.view.PreviewCell;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class PreviewFragment extends Fragment implements OnClickListener {
	private static final int QUIT = 0;
	private static final int START_BY_ID = 1;
	private static final int START_BY_LIST = 2;
	private static final int START_ALL = 3;

	private static final String TARGET_MONTH = "TARGET_MONTH";
	private static final String LIST = "LIST";
	private static final String ID_LIST = "ID_LIST";
	private static final String LAST = "LAST";
	private static final String BEGIN = "BEGIN";
	private static final String CONNECTION_SUCCESS = "CONNECTION_SUCCESS";

	private SimpleDateFormat formatter;

	private OnPreviewFragmentListener listener;

	private Date targetMonth;

	private MainHandler mainHandler = new MainHandler();
	private PreviewHandler previewHandler;

	private List<PreviewCell> cellList = new ArrayList<PreviewCell>(42);

	private Long last = Long.MIN_VALUE;

	public static PreviewFragment newInstance() {
		PreviewFragment fragment = new PreviewFragment();
		return fragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		HandlerThread thread = new HandlerThread(this.getClass().getName());
		thread.start();
		this.previewHandler = new PreviewHandler(thread.getLooper());

		this.updateStatus(R.string.previewCalculateStartString);

		this.listener.onShowAll();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.previewHandler.sendEmptyMessage(QUIT);
	}

	public void upadte(long id) {
		if (this.getActivity() != null) {
			this.updateStatus(R.string.previewCalculateStartString);

			Message message = Message.obtain(this.previewHandler, START_BY_ID);
			Bundle bundle = new Bundle();
			bundle.putLong(Constants.ArgumentKey.ID, id);
			synchronized (last) {
				last = System.currentTimeMillis();
				bundle.putLong(LAST, last);
				long begin = this.cellList.get(0).getTargetDate().getTime();
				bundle.putLong(BEGIN, begin);
			}
			message.setData(bundle);
			message.sendToTarget();
		}
	}

	public void update(ArrayList<AlarmTypeVo> list) {
		if (this.getActivity() != null) {
			this.updateStatus(R.string.previewCalculateStartString);

			Message message = Message
					.obtain(this.previewHandler, START_BY_LIST);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(LIST, list);
			synchronized (last) {
				last = System.currentTimeMillis();
				bundle.putLong(LAST, last);
				long begin = this.cellList.get(0).getTargetDate().getTime();
				bundle.putLong(BEGIN, begin);
			}
			message.setData(bundle);
			message.sendToTarget();
		}
	}

	public void update() {
		if (this.getActivity() != null) {
			this.updateStatus(R.string.previewCalculateStartString);

			Message message = Message.obtain(this.previewHandler, START_ALL);
			Bundle bundle = new Bundle();
			synchronized (last) {
				last = System.currentTimeMillis();
				bundle.putLong(LAST, last);
				long begin = this.cellList.get(0).getTargetDate().getTime();
				bundle.putLong(BEGIN, begin);
			}
			message.setData(bundle);
			message.sendToTarget();
		}
	}

	public void updateStatus(int resId) {
		ViewFlipper flipper = (ViewFlipper) this.getView().findViewById(
				R.id.previewFlipper);
		int current = flipper.getDisplayedChild();
		current++;
		current = current % flipper.getChildCount();
		TextView textView = (TextView) flipper.getChildAt(current);
		textView.setText(resId);
		flipper.showNext();
	}

	private class PreviewHandler extends Handler {
		public PreviewHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				if (msg.what == QUIT) {
					this.removeMessages(START_BY_ID);
					this.removeMessages(START_BY_LIST);
					this.getLooper().quit();
				} else if (msg.what == START_BY_ID || msg.what == START_BY_LIST) {
					List<AlarmTypeVo> list = null;
					Bundle bundle = msg.getData();
					Long current = bundle.getLong(LAST);
					long begin = bundle.getLong(BEGIN);
					if (msg.what == START_BY_ID) {
						AlarmDb db = new AlarmDb(
								PreviewFragment.this.getActivity());
						list = db.getAlarmTypeList(bundle
								.getLong(Constants.ArgumentKey.ID));
						db.close();
					} else {
						list = bundle.getParcelableArrayList(LIST);
					}

					Calendar calendar = GregorianCalendar.getInstance();
					calendar.setTimeInMillis(begin);
					boolean connectionSuccess = true;
					List<AlarmTypeVo> tmpList = new ArrayList<AlarmTypeVo>(
							list.size());
					tmpList.addAll(list);
					Set<Date> dateSet = calculate(calendar, tmpList);
					if (!tmpList.containsAll(list)) {
						connectionSuccess = false;
					}
					long[] ids = new long[1];
					ids[0] = Constants.TEMPOLARY_ID;

					bundle = new Bundle();
					bundle.putLongArray(ID_LIST, ids);
					ArrayList<Date> dateList = new ArrayList<Date>(list.size());
					dateList.addAll(dateSet);
					long[] times = new long[dateList.size()];
					for (int i = 0; i < times.length; i++) {
						times[i] = dateList.get(i).getTime();
					}
					bundle.putLongArray(String.valueOf(Constants.TEMPOLARY_ID),
							times);

					bundle.putLong(LAST, current);
					bundle.putBoolean(CONNECTION_SUCCESS, connectionSuccess);

					Message message = Message.obtain(mainHandler, msg.what);
					message.setData(bundle);
					message.sendToTarget();
				} else if (msg.what == START_ALL) {
					Bundle bundle = msg.getData();
					Long current = bundle.getLong(LAST);
					long begin = bundle.getLong(BEGIN);
					bundle = new Bundle();
					AlarmDb db = new AlarmDb(PreviewFragment.this.getActivity());
					List<AlarmVo> alarmList = db.getAlarmList();
					long[] ids = new long[alarmList.size()];
					for (int i = 0; i < alarmList.size(); i++) {
						AlarmVo vo = alarmList.get(i);
						ids[i] = vo.getId();
					}
					bundle.putLongArray(ID_LIST, ids);

					Iterator<AlarmVo> it = alarmList.iterator();
					Calendar calendar = GregorianCalendar.getInstance();
					calendar.setTimeInMillis(begin);
					boolean connectionSuccess = true;
					while (it.hasNext()) {
						AlarmVo alarmVo = it.next();
						List<AlarmTypeVo> list = db.getAlarmTypeList(alarmVo
								.getId());
						List<AlarmTypeVo> tmpList = new ArrayList<AlarmTypeVo>();
						tmpList.addAll(list);
						Set<Date> dateSet = calculate(
								(Calendar) calendar.clone(), tmpList);
						if (!tmpList.containsAll(list)) {
							connectionSuccess = false;
						}
						ArrayList<Date> dateList = new ArrayList<Date>(
								dateSet.size());
						dateList.addAll(dateSet);
						long[] times = new long[dateSet.size()];
						for (int i = 0; i < times.length; i++) {
							times[i] = dateList.get(i).getTime();
						}
						bundle.putLongArray(String.valueOf(alarmVo.getId()),
								times);
					}
					db.close();

					bundle.putLong(LAST, current);
					bundle.putBoolean(CONNECTION_SUCCESS, connectionSuccess);

					Message message = Message.obtain(mainHandler, START_ALL);
					message.setData(bundle);
					message.sendToTarget();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Set<Date> calculate(Calendar calendar, List<AlarmTypeVo> list) {
		Set<Date> dateSet = new HashSet<Date>();
		if (this.getActivity() != null && !this.getActivity().isFinishing()) {
			Date startDate = calendar.getTime();
			calendar.add(Calendar.DAY_OF_MONTH, 42);
			Date endDate = calendar.getTime();
			dateSet.addAll(Util.calculate(startDate, endDate, list,
					PreviewFragment.this.getActivity()));
		}
		return dateSet;
	}

	private class MainHandler extends Handler {
		public MainHandler() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			if (PreviewFragment.this.isResumed()) {
				if (msg.what == START_BY_ID || msg.what == START_BY_LIST
						|| msg.what == START_ALL) {
					Map<Long, Set<Date>> map = new HashMap<Long, Set<Date>>();
					Bundle bundle = msg.getData();
					long[] ids = bundle.getLongArray(ID_LIST);
					for (int i = 0; i < ids.length; i++) {
						String id = String.valueOf(ids[i]);
						long[] times = bundle.getLongArray(id);
						Set<Date> dateSet = new HashSet<Date>(times.length);
						for (int j = 0; j < times.length; j++) {
							dateSet.add(new Date(times[j]));
						}
						map.put(ids[i], dateSet);
					}

					Long current = bundle.getLong(LAST);
					int resId;
					if (bundle.getBoolean(CONNECTION_SUCCESS)) {
						resId = R.string.previewCalculateCompleteString;
					} else {
						resId = R.string.previewCalculateCompleteWithConnectionFailString;
					}
					synchronized (last) {
						if (last.equals(current)) {
							Iterator<PreviewCell> it = PreviewFragment.this.cellList
									.iterator();
							while (it.hasNext()) {
								it.next().setDate(map);
							}

							updateStatus(resId);
						}
					}
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.preview, null);
		Calendar calendar = Util.getClearCalendarInstance();
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(TARGET_MONTH)) {
			calendar.setTimeInMillis(savedInstanceState.getLong(TARGET_MONTH));
			this.targetMonth = calendar.getTime();
		} else {
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			this.targetMonth = calendar.getTime();
		}
		this.formatter = new SimpleDateFormat(this.getActivity().getString(
				R.string.dayOfWeekFormat));
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		TextView textView = (TextView) view
				.findViewById(R.id.previewMondayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		textView = (TextView) view.findViewById(R.id.previewTuesdayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		textView = (TextView) view.findViewById(R.id.previewWednesdayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		textView = (TextView) view.findViewById(R.id.previewThursdayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		textView = (TextView) view.findViewById(R.id.previewFridayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		textView = (TextView) view.findViewById(R.id.previewSaturdayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		textView = (TextView) view.findViewById(R.id.previewSundayTitle);
		textView.setText(this.formatter.format(calendar.getTime()));

		this.formatter = new SimpleDateFormat(this.getActivity().getString(
				R.string.previewMonthFormat));
		textView = (TextView) view.findViewById(R.id.previewMonthText);
		textView.setText(formatter.format(this.targetMonth));

		int offset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
		calendar.add(Calendar.DAY_OF_MONTH, offset * -1);

		Map<Long, Set<Date>> map = new HashMap<Long, Set<Date>>();
		LinearLayout root = (LinearLayout) view
				.findViewById(R.id.previewCellLayout);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params1.weight = 1;
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0,
				LinearLayout.LayoutParams.MATCH_PARENT);
		params2.weight = 1;

		PreviewCell.fadeInAnimation = AnimationUtils.loadAnimation(
				this.getActivity(), R.anim.fade_in);
		PreviewCell.fadeOutAnimation = AnimationUtils.loadAnimation(
				this.getActivity(), R.anim.fade_out);
		for (int i = 0; i < 6; i++) {
			LinearLayout layout = new LinearLayout(this.getActivity());
			root.addView(layout, params1);
			for (int j = 0; j < 7; j++) {
				PreviewCell cell = new PreviewCell(this.getActivity(),
						this.targetMonth, calendar.getTime(), map);
				this.cellList.add(cell);
				layout.addView(cell, params2);
				calendar.add(Calendar.DAY_OF_MONTH, 1);

				cell.setOnClickListener(this);
			}
		}

		ViewFlipper flipper = (ViewFlipper) view
				.findViewById(R.id.previewFlipper);
		flipper.setInAnimation(this.getActivity(), R.anim.preview_in);
		flipper.setOutAnimation(this.getActivity(), R.anim.preview_out);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		textView = new TextView(this.getActivity());
		textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		flipper.addView(textView, params);
		textView = new TextView(this.getActivity());
		textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		flipper.addView(textView, params);

		view.findViewById(R.id.previewPreviousButton).setOnClickListener(this);
		view.findViewById(R.id.previewNextButton).setOnClickListener(this);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(TARGET_MONTH, this.targetMonth.getTime());
	}

	@Override
	public void onClick(View paramView) {
		if (this.listener != null) {
			if (paramView.getId() == R.id.previewNextButton
					|| paramView.getId() == R.id.previewPreviousButton) {
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.targetMonth);
				if (paramView.getId() == R.id.previewNextButton) {
					calendar.add(Calendar.MONTH, 1);
				} else if (paramView.getId() == R.id.previewPreviousButton) {
					calendar.add(Calendar.MONTH, -1);
				}
				this.targetMonth = calendar.getTime();
				TextView textView = (TextView) this.getView().findViewById(
						R.id.previewMonthText);
				textView.setText(formatter.format(this.targetMonth));

				int offset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
				calendar.add(Calendar.DAY_OF_MONTH, offset * -1);

				Map<Long, Set<Date>> map = new HashMap<Long, Set<Date>>();
				for (int i = 0; i < 42; i++) {
					PreviewCell cell = this.cellList.get(i);
					cell.setData(this.targetMonth, calendar.getTime(), map);
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				}
				this.listener.onMoveMonth();
			} else if (paramView instanceof PreviewCell) {
				PreviewCell cell = (PreviewCell) paramView;
				this.listener.onCellClick(cell.getTargetDate(), cell.getMap());
			}
		}
	}

	public interface OnPreviewFragmentListener extends OnShowAllListener {
		void onMoveMonth();

		void onCellClick(Date date, Map<Long, Set<Date>> map);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnPreviewFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnPreviewFragmentListener");
		}
	}
}