package com.foodorderingapp.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.ShopLocationDTO;
import com.foodorderingapp.ui.shop.ShopDetailActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    private static final String TAG = "FoodMapActivity";

    private GoogleMap mMap;
    private View layoutBottom;
    private TextView tvShopName, tvShopInfo;
    private final List<ShopLocationDTO> shopLocations = new ArrayList<>();
    private final List<Marker> markers = new ArrayList<>();
    private Double userLat = null;
    private Double userLng = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_map);

        bindViews();
        setupMapFragment();
        checkLocationPermission();
        loadShopLocations();
    }

    private void bindViews() {
        layoutBottom = findViewById(R.id.layout_map_bottom);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvShopInfo = findViewById(R.id.tv_shop_info);
        findViewById(R.id.btn_back_map).setOnClickListener(v -> finish());
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Áp dụng Custom Map Style (ẩn POI business)
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                android.util.Log.e(TAG, "Style parsing failed.");
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Can't find style. Error: ", e);
        }

        // 2. Cấu hình UI
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);

        // 3. Cắm markers nếu đã có dữ liệu
        if (!shopLocations.isEmpty()) {
            addMarkersToMap();
        }
    }

    private void loadShopLocations() {
        ApiClient.getApiService().getShopLocations().enqueue(new Callback<List<ShopLocationDTO>>() {
            @Override
            public void onResponse(Call<List<ShopLocationDTO>> call, Response<List<ShopLocationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shopLocations.clear();
                    shopLocations.addAll(response.body());
                    if (mMap != null) {
                        addMarkersToMap();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ShopLocationDTO>> call, Throwable t) {
                android.util.Log.e(TAG, "Failed to load shop locations: " + t.getMessage());
                Toast.makeText(FoodMapActivity.this, "Không thể tải dữ liệu bản đồ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkersToMap() {
        if (mMap == null || shopLocations.isEmpty()) return;

        mMap.clear();
        markers.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (ShopLocationDTO shop : shopLocations) {
            if (shop.getLatitude() == null || shop.getLongitude() == null) continue;

            LatLng latLng = new LatLng(shop.getLatitude(), shop.getLongitude());

            // Chọn icon dựa trên trạng thái mở cửa
            float hue = shop.isCurrentlyOpen()
                    ? BitmapDescriptorFactory.HUE_GREEN   // Đang mở: xanh
                    : BitmapDescriptorFactory.HUE_ORANGE;  // Đóng: cam

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(shop.getName())
                    .snippet(shop.isCurrentlyOpen() ? "🟢 Đang mở cửa" : "🔴 Đã đóng cửa")
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            marker.setTag(shop.getId());
            markers.add(marker);

            boundsBuilder.include(latLng);
        }

        // Zoom để fit tất cả markers
        if (!markers.isEmpty()) {
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), 100));
            } catch (Exception e) {
                // Fallback nếu bounds quá nhỏ
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        markers.get(0).getPosition(), 15));
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String shopId = (String) marker.getTag();
        if (shopId == null) return false;

        marker.showInfoWindow();

        // Mở ShopDetailActivity khi click vào InfoWindow
        mMap.setOnInfoWindowClickListener(m -> {
            String id = (String) m.getTag();
            Intent intent = new Intent(FoodMapActivity.this, ShopDetailActivity.class);
            intent.putExtra("SHOP_ID", id);
            startActivity(intent);
        });

        // Hiển thị thông tin ở bottom panel
        for (ShopLocationDTO shop : shopLocations) {
            if (shop.getId().equals(shopId)) {
                tvShopName.setText(shop.getName());
                String info = "⭐ " + (shop.getRating() != null ? String.format("%.1f", shop.getRating()) : "N/A");
                if (shop.getAddress() != null) {
                    info += " | " + shop.getAddress();
                }
                tvShopInfo.setText(info);
                layoutBottom.setVisibility(View.VISIBLE);
                break;
            }
        }

        return true;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLastLocation();
        }
    }

    private void getUserLastLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnown == null) {
                        lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (lastKnown != null) {
                        userLat = lastKnown.getLatitude();
                        userLng = lastKnown.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to get location: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLastLocation();
            }
        }
    }
}