package com.mtsealove.github.iot.Database;

import androidx.annotation.Nullable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {
    @POST("/Login")
    Call<Account> PostLogin(@Body LoginData loginData);

    @POST("/Update/Location")
    Call<RestResult> UpdateLocation(@Body RequestAddress requestAddress);

    @GET("/GetItemList")
    Call<AItemList> GetLItemList(@Query("driver_id") String driver_id, @Query("sort") @Nullable String sort);

    @POST("/Update/Status/Driver")
    Call<RestResult> UpdateDriverStatus(@Body RequestDriverStatus requestDriverStatus);

    @POST("/Update/Status/Aitem")
    Call<RestResult> UpdateAitemStatus(@Body RequestAitemStatus aitemStatus);

    @GET("/Get/Aitem")
    Call<AItem> GetAItem(@Query("invoice") String invoice);

    @GET("/Get/Status/Done")
    Call<RestResult> GetStatusDone(@Query("driver_id") String driver_id, @Query("status") int status);
}
