package com.foodorderingapp.ui.home.vendor;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foodorderingapp.R;

public class VendorOrdersFragment extends Fragment {

    public VendorOrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_vendor_orders,
                container,
                false
        );
    }
}