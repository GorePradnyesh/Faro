package com.zik.faro.frontend;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zik.faro.frontend.faroservice.auth.TokenCache;

public class MoreOptionsPage extends Fragment{

    static EventListHandler eventListHandler = EventListHandler.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_more_options_page, container, false);
        Button logout = (Button)v.findViewById(R.id.logout);
        TextView tv = (TextView) v.findViewById(R.id.text);
        tv.setText(this.getTag() + " Content");
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventListHandler.clearListAndMapOnLogout();
                TokenCache.getTokenCache().deleteToken();
                getActivity().finish();
            }
        });
        return v;
    }
}
