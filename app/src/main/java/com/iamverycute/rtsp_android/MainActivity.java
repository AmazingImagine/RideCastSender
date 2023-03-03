package com.iamverycute.rtsp_android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.iamverycute.AccessibilityService.InputService;
import com.iamverycute.wifip2p.activity.BaseActivity;
import com.iamverycute.wifip2p.activity.SendTask;
import com.iamverycute.wifip2p.socket.SendSocket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements ActivityResultCallback<ActivityResult>, CompoundButton.OnCheckedChangeListener, ServiceConnection, EasyPermissions.PermissionCallbacks  {
    public static final String TAG = "MainActivity";
    private Handler mHandler;
    private OnRecordingEvent event;
    private SwitchCompat switchButton;

    private SwitchCompat reCtrlButton;
    private ActivityResultLauncher<Intent> startActivityForResult;

    private ListView mTvDevice;
    private AlertDialog mDialog;
    private ArrayList<String> mListDeviceName = new ArrayList();
    private ArrayList<WifiP2pDevice> mListDevice = new ArrayList<>();

    private static MainActivity instance;

    public static MainActivity getInstance(){
        return instance;
    }

    public void OnNotify(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.textViewMsg);
                tv.setText(msg);
            }
        });
    }

    public void StartCast(){
        bindService(new Intent(MainActivity.this, SLService.class), MainActivity.this, Context.BIND_AUTO_CREATE);
        mHandler.postDelayed(() -> startActivityForResult.launch(event.Granting()), 1000);
        OnNotify("屏幕投射启动中");
    }

    public void OnStreamReady(String strUrl){
        SendSocket.getInstance().OnStreamReady(strUrl);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        instance = this;

//        String[] permissions = new String[1];
//        permissions[0] = Manifest.permission.RECORD_AUDIO;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            permissions[1] = Manifest.permission.POST_NOTIFICATIONS;
//        }
        //ActivityCompat.requestPermissions(this, permissions, 0);
        mTvDevice = (ListView) findViewById(R.id.lv_device);
        //申请文件读写权限
        requireSomePermission();

        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        }
        startActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
        mHandler = new Handler(this.getMainLooper());
        switchButton = findViewById(R.id.checkbox);
        switchButton.setOnCheckedChangeListener(this);

        reCtrlButton = findViewById(R.id.reversecontrol);
        reCtrlButton.setOnCheckedChangeListener(this);

        bindService(new Intent(MainActivity.this, InputService.class), MainActivity.this, Context.BIND_AUTO_CREATE);
    }

    @AfterPermissionGranted(1000)
    private void requireSomePermission() {
        String[] perms = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            //有权限
        } else {
            //没权限
            EasyPermissions.requestPermissions(this, "需要文件读取权限",
                    1000, perms);
        }
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        int resultCode = result.getResultCode();
        if (resultCode == Activity.RESULT_OK) {
            event.Success(resultCode, result.getData());
        } else {
            switchButton.setChecked(false);
        }
    }

    @Override
    protected void onResume() {
        boolean ret = isAccessibilitySettingsOn(this);
        reCtrlButton.setChecked(ret);
        super.onResume();
    }

    /**
     * 检查Accessibility权限
     * */
    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        if(id == R.id.reversecontrol){
            if(b){
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                PackageManager packageManager = getPackageManager();
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                }
            }
        }
        else if(id == R.id.checkbox){
            if (b) {
                mDialog = new AlertDialog.Builder(this, R.style.Transparent).create();
                mDialog.show();
                mDialog.setCancelable(false);
                mDialog.setContentView(R.layout.loading_progressba);

                //搜索设备
                connectServer();
            } else {
                if(null != event){
                    event.Dispose();
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if(!componentName.toString().contains("InputService"))
            event = ((SLBinder) iBinder).getContext();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opensource) {
            try {
                startActivity(Intent.getIntentOld(getString(R.string.project_url)));
            } catch (URISyntaxException ignored) {
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.e(TAG,"权限申请成功");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.e(TAG,"权限申请失败" + perms.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public interface OnRecordingEvent {
        void Success(int resultCode, Intent data);

        Intent Granting();

        void Dispose();
    }

    /**
     * 搜索设备
     */
    private void connectServer() {
        clearDeviceView();

        WifiP2pManager.ActionListener listener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION 广播，此时就可以调用 requestPeers 方法获取设备列表信息
                Log.e(TAG, "搜索设备成功");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e(TAG, "搜索设备失败");
            }
        };

        mWifiP2pManager.discoverPeers(mChannel, listener);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mWifiP2pManager.stopPeerDiscovery(mChannel,listener);
                mDialog.dismiss();
            }
        },5 * 1000);  //延迟10秒执行
    }

    private void clearDeviceView() {
        mListDeviceName.clear();
        mListDevice.clear();
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mListDeviceName);
        mTvDevice.setAdapter(adapter);
    }

    @Override
    public void onConnection(WifiP2pInfo wifiP2pInfo) {
        if (wifiP2pInfo != null) {
            mWifiP2pInfo = wifiP2pInfo;
            String hostAddress = mWifiP2pInfo.groupOwnerAddress.getHostAddress();
            new SendTask(MainActivity.this).execute(hostAddress);
            Log.e(TAG, "WifiP2pInfo:" + hostAddress);
        }
    }

    /**
     * 连接设备
     */
    private void connect(WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        if (wifiP2pDevice != null) {
            config.deviceAddress = wifiP2pDevice.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e(TAG, "连接成功"+wifiP2pDevice.deviceAddress);
                    //Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
//                    bindService(new Intent(MainActivity.this, SLService.class), MainActivity.this, Context.BIND_AUTO_CREATE);
//                    mHandler.postDelayed(() -> startActivityForResult.launch(event.Granting()), 1000);

                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "连接失败");
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onPeersInfo(Collection<WifiP2pDevice> wifiP2pDeviceList) {
        super.onPeersInfo(wifiP2pDeviceList);

        for (WifiP2pDevice device : wifiP2pDeviceList) {
            if (!mListDeviceName.contains(device.deviceName) && !mListDevice.contains(device)) {
                mListDeviceName.add("设备：" + device.deviceName + "----" + device.deviceAddress);
                mListDevice.add(device);
            }
        }

        //进度条消失
        if(mDialog!=null)
            mDialog.dismiss();
        showDeviceInfo();
    }

    private void showDeviceInfo() {
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mListDeviceName);
        mTvDevice.setAdapter(adapter);
        mTvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WifiP2pDevice wifiP2pDevice = mListDevice.get(i);
                connect(wifiP2pDevice);
            }
        });
    }

}