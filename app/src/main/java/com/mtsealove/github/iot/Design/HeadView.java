package com.mtsealove.github.iot.Design;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.mtsealove.github.iot.MainActivity;
import com.mtsealove.github.iot.R;
import com.mtsealove.github.iot.RfidActivity;

public class HeadView extends RelativeLayout {
    ImageView menuIV;
    Context context;

    public HeadView(Context context) {
        super(context);
        init(context);
    }

    public HeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public HeadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.view_head, HeadView.this, false);
        addView(layout);
        menuIV = layout.findViewById(R.id.menuIv);
        menuIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDrawer();
            }
        });
    }

    private void OpenDrawer() {
        switch (context.getClass().getSimpleName()){
            case "MainActivity":
                MainActivity.OpenDrawer();
                break;
            case "RfidActivity":
                RfidActivity.OpenDrawer();
                break;
        }
    }
}
