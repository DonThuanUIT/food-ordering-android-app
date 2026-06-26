package com.foodorderingapp.ui.home.shipper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.UpdateStatusRequest;
import com.foodorderingapp.model.response.OrderResponse;
import com.google.android.material.button.MaterialButton;
import com.foodorderingapp.utils.constants.AppConstants;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipperDeliveryMapActivity extends AppCompatActivity {

    private static final String TAG = "ShipperDeliveryMap";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2002;

    private MapView mapView;
    private IMapController mapController;
    private TextView tvOrderTitle;
    private TextView tvShopName;
    private TextView tvShopAddress;
    private TextView tvCustomerName;
    private TextView tvDeliveryBuilding;
    private MaterialButton btnAction;

    private String orderId;
    private String shopName;
    private String shopAddress;
    private double shopLat;
    private double shopLng;
    private String customerName;
    private String buildingName;
    private double buildingLat;
    private double buildingLng;
    private String dropOff;
    private String orderStatus;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Double currentLat = null;
    private Double currentLng = null;

    private Marker shopMarker;
    private Marker buildingMarker;
    private Marker shipperMarker;
    private Polyline routeLine;
    private long lastRouteFetchTime = 0;
    private GeoPoint lastFetchedShipperLocation = null;

    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            sendShipperLocationToServer();
            updateHandler.postDelayed(this, 5000); // repeat every 5 seconds
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_shipper_delivery_map);

        // Get Intent extras
        orderId = getIntent().getStringExtra("ORDER_ID");
        shopName = getIntent().getStringExtra("SHOP_NAME");
        shopAddress = getIntent().getStringExtra("SHOP_ADDRESS");
        shopLat = getIntent().getDoubleExtra("SHOP_LATITUDE", 0.0);
        shopLng = getIntent().getDoubleExtra("SHOP_LONGITUDE", 0.0);
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");
        buildingName = getIntent().getStringExtra("BUILDING_NAME");
        buildingLat = getIntent().getDoubleExtra("BUILDING_LATITUDE", 0.0);
        buildingLng = getIntent().getDoubleExtra("BUILDING_LONGITUDE", 0.0);
        dropOff = getIntent().getStringExtra("DROP_OFF");
        orderStatus = getIntent().getStringExtra("ORDER_STATUS");

        bindViews();
        setupMap();
        checkLocationPermissions();
    }

    private void bindViews() {
        mapView = findViewById(R.id.map_view);
        tvOrderTitle = findViewById(R.id.tv_order_title);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvShopAddress = findViewById(R.id.tv_shop_address);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvDeliveryBuilding = findViewById(R.id.tv_delivery_building);
        btnAction = findViewById(R.id.btn_action);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        if (orderId != null) {
            tvOrderTitle.setText("Đơn hàng: " + orderId.substring(0, 8).toUpperCase());
        }
        tvShopName.setText("Quán: " + (shopName != null ? shopName : "--"));
        tvShopAddress.setText(shopAddress != null ? shopAddress : "Địa chỉ lấy hàng");
        tvCustomerName.setText("Khách: " + (customerName != null ? customerName : "--"));
        tvDeliveryBuilding.setText("Giao đến: " + (buildingName != null ? buildingName : "--") + " (" + (dropOff != null ? dropOff : "") + ")");

        updateButtonState();

        btnAction.setOnClickListener(v -> handleActionButtonClick());
    }

    private void updateButtonState() {
        if ("CONFIRMED".equals(orderStatus)) {
            btnAction.setText("Bắt đầu giao hàng");
            btnAction.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.vendor_dark_orange));
            btnAction.setVisibility(View.VISIBLE);
        } else if ("DELIVERING".equals(orderStatus)) {
            btnAction.setText("Đã giao đến nơi");
            btnAction.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_green));
            btnAction.setVisibility(View.VISIBLE);
        } else {
            btnAction.setVisibility(View.GONE);
        }
    }

    private void handleActionButtonClick() {
        if ("CONFIRMED".equals(orderStatus)) {
            updateOrderStatusOnServer("DELIVERING");
        } else if ("DELIVERING".equals(orderStatus)) {
            updateOrderStatusOnServer("COMPLETED");
        }
    }

    private void updateOrderStatusOnServer(String newStatus) {
        btnAction.setEnabled(false);
        UpdateStatusRequest request = new UpdateStatusRequest(newStatus, null);
        ApiClient.getApiService().updateDeliveryOrderStatus(orderId, request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                btnAction.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    orderStatus = response.body().getStatus();
                    updateButtonState();
                    Toast.makeText(ShipperDeliveryMapActivity.this, "Đã cập nhật trạng thái đơn hàng!", Toast.LENGTH_SHORT).show();
                    if ("COMPLETED".equals(orderStatus) || "RECEIVED".equals(orderStatus)) {
                        finish();
                    }
                } else {
                    Toast.makeText(ShipperDeliveryMapActivity.this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                btnAction.setEnabled(true);
                Toast.makeText(ShipperDeliveryMapActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase ESRI_STREETS = 
        new org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
            "EsriStreets",
            0, 20, 256, "",
            new String[] {
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"
            },
            "Tiles © Esri"
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl() + 
                       org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex) + "/" + 
                       org.osmdroid.util.MapTileIndex.getY(pMapTileIndex) + "/" + 
                       org.osmdroid.util.MapTileIndex.getX(pMapTileIndex);
            }
        };

    private void setupMap() {
        mapView.setTileSource(ESRI_STREETS);
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
            buildingMarker.setTitle("Điểm giao: " + buildingName);
            buildingMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location));
            buildingMarker.getIcon().setTint(Color.parseColor("#10B981")); // Emerald Green
            mapView.getOverlays().add(buildingMarker);
        }

        // Draw initial route
        updateRoute();

        // Center on Shop
        if (shopLat != 0.0 && shopLng != 0.0) {
            mapController.setCenter(new GeoPoint(shopLat, shopLng));
        } else if (buildingLat != 0.0 && buildingLng != 0.0) {
            mapController.setCenter(new GeoPoint(buildingLat, buildingLng));
        } else {
            // Default center: HCMC University Village
            mapController.setCenter(new GeoPoint(10.8756, 106.8006));
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Không có quyền GPS, không thể đồng bộ lộ trình", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        updateShipperMarkerOnMap();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(@NonNull String provider) {}
                    @Override
                    public void onProviderDisabled(@NonNull String provider) {}
                };

                // Request location updates every 3 seconds or 3 meters change
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3f, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 3f, locationListener);

                    // Get last known location for initial view
                    Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnown == null) {
                        lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (lastKnown != null) {
                        currentLat = lastKnown.getLatitude();
                        currentLng = lastKnown.getLongitude();
                        updateShipperMarkerOnMap();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi thiết lập GPS: ", e);
        }

        // Start periodic sync with server every 5 seconds
        updateHandler.post(updateRunnable);
    }

    private void updateShipperMarkerOnMap() {
        if (currentLat == null || currentLng == null) return;
        GeoPoint pt = new GeoPoint(currentLat, currentLng);

        if (shipperMarker == null) {
            shipperMarker = new Marker(mapView);
            shipperMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            shipperMarker.setTitle("Vị trí của tôi");
            // Standard person icon or location icon
            shipperMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location));
            shipperMarker.getIcon().setTint(Color.parseColor("#3B82F6")); // Blue marker for Shipper
            mapView.getOverlays().add(shipperMarker);
        }

        shipperMarker.setPosition(pt);
        mapView.invalidate();

        // Update route line dynamically
        updateRoute();
    }

    private void sendShipperLocationToServer() {
        if (currentLat == null || currentLng == null || orderId == null) return;

        Log.d(TAG, "Đang đồng bộ tọa độ lên server: " + currentLat + ", " + currentLng);
        ApiClient.getApiService().updateShipperLocation(orderId, currentLat, currentLng).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Đã đồng bộ tọa độ thành công!");
                } else {
                    Log.e(TAG, "Đồng bộ tọa độ thất bại, response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi mạng khi đồng bộ tọa độ: ", t);
            }
        });
    }

    private void updateRoute() {
        List<GeoPoint> waypoints = new ArrayList<>();
        if (currentLat != null && currentLng != null) {
            GeoPoint shipperPt = new GeoPoint(currentLat, currentLng);
            
            // Check if we fetched recently (less than 10 seconds ago) AND shipper location didn't change much
            long now = System.currentTimeMillis();
            if (lastFetchedShipperLocation != null && now - lastRouteFetchTime < 10000) {
                double dist = lastFetchedShipperLocation.distanceToAsDouble(shipperPt);
                if (dist < 15.0) {
                    // Location changed by less than 15 meters and it's been less than 10s, skip OSRM request
                    return;
                }
            }
            
            lastRouteFetchTime = now;
            lastFetchedShipperLocation = shipperPt;
            
            waypoints.add(shipperPt);
        }
        
        if (shopLat != 0.0 && shopLng != 0.0) {
            waypoints.add(new GeoPoint(shopLat, shopLng));
        }
        if (buildingLat != 0.0 && buildingLng != 0.0) {
            waypoints.add(new GeoPoint(buildingLat, buildingLng));
        }
        
        if (waypoints.size() >= 2) {
            fetchRouteAndDraw(waypoints);
        }
    }

    private void fetchRouteAndDraw(List<GeoPoint> waypoints) {
        if (waypoints == null || waypoints.size() < 2) return;

        new Thread(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder("https://router.project-osrm.org/route/v1/driving/");
                for (int i = 0; i < waypoints.size(); i++) {
                    GeoPoint pt = waypoints.get(i);
                    urlBuilder.append(pt.getLongitude()).append(",").append(pt.getLatitude());
                    if (i < waypoints.size() - 1) {
                        urlBuilder.append(";");
                    }
                }
                urlBuilder.append("?overview=full&geometries=geojson");

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", getPackageName());

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonObj = new JSONObject(response.toString());
                    JSONArray routesArray = jsonObj.getJSONArray("routes");
                    if (routesArray.length() > 0) {
                        JSONObject routeObj = routesArray.getJSONObject(0);
                        JSONObject geometryObj = routeObj.getJSONObject("geometry");
                        JSONArray coordinatesArray = geometryObj.getJSONArray("coordinates");

                        List<GeoPoint> routePoints = new ArrayList<>();
                        for (int i = 0; i < coordinatesArray.length(); i++) {
                            JSONArray coord = coordinatesArray.getJSONArray(i);
                            double lon = coord.getDouble(0);
                            double lat = coord.getDouble(1);
                            routePoints.add(new GeoPoint(lat, lon));
                        }

                        runOnUiThread(() -> {
                            if (routeLine == null) {
                                routeLine = new Polyline();
                                routeLine.setColor(Color.parseColor("#4F46E5")); // Indigo line
                                routeLine.setWidth(8f);
                                mapView.getOverlays().add(routeLine);
                            }
                            routeLine.setPoints(routePoints);
                            mapView.invalidate();
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Routing fetch error: ", e);
            }
        }).start();
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
        updateHandler.removeCallbacks(updateRunnable);
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
