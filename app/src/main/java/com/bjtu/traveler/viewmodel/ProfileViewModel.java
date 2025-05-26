// app/src/main/java/com/bjtu/traveler/viewmodel/ProfileViewModel.java
package com.bjtu.traveler.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {
    public final UserRepository userRepository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
    }

    // 直接返回当前用户对象
    public User getCurrentUser() {
        return userRepository.getCurrentUser();
    }
}