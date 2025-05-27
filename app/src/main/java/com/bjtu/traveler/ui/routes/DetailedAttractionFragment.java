package com.bjtu.traveler.ui.routes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView; 
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bjtu.traveler.R;

public class DetailedAttractionFragment extends Fragment {

    private static final String TAG = "WebViewFragment";
    private static final String ARG_URL = "url";

    private WebView webView;
    private ProgressBar progressBar;
    private String urlToLoad;
    private Toolbar toolbar;
    private ImageView toolbarIcon; // 仍然需要引用
    private TextView toolbarTitleTextView;


    public static DetailedAttractionFragment newInstance(String url, String title) { // 移除了 logoUrl 参数
        DetailedAttractionFragment fragment = new DetailedAttractionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            urlToLoad = getArguments().getString(ARG_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detailed_attraction, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        toolbarIcon = view.findViewById(R.id.toolbar_icon); // 引用 ImageView
        webView = view.findViewById(R.id.web_view);
        progressBar = view.findViewById(R.id.progress_bar);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupWebView();
        if (urlToLoad != null && !urlToLoad.isEmpty()) {
            webView.loadUrl(urlToLoad);
        } else {
            Log.e(TAG, "URL to load is null or empty.");
            Toast.makeText(getContext(), "无法加载页面，链接无效。", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    // setupWebView 和 onDestroyView 方法保持不变
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    progressBar.setProgress(newProgress);
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroyView();
    }
}