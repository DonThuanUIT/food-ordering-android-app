package com.foodorderingapp.ui.home.vendor;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.CategoryRequest;
import com.foodorderingapp.model.request.FoodRequest;
import com.foodorderingapp.model.response.CategoryResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.ui.adapter.FoodAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.utils.CategoryIconHelper;
import com.foodorderingapp.viewmodel.UploadImageViewModel;
import com.foodorderingapp.viewmodel.ViewModelFactory;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorMenuFragment extends Fragment implements FoodAdapter.OnFoodActionProvider {

    private RecyclerView rvMenu;
    private FoodAdapter adapter;
    private List<FoodResponse> foodList = new ArrayList<>();
    private List<CategoryResponse> categories = new ArrayList<>();
    
    private SearchView searchView;
    private ChipGroup chipGroupCategories;
    private FloatingActionButton fabScrollTop;
    private FloatingActionButton fabAddFood;
    
    private ImageView imgPreview;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    private UUID currentShopId;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 15;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private UUID selectedCategoryId = null;
    private BottomSheetDialog currentAddFoodDialog;

    private TextView tvStatTotalCount;
    private TextView tvStatInStockCount;
    private TextView tvStatSoldOutCount;
    private TextView tvStatCategoriesCount;

    private MaterialCardView cardStatTotal;
    private MaterialCardView cardStatInStock;
    private MaterialCardView cardStatSoldOut;
    private MaterialCardView cardStatCategories;

    // MVVM Integration
    private UploadImageViewModel uploadViewModel;
    private String currentUploadedUrl = null; // Biến cục bộ lưu URL Cloudinary

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        uploadViewModel = new ViewModelProvider(this, factory).get(UploadImageViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            uploadViewModel.uploadImage(selectedImageUri);
                            
                            if (imgPreview != null) {
                                imgPreview.setImageURI(selectedImageUri);
                                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_menu, container, false);
        
        rvMenu = view.findViewById(R.id.rv_vendor_menu);
        searchView = view.findViewById(R.id.search_view_menu);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        fabScrollTop = view.findViewById(R.id.fab_scroll_top);
        fabAddFood = view.findViewById(R.id.fab_add_food);

        tvStatTotalCount = view.findViewById(R.id.tv_stat_total_count);
        tvStatInStockCount = view.findViewById(R.id.tv_stat_in_stock_count);
        tvStatSoldOutCount = view.findViewById(R.id.tv_stat_sold_out_count);
        tvStatCategoriesCount = view.findViewById(R.id.tv_stat_categories_count);

        cardStatTotal = view.findViewById(R.id.card_stat_total);
        cardStatInStock = view.findViewById(R.id.card_stat_in_stock);
        cardStatSoldOut = view.findViewById(R.id.card_stat_sold_out);
        cardStatCategories = view.findViewById(R.id.card_stat_categories);

        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupScrollToTop();
        
        initUploadObservers();
        
        if (fabAddFood != null) {
            fabAddFood.setOnClickListener(v -> {
                if (currentShopId != null) {
                    showAddFoodDialog();
                } else {
                    Toast.makeText(getContext(), "Đang tải thông tin cửa hàng...", Toast.LENGTH_SHORT).show();
                    fetchShopInfoAndLoadData();
                }
            });
        }
        
        fetchShopInfoAndLoadData();
        return view;
    }

    private void initUploadObservers() {
        uploadViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (currentAddFoodDialog != null && currentAddFoodDialog.isShowing()) {
                ProgressBar pb = currentAddFoodDialog.findViewById(R.id.progress_upload);
                Button btnAdd = currentAddFoodDialog.findViewById(R.id.btn_add_to_menu);
                if (pb != null) pb.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (btnAdd != null) btnAdd.setEnabled(!isLoading);
            }
        });

        uploadViewModel.getUploadSuccessUrl().observe(getViewLifecycleOwner(), url -> {
            this.currentUploadedUrl = url;
            if (imgPreview != null) {
                Glide.with(this).load(url).into(imgPreview);
            }
        });

        uploadViewModel.getUploadError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) ToastUtils.error(requireContext(), error);
        });
    }

    private void fetchShopInfoAndLoadData() {
        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String idStr = response.body().get(0).getId();
                    currentShopId = idStr != null ? UUID.fromString(idStr) : null;
                    loadData(true);
                }
            }
            @Override public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                if (getContext() != null) ToastUtils.error(getContext(), "Không thể kết nối Backend!");
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FoodAdapter(foodList, this);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMenu.setAdapter(adapter);
        rvMenu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            loadData(false);
                        }
                    }
                }
            }
        });
    }

     private void setupSearch() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    adapter.filter(query);
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);
                    return true;
                }
            });
        }
    }

    private void setupFilters() {
        if (chipGroupCategories != null) {
            chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    selectedCategoryId = null;
                    adapter.setCategoryFilter("Tất cả");
                    loadData(true);
                    return;
                }
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    Object tag = chip.getTag();
                    if (tag instanceof UUID) {
                        selectedCategoryId = (UUID) tag;
                        String categoryName = "";
                        for (CategoryResponse c : categories) {
                            if (c.getId().equals(selectedCategoryId)) {
                                categoryName = c.getName();
                                break;
                            }
                        }
                        adapter.setCategoryFilter(categoryName);
                    } else {
                        selectedCategoryId = null;
                        adapter.setCategoryFilter("Tất cả");
                    }
                    loadData(true);
                }
            });
        }

        // Stats cards click filters
        if (cardStatTotal != null) {
            cardStatTotal.setOnClickListener(v -> {
                updateStatsHighlight("Tất cả");
                if (adapter != null) {
                    adapter.setStatusFilter("Tất cả");
                }
            });
        }
        if (cardStatInStock != null) {
            cardStatInStock.setOnClickListener(v -> {
                updateStatsHighlight("Sẵn có");
                if (adapter != null) {
                    adapter.setStatusFilter("Sẵn có");
                }
            });
        }
        if (cardStatSoldOut != null) {
            cardStatSoldOut.setOnClickListener(v -> {
                updateStatsHighlight("Hết món");
                if (adapter != null) {
                    adapter.setStatusFilter("Hết món");
                }
            });
        }
    }

    private void updateStatsHighlight(String selectedStat) {
        if (cardStatTotal == null || cardStatInStock == null || cardStatSoldOut == null) return;
        
        // Reset strokes
        cardStatTotal.setStrokeWidth(0);
        cardStatInStock.setStrokeWidth(0);
        cardStatSoldOut.setStrokeWidth(0);
        
        // Highlight active one with orange border
        int strokeWidthPx = (int) (1.5f * getResources().getDisplayMetrics().density);
        ColorStateList orangeColor = ColorStateList.valueOf(Color.parseColor("#F46E26"));
        if ("Tất cả".equals(selectedStat)) {
            cardStatTotal.setStrokeColor(orangeColor);
            cardStatTotal.setStrokeWidth(strokeWidthPx);
        } else if ("Sẵn có".equals(selectedStat)) {
            cardStatInStock.setStrokeColor(orangeColor);
            cardStatInStock.setStrokeWidth(strokeWidthPx);
        } else if ("Hết món".equals(selectedStat)) {
            cardStatSoldOut.setStrokeColor(orangeColor);
            cardStatSoldOut.setStrokeWidth(strokeWidthPx);
        }
    }

    private void setupScrollToTop() {
        rvMenu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (fabScrollTop == null) return;
                if (dy > 30) fabScrollTop.show();
                else if (dy < -30) fabScrollTop.hide();
                if (!recyclerView.canScrollVertically(-1)) fabScrollTop.hide();
            }
        });
        if (fabScrollTop != null) fabScrollTop.setOnClickListener(v -> rvMenu.smoothScrollToPosition(0));
    }

    private void loadData(boolean reload) {
        if (currentShopId == null) return;
        if (reload) {
            currentPage = 0;
            isLastPage = false;
            foodList.clear();
            adapter.updateData(new ArrayList<>());
        }
        if (isLastPage || isLoading) return;

        isLoading = true;
        ApiClient.getApiService().getAllFoods(currentShopId, selectedCategoryId, currentPage, PAGE_SIZE)
                .enqueue(new Callback<com.foodorderingapp.model.response.PageResponse<FoodResponse>>() {
            @Override
            public void onResponse(Call<com.foodorderingapp.model.response.PageResponse<FoodResponse>> call, Response<com.foodorderingapp.model.response.PageResponse<FoodResponse>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodResponse> newFoods = response.body().getContent();
                    foodList.addAll(newFoods);
                    adapter.updateData(foodList);
                    updateStatsCounts();

                    isLastPage = response.body().isLast() || newFoods.size() < PAGE_SIZE;
                    if (!isLastPage) {
                        currentPage++;
                    }
                    if (reload && foodList.size() > 0 && rvMenu != null) {
                        rvMenu.scrollToPosition(0);
                    }
                }
            }
            @Override 
            public void onFailure(Call<com.foodorderingapp.model.response.PageResponse<FoodResponse>> call, Throwable t) {
                isLoading = false;
            }
        });
        if (reload) {
            loadCategories();
        }
    }

    private void loadCategories() {
        loadCategories(null);
    }

    private void loadCategories(UUID selectedId) {
        if (currentShopId == null) return;
        ApiClient.getApiService().getAllCategories(currentShopId).enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategoryChips();
                    updateStatsCounts();
                    if (currentAddFoodDialog != null && currentAddFoodDialog.isShowing()) {
                        updateDialogCategories(currentAddFoodDialog.findViewById(R.id.chip_group_category), selectedId);
                    }
                }
            }
            @Override public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {}
        });
    }

    private void updateCategoryChips() {
        if (chipGroupCategories == null) return;
        chipGroupCategories.removeAllViews();
        Chip allChip = new Chip(requireContext());
        allChip.setText(CategoryIconHelper.getEmojiForDisplay("Tất cả"));
        allChip.setTag("ALL");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setChipBackgroundColor(getChipBackgroundStateList());
        allChip.setTextColor(getChipTextStateList());
        allChip.setChipStrokeColor(getChipStrokeColorStateList());
        allChip.setChipStrokeWidth(1 * getResources().getDisplayMetrics().density);
        chipGroupCategories.addView(allChip);
        for (CategoryResponse category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(CategoryIconHelper.getEmojiForDisplay(category.getName()));
            chip.setTag(category.getId());
            chip.setCheckable(true);
            chip.setChipBackgroundColor(getChipBackgroundStateList());
            chip.setTextColor(getChipTextStateList());
            chip.setChipStrokeColor(getChipStrokeColorStateList());
            chip.setChipStrokeWidth(1 * getResources().getDisplayMetrics().density);
            chip.setOnLongClickListener(v -> {
                showDeleteCategoryConfirmDialog(category);
                return true;
            });
            chipGroupCategories.addView(chip);
        }
    }

    @Override
    public void onStatusChanged(FoodResponse food, boolean isAvailable) {
        if (currentShopId == null || food == null || food.getId() == null) return;
        
        // Find and update local list first
        for (FoodResponse f : foodList) {
            if (f.getId() != null && f.getId().equals(food.getId())) {
                f.setIsAvailable(isAvailable);
                break;
            }
        }
        
        // Instant local update to adapter
        if (adapter != null) {
            adapter.updateFoodAvailability(food.getId(), isAvailable);
            updateStatsCounts();
        }
        
        ApiClient.getApiService().toggleFoodAvailability(currentShopId, food.getId()).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse updated = response.body();
                    for (FoodResponse f : foodList) {
                        if (f.getId() != null && f.getId().equals(food.getId())) {
                            f.setIsAvailable(updated.getIsAvailable());
                            break;
                        }
                    }
                    if (adapter != null) {
                        adapter.updateFoodAvailability(food.getId(), updated.getIsAvailable());
                        updateStatsCounts();
                    }
                } else {
                    // Rollback on failure
                    for (FoodResponse f : foodList) {
                        if (f.getId() != null && f.getId().equals(food.getId())) {
                            f.setIsAvailable(!isAvailable);
                            break;
                        }
                    }
                    if (adapter != null) {
                        adapter.updateFoodAvailability(food.getId(), !isAvailable);
                        updateStatsCounts();
                    }
                    Toast.makeText(getContext(), "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            }
            @Override 
            public void onFailure(Call<FoodResponse> call, Throwable t) {
                // Rollback on network failure
                for (FoodResponse f : foodList) {
                    if (f.getId() != null && f.getId().equals(food.getId())) {
                        f.setIsAvailable(!isAvailable);
                        break;
                    }
                }
                if (adapter != null) {
                    adapter.updateFoodAvailability(food.getId(), !isAvailable);
                    updateStatsCounts();
                }
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddNewItemClick() {
        if (currentShopId != null) showAddFoodDialog();
    }

    private void showAddFoodDialog() {
        currentUploadedUrl = null;
        currentAddFoodDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_food, null);
        
        imgPreview = dialogView.findViewById(R.id.img_upload_preview);
        EditText etName = dialogView.findViewById(R.id.et_food_name);
        EditText etPrice = dialogView.findViewById(R.id.et_food_price);
        EditText etDesc = dialogView.findViewById(R.id.et_food_description);
        ChipGroup dialogChips = dialogView.findViewById(R.id.chip_group_category);
        Button btnAdd = dialogView.findViewById(R.id.btn_add_to_menu);
        ImageView btnBack = dialogView.findViewById(R.id.btn_back_dialog);
        View btnAddNewCategory = dialogView.findViewById(R.id.btn_add_category);

        updateDialogCategories(dialogChips, null);

        if (btnAddNewCategory != null) btnAddNewCategory.setOnClickListener(v -> showAddNewCategoryDialog());
        if (btnBack != null) btnBack.setOnClickListener(v -> currentAddFoodDialog.dismiss());

        View uploadArea = dialogView.findViewById(R.id.upload_area);
        if (uploadArea != null) {
            uploadArea.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                    Toast.makeText(getContext(), "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show();
                    return;
                }

                UUID catId = getSelectedCategoryId(dialogChips);
                saveFoodToDb(currentAddFoodDialog, name, priceStr, etDesc.getText().toString(), catId, currentUploadedUrl);
            });
        }

        currentAddFoodDialog.setContentView(dialogView);
        currentAddFoodDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        currentAddFoodDialog.show();
    }

    private void saveFoodToDb(BottomSheetDialog dialog, String name, String price, String desc, UUID catId, String url) {
        if (currentShopId == null) return;
        try {
            FoodRequest request = new FoodRequest(catId, name, desc, new BigDecimal(price), url);
            ApiClient.getApiService().createFood(currentShopId, request).enqueue(new Callback<FoodResponse>() {
                @Override
                public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                    if (response.isSuccessful()) {
                        ToastUtils.success(getContext(), "Đã thêm món ăn!");
                        loadData(true);
                        dialog.dismiss();
                    }
                }
                @Override public void onFailure(Call<FoodResponse> call, Throwable t) {}
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDialogCategories(ChipGroup dialogChips, UUID selectedId) {
        if (dialogChips == null) return;
        dialogChips.removeAllViews();
        boolean hasSelected = false;
        for (int i = 0; i < categories.size(); i++) {
            CategoryResponse cat = categories.get(i);
            Chip chip = new Chip(requireContext());
            String displayName = CategoryIconHelper.getEmojiForDisplay(cat.getName()) + " " + CategoryIconHelper.getNameForDisplay(cat.getName());
            chip.setText(displayName);
            chip.setTag(cat.getId());
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setChipBackgroundColor(getChipBackgroundStateList());
            chip.setTextColor(getChipTextStateList());
            chip.setChipStrokeColor(getChipStrokeColorStateList());
            chip.setChipStrokeWidth(1 * getResources().getDisplayMetrics().density);
            if (selectedId != null && selectedId.equals(cat.getId())) {
                chip.setChecked(true);
                hasSelected = true;
            } else if (selectedId == null && i == 0) {
                chip.setChecked(true);
                hasSelected = true;
            }
            dialogChips.addView(chip);
        }
        if (!hasSelected && dialogChips.getChildCount() > 0) ((Chip) dialogChips.getChildAt(0)).setChecked(true);
    }

    private void showAddNewCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm danh mục mới");
        final EditText input = new EditText(requireContext());
        input.setHint("Tên danh mục");
        builder.setView(input);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(requireContext());
                progressDialog.setMessage("AI đang sinh biểu tượng...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                com.foodorderingapp.utils.GeminiEmojiHelper.generateEmojiForCategory(name, new com.foodorderingapp.utils.GeminiEmojiHelper.EmojiCallback() {
                    @Override
                    public void onSuccess(String emoji) {
                        progressDialog.dismiss();
                        String formattedName = emoji + "|" + name;
                        addNewCategory(formattedName);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        String emoji = CategoryIconHelper.getCategoryEmoji(name);
                        String formattedName = emoji + "|" + name;
                        addNewCategory(formattedName);
                    }
                });
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addNewCategory(String name) {
        if (currentShopId == null) return;
        ApiClient.getApiService().createCategory(currentShopId, new CategoryRequest(name)).enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Category added!", Toast.LENGTH_SHORT).show();
                    loadCategories(response.body().getId());
                }
            }
            @Override public void onFailure(Call<CategoryResponse> call, Throwable t) {}
        });
    }

    private UUID getSelectedCategoryId(ChipGroup group) {
        if (group == null) return categories.isEmpty() ? UUID.randomUUID() : categories.get(0).getId();
        int id = group.getCheckedChipId();
        if (id != View.NO_ID) {
            Chip chip = group.findViewById(id);
            if (chip != null && chip.getTag() instanceof UUID) {
                return (UUID) chip.getTag();
            }
        }
        return categories.isEmpty() ? UUID.randomUUID() : categories.get(0).getId();
    }

    @Override
    public void onFoodImageClick(FoodResponse food) {
        if (food.getImageUrl() == null || food.getImageUrl().isEmpty()) return;
        showFullImageDialog(food.getImageUrl(), food.getName());
    }

    private void showFullImageDialog(String imageUrl, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_full_image, null);
        ImageView imgFull = view.findViewById(R.id.img_full_size);
        TextView tvTitle = view.findViewById(R.id.tv_image_title);
        View btnClose = view.findViewById(R.id.btn_close_image);
        if (tvTitle != null) tvTitle.setText(name);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.logo_food).error(R.drawable.logo_food).into(imgFull);
        AlertDialog dialog = builder.setView(view).create();
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        view.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private ColorStateList getChipBackgroundStateList() {
        return androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.chip_bg_color);
    }

    private ColorStateList getChipTextStateList() {
        return androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.chip_text_color);
    }

    private ColorStateList getChipStrokeColorStateList() {
        return androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.chip_stroke_color);
    }

    private void updateStatsCounts() {
        if (tvStatTotalCount == null || tvStatInStockCount == null || tvStatSoldOutCount == null || tvStatCategoriesCount == null) return;
        int totalCount = foodList.size();
        int inStockCount = 0;
        int soldOutCount = 0;
        for (FoodResponse food : foodList) {
            if (food.getIsAvailable() != null ? food.getIsAvailable() : true) inStockCount++;
            else soldOutCount++;
        }
        tvStatTotalCount.setText(String.valueOf(totalCount));
        tvStatInStockCount.setText(String.valueOf(inStockCount));
        tvStatSoldOutCount.setText(String.valueOf(soldOutCount));
        tvStatCategoriesCount.setText(String.valueOf(categories.size()));
    }

    @Override
    public void onFoodLongClick(FoodResponse food) {
        showFoodOptionsDialog(food);
    }

    private void showFoodOptionsDialog(FoodResponse food) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_food_options, null);

        TextView tvTitle = view.findViewById(R.id.tv_food_title);
        View btnEdit = view.findViewById(R.id.layout_option_edit);
        View btnDelete = view.findViewById(R.id.layout_option_delete);

        if (tvTitle != null) tvTitle.setText(food.getName());

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                dialog.dismiss();
                showEditFoodDialog(food);
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteConfirmDialog(food);
            });
        }

        dialog.setContentView(view);
        dialog.show();
    }

    private void showEditFoodDialog(FoodResponse food) {
        currentUploadedUrl = food.getImageUrl();
        currentAddFoodDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_food, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        if (tvDialogTitle != null) {
            tvDialogTitle.setText("Chỉnh sửa món ăn");
        }

        imgPreview = dialogView.findViewById(R.id.img_upload_preview);
        EditText etName = dialogView.findViewById(R.id.et_food_name);
        EditText etPrice = dialogView.findViewById(R.id.et_food_price);
        EditText etDesc = dialogView.findViewById(R.id.et_food_description);
        ChipGroup dialogChips = dialogView.findViewById(R.id.chip_group_category);
        Button btnSave = dialogView.findViewById(R.id.btn_add_to_menu);
        ImageView btnBack = dialogView.findViewById(R.id.btn_back_dialog);
        View btnAddNewCategory = dialogView.findViewById(R.id.btn_add_category);

        // Prepopulate fields
        if (etName != null) etName.setText(food.getName());
        if (etPrice != null && food.getPrice() != null) etPrice.setText(food.getPrice().toPlainString());
        if (etDesc != null) etDesc.setText(food.getDescription() != null ? food.getDescription() : "");
        
        if (imgPreview != null && food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(food.getImageUrl())
                    .placeholder(R.drawable.logo_food)
                    .error(R.drawable.logo_food)
                    .into(imgPreview);
            imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        updateDialogCategories(dialogChips, food.getCategoryId());

        if (btnAddNewCategory != null) btnAddNewCategory.setOnClickListener(v -> showAddNewCategoryDialog());
        if (btnBack != null) btnBack.setOnClickListener(v -> currentAddFoodDialog.dismiss());

        View uploadArea = dialogView.findViewById(R.id.upload_area);
        if (uploadArea != null) {
            uploadArea.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        if (btnSave != null) {
            btnSave.setText("Lưu thay đổi");
            btnSave.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                String priceStr = etPrice != null ? etPrice.getText().toString().trim() : "";
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                    Toast.makeText(getContext(), "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show();
                    return;
                }

                UUID catId = getSelectedCategoryId(dialogChips);
                saveEditedFoodToDb(currentAddFoodDialog, food.getId(), name, priceStr, etDesc != null ? etDesc.getText().toString() : "", catId, currentUploadedUrl);
            });
        }

        currentAddFoodDialog.setContentView(dialogView);
        currentAddFoodDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        currentAddFoodDialog.show();
    }

    private void saveEditedFoodToDb(BottomSheetDialog dialog, UUID foodId, String name, String price, String desc, UUID catId, String url) {
        if (currentShopId == null) return;
        try {
            FoodRequest request = new FoodRequest(catId, name, desc, new BigDecimal(price), url);
            ApiClient.getApiService().updateFood(currentShopId, foodId, request).enqueue(new Callback<FoodResponse>() {
                @Override
                public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                    if (response.isSuccessful()) {
                        ToastUtils.success(getContext(), "Đã cập nhật món ăn!");
                        loadData(true);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<FoodResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmDialog(FoodResponse food) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa món ăn?")
                .setMessage("Bạn có chắc chắn muốn xóa món '" + food.getName() + "' khỏi thực đơn?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteFoodFromDb(food.getId()))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteFoodFromDb(UUID foodId) {
        if (currentShopId == null) return;
        ApiClient.getApiService().deleteFood(currentShopId, foodId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    ToastUtils.success(getContext(), "Đã xóa món ăn thành công!");
                    loadData(true);
                } else {
                    Toast.makeText(getContext(), "Không thể xóa món ăn!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteCategoryConfirmDialog(CategoryResponse category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa danh mục?")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "'? Lưu ý: Chỉ có thể xóa danh mục không chứa món ăn nào.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategoryFromDb(category.getId()))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteCategoryFromDb(UUID categoryId) {
        if (currentShopId == null) return;
        ApiClient.getApiService().deleteCategory(currentShopId, categoryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    ToastUtils.success(getContext(), "Đã xóa danh mục thành công!");
                    loadData(true);
                } else {
                    String errorMsg = "Không thể xóa danh mục. Vui lòng kiểm tra lại!";
                    try {
                        if (response.errorBody() != null) {
                            String errJson = response.errorBody().string();
                            if (errJson.contains("Cannot delete")) {
                                errorMsg = "Không thể xóa vì danh mục vẫn còn món ăn!";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
