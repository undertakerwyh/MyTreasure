package com.feicuiedu.treasure.treasure.home.map;

import com.feicuiedu.treasure.net.NetClient;
import com.feicuiedu.treasure.treasure.Area;
import com.feicuiedu.treasure.treasure.Treasure;
import com.feicuiedu.treasure.treasure.TreasureApi;
import com.feicuiedu.treasure.treasure.TreasureRepo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2016/11/11.
 */

public class MapPresenter {

    private TreasureApi treasureApi;
    private MapMvpView mapMvpView;
    private Area area;
    public MapPresenter(MapMvpView mapMvpView){
        this.mapMvpView = mapMvpView;
    }

    public void getTreasure(final Area area){
        if(TreasureRepo.getInstance().isCached(area)){
            return;
        }
        this.area = area;
        treasureApi = NetClient.getInstance().getTreasureApi();
        treasureApi.getTreaInArea(area).enqueue(new Callback<List<Treasure>>() {
            @Override
            public void onResponse(Call<List<Treasure>> call, Response<List<Treasure>> response) {
                if(response.isSuccessful()){
                    List<Treasure> body = response.body();
                    if(body==null){
                        mapMvpView.showMessage("出现未知错误");
                        return;
                    }
                    mapMvpView.setData(body);
                    TreasureRepo.getInstance().addTreasure(body);
                    TreasureRepo.getInstance().cache(area);
                }
            }

            @Override
            public void onFailure(Call<List<Treasure>> call, Throwable t) {
                mapMvpView.showMessage(t.getMessage());
            }
        });
    }
}
