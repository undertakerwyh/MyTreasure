package com.feicuiedu.treasure.treasure;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2016/11/11.
 */

public interface TreasureApi {
    @POST("/Handler/TreasureHandler.ashx?action=show")
    Call<List<Treasure>>getTreaInArea(@Body Area area);
}
