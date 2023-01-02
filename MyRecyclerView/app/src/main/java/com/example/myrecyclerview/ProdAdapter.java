package com.example.myrecyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ProdAdapter extends RecyclerView.Adapter<ProdAdapter.ViewHolder>{
    //ViwType 코드
    public static final int VERTICAL_LIST = 0;
    public static final int ALBUM_LIST = 1;
    int     mItemViewType;

    private ArrayList<ProdData> prodDataSet;
    private Context context;
    private Cursor mCursor;
    private ProdDB db;

    private OnItemClickListener listener; // 이벤트 리스너를 변수로 선언
    public interface OnItemClickListener { // 인터페이스 정의
      void onItemClick(View v, int position);
    }

    //============ 생성자 =============================
    // 생성자를 통해서 외부에서 데이터를 전달받도록 함
    public ProdAdapter(Context mContext, ArrayList<ProdData> dataSet, ProdDB db, OnItemClickListener mOnClickListener) {
        this.context = mContext;
        prodDataSet = dataSet;
        this.db = db;
        this.listener = mOnClickListener;
    }
    //============ 생성자 ============

    //================ViewType getter, setter================
    @Override
    public int getItemViewType(int position){
        return mItemViewType;
    }

    public void setItemViewType(int viewType) {
        this.mItemViewType = viewType;
        notifyDataSetChanged();
    }
    //================앨범형 리스트형 변경 ViewType================



    //============뷰홀더 클래스 =========================================
    public class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView prod_name;
        protected TextView prod_price;
        protected TextView prod_link;
        protected TextView prod_note;
        protected ImageView prod_img;


        public ViewHolder(@NonNull View itemView) { //View 객체 홀더
            super(itemView);

            this.prod_name=(TextView) itemView.findViewById(R.id.prod_name);
            this.prod_price=(TextView) itemView.findViewById(R.id.prod_price);
            this.prod_link=(TextView) itemView.findViewById(R.id.prod_link);
            this.prod_note=(TextView) itemView.findViewById(R.id.prod_note);
            this.prod_img = (ImageView) itemView.findViewById(R.id.prod_img);
        }

        //View 객체 값 설정
        void onBind(ProdData data, final OnItemClickListener listener) {
            prod_name.setText(data.getProd_name());
            prod_price.setText(data.getProd_price());
            prod_note.setText(data.getProd_note());
            prod_img.setImageBitmap(data.getProd_bit_image());

            //상품 클릭 동작
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,getAdapterPosition());
                }
            });
        }
    }
    //============뷰홀더 클래스 =========================================



    //=============== ViewHolder 생성 RecyclerView Adapter 필수 구현 항목 =============================
    @NonNull
    @Override   // ViewHolder 객체를 생성하여 리턴한다.
    public ProdAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context= parent.getContext();
        //목록형 뷰홀더 생성
        if( viewType == VERTICAL_LIST){
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.recyclerview_item, parent, false);
            ProdAdapter.ViewHolder viewHolder = new ProdAdapter.ViewHolder(view);
            return viewHolder;
        }
        //앨범형 생성
        else{
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.recyclerview_item2, parent, false);
            ProdAdapter.ViewHolder viewHolder = new ProdAdapter.ViewHolder(view);
            return viewHolder;
        }
    }
    //=============== RecyclerView Adapter 필수 구현 항목 =============================

    @Override
    //뷰홀더에 바인드
    public void onBindViewHolder(@NonNull ProdAdapter.ViewHolder holder, int position) {
        // Item을 하나, 하나 집어넣어주는(bind 되는) 함수입니다.
        holder.onBind(prodDataSet.get(position),listener);
    }

    //어댑터에 추가할 데이터리스트 추가
    void addItem(ProdData data){
        prodDataSet.add(data);
    }

    @Override   // 전체 데이터의 갯수를 리턴한다.
    public int getItemCount() {
        return prodDataSet.size();
    }

    /*필터리스트 설정*/
    public void  filterList(ArrayList<ProdData> filteredList) {
        prodDataSet = filteredList;
        notifyDataSetChanged();
    }

    public void removeItem(int position){
        prodDataSet.remove(position);
        notifyItemRemoved(position);
    }
}