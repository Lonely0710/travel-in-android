package com.bjtu.traveler.ui.common;

import android.graphics.Bitmap; // Import Bitmap for favicon
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar; // Import ProgressBar
//import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView; // Import LottieAnimationView
import com.bjtu.traveler.R;
import android.util.Log;

/**
 * 通用的 WebView Fragment，用于加载和显示网页内容。
 * 包含一个带有返回按钮和图标的顶部栏。
 */
public class WebViewFragment extends Fragment {

    // 用于传递加载 URL 的参数 Key
    private static final String ARG_URL = "url";
    // Fragment 的 TAG，方便日志输出
    private static final String TAG = "WebViewFragment";

    // 需要加载的网页 URL
    private String url;
    // WebView 控件，用于显示网页
    private WebView webView;
    // TextView tvTitle; // 已根据用户要求移除标题文本
    // 顶部栏中的图标 ImageView
    private ImageView ivTitleIcon;
    // 顶部栏中的返回按钮 ImageView
    private ImageView btnBack;
    // 顶部栏下方的加载进度条
    private ProgressBar progressBar; // Add ProgressBar field
    // 覆盖在 WebView 上的 Lottie 加载动画
    private LottieAnimationView loadingAnimationView; // Add LottieAnimationView field

    /**
     * 创建一个新的 WebViewFragment 实例。
     * @param url 需要加载的网页 URL。
     * @return WebViewFragment 实例。
     */
    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从 arguments 中获取 URL
        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 填充 fragment_webview 布局
        View root = inflater.inflate(R.layout.fragment_webview, container, false);

        // 查找 UI 元素
        webView = root.findViewById(R.id.webview);
        progressBar = root.findViewById(R.id.webview_progress_bar); // Find ProgressBar
        loadingAnimationView = root.findViewById(R.id.webview_loading_animation); // Find LottieAnimationView
        // 假设包含的顶部栏布局的根视图 ID 是 topbar_webview
        View topBar = root.findViewById(R.id.topbar_webview); 

        // 配置顶部栏
        if (topBar != null) {
            // 查找顶部栏中的图标 ImageView 和返回按钮 ImageView
            ivTitleIcon = topBar.findViewById(R.id.iv_title_icon);
            btnBack = topBar.findViewById(R.id.btn_back);

            // 设置返回按钮
            if (btnBack != null) {
                // 使用 ic_arrow_left.xml 作为返回按钮图标
                btnBack.setImageResource(R.drawable.ic_arrow_left); 
                btnBack.setOnClickListener(v -> {
                    // 点击返回按钮时，弹出当前 Fragment，返回上一页
                    getParentFragmentManager().popBackStack();
                });
            }

            // 设置顶部栏图标 - 根据 URL 动态设置
            if (ivTitleIcon != null) {
                 // 初始时隐藏图标
                 ivTitleIcon.setVisibility(View.GONE);
                 // 图标将根据加载完成后的 URL 在 onPageFinished 中设置
            }
            // tvTitle = topBar.findViewById(R.id.tv_title); // 已移除标题文本
        } else {
            // 如果找不到顶部栏或其元素，可以记录错误或采取默认行为
             Log.e(TAG, "Topbar or its elements not found.");
        }

        // 配置 WebView 的设置和客户端
        if (webView != null) {
            // 启用 JavaScript
            webView.getSettings().setJavaScriptEnabled(true);
            // 启用 DOM 存储
            webView.getSettings().setDomStorageEnabled(true);
            // 设置页面概览模式，宽度适应屏幕
            webView.getSettings().setLoadWithOverviewMode(true);
            // 设置使用广角视口
            webView.getSettings().setUseWideViewPort(true);
            // 启用内置的缩放控件
            webView.getSettings().setBuiltInZoomControls(true);
            // 隐藏缩放控件
            webView.getSettings().setDisplayZoomControls(false);
            // 设置缓存模式
            webView.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);

