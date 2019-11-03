package com.mtsealove.github.iot.Design;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mtsealove.github.iot.Database.AItem;
import com.mtsealove.github.iot.Database.StatusMap;
import com.mtsealove.github.iot.R;

public class AItemView extends LinearLayout {
    Context context;
    TextView nameTv, invoiceTv, statusTv, stAddrTv, dstAddrTv;
    AItem aItem;

    public AItemView(Context context) {
        super(context);
        init(context);
    }

    public AItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public AItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.view_aitem, AItemView.this, false);
        addView(layout);
        nameTv = layout.findViewById(R.id.nameTv);
        invoiceTv = layout.findViewById(R.id.invoiceTv);
        statusTv = layout.findViewById(R.id.statusTv);
        stAddrTv = layout.findViewById(R.id.stAddrTv);
        dstAddrTv = layout.findViewById(R.id.dstAddrTv);
    }

    public void SetItem(AItem aItem) {
        this.aItem = aItem;
        nameTv.setText("상품명: "+aItem.getItemName());
        invoiceTv.setText("송장번호: "+aItem.getInvoiceNum());
        try{
            statusTv.setText("상태: "+StatusMap.GetStatus(Integer.parseInt(aItem.getStatus())));
        } catch (Exception e){

        }
        stAddrTv.setText("출발주소: "+aItem.getStAddress());
        dstAddrTv.setText("도착주소: "+aItem.getDstAddress());
    }


}
