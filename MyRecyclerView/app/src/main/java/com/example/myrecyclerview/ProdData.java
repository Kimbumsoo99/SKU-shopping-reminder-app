package com.example.myrecyclerview;

import android.graphics.Bitmap;

public class ProdData {
    private String prod_name;   //상품이름
    private String prod_price;  //상품가격
    private String prod_note;   //상품메모
    private int prod_star;      //상품 관심
    private String prod_date;   //상품 등록 날짜
    private Bitmap prod_bit_image; //상품이미지
    private String prod_link;   //상품 링크

    public ProdData(String prod_name, String prod_price,  String prod_link, String prod_note,
                    int prod_star, String prod_date) {
        this.prod_name = prod_name;
        this.prod_price = prod_price;
        this.prod_note = prod_note;
        this.prod_link = prod_link;
        this.prod_star = prod_star;
        this.prod_date = prod_date;
    }

    public Bitmap getProd_bit_image() {
        return prod_bit_image;
    }

    public void setProd_bit_image(Bitmap prod_bit_image) {
        this.prod_bit_image = prod_bit_image;
    }

    public int getProd_star() {
        return prod_star;
    }

    public String getProd_link() {
        return prod_link;
    }

    public void setProd_link(String prod_link) {
        this.prod_link = prod_link;
    }

    public void setProd_star(int prod_star) {
        this.prod_star = prod_star;
    }

    public String getProd_date() {
        return prod_date;
    }

    public void setProd_date(String prod_date) {
        this.prod_date = prod_date;
    }

    public String getProd_price() {
        return prod_price;
    }

    public void setProd_price(String prod_price) {
        this.prod_price = prod_price;
    }

    public String getProd_name() {
        return prod_name;
    }

    public void setProd_name(String prod_name) {
        this.prod_name = prod_name;
    }

    public String getProd_note() {
        return prod_note;
    }

    public void setProd_note(String prod_note) {
        this.prod_note = prod_note;
    }
}