            // 设置 WebViewClient 处理页面导航和加载完成事件
            webView.setWebViewClient(new WebViewClient(){
                 @Override
                 public boolean shouldOverrideUrlLoading(WebView view, String url) {
                     // 在当前 WebView 中加载 URL，而不是打开外部浏览器
                     view.loadUrl(url);
                     return true;
                 }

                 // 在页面加载开始时显示动画和进度条
                 @Override
                 public void onPageStarted(WebView view, String url, Bitmap favicon) {
                     super.onPageStarted(view, url, favicon);
                     if (loadingAnimationView != null) loadingAnimationView.setVisibility(View.VISIBLE);
                     if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                 }

                 // 在页面加载完成后隐藏动画和进度条，并更新图标
                 @Override
                 public void onPageFinished(WebView view, String url) {
                     super.onPageFinished(view, url);
                     if (loadingAnimationView != null) loadingAnimationView.setVisibility(View.GONE);
                     if (progressBar != null) progressBar.setVisibility(View.GONE);

                     if (ivTitleIcon != null && url != null) {
                         // 将 URL 转换为小写以便进行不区分大小写的比较
                         String lowerCaseUrl = url.toLowerCase();
                         // 根据 URL 中的关键字设置图标
                         if (lowerCaseUrl.contains("airbnb")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_airbnb);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("amap") || lowerCaseUrl.contains("gaode")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_gaode);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                             // 设置高德专属topbar背景色
                             View topBar = getView().findViewById(R.id.topbar_webview);
                             if (topBar != null) {
                                 topBar.setBackgroundColor(0xFF262B46);
                             }
                             // 设置返回按钮为白色
                             if (btnBack != null) {
                                 btnBack.setImageResource(R.drawable.ic_arrow_left_white);
                             }
                         } else if (lowerCaseUrl.contains("feizhu")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_feizhu);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("mafengwo")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_mafengwo);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("meituan")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_meituan);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("qunaer")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_qunaer);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("unsplash")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_unsplash);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("wiki")) {
                             ivTitleIcon.setImageResource(R.drawable.ic_web_wiki);
                             ivTitleIcon.setVisibility(View.VISIBLE);
                         } else if (lowerCaseUrl.contains("ctrip") || lowerCaseUrl.contains("xiecheng")) { // 携程或 ctrip
                              ivTitleIcon.setImageResource(R.drawable.ic_web_xiecheng); // 使用 xiecheng 图标
                              ivTitleIcon.setVisibility(View.VISIBLE);
                         } else {
                             // 如果没有匹配的关键字，隐藏图标
                             ivTitleIcon.setVisibility(View.GONE);
                         }
                     }
                 }
            });

            // 添加 WebChromeClient 处理进度条、页面标题等
             webView.setWebChromeClient(new android.webkit.WebChromeClient() {
                 @Override
                 public void onReceivedTitle(WebView view, String title) {
                     super.onReceivedTitle(view, title);
                     // 如果需要，可以在这里获取页面标题（目前未用于 UI）
                 }
                 // 实现 onProgressChanged 来显示进度条
                 @Override
                 public void onProgressChanged(WebView view, int newProgress) {
                     super.onProgressChanged(view, newProgress);
                     if (progressBar != null) {
                          progressBar.setProgress(newProgress);
                          // 进度达到 100 时隐藏进度条，加载完成时在 onPageFinished 中也隐藏了
                          if (newProgress == 100) {
                               progressBar.setVisibility(View.GONE);
                          }
                     }
                 }
             });

            // 如果 URL 不为空，加载网页
            if (url != null) {
                webView.loadUrl(url);
            }
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理 WebView 以防止内存泄漏
        if (webView != null) {
            webView.removeAllViews();
            // 移除 WebViewClient 和 WebChromeClient 引用
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.destroy();
            webView = null;
        }
         // 清理 Lottie 动画
        if (loadingAnimationView != null) {
             loadingAnimationView.cancelAnimation();
        }
    }
} 