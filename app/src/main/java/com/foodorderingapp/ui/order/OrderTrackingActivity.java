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
    private Polyline routeLineShop;
    private Polyline routeLineBuilding;

    private com.google.android.material.card.MaterialCardView cardStepPickup;
    private com.google.android.material.card.MaterialCardView cardStepDeliver;
    private android.view.View viewStepDivider;
    private android.widget.TextView tvStepPickupNum;
    private android.widget.TextView tvStepPickupLabel;
    private android.widget.TextView tvStepDeliverNum;
    private android.widget.TextView tvStepDeliverLabel;

    private List<GeoPoint> staticShopToBuildingRoutePoints = new ArrayList<>();
    private long lastRouteFetchTime = 0;
    private GeoPoint lastFetchedShipperLocation = null;

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
        bindStepViews();
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
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
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

        // Draw initial route lines
        if (shopLat != 0.0 && buildingLat != 0.0) {
            routeLineShop = new Polyline();
            List<GeoPoint> ptsShop = new ArrayList<>();
            ptsShop.add(new GeoPoint(shopLat, shopLng));
            routeLineShop.setPoints(ptsShop);
            routeLineShop.setColor(Color.parseColor("#40F46E26")); // Muted orange
            routeLineShop.setWidth(8f);
            mapView.getOverlays().add(routeLineShop);

            routeLineBuilding = new Polyline();
            List<GeoPoint> ptsBuilding = new ArrayList<>();
            ptsBuilding.add(new GeoPoint(shopLat, shopLng));
            ptsBuilding.add(new GeoPoint(buildingLat, buildingLng));
            routeLineBuilding.setPoints(ptsBuilding);
            routeLineBuilding.setColor(Color.parseColor("#4010B981")); // Muted green
            routeLineBuilding.setWidth(8f);
            mapView.getOverlays().add(routeLineBuilding);
            fetchStaticRoute();
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
        if (routeLineShop != null && routeLineBuilding != null) {
            // Check if we fetched recently (less than 10 seconds ago) AND shipper location didn't change much
            long now = System.currentTimeMillis();
            if (lastFetchedShipperLocation != null && now - lastRouteFetchTime < 10000) {
                double dist = lastFetchedShipperLocation.distanceToAsDouble(shipperPt);
                if (dist < 15.0) {
                    // Update the active line start point to current shipper Pt so it tracks smoothly
                    if ("CONFIRMED".equalsIgnoreCase(orderStatus)) {
                        List<GeoPoint> shopPts = new ArrayList<>();
                        shopPts.add(shipperPt);
                        if (shopLat != 0.0 && shopLng != 0.0) {
                            shopPts.add(new GeoPoint(shopLat, shopLng));
                        }
                        routeLineShop.setPoints(shopPts);
                    } else {
                        List<GeoPoint> bldPts = new ArrayList<>();
                        bldPts.add(shipperPt);
                        if (buildingLat != 0.0 && buildingLng != 0.0) {
                            bldPts.add(new GeoPoint(buildingLat, buildingLng));
                        }
                        routeLineBuilding.setPoints(bldPts);
                    }
                    mapView.invalidate();
                    return;
                }
            }
            
            lastRouteFetchTime = now;
            lastFetchedShipperLocation = shipperPt;
            
            // Query OSRM for active leg
            List<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(shipperPt);
            if ("CONFIRMED".equalsIgnoreCase(orderStatus)) {
                if (shopLat != 0.0 && shopLng != 0.0) {
                    waypoints.add(new GeoPoint(shopLat, shopLng));
                }
            } else {
                if (buildingLat != 0.0 && buildingLng != 0.0) {
                    waypoints.add(new GeoPoint(buildingLat, buildingLng));
                }
            }
            
            if (waypoints.size() >= 2) {
                fetchRouteAndDraw(waypoints);
            }
        }
    }

    private void fetchRouteAndDraw(List<GeoPoint> waypoints) {
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

                java.net.URL url = new java.net.URL(urlBuilder.toString());
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", getPackageName());
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    org.json.JSONObject jsonObj = new org.json.JSONObject(response.toString());
                    org.json.JSONArray routesArray = jsonObj.getJSONArray("routes");
                    if (routesArray.length() > 0) {
                        org.json.JSONObject routeObj = routesArray.getJSONObject(0);
                        org.json.JSONObject geometryObj = routeObj.getJSONObject("geometry");
                        org.json.JSONArray coordinatesArray = geometryObj.getJSONArray("coordinates");

                        List<GeoPoint> routePoints = new ArrayList<>();
                        for (int i = 0; i < coordinatesArray.length(); i++) {
                            org.json.JSONArray coord = coordinatesArray.getJSONArray(i);
                            double lon = coord.getDouble(0);
                            double lat = coord.getDouble(1);
                            routePoints.add(new GeoPoint(lat, lon));
                        }

                        runOnUiThread(() -> {
                             if ("CONFIRMED".equalsIgnoreCase(orderStatus)) {
                                 routeLineShop.setPoints(routePoints);
                                 routeLineShop.setColor(Color.parseColor("#F46E26")); // Active
                                 
                                 if (staticShopToBuildingRoutePoints != null && !staticShopToBuildingRoutePoints.isEmpty()) {
                                     routeLineBuilding.setPoints(staticShopToBuildingRoutePoints);
                                 } else {
                                     List<GeoPoint> shopToBuilding = new ArrayList<>();
                                     if (shopLat != 0.0 && buildingLat != 0.0) {
                                         shopToBuilding.add(new GeoPoint(shopLat, shopLng));
                                         shopToBuilding.add(new GeoPoint(buildingLat, buildingLng));
                                     }
                                     routeLineBuilding.setPoints(shopToBuilding);
                                 }
                                 routeLineBuilding.setColor(Color.parseColor("#4010B981")); // Faded green
                             } else {
                                 routeLineBuilding.setPoints(routePoints);
                                 routeLineBuilding.setColor(Color.parseColor("#10B981")); // Active
                                 
                                 List<GeoPoint> shopToShipper = new ArrayList<>();
                                 if (shopLat != 0.0 && lastFetchedShipperLocation != null) {
                                     shopToShipper.add(new GeoPoint(shopLat, shopLng));
                                     shopToShipper.add(lastFetchedShipperLocation);
                                 }
                                 routeLineShop.setPoints(shopToShipper);
                                 routeLineShop.setColor(Color.parseColor("#40718096")); // Faded grey
                             }
                             mapView.invalidate();
                         });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Routing fetch error: ", e);
            }
        }).start();
    }

    private void fetchStaticRoute() {
        if (shopLat == 0.0 || buildingLat == 0.0) return;
        new Thread(() -> {
            try {
                String urlStr = "https://router.project-osrm.org/route/v1/driving/" +
                        shopLng + "," + shopLat + ";" + buildingLng + "," + buildingLat +
                        "?overview=full&geometries=geojson";
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", getPackageName());
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    org.json.JSONObject jsonObj = new org.json.JSONObject(response.toString());
                    org.json.JSONArray routesArray = jsonObj.getJSONArray("routes");
                    if (routesArray.length() > 0) {
                        org.json.JSONObject routeObj = routesArray.getJSONObject(0);
                        org.json.JSONObject geometryObj = routeObj.getJSONObject("geometry");
                        org.json.JSONArray coordinatesArray = geometryObj.getJSONArray("coordinates");
                        List<GeoPoint> pts = new ArrayList<>();
                        for (int i = 0; i < coordinatesArray.length(); i++) {
                            org.json.JSONArray coord = coordinatesArray.getJSONArray(i);
                            pts.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
                        }
                        staticShopToBuildingRoutePoints = pts;
                        runOnUiThread(() -> {
                            if ("CONFIRMED".equalsIgnoreCase(orderStatus) && routeLineBuilding != null) {
                                routeLineBuilding.setPoints(staticShopToBuildingRoutePoints);
                                routeLineBuilding.setColor(Color.parseColor("#4010B981"));
                                mapView.invalidate();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch static route: ", e);
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
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }

    private void bindStepViews() {
        cardStepPickup = findViewById(R.id.card_step_pickup);
        cardStepDeliver = findViewById(R.id.card_step_deliver);
        viewStepDivider = findViewById(R.id.view_step_divider);
        tvStepPickupNum = findViewById(R.id.tv_step_pickup_num);
        tvStepPickupLabel = findViewById(R.id.tv_step_pickup_label);
        tvStepDeliverNum = findViewById(R.id.tv_step_deliver_num);
        tvStepDeliverLabel = findViewById(R.id.tv_step_deliver_label);
        updateStepIndicator();
    }

    private void updateStepIndicator() {
        if (cardStepPickup == null) return;
        if ("CONFIRMED".equalsIgnoreCase(orderStatus)) {
            cardStepPickup.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F46E26")));
            tvStepPickupNum.setText("1");
            tvStepPickupNum.setTextColor(Color.WHITE);
            tvStepPickupLabel.setTextColor(Color.parseColor("#F46E26"));

            viewStepDivider.setBackgroundColor(Color.parseColor("#2D2D30"));

            cardStepDeliver.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#2D2D30")));
            tvStepDeliverNum.setText("2");
            tvStepDeliverNum.setTextColor(Color.parseColor("#718096"));
            tvStepDeliverLabel.setTextColor(Color.parseColor("#718096"));
        } else {
            cardStepPickup.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
            tvStepPickupNum.setText("✔");
            tvStepPickupNum.setTextColor(Color.WHITE);
            tvStepPickupLabel.setTextColor(Color.parseColor("#10B981"));

            viewStepDivider.setBackgroundColor(Color.parseColor("#10B981"));

            cardStepDeliver.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
            tvStepDeliverNum.setText("2");
            tvStepDeliverNum.setTextColor(Color.WHITE);
            tvStepDeliverLabel.setTextColor(Color.parseColor("#10B981"));
        }
    }
}
