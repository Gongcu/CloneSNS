package com.example.healthtagram.loading;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    public void progressON() {
        if(getActivity()!=null)
            BaseApplication.getInstance().progressON(getActivity());
    }

    public void progressOFF() {
        BaseApplication.getInstance().progressOFF();
    }
}
