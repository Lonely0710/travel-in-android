package com.bjtu.traveler.ui.routes; // 假设 ChatFragment 也在 ui.routes 包下

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bjtu.traveler.R;
import com.bjtu.traveler.TravelerApplication;
import com.bjtu.traveler.adapter.ChatAdapter;
import com.bjtu.traveler.utils.DeepSeekApiClient;
import com.bjtu.traveler.data.model.ChatMessage;
import com.bjtu.traveler.data.model.TravelPlan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import android.view.inputmethod.InputMethodManager;

// 确保 ChatFragment 确实在这个包下，如果它在 ui.chat 下，需要修改包名
// 如果 ChatFragment 真的在 ui.routes，那么之前的错误就是奇怪的编译问题
// 但为了保持一致性，我们假设它现在被移动到 ui.routes 了
public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private LinearLayout initialStateContainer;
    private ConstraintLayout chatContainer;
    private LottieAnimationView lottieAnimationView;
    private Button startJourneyButton;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private Handler handler = new Handler(Looper.getMainLooper());

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private TravelPlan currentTravelPlan;

    private ChatState currentChatState;

    private enum ChatState {
        IDLE,
        ASKING_DESTINATION,
        ASKING_DAYS,
        ASKING_BUDGET,
        ASKING_PREFERENCES,
        PLANNING_COMPLETE // 等待跳转
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initialStateContainer = view.findViewById(R.id.initial_state_container);
        chatContainer = view.findViewById(R.id.chat_container);
        lottieAnimationView = view.findViewById(R.id.lottie_animation_view);
        startJourneyButton = view.findViewById(R.id.start_journey_button);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        messageEditText = view.findViewById(R.id.message_edit_text);
        sendButton = view.findViewById(R.id.send_button);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        currentTravelPlan = new TravelPlan();
        currentChatState = ChatState.IDLE; // 初始状态

        // === 在这里使用预加载的 Lottie 动画 ===
        lottieAnimationView.setCacheComposition(true); // 仍然可以设置缓存
        if (TravelerApplication.chatLottieComposition != null) {
            // 如果 composition 已经预加载完成，直接设置
            lottieAnimationView.setComposition(TravelerApplication.chatLottieComposition);
            Log.d(TAG, "Lottie animation set from pre-loaded composition.");
        } else {
            // 如果预加载还未完成或失败，则回退到从资源文件加载
            lottieAnimationView.setAnimation(R.raw.mapping_man);
            Log.d(TAG, "Lottie animation loading directly (pre-load not ready or failed).");
        }
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE); // 设置循环播放
        lottieAnimationView.playAnimation(); // 开始播放动画
        setupInitialState(); // 设置初始UI状态

        startJourneyButton.setOnClickListener(v -> {
            startChat();
        });

        sendButton.setOnClickListener(v -> {
            sendMessage();
        });

        // --- 监听键盘回车事件 ---
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                // 如果是发送动作，或者回车键被按下
                sendMessage();
                return true; // 表示已处理事件
            }
            return false; // 表示未处理事件
        });

        return view;
    }

    private void setupInitialState() {
        initialStateContainer.setVisibility(View.VISIBLE);
        chatContainer.setVisibility(View.GONE);
        lottieAnimationView.playAnimation();
    }

    private void startChat() {
        initialStateContainer.setVisibility(View.GONE);
        chatContainer.setVisibility(View.VISIBLE);
        // 在切换到聊天界面时，停止Lottie动画并隐藏它
        lottieAnimationView.cancelAnimation(); // 停止动画
        lottieAnimationView.setVisibility(View.GONE); // 隐藏Lottie视图

        // 第一个 AI 消息 - 欢迎语
        addBotMessage("你好！我是你的专属AI旅游规划师。很高兴为你服务！");
        currentChatState = ChatState.ASKING_DESTINATION; // 先设置状态，防止用户在提问前发送消息

        // 延迟弹出第一个提问
        handler.postDelayed(() -> {
            if (isAdded()) { // 确保 Fragment 仍然附加到 Activity
                addBotMessage("首先，告诉我你希望去哪里旅行？");
            }
        }, 800); // 延迟 800 毫秒
    }

    private void addMessage(String content, boolean isSentByUser) {
        messageList.add(new ChatMessage(content, isSentByUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1); // 滚动到最新消息
    }

    private void addBotMessage(String content) {
        addMessage(content, false);
    }

    private void addUserMessage(String content) {
        addMessage(content, true);
    }

    private void sendMessage() {
        String userText = messageEditText.getText().toString().trim();
        if (userText.isEmpty()) {
            Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserMessage(userText);
        messageEditText.setText(""); // 清空输入框
        processUserInput(userText); // 处理用户输入

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    private void processUserInput(String rawInput) {
        // 显示加载状态
        addBotMessage("正在分析您的输入...");
        showLoading(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                String simplifiedInput = null;
                // 根据当前状态，调用AI简化信息
                switch (currentChatState) {
                    case ASKING_DESTINATION:
                        simplifiedInput = DeepSeekApiClient.getSimplifiedInput(rawInput, "旅游目的地");
                        currentTravelPlan.setRawDestinationInput(rawInput);
                        break;
                    case ASKING_DAYS:
                        simplifiedInput = DeepSeekApiClient.getSimplifiedInput(rawInput, "天数");
                        currentTravelPlan.setRawDaysInput(rawInput);
                        break;
                    case ASKING_BUDGET:
                        simplifiedInput = DeepSeekApiClient.getSimplifiedInput(rawInput, "预算");
                        currentTravelPlan.setRawBudgetInput(rawInput);
                        break;
                    case ASKING_PREFERENCES:
                        simplifiedInput = DeepSeekApiClient.getSimplifiedInput(rawInput, "偏好");
                        currentTravelPlan.setRawPreferencesInput(rawInput);
                        break;
                }
                return simplifiedInput;
            } catch (IOException e) {
                Log.e(TAG, "DeepSeek API call failed: " + e.getMessage(), e);
                return null; // 返回null表示API调用失败
            }
        }).thenAccept(simplifiedInput -> {
            requireActivity().runOnUiThread(() -> {
                showLoading(false); // 隐藏加载状态
                // 移除"正在分析..."消息
                if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getContent().equals("正在分析您的输入...")) {
                    messageList.remove(messageList.size() - 1);
                    chatAdapter.notifyItemRemoved(messageList.size());
                }

                if (simplifiedInput == null || simplifiedInput.equalsIgnoreCase("null") || simplifiedInput.isEmpty()) {
                    handleInvalidInput(); // 处理无效输入
                } else {
                    handleSimplifiedInput(simplifiedInput); // 处理简化后的输入
                }
            });
        }).exceptionally(e -> {
            requireActivity().runOnUiThread(() -> {
                showLoading(false); // 隐藏加载状态
                // 移除"正在分析..."消息
                if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getContent().equals("正在分析您的输入...")) {
                    messageList.remove(messageList.size() - 1);
                    chatAdapter.notifyItemRemoved(messageList.size());
                }
                Log.e(TAG, "Error processing user input: " + e.getMessage(), e);
                addBotMessage("抱歉，我的AI助手暂时无法处理您的请求。请稍后再试或换种方式提问。");
                Toast.makeText(getContext(), "AI助手错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            return null;
        });
    }

    private void handleInvalidInput() {
        switch (currentChatState) {
            case ASKING_DESTINATION:
                addBotMessage("抱歉，我没有识别出有效的目的地。请告诉我您想去哪个城市旅行？");
                break;
            case ASKING_DAYS:
                addBotMessage("抱歉，我没有识别出有效的旅行天数。请告诉我您计划旅行几天？（例如：3天）");
                break;
            case ASKING_BUDGET:
                addBotMessage("抱歉，我没有识别出有效的预算。请告诉我您的预算大概是多少？（例如：5000元）");
                break;
            case ASKING_PREFERENCES:
                addBotMessage("抱歉，我没有识别出有效的偏好。请再次告诉我您的旅行偏好，例如：喜欢历史文化、美食、户外活动等。");
                break;
        }
    }

    private void handleSimplifiedInput(String simplifiedInput) {
        switch (currentChatState) {
            case ASKING_DESTINATION:
                currentTravelPlan.setDestination(simplifiedInput);
                addBotMessage("好的，要去" + simplifiedInput + "！计划旅行几天呢？");
                currentChatState = ChatState.ASKING_DAYS;
                break;
            case ASKING_DAYS:
                try {
                    int days = Integer.parseInt(simplifiedInput.replaceAll("[^\\d]", "")); // 提取数字
                    if (days <= 0) throw new NumberFormatException();
                    currentTravelPlan.setDays(days);
                    addBotMessage("好的，" + days + "天。预算大概是多少呢？");
                    currentChatState = ChatState.ASKING_BUDGET;
                } catch (NumberFormatException e) {
                    addBotMessage("抱歉，天数输入无效。请告诉我您计划旅行几天？");
                }
                break;
            case ASKING_BUDGET:
                try {
                    int budget = Integer.parseInt(simplifiedInput.replaceAll("[^\\d]", "")); // 提取数字
                    if (budget < 0) throw new NumberFormatException();
                    currentTravelPlan.setBudget(budget);
                    addBotMessage("好的，预算是" + budget + "元。最后，您对旅行有什么偏好吗？例如：喜欢历史文化、美食、户外活动、购物等。");
                    currentChatState = ChatState.ASKING_PREFERENCES;
                } catch (NumberFormatException e) {
                    addBotMessage("抱歉，预算输入无效。请告诉我您的预算大概是多少？");
                }
                break;
            case ASKING_PREFERENCES:
                String[] preferencesArray = simplifiedInput.split("\\+");
                List<String> preferencesList = Arrays.asList(preferencesArray);
                currentTravelPlan.setPreferences(preferencesList);
                addBotMessage("好的，我已了解您的偏好。正在为您准备专属的旅行计划，请稍候...");
                currentChatState = ChatState.PLANNING_COMPLETE;
                messageEditText.setEnabled(false); // 禁用输入
                sendButton.setEnabled(false);     // 禁用发送
                sendButton.setAlpha(0.5f);

                // 立即或稍作非常短的延迟后跳转，让用户看到上面的消息
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        navigateToRoutesFragmentManually(); // 跳转到 RoutesFragment
                    }
                }, 500);
                break;
        }
    }

    private void showLoading(boolean isLoading) {
        messageEditText.setEnabled(!isLoading);
        sendButton.setEnabled(!isLoading);
        if (isLoading) {
            // 可以使用一个 ProgressBar 或 Lottie 动画来表示加载
            // sendButton.setImageResource(R.drawable.ic_hourglass_empty); // 假设你有一个沙漏图标
            // 或者更简单：
            sendButton.setAlpha(0.5f); // 半透明表示不可用
        } else {
            // sendButton.setImageResource(R.drawable.ic_send); // 恢复发送图标
            sendButton.setAlpha(1.0f); // 恢复透明度
        }
    }

    // === 新的 manual navigation 方法 ===
    private void navigateToRoutesFragmentManually() {
        if (getContext() == null || currentTravelPlan == null) {
            Log.e(TAG, "Context or TravelPlan is null, cannot navigate manually.");
            Toast.makeText(getContext(), "内部错误：无法跳转。", Toast.LENGTH_SHORT).show();
            return;
        }

        addBotMessage("准备跳转到行程规划页面...");
        Log.d(TAG, "Manually navigating to RoutesFragment with plan: " + currentTravelPlan.toString());

        // 创建 RoutesFragment 实例
        com.bjtu.traveler.ui.routes.RoutesFragment routesFragment = new com.bjtu.traveler.ui.routes.RoutesFragment();

        // 创建 Bundle 来传递数据
        Bundle bundle = new Bundle();
        bundle.putSerializable("travelPlan", currentTravelPlan); // TravelPlan 必须实现 Serializable
        routesFragment.setArguments(bundle); // 将 Bundle 设置给新的 Fragment 实例

        // 获取 FragmentManager 并执行事务
        // R.id.fragment_container 是您 activity_main.xml 中 FrameLayout 的 ID
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, routesFragment) // 替换掉当前容器中的 Fragment
                    .addToBackStack(null) // 将当前 ChatFragment 加入返回栈，以便用户可以按返回键返回
                    .commit(); // 提交事务
        } else {
            Log.e(TAG, "Parent FragmentManager is null, cannot navigate manually.");
            Toast.makeText(getContext(), "内部错误：无法跳转。", Toast.LENGTH_SHORT).show();
        }
    }

    // ... （Lottie 动画管理可以根据需要添加 onResume/onPause/onDestroyView）
    @Override
    public void onResume() {
        super.onResume();
        if (currentChatState == ChatState.IDLE && lottieAnimationView != null) {
            lottieAnimationView.resumeAnimation(); // 恢复播放
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentChatState == ChatState.IDLE && lottieAnimationView != null) {
            lottieAnimationView.pauseAnimation(); // 暂停播放
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (lottieAnimationView != null) {
            lottieAnimationView.cancelAnimation(); // 销毁视图时取消动画
        }
    }
}