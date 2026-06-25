package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.VoucherResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    public interface VoucherActionHandler {
        void onStatusToggled(VoucherResponse voucher, boolean isActive);
        void onEditClicked(VoucherResponse voucher);
        void onDeleteClicked(VoucherResponse voucher);
    }

    private final List<VoucherResponse> vouchers;
    private final VoucherActionHandler actionHandler;

    public VoucherAdapter(List<VoucherResponse> vouchers, VoucherActionHandler actionHandler) {
        this.vouchers = vouchers;
        this.actionHandler = actionHandler;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        holder.bind(vouchers.get(position));
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    public void updateData(List<VoucherResponse> newVouchers) {
        vouchers.clear();
        vouchers.addAll(newVouchers);
        notifyDataSetChanged();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDiscountBadge;
        private final TextView tvDiscountTypeLabel;
        private final TextView tvVoucherCode;
        private final TextView tvVoucherTitle;
        private final SwitchCompat switchActive;
        private final TextView tvVoucherDates;
        private final TextView tvVoucherConditions;
        private final TextView tvVoucherApplyType;
        private final View btnEdit;
        private final View btnDelete;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscountBadge = itemView.findViewById(R.id.tv_discount_badge);
            tvDiscountTypeLabel = itemView.findViewById(R.id.tv_discount_type_label);
            tvVoucherCode = itemView.findViewById(R.id.tv_voucher_code);
            tvVoucherTitle = itemView.findViewById(R.id.tv_voucher_title);
            switchActive = itemView.findViewById(R.id.switch_active);
            tvVoucherDates = itemView.findViewById(R.id.tv_voucher_dates);
            tvVoucherConditions = itemView.findViewById(R.id.tv_voucher_conditions);
            tvVoucherApplyType = itemView.findViewById(R.id.tv_voucher_apply_type);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(VoucherResponse voucher) {
            // 1. Discount Display
            if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
                tvDiscountBadge.setText(voucher.getDiscountValue().stripTrailingZeros().toPlainString() + "%");
                tvDiscountTypeLabel.setText("GIẢM");
            } else {
                tvDiscountBadge.setText(formatShortCurrency(voucher.getDiscountValue()));
                tvDiscountTypeLabel.setText("GIẢM ĐỒNG");
            }

            // 2. Voucher Code & Title
            tvVoucherCode.setText(voucher.getCode());
            tvVoucherTitle.setText(voucher.getTitle());

            // 3. Status Switch (Avoid trigger handler during binding)
            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(voucher.getActive() != null ? voucher.getActive() : false);
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (actionHandler != null) {
                    actionHandler.onStatusToggled(voucher, isChecked);
                }
            });

            // 4. Validity dates
            String start = formatDateTimeShort(voucher.getStartDate());
            String end = formatDateTimeShort(voucher.getEndDate());
            tvVoucherDates.setText("Hiệu lực: " + start + " - " + end);

            // 5. Conditions
            String minOrder = formatCurrency(voucher.getMinOrderValue());
            String maxDiscount = "";
            if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType()) && voucher.getMaxDiscountValue() != null) {
                maxDiscount = " • Giảm tối đa: " + formatCurrency(voucher.getMaxDiscountValue());
            }
            tvVoucherConditions.setText("Đơn tối thiểu: " + minOrder + maxDiscount);

            // 6. Application Range
            if ("ALL_MENU".equalsIgnoreCase(voucher.getApplyType())) {
                tvVoucherApplyType.setText("Áp dụng: Toàn bộ thực đơn");
                tvVoucherApplyType.setTextColor(itemView.getContext().getResources().getColor(R.color.status_success));
            } else {
                int foodCount = voucher.getFoodIds() != null ? voucher.getFoodIds().size() : 0;
                tvVoucherApplyType.setText("Áp dụng: " + foodCount + " món ăn cụ thể");
                tvVoucherApplyType.setTextColor(itemView.getContext().getResources().getColor(R.color.brand_orange_dark));
            }

            // 7. Click Handlers
            btnEdit.setOnClickListener(v -> {
                if (actionHandler != null) {
                    actionHandler.onEditClicked(voucher);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (actionHandler != null) {
                    actionHandler.onDeleteClicked(voucher);
                }
            });
        }

        private String formatDateTimeShort(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.isEmpty()) return "";
            try {
                String clean = isoDateTime;
                if (clean.contains(".")) {
                    clean = clean.substring(0, clean.indexOf("."));
                }
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm", Locale.US);
                Date date = parser.parse(clean);
                return formatter.format(date);
            } catch (Exception e) {
                return isoDateTime;
            }
        }

        private String formatCurrency(BigDecimal value) {
            if (value == null) return "0đ";
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(value) + "đ";
        }

        private String formatShortCurrency(BigDecimal value) {
            if (value == null) return "0";
            if (value.compareTo(new BigDecimal(1000)) >= 0) {
                BigDecimal kValue = value.divide(new BigDecimal(1000));
                return kValue.stripTrailingZeros().toPlainString() + "k";
            }
            return value.stripTrailingZeros().toPlainString();
        }
    }
}
