package com.foodorderingapp.ui.home.vendor;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
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
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private ImageView btnSearchToggle;
    
    private Uri selectedImageUri;
    private ImageView imgPreview;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    private UUID currentShopId;
    private BottomSheetDialog currentAddFoodDialog;

    private TextView tvStatTotalCount;
    private TextView tvStatInStockCount;
    private TextView tvStatSoldOutCount;
    private TextView tvStatCategoriesCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (imgPreview != null) {
                            imgPreview.setImageURI(selectedImageUri);
                            imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
        btnSearchToggle = view.findViewById(R.id.btn_search_toggle);

        tvStatTotalCount = view.findViewById(R.id.tv_stat_total_count);
        tvStatInStockCount = view.findViewById(R.id.tv_stat_in_stock_count);
        tvStatSoldOutCount = view.findViewById(R.id.tv_stat_sold_out_count);
        tvStatCategoriesCount = view.findViewById(R.id.tv_stat_categories_count);

        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupScrollToTop();
        
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

    private void fetchShopInfoAndLoadData() {
        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String idStr = response.body().get(0).getId();
                    currentShopId = idStr != null ? UUID.fromString(idStr) : null;
                    loadData(false);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Bạn chưa có cửa hàng. Vui lòng tạo cửa hàng trên hệ thống.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FoodAdapter(foodList, this);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMenu.setAdapter(adapter);
    }

    private void setupSearch() {
        if (btnSearchToggle != null) {
            btnSearchToggle.setOnClickListener(v -> {
                if (searchView.getVisibility() == View.VISIBLE) {
                    searchView.setVisibility(View.GONE);
                    searchView.setQuery("", false);
                    adapter.filter("");
                } else {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.requestFocus();
                }
            });
        }

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
                    adapter.setCategoryFilter("All");
                    return;
                }
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    adapter.setCategoryFilter(chip.getText().toString());
                }
            });
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
        if (fabScrollTop != null) {
            fabScrollTop.setOnClickListener(v -> rvMenu.smoothScrollToPosition(0));
        }
    }

    private void loadData(boolean scrollToBottom) {
        if (currentShopId == null) return;

        // Load Foods
        ApiClient.getApiService().getAllFoods(currentShopId, null).enqueue(new Callback<List<FoodResponse>>() {
            @Override
            public void onResponse(Call<List<FoodResponse>> call, Response<List<FoodResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodList = response.body();
                    adapter.updateData(foodList);
                    updateStatsCounts();
                    if (scrollToBottom) {
                        rvMenu.postDelayed(() -> rvMenu.smoothScrollToPosition(adapter.getItemCount() - 1), 300);
                    }
                }
            }
            @Override public void onFailure(Call<List<FoodResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách món ăn", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        loadCategories();
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
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setChipBackgroundColor(getChipBackgroundStateList());
        allChip.setTextColor(getChipTextStateList());
        chipGroupCategories.addView(allChip);

        for (CategoryResponse category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setChipBackgroundColor(getChipBackgroundStateList());
            chip.setTextColor(getChipTextStateList());
            chipGroupCategories.addView(chip);
        }
    }

    @Override
    public void onStatusChanged(FoodResponse food, boolean isAvailable) {
        if (currentShopId == null) return;

        ApiClient.getApiService().toggleFoodAvailability(currentShopId, food.getId()).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                if (response.isSuccessful()) {
                    loadData(false); 
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    }
                    loadData(false); 
                }
            }
            @Override public void onFailure(Call<FoodResponse> call, Throwable t) {
                loadData(false);
            }
        });
    }

    @Override
    public void onAddNewItemClick() {
        if (currentShopId != null) {
            showAddFoodDialog();
        }
    }

    private void showAddFoodDialog() {
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
        View btnEditCategory = dialogView.findViewById(R.id.btn_edit_category);

        updateDialogCategories(dialogChips);

        if (btnAddNewCategory != null) {
            btnAddNewCategory.setOnClickListener(v -> showAddNewCategoryDialog());
        }

        if (btnEditCategory != null) {
            btnEditCategory.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
            dialogChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
                btnEditCategory.setVisibility(checkedIds.isEmpty() ? View.GONE : View.VISIBLE);
            });
            btnEditCategory.setOnClickListener(v -> {
                UUID selectedId = getSelectedCategoryId(dialogChips);
                if (selectedId != null) {
                    String currentName = "";
                    for (CategoryResponse cat : categories) {
                        if (cat.getId().equals(selectedId)) {
                            currentName = cat.getName();
                            break;
                        }
                    }
                    if (!currentName.isEmpty()) {
                        showEditCategoryDialog(selectedId, currentName);
                    }
                } else {
                    Toast.makeText(getContext(), "Vui lòng chọn loại món ăn để sửa", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
                if (selectedImageUri != null) {
                    uploadAndSave(currentAddFoodDialog, name, priceStr, etDesc.getText().toString(), catId);
                } else {
                    saveFoodToDb(currentAddFoodDialog, name, priceStr, etDesc.getText().toString(), catId, null);
                }
            });
        }

        currentAddFoodDialog.setContentView(dialogView);
        currentAddFoodDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
        currentAddFoodDialog.show();
    }

    private void updateDialogCategories(ChipGroup dialogChips) {
        updateDialogCategories(dialogChips, null);
    }

    private void updateDialogCategories(ChipGroup dialogChips, UUID selectedId) {
        if (dialogChips == null) return;
        dialogChips.removeAllViews();
        boolean hasSelected = false;
        for (int i = 0; i < categories.size(); i++) {
            CategoryResponse cat = categories.get(i);
            Chip chip = new Chip(requireContext());
            chip.setText(cat.getName());
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setChipBackgroundColor(getChipBackgroundStateList());
            chip.setTextColor(getChipTextStateList());
            if (selectedId != null) {
                if (selectedId.equals(cat.getId())) {
                    chip.setChecked(true);
                    hasSelected = true;
                }
            } else {
                if (i == 0) {
                    chip.setChecked(true);
                    hasSelected = true;
                }
            }
            dialogChips.addView(chip);
        }
        if (!hasSelected && dialogChips.getChildCount() > 0) {
            ((Chip) dialogChips.getChildAt(0)).setChecked(true);
        }

        if (currentAddFoodDialog != null && currentAddFoodDialog.isShowing()) {
            View btnEdit = currentAddFoodDialog.findViewById(R.id.btn_edit_category);
            if (btnEdit != null) {
                btnEdit.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void showAddNewCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Category");

        final EditText input = new EditText(requireContext());
        input.setHint("Category name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                addNewCategory(name);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

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
                } else {
                    Toast.makeText(getContext(), "Failed to add category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditCategoryDialog(UUID categoryId, String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rename Category");

        final EditText input = new EditText(requireContext());
        input.setHint("Category name");
        input.setText(currentName);
        if (currentName != null) {
            input.setSelection(currentName.length());
        }
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!TextUtils.isEmpty(name) && !name.equalsIgnoreCase(currentName)) {
                editCategory(categoryId, name);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void editCategory(UUID categoryId, String name) {
        if (currentShopId == null) return;
        ApiClient.getApiService().updateCategory(currentShopId, categoryId, new CategoryRequest(name)).enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Category renamed!", Toast.LENGTH_SHORT).show();
                    loadCategories(response.body().getId());
                } else {
                    Toast.makeText(getContext(), "Failed to rename category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private UUID getSelectedCategoryId(ChipGroup group) {
        if (group == null) return categories.isEmpty() ? UUID.randomUUID() : categories.get(0).getId();
        int id = group.getCheckedChipId();
        if (id != View.NO_ID) {
            Chip chip = group.findViewById(id);
            if (chip != null) {
                String name = chip.getText().toString();
                for (CategoryResponse c : categories) {
                    if (c.getName().equalsIgnoreCase(name)) return c.getId();
                }
            }
        }
        return categories.isEmpty() ? UUID.randomUUID() : categories.get(0).getId();
    }

    private void uploadAndSave(BottomSheetDialog dialog, String name, String price, String desc, UUID catId) {
        File file = uriToFile(selectedImageUri);
        if (file == null) return;
        RequestBody rb = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), rb);

        ApiClient.getApiService().uploadImage(part).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveFoodToDb(dialog, name, price, desc, catId, response.body().get("url"));
                }
            }
            @Override public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveFoodToDb(BottomSheetDialog dialog, String name, String price, String desc, UUID catId, String url) {
        if (currentShopId == null) return;

        try {
            FoodRequest request = new FoodRequest(catId, name, desc, new BigDecimal(price), url);
            ApiClient.getApiService().createFood(currentShopId, request).enqueue(new Callback<FoodResponse>() {
                @Override
                public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                    if (response.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đã thêm món ăn!", Toast.LENGTH_SHORT).show();
                        }
                        loadData(true);
                        dialog.dismiss();
                        selectedImageUri = null;
                    }
                }
                @Override public void onFailure(Call<FoodResponse> call, Throwable t) {}
            });
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File uriToFile(Uri uri) {
        try {
            File file = new File(requireContext().getCacheDir(), "temp_food_" + System.currentTimeMillis() + ".jpg");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buf = new byte[1024]; int len;
            while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            os.close(); is.close();
            return file;
        } catch (Exception e) { return null; }
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

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .into(imgFull);

        AlertDialog dialog = builder.setView(view).create();

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        view.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private ColorStateList getChipBackgroundStateList() {
        int[][] states = new int[][] {
            new int[] {android.R.attr.state_checked},
            new int[] {-android.R.attr.state_checked}
        };
        int[] colors = new int[] {
            Color.parseColor("#FF5722"), // Active: Orange (@color/button_orange)
            Color.parseColor("#EDF2F7")  // Inactive: gray-blue
        };
        return new ColorStateList(states, colors);
    }

    private ColorStateList getChipTextStateList() {
        int[][] states = new int[][] {
            new int[] {android.R.attr.state_checked},
            new int[] {-android.R.attr.state_checked}
        };
        int[] colors = new int[] {
            Color.WHITE,                 // Active: white
            Color.parseColor("#4A5568")  // Inactive: charcoal gray
        };
        return new ColorStateList(states, colors);
    }

    private void updateStatsCounts() {
        if (tvStatTotalCount == null || tvStatInStockCount == null || tvStatSoldOutCount == null || tvStatCategoriesCount == null) {
            return;
        }

        int totalCount = foodList.size();
        int inStockCount = 0;
        int soldOutCount = 0;

        for (FoodResponse food : foodList) {
            boolean isAvailable = food.getIsAvailable() != null ? food.getIsAvailable() : true;
            if (isAvailable) {
                inStockCount++;
            } else {
                soldOutCount++;
            }
        }

        int categoriesCount = categories.size();

        tvStatTotalCount.setText(String.valueOf(totalCount));
        tvStatInStockCount.setText(String.valueOf(inStockCount));
        tvStatSoldOutCount.setText(String.valueOf(soldOutCount));
        tvStatCategoriesCount.setText(String.valueOf(categoriesCount));
    }
}
