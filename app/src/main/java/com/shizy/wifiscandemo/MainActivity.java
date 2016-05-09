package com.shizy.wifiscandemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                long interval = System.currentTimeMillis() - mStartTime;
                mScanCount ++;
                mInterval = interval / mScanCount;

                mScanResults = mWifiManager.getScanResults();
                updateTime();
                mWifiManager.startScan();
            }
        }
    };

    private BroadcastReceiver mBatteryReceiver = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                mCurrentBattery = ((level * 100.0f) / scale) + "%";
                if (mStartBattery == null) {
                    mStartBattery = mCurrentBattery;
                }
                updateBattery();
            }
        }

    };

    private TextView mBatteryView;
    private TextView mTimeView;
    private ListView mListView;

    private WifiManager mWifiManager;

    private List<ScanResult> mScanResults;

    private ScanAdapter mAdapter;

    private String mStartBattery;
    private String mCurrentBattery;

    private long mStartTime;
    private long mStopTime;
    private long mInterval;
    private long mScanCount;

    private boolean hasStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBatteryView = (TextView) findViewById(R.id.battery);
        mTimeView = (TextView) findViewById(R.id.time);
        mListView = (ListView) findViewById(R.id.listview);

        mAdapter = new ScanAdapter();
        mListView.setAdapter(mAdapter);
    }

    private void updateBattery() {
        String start = "StartBattery: " + (mStartBattery == null ? "" : mStartBattery);
        String current = "CurrentBattery: " + (mCurrentBattery == null ? "" : mCurrentBattery);

        String info = start + "\n" + current;
        mBatteryView.setText(info);
    }

    private void updateTime() {
        String start = "StartTime: " + mStartTime;
        String stop = "StopTime: " + mStopTime;

        long totalTime = (mStopTime - mStartTime);
        String total = "TotalTime: " + (totalTime > 0 ? getDurationString(totalTime) : "");
        String interval = "ScanCount: " + mScanCount + " | Average Interval: " + mInterval;

        String info = start + "\n" + stop + "\n" + total + "\n" + interval;
        mTimeView.setText(info);
        mAdapter.notifyDataSetChanged();
    }

    public void startScan(View view) {
        if (hasStart) {
            return;
        }
        hasStart = true;

        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        }

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        mStartBattery = null;
        mCurrentBattery = null;
        mStartTime = System.currentTimeMillis();
        mStopTime = 0;
        mInterval = 0;
        mScanCount = 0;

        registerBroadCast();
        mWifiManager.startScan();
        updateTime();
    }

    public void stopScan(View view) {
        if (!hasStart) {
            return;
        }
        hasStart = false;

        unRegisterBroadCast();

        mStopTime = System.currentTimeMillis();
        updateTime();
    }

    private void registerBroadCast() {
        IntentFilter scanFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mScanReceiver, scanFilter);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryReceiver, batteryFilter);
    }

    private void unRegisterBroadCast() {
        try {
            unregisterReceiver(mScanReceiver);
            unregisterReceiver(mBatteryReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private String getDurationString(long ms) {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return TIME_FORMAT.format(new Date(ms));
    }

    private class ScanAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mScanResults == null ? 0 : mScanResults.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
            }

            ScanResult result = mScanResults.get(i);
            ((TextView)view).setText(result.SSID);

            return view;
        }
    }

}
