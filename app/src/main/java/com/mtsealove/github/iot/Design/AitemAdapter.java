package com.mtsealove.github.iot.Design;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mtsealove.github.iot.Database.AItem;
import com.mtsealove.github.iot.R;

import java.util.ArrayList;

public class AitemAdapter extends RecyclerView.Adapter<AitemAdapter.ItemViewHolder> {
    private ArrayList<AItem> aItemArrayList = null;

    public AitemAdapter(ArrayList<AItem> aItemArrayList) {
        this.aItemArrayList = aItemArrayList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_aitem, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.OnBind(aItemArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return aItemArrayList.size();
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
   class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, invoiceTv, statusTv, stAddrTv, dstAddrTv;

        ItemViewHolder(View itemView) {
            super(itemView);
            this.nameTv = itemView.findViewById(R.id.nameTv);
            this.invoiceTv = itemView.findViewById(R.id.invoiceTv);
            this.statusTv = itemView.findViewById(R.id.statusTv);
            this.stAddrTv = itemView.findViewById(R.id.stAddrTv);
            this.dstAddrTv = itemView.findViewById(R.id.dstAddrTv);
        }

        void OnBind(AItem aItem) {
            nameTv.setText("상품명: "+aItem.getItemName());
            invoiceTv.setText("송장번호: "+aItem.getInvoiceNum());
            statusTv.setText("상태: "+aItem.getAction());
            stAddrTv.setText("출발주소:"+aItem.getStAddress());
            dstAddrTv.setText("도착주소: "+aItem.getDstAddress());
        }

    }
}
