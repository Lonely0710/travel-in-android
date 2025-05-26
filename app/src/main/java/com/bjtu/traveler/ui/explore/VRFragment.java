package com.bjtu.traveler.ui.explore;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bjtu.traveler.R;

public class VRFragment extends Fragment implements SensorEventListener {
    private static final int REQUEST_PERMISSIONS_CODE = 1001;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vr, container, false);
        // 新增：发现按钮跳转
        TextView tvBackDiscover = view.findViewById(R.id.tv_back_discover);
        tvBackDiscover.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new DiscoverFragment())
                .addToBackStack(null)
                .commit();
        });
        webView = view.findViewById(R.id.webViewVR);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 支持混合内容加载（http/https）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // WebView 权限处理
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().runOnUiThread(() -> {
                        new AlertDialog.Builder(getActivity())
                            .setTitle("权限请求")
                            .setMessage("网页请求访问摄像头/麦克风，是否允许？")
                            .setPositiveButton("允许", (dialog, which) -> request.grant(request.getResources()))
                            .setNegativeButton("拒绝", (dialog, which) -> request.deny())
                            .show();
                    });
                }
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://tour.quanjingke.com/xiangmu/dongmanjituan/xizangwenhua/index.html");
        // 沉浸式全屏
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 动态权限申请
        requestNecessaryPermissions();
        // 陀螺仪注册
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
        return view;
    }

    private void requestNecessaryPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean needRequest = false;
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                    needRequest = true;
                    break;
                }
            }
            if (needRequest) {
                requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // 可在此处理陀螺仪数据，实现视角切换等
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            // 示例：可将数据传递给WebView或VR渲染逻辑
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 可选实现
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
