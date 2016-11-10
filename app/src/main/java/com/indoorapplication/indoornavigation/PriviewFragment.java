package com.indoorapplication.indoornavigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.im.util.IMFloorInfo;
import com.amap.api.im.view.IMIndoorMapFragment;
import com.indoorapplication.indoornavigation.route.PathFragment;
import com.indoorapplication.indoornavigation.route.PoiSelectFragment;

/**
 * Created by liyuanqing on 2016/10/25.
 */

public class PriviewFragment extends Fragment {

    private IMIndoorMapFragment mIndoorMapFragment = null;

    public static final PriviewFragment newInstance(Context context, IMIndoorMapFragment indoorMapFragment)
    {
        PriviewFragment retNewFragment = new PriviewFragment();
        //retNewFragment.mPoiSelectFragment = PoiSelectFragment.newInstance(context,new IMFloorInfo(1, "0",""));
        retNewFragment.mIndoorMapFragment = indoorMapFragment;
        return retNewFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = View.inflate(getActivity(), R.layout.fragment_preview, null);

        FragmentTransaction transcation = getActivity().getSupportFragmentManager().beginTransaction();
        transcation.setCustomAnimations(0, 0, 0,0);
//        if (!mPoiSelectFragment.isAdded()) {
//            transcation.hide(this).add(R.id.indoor_main_view, mPoiSelectFragment);
//            mPoiSelectFragment.setIndoorMapFragment(mIndoorMapFragment);
//            mPoiSelectFragment.setPathFragment(this);
//        } else {
//            transcation.hide(this).show(mPoiSelectFragment);
//        }
        transcation.show(mIndoorMapFragment);
        transcation.commit();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
