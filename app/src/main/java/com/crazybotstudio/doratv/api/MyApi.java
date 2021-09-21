package com.crazybotstudio.doratv.api;

import com.crazybotstudio.doratv.models.MainCategory;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;

public interface MyApi {

    @GET("getmaincategory.php")
    Call<List<MainCategory>> getCategory();

}
