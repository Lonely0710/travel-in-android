package com.bjtu.traveler.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bjtu.traveler.data.model.Attraction;
import com.bjtu.traveler.api.OverpassApi;
import java.util.List;

public class ExploreViewModel extends ViewModel {
    private final MutableLiveData<List<Attraction>> attractions = new MutableLiveData<>();

    public LiveData<List<Attraction>> getAttractions() {
        return attractions;
    }

    public void loadAttractions(String city) {
        OverpassApi.fetchAttractions(city, new OverpassApi.Callback() {
            @Override
            public void onSuccess(List<Attraction> data) {
                attractions.setValue(data);
            }
            @Override
            public void onFailure(Exception e) {
                attractions.setValue(null);
            }
        });
    }
}