package com.foodorderingapp.ui.shop;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ShopUpdateRequest;
import com.foodorderingapp.model.response.ShopResponse;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopMapActivity extends AppCompatActivity {

    private static final String TAG = "ShopMapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private IMapController mapController;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private View cardSuggestionsContainer;
    private RecyclerView rvSuggestions;
    private TextView tvSelectedAddress;
    private MaterialButton btnConfirm;

    private String shopId;
    private double selectedLat = 10.8698; // Default HCMC UT
    private double selectedLng = 106.8034;
    private String selectedAddress = "";

    private SuggestionAdapter suggestionAdapter;
    private final List<AddressSuggestion> suggestionList = new ArrayList<>();
    
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final Handler reverseGeocodeHandler = new Handler(Looper.getMainLooper());
    private Runnable reverseGeocodeRunnable;
    private boolean isProgrammaticMove = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // OSMdroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_shop_map);

        shopId = getIntent().getStringExtra("SHOP_ID");
        double initialLat = getIntent().getDoubleExtra("LATITUDE", 0.0);
        double initialLng = getIntent().getDoubleExtra("LONGITUDE", 0.0);
        String initialAddress = getIntent().getStringExtra("ADDRESS");

        if (initialLat != 0.0 && initialLng != 0.0) {
            selectedLat = initialLat;
            selectedLng = initialLng;
            selectedAddress = initialAddress != null ? initialAddress : "";
        }

        bindViews();
        setupMap();
        setupSearch();
        checkLocationPermission();
    }

    private void bindViews() {
        mapView = findViewById(R.id.map_view);
        etSearch = findViewById(R.id.et_address_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        cardSuggestionsContainer = findViewById(R.id.card_suggestions_container);
        rvSuggestions = findViewById(R.id.rv_address_suggestions);
        tvSelectedAddress = findViewById(R.id.tv_selected_address);
        btnConfirm = findViewById(R.id.btn_confirm_location);

        findViewById(R.id.btn_back_map).setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
            cardSuggestionsContainer.setVisibility(View.GONE);
        });

        btnConfirm.setOnClickListener(v -> saveShopLocation());

        if (selectedAddress != null && !selectedAddress.isEmpty()) {
            tvSelectedAddress.setText(selectedAddress);
            etSearch.setText(selectedAddress);
            btnClearSearch.setVisibility(View.VISIBLE);
        }
    }

    private void setupMap() {
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false); // Hide default ugly +/- buttons

        mapController = mapView.getController();
        mapController.setZoom(17.0);
        
        GeoPoint startPoint = new GeoPoint(selectedLat, selectedLng);
        mapController.setCenter(startPoint);

        // Listen to map scrolling to perform reverse geocoding on the center pin
        mapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if (isProgrammaticMove) {
                    isProgrammaticMove = false;
                    return true;
                }
                triggerReverseGeocoding();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        }, 800)); // Debounce reverse geocoding by 800ms
    }

    private void setupSearch() {
        suggestionAdapter = new SuggestionAdapter(suggestionList, suggestion -> {
            isProgrammaticMove = true;
            selectedLat = suggestion.lat;
            selectedLng = suggestion.lon;
            selectedAddress = suggestion.displayName;

            tvSelectedAddress.setText(selectedAddress);
            etSearch.setText(selectedAddress);
            cardSuggestionsContainer.setVisibility(View.GONE);

            GeoPoint target = new GeoPoint(selectedLat, selectedLng);
            mapController.animateTo(target);
        });

        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }

                if (isProgrammaticMove) {
                    return;
                }

                // Debounce search API calls by 500ms
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> performSearch(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void triggerReverseGeocoding() {
        reverseGeocodeHandler.removeCallbacks(reverseGeocodeRunnable);
        reverseGeocodeRunnable = () -> {
            IGeoPoint center = mapView.getMapCenter();
            selectedLat = center.getLatitude();
            selectedLng = center.getLongitude();
            performReverseGeocoding(selectedLat, selectedLng);
        };
        reverseGeocodeHandler.postDelayed(reverseGeocodeRunnable, 800);
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            runOnUiThread(() -> cardSuggestionsContainer.setVisibility(View.GONE));
            return;
        }

        new Thread(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlStr = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery 
                        + "&format=json&limit=5&countrycodes=vn&accept-language=vi";
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", getPackageName()); // Nominatim requires a valid user agent

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<AddressSuggestion> results = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String name = obj.getString("display_name");
                        double lat = obj.getDouble("lat");
                        double lon = obj.getDouble("lon");
                        results.add(new AddressSuggestion(name, lat, lon));
                    }

                    runOnUiThread(() -> {
                        suggestionList.clear();
                        suggestionList.addAll(results);
                        suggestionAdapter.notifyDataSetChanged();
                        if (suggestionList.isEmpty()) {
                            cardSuggestionsContainer.setVisibility(View.GONE);
                        } else {
                            cardSuggestionsContainer.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Search geocoding error: ", e);
            }
        }).start();
    }

    private void performReverseGeocoding(double lat, double lon) {
        new Thread(() -> {
            try {
                String urlStr = "https://nominatim.openstreetmap.org/reverse?lat=" + lat 
                        + "&lon=" + lon + "&format=json&accept-language=vi";
                
                URL url = new URL(urlStr);
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
                    String displayName = jsonObj.getString("display_name");

                    runOnUiThread(() -> {
                        selectedAddress = displayName;
                        tvSelectedAddress.setText(selectedAddress);
                        
                        isProgrammaticMove = true;
                        etSearch.setText(selectedAddress);
                        btnClearSearch.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Reverse geocoding error: ", e);
            }
        }).start();
    }

    private void saveShopLocation() {
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(this, "Không có mã cửa hàng để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAddress == null || selectedAddress.isEmpty() || selectedAddress.equals("Chưa chọn vị trí")) {
            Toast.makeText(this, "Vui lòng chọn hoặc ghim một địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang lưu...");

        ShopUpdateRequest request = new ShopUpdateRequest();
        request.setAddress(selectedAddress);
        request.setLatitude(selectedLat);
        request.setLongitude(selectedLng);

        ApiClient.getApiService().updateShopProfile(java.util.UUID.fromString(shopId), request).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận vị trí");
                if (response.isSuccessful()) {
                    Toast.makeText(ShopMapActivity.this, "Cập nhật vị trí cửa hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ShopMapActivity.this, "Lỗi khi lưu vị trí lên server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận vị trí");
                Toast.makeText(ShopMapActivity.this, "Lỗi mạng khi lưu vị trí", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            zoomToCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                zoomToCurrentLocation();
            }
        }
    }

    private void zoomToCurrentLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                
                Location lastKnown = null;
                if (isNetworkEnabled && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (isGpsEnabled && lastKnown == null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

                if (lastKnown != null && getIntent().getDoubleExtra("LATITUDE", 0.0) == 0.0) {
                    selectedLat = lastKnown.getLatitude();
                    selectedLng = lastKnown.getLongitude();
                    GeoPoint currentPoint = new GeoPoint(selectedLat, selectedLng);
                    
                    isProgrammaticMove = true;
                    mapController.setCenter(currentPoint);
                    triggerReverseGeocoding();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get current location: ", e);
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

    // Static structures and RecyclerView Adapters
    private static class AddressSuggestion {
        String displayName;
        double lat;
        double lon;

        AddressSuggestion(String displayName, double lat, double lon) {
            this.displayName = displayName;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private interface OnSuggestionClickListener {
        void onClick(AddressSuggestion suggestion);
    }

    private static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
        private final List<AddressSuggestion> list;
        private final OnSuggestionClickListener listener;

        SuggestionAdapter(List<AddressSuggestion> list, OnSuggestionClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_suggestion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AddressSuggestion item = list.get(position);
            holder.tvText.setText(item.displayName);
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvText;
            ViewHolder(View itemView) {
                super(itemView);
                tvText = itemView.findViewById(R.id.tv_suggestion_text);
            }
        }
    }
}
