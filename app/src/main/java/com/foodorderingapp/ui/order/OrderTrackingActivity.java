package com.foodorderingapp.ui.order;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.foodorderingapp.R;
import com.foodorderingapp.utils.StompClient;
import com.foodorderingapp.utils.constants.AppConstants;
import com.foodorderingapp.utils.TokenManager;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderTrackingActivity extends AppCompatActivity {

    private static final String TAG = "OrderTrackingActivity";

    private MapView mapView;
    private IMapController mapController;
    private TextView tvOrderTitle;
    private TextView tvShopName;
    private TextView tvShopAddress;
    private TextView tvDeliveryBuilding;
    private TextView tvShipperName;
    private TextView tvShipperPhone;
    private TextView tvOrderStatus;

    private String orderId;
    private String shopName;
    private String shopAddress;
    private double shopLat;
    private double shopLng;
    private String buildingName;
    private double buildingLat;
    private double buildingLng;
    private String orderStatus;
    private String shipperName;
    private String shipperPhone;

    private Marker shopMarker;
    private Marker buildingMarker;
    private Marker shipperMarker;
    private Polyline routeLine;

    private StompClient stompClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_order_tracking);

        // Get Intent extras
        orderId = getIntent().getStringExtra("ORDER_ID");
        shopName = getIntent().getStringExtra("SHOP_NAME");
        shopAddress = getIntent().getStringExtra("SHOP_ADDRESS");
        shopLat = getIntent().getDoubleExtra("SHOP_LATITUDE", 0.0);
        shopLng = getIntent().getDoubleExtra("SHOP_LONGITUDE", 0.0);
        buildingName = getIntent().getStringExtra("BUILDING_NAME");
        buildingLat = getIntent().getDoubleExtra("BUILDING_LATITUDE", 0.0);
        buildingLng = getIntent().getDoubleExtra("BUILDING_LONGITUDE", 0.0);
        orderStatus = getIntent().getStringExtra("ORDER_STATUS");
        shipperName = getIntent().getStringExtra("SHIPPER_NAME");
        shipperPhone = getIntent().getStringExtra("SHIPPER_PHONE");

        bindViews();
        setupMap();
        connectWebSocket();
    }

    private void bindViews() {
        mapView = findViewById(R.id.map_view);
        tvOrderTitle = findViewById(R.id.tv_order_title);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvShopAddress = findViewById(R.id.tv_shop_address);
        tvDeliveryBuilding = findViewById(R.id.tv_delivery_building);
        tvShipperName = findViewById(R.id.tv_shipper_name);
        tvShipperPhone = findViewById(R.id.tv_shipper_phone);
        tvOrderStatus = findViewById(R.id.tv_order_status);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        if (orderId != null) {
            tvOrderTitle.setText("Đơn hàng: " + orderId.substring(0, 8).toUpperCase());
        }
        tvShopName.setText("Quán: " + (shopName != null ? shopName : "--"));
        tvShopAddress.setText(shopAddress != null ? shopAddress : "Địa chỉ lấy hàng");
        tvDeliveryBuilding.setText("Giao đến: " + (buildingName != null ? buildingName : "--"));

        tvShipperName.setText("Tài xế: " + (shipperName != null ? shipperName : "Đang gán tài xế..."));
        tvShipperPhone.setText("SĐT: " + (shipperPhone != null ? shipperPhone : "--"));

        if (orderStatus != null) {
            tvOrderStatus.setText(orderStatus);
            tvOrderStatus.setVisibility(View.VISIBLE);
        } else {
            tvOrderStatus.setVisibility(View.GONE);
        }
    }

    private static final org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase CARTO_VOYAGER = 
        new org.osmdroid.tileprovider.tilesource.XYTileSource(
            "CartoVoyager",
            0, 20, 256, ".png",
            new String[] {
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
            },
            "© OpenStreetMap contributors, © CARTO"
        );

    private void setupMap() {
        String mapKey = AppConstants.GOONG_MAP_KEY;
        if (mapKey != null && !mapKey.isEmpty() && !mapKey.startsWith("YOUR_")) {
            org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase goongTiles = 
                new org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "GoongMaps",
                    0, 20, 256, ".png?api_key=" + mapKey,
                    new String[] { "https://tiles.goong.io/assets/goong_map_web/" },
                    "© Goong Maps, © OpenStreetMap contributors"
                );
            mapView.setTileSource(goongTiles);
        } else {
            mapView.setTileSource(CARTO_VOYAGER);
        }
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        mapController = mapView.getController();
        mapController.setZoom(17.0);

        // Put markers for Shop & Building
        if (shopLat != 0.0 && shopLng != 0.0) {
            shopMarker = new Marker(mapView);
            shopMarker.setPosition(new GeoPoint(shopLat, shopLng));
            shopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            shopMarker.setTitle("Cửa hàng: " + shopName);
            shopMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location));
            shopMarker.getIcon().setTint(ContextCompat.getColor(this, R.color.vendor_dark_orange));
            mapView.getOverlays().add(shopMarker);
        }

        if (buildingLat != 0.0 && buildingLng != 0.0) {
            buildingMarker = new Marker(mapView);
            buildingMarker.setPosition(new GeoPoint(buildingLat, buildingLng));
            buildingMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            buildingMarker.setTitle("Giao đến: " + buildingName);
            buildingMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location));
            buildingMarker.getIcon().setTint(Color.parseColor("#10B981")); // Emerald Green
            mapView.getOverlays().add(buildingMarker);
        }

        // Draw initial route line
        if (shopLat != 0.0 && buildingLat != 0.0) {
            routeLine = new Polyline();
            List<GeoPoint> pts = new ArrayList<>();
            pts.add(new GeoPoint(shopLat, shopLng));
            pts.add(new GeoPoint(buildingLat, buildingLng));
            routeLine.setPoints(pts);
            routeLine.setColor(Color.parseColor("#4F46E5")); // Indigo line
            routeLine.setWidth(6f);
            mapView.getOverlays().add(routeLine);
        }

        // Center map
        if (shopLat != 0.0 && shopLng != 0.0) {
            mapController.setCenter(new GeoPoint(shopLat, shopLng));
        } else if (buildingLat != 0.0 && buildingLng != 0.0) {
            mapController.setCenter(new GeoPoint(buildingLat, buildingLng));
        } else {
            // Default center: HCMC University Village
            mapController.setCenter(new GeoPoint(10.8756, 106.8006));
        }
    }

    private void connectWebSocket() {
        if (orderId == null) return;

        String wsUrl = AppConstants.getWsChatUrl();
        Map<String, String> headers = new HashMap<>();
        String token = TokenManager.getInstance().getAccessToken();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }

        stompClient = new StompClient(wsUrl, headers);
        stompClient.setConnectionListener(new StompClient.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Kết nối WebSocket thành công, đang subscribe topic location");
                stompClient.subscribe("/topic/orders/" + orderId + "/location", payload -> {
                    try {
                        JSONObject json = new JSONObject(payload);
                        double lat = json.getDouble("latitude");
                        double lon = json.getDouble("longitude");
                        
                        runOnUiThread(() -> updateShipperLocationOnMap(lat, lon));
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi phân giải JSON location: ", e);
                    }
                });
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Đã ngắt kết nối WebSocket");
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Lỗi kết nối WebSocket: ", t);
            }
        });

        stompClient.connect();
    }

    private void updateShipperLocationOnMap(double lat, double lon) {
        GeoPoint toPoint = new GeoPoint(lat, lon);

        if (shipperMarker == null) {
            shipperMarker = new Marker(mapView);
            shipperMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            shipperMarker.setTitle("Tài xế");
            shipperMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location));
            shipperMarker.getIcon().setTint(Color.parseColor("#3B82F6")); // Blue
            mapView.getOverlays().add(shipperMarker);
            shipperMarker.setPosition(toPoint);
            mapView.invalidate();
            updateRouteLine(toPoint);
        } else {
            animateMarker(shipperMarker, toPoint);
        }
    }

    private void animateMarker(final Marker marker, final GeoPoint toPosition) {
        if (marker == null) return;
        final GeoPoint startPosition = marker.getPosition();
        final long start = android.os.SystemClock.uptimeMillis();
        final long duration = 1500; // 1.5s transition

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = android.os.SystemClock.uptimeMillis() - start;
                float t = Math.min(1f, (float) elapsed / duration);

                double lat = t * toPosition.getLatitude() + (1 - t) * startPosition.getLatitude();
                double lng = t * toPosition.getLongitude() + (1 - t) * startPosition.getLongitude();

                marker.setPosition(new GeoPoint(lat, lng));
                mapView.invalidate();

                updateRouteLine(new GeoPoint(lat, lng));

                if (t < 1f) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void updateRouteLine(GeoPoint shipperPt) {
        if (routeLine != null) {
            List<GeoPoint> pts = new ArrayList<>();
            pts.add(shipperPt);
            pts.add(new GeoPoint(shopLat, shopLng));
            pts.add(new GeoPoint(buildingLat, buildingLng));
            routeLine.setPoints(pts);
            mapView.invalidate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}
