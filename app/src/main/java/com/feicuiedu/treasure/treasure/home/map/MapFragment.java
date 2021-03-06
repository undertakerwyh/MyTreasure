package com.feicuiedu.treasure.treasure.home.map;


import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.feicuiedu.treasure.R;
import com.feicuiedu.treasure.commons.ActivityUtils;
import com.feicuiedu.treasure.components.TreasureView;
import com.feicuiedu.treasure.treasure.Area;
import com.feicuiedu.treasure.treasure.Treasure;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 *
 */
public class MapFragment extends Fragment implements MapMvpView{

    @BindView(R.id.map_frame)
    FrameLayout mapFrame;
    @BindView(R.id.centerLayout)
    RelativeLayout centerLayout;
    @BindView(R.id.treasureView)
    TreasureView treasureView;
    @BindView(R.id.layout_bottom)
    FrameLayout layoutBottom;
    @BindView(R.id.hide_treasure)
    RelativeLayout hideTreasure;
    @BindView(R.id.btn_HideHere)
    Button btnHideHere;
    @BindView(R.id.tv_currentLocation)
    TextView tvCurrentLocation;
    @BindView(R.id.iv_located)
    ImageView ivLocated;
    @BindView(R.id.et_treasureTitle)
    EditText etTreasureTitle;

    private MapView mapView;
    private BaiduMap baiduMap;
    private Unbinder bind;
    private LatLng myLocation;
    private LatLng target;

    private ActivityUtils activityUtils;

    private boolean isFirst = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        bind = ButterKnife.bind(this, view);
        activityUtils = new ActivityUtils(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化百度地图
        initBaiduMap();

        //初始化位置
        initLocation();
    }

    private void initLocation() {
        baiduMap.setMyLocationEnabled(true);
        final LocationClient myLocationClient = new LocationClient(getContext());
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true);
        locationClientOption.setIsNeedAddress(true);
        locationClientOption.setAddrType("bd09ll");
        myLocationClient.setLocOption(locationClientOption);
        myLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null) {
                    myLocationClient.requestLocation();
                    return;
                }
                double lng = bdLocation.getLongitude();
                double lat = bdLocation.getLatitude();
                myLocation = new LatLng(lat, lng);
                MyLocationData myLocationData = new MyLocationData.Builder()
                        .latitude(lat)
                        .longitude(lng)
                        .accuracy(100f)
                        .build();
                baiduMap.setMyLocationData(myLocationData);
                if (isFirst) {
                    moveToMyLocation();
                    isFirst = false;
                }
            }
        });
        myLocationClient.start();
        myLocationClient.requestLocation();

    }

    @OnClick(R.id.tv_located)
    public void moveToMyLocation() {
        MapStatus mapStatus = new MapStatus.Builder()
                .target(myLocation)
                .rotate(0)
                .zoom(19)
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.animateMapStatus(mapStatusUpdate);
    }

    @OnClick(R.id.tv_compass)
    public void switchCompass() {
        boolean compassEnabled = baiduMap.getUiSettings().isCompassEnabled();
        baiduMap.getUiSettings().setCompassEnabled(!compassEnabled);
        baiduMap.setCompassPosition(new Point(100,100));
    }

    private void initBaiduMap() {

        // 查看百度地图的ＡＰＩ

        // 百度地图状态
        MapStatus mapStatus = new MapStatus.Builder()
                .overlook(0)// 0--(-45) 地图的俯仰角度
                .zoom(15)// 3--21 缩放级别
                .build();

        BaiduMapOptions options = new BaiduMapOptions()
                .mapStatus(mapStatus)// 设置地图的状态
                .compassEnabled(true)// 指南针
                .zoomGesturesEnabled(true)// 设置允许缩放手势
                .rotateGesturesEnabled(true)// 旋转
                .scaleControlEnabled(false)// 不显示比例尺控件
                .zoomControlsEnabled(false)// 不显示缩放控件
                ;

        // 创建一个MapView
        mapView = new MapView(getContext(), options);

        // 在当前的Layout上面添加MapView
        mapFrame.addView(mapView, 0);

        // MapView 的控制器
        baiduMap = mapView.getMap();

        // 怎么对地图状态进行监听？
        baiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(currentMarker!=null){
                    currentMarker.setVisible(true);
                }
                currentMarker = marker;
                marker.setVisible(false);
                InfoWindow infoWindow = new InfoWindow(click_dot, marker.getPosition(), 0, new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {

                    }
                });
                baiduMap.showInfoWindow(infoWindow);
                return false;
            }
        });

    }
    private Marker currentMarker;


    // 地图类型的切换（普通视图--卫星视图）
    @OnClick(R.id.tv_satellite)
    public void switchMapType() {

        // 先获得当前的类型
        int type = baiduMap.getMapType();
        type = type == BaiduMap.MAP_TYPE_NORMAL ? BaiduMap.MAP_TYPE_SATELLITE : BaiduMap.MAP_TYPE_NORMAL;
        baiduMap.setMapType(type);
    }

    // 百度地图状态的监听
    private BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            LatLng target = mapStatus.target;
            if(target!=MapFragment.this.target){
                updateMapArea();
                MapFragment.this.target = target;
            }
        }
    };

    private void updateMapArea() {
        MapStatus mapStatus = baiduMap.getMapStatus();
        double lng = mapStatus.target.longitude;
        double lat = mapStatus.target.latitude;
        Area area = new Area();
        area.setMaxLat(Math.ceil(lat));
        area.setMaxLng(Math.ceil(lng));
        area.setMinLat(Math.floor(lat));
        area.setMinLng(Math.floor(lng));
        new MapPresenter(this).getTreasure(area);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }


    @OnClick({R.id.iv_scaleUp, R.id.iv_scaleDown})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_scaleUp:
                baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
                break;
            case R.id.iv_scaleDown:
                baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
                break;
        }
    }

    @Override
    public void showMessage(String msg) {
        activityUtils.showToast(msg);
    }

    @Override
    public void setData(List<Treasure> list) {
        for(Treasure treasure :list){
            LatLng latLng = new LatLng(treasure.getLatitude(),treasure.getLongitude());
            addMarker(latLng,treasure.getId());
        }
    }
    private BitmapDescriptor dot = BitmapDescriptorFactory.fromResource(R.drawable.treasure_dot);
    private BitmapDescriptor click_dot = BitmapDescriptorFactory.fromResource(R.drawable.treasure_expanded);


    private void addMarker(LatLng latLng, int treasureId) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(dot);
        markerOptions.anchor(0.5f,0.5f);
        Bundle bundle = new Bundle();
        bundle.putInt("id",treasureId);
        markerOptions.extraInfo(bundle);
        baiduMap.addOverlay(markerOptions);
    }
}
