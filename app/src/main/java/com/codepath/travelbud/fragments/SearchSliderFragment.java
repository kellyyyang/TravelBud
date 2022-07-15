package com.codepath.travelbud.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.travelbud.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchSliderFragment extends Fragment {

    public SearchSliderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_search_slider, container, false);

        View result = inflater.inflate(R.layout.fragment_search_slider, container, false);
        ViewPager pager = (ViewPager)result.findViewById(R.id.pager);

//        pager.setAdapter(buildAdapter());

        return(result);
    }

//    private PagerAdapter buildAdapter() {
//        return(new SampleAdapter(getActivity(), getChildFragmentManager()));
//    }
}