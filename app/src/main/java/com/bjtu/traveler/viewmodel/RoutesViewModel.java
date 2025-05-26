package com.bjtu.traveler.viewmodel;

import androidx.lifecycle.ViewModel;

import android.util.Log; // 用于Bmob回调日志

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bjtu.traveler.data.model.DayPlan;

// Bmob相关导入
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import java.util.ArrayList;
import java.util.List;

public class RoutesViewModel extends ViewModel {

    private static final String TAG = "RoutesViewModel";
    private final MutableLiveData<List<DayPlan>> dayPlansLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(); // 用于传递错误信息

    public LiveData<List<DayPlan>> getDayPlansLiveData() {
        return dayPlansLiveData;
    }
    public LiveData<String> getErrorMessage() { return errorMessage; }


    public RoutesViewModel() {
        // 初始化时可以加载默认数据或触发Bmob查询
        // loadDayPlansFromBmob(); // 或者你可以通过一个方法从Fragment触发加载
        loadStaticDayPlans(); // 暂时使用静态数据，方便UI调试
    }

    // 使用静态数据加载行程（用于UI调试，实际应从Bmob加载）
    public void loadStaticDayPlans() {
        List<DayPlan> plans = new ArrayList<>();
        plans.add(new DayPlan(
                "Day 1",
                "解放碑", "bus", // 使用字符串类型
                "洪崖洞", "walk",
                "九街", "walk"
        ));
        plans.add(new DayPlan(
                "Day 2",
                "磁器口", "bus",
                "长江索道", "bus",
                "观音桥", "walk"
        ));
        plans.add(new DayPlan(
                "Day 3",
                "待定景点A", "sightseeing", // 假设有此类型图标
                "待定美食B", "food",        // 假设有此类型图标
                "自由活动C", "walk"
        ));
        dayPlansLiveData.setValue(plans);
    }


    // 从Bmob加载行程计划数据
    public void loadDayPlansFromBmob() {
        BmobQuery<DayPlan> query = new BmobQuery<>();
        // query.addWhereEqualTo("itineraryId", "someItineraryId"); // 如果DayPlan属于某个行程，可以添加查询条件
        // query.order("dayNumber"); // 如果有天数字段，可以排序
        query.findObjects(new FindListener<DayPlan>() {
            @Override
            public void done(List<DayPlan> list, BmobException e) {
                if (e == null) {
                    Log.d(TAG, "Bmob query success: " + list.size() + " items found.");
                    dayPlansLiveData.postValue(list); // 使用postValue因为可能在非UI线程回调
                } else {
                    Log.e(TAG, "Bmob query failed: " + e.getMessage(), e);
                    errorMessage.postValue("加载行程失败: " + e.getMessage());
                    // 可以加载一些本地的默认数据或显示空状态
                    dayPlansLiveData.postValue(new ArrayList<>()); // 清空或设置默认
                }
            }
        });
    }

    // --- Bmob数据操作示例 (保存DayPlan) ---
    // public void saveDayPlanToBmob(DayPlan plan) {
    //     plan.save(new SaveListener<String>() {
    //         @Override
    //         public void done(String objectId, BmobException e) {
    //             if (e == null) {
    //                 Log.d(TAG, "DayPlan saved successfully, objectId: " + objectId);
    //                 // 可以在这里触发一次刷新，或者通知UI保存成功
    //             } else {
    //                 Log.e(TAG, "Failed to save DayPlan: " + e.getMessage(), e);
    //                 errorMessage.postValue("保存行程点失败: " + e.getMessage());
    //             }
    //         }
    //     });
    // }

    // 在ViewModel销毁时，可以考虑取消Bmob的异步查询（如果正在进行）
    // @Override
    // protected void onCleared() {
    //     super.onCleared();
    //     // 取消Bmob查询的操作，Bmob SDK本身可能没有直接的cancel方法给FindListener
    //     // 但如果长时间操作，应注意管理生命周期
    // }
}