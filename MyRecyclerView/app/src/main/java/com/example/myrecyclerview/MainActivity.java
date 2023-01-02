package com.example.myrecyclerview;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //==================req, res 코드=============================
    public static final int REG_CODE_FROM_MAIN = 101;   //메인 -> 등록창
    public static final int CHECK_PROD = 102; //메인 -> 확인창
    public static final int PROD_REG_CODE = 103;   //상품 등록 코드
    public static final int PROD_DATA_CHANGE = 108; //상품 수정
    public static final int PROD_DATA_DELETE = 105; //상품 삭제

    ImageButton main_menu, prod_reg,album_list, vertical_list;    //우측상단 4가지 이미지버튼

    //리사이클러뷰 관련
    RecyclerView recyclerView;  //리사이클러뷰
    ArrayList<ProdData> prodDataSet = new ArrayList<>();   //사용 할 View객체 >> 데이터Set,검색filter 데이터Set
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;        //LayoutManager
    ProdAdapter prodAdapter;    //어댑터

    //검색창 위젯
    EditText searchET;
    ArrayList<ProdData> filteredList;
    HashSet hs; //검색 중복 확인 변수

    //숫자표현 특수문자
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");       // 3자리 숫자마다 , 표시
    private String result="";

    Context tmpContext; //컨텍스트 저장 변수


    //===================상품 값=======================================
    String Name;     //상품 이름 받기
    String Price;   //상품 가격 받기
    String Link;    //상품 링크 받기
    String Note;    //상품 이미지
    int Star;
    String Date;    //상품날짜

    //====정렬 버튼 관련=====

    //푸시알림 관련
    private NotificationHelper notificationHelper;

    //DB
    ProdDB db;
    SQLiteDatabase sqlDB;

    byte[] Image_byte;

    Button data_get;
    String tmp;

    //===========Intent 응답 ==============
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PROD_REG_CODE) {  //등록코드
             byte[] Image_byte = data.getByteArrayExtra("image");    //비트맵이미지 받기

            Bitmap image;
            if(Image_byte != null) {
                image = BitmapFactory.decodeByteArray(Image_byte, 0, Image_byte.length);  //바이트배열 -> 비트맵
                ImageView prod_img = (ImageView) findViewById(R.id.prod_img);
                prod_img.setImageBitmap(image); //비트맵으로 이미지 출력
            }
            else{
                Image_byte = null;
                image = null;

            }

            Name = data.getStringExtra("name");                     //상품이름 받기
            Price = data.getStringExtra("price");                   //가격
            Link = data.getStringExtra("link");                     //링크
            Note = data.getStringExtra("note");                     //메모
            Star = data.getIntExtra("star",0);             //관심
            Date = data.getStringExtra("date");                     //날짜

            ProdData regData = new ProdData(Name,Price,Link,Note       //data 데이터
                    ,Star,Date);
            regData.setProd_bit_image(image);      //bitmap이미지 저장

            sqlDB = db.getWritableDatabase();
            SQLiteStatement p = sqlDB.compileStatement(
                    "INSERT INTO prod_List VALUES(?,?,?,?,?,?,?);");
            p.bindString(1,regData.getProd_name());
            p.bindString(2,regData.getProd_price());
            p.bindString(3,regData.getProd_link());
            p.bindString(4,regData.getProd_note());
            p.bindLong(5,regData.getProd_star());
            p.bindString(6,regData.getProd_date());
            if(Image_byte != null)p.bindBlob(7,Image_byte);
            p.execute();
            //sqlDB.close();


            //db.AddTask(regData);
           // Toast.makeText(tmpContext, regData.getProd_name()+" 됐나", Toast.LENGTH_SHORT).show();


            prodDataSet.add(regData);      //데이터 설정한 dataSet을 ProdData 리스트에 추가

            prodAdapter.notifyItemInserted(prodDataSet.size()); //어댑터의 Data추가
            prodAdapter.notifyDataSetChanged(); //새로고침
        }
        else if(requestCode == CHECK_PROD){ //확인창 req code
            if(resultCode == PROD_DATA_DELETE){     //삭제
                int pos = data.getIntExtra("position",-1);  //현재 위치
                String prod_name = data.getStringExtra("name");
                Toast.makeText(getApplicationContext(), prodDataSet.get(pos).getProd_name()+" 삭제 완료", Toast.LENGTH_SHORT).show();
                sqlDB.execSQL("DELETE FROM prod_List WHERE prod_name = '"
                        + prodDataSet.get(pos).getProd_name() + "';");
                prodDataSet.remove(pos);    //리스트 현재 위치 삭제
                prodAdapter.notifyDataSetChanged();
                if(prodDataSet.size()==0){
                    data_get.setVisibility(View.VISIBLE);
                    data_get.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getInitialData();
                        }
                    });
                }
            }   //삭제

            else if(resultCode == PROD_DATA_CHANGE){    //수정
                //이름,가격,링크,메모,관심,날짜,이미지,포지션 받기
                int pos = data.getIntExtra("position",-1);

                byte[] Image_byte = data.getByteArrayExtra("image");    //비트맵이미지 받기
                Bitmap image;
                if(Image_byte != null) {
                    image = BitmapFactory.decodeByteArray(Image_byte, 0, Image_byte.length);  //바이트배열 -> 비트맵
                    ImageView prod_img = (ImageView) findViewById(R.id.prod_img);
                    prod_img.setImageBitmap(image); //비트맵으로 이미지 출력
                }
                else{
                    image = null;
                }

                Name = data.getStringExtra("name");   //상품 이름 받기
                Price = data.getStringExtra("price");//상품 가격 받기
                Link = data.getStringExtra("link");//상품 링크 받기
                Note = data.getStringExtra("note");//상품 메모 받기
                Star = data.getIntExtra("star",0);  //관심 날짜
                Date = data.getStringExtra("date");

                //데이터 생성
                ProdData modData = new ProdData(Name, Price, Link, Note, Star, Date);
                modData.setProd_bit_image(image);


                prodDataSet.set(pos,modData);   //현재 위치에 데이터 변경
                prodAdapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "수정 완료", Toast.LENGTH_SHORT).show();
                sqlDB.execSQL("UPDATE prod_List "+"SET prod_price ='" + Price +
                        "' WHERE prod_name = '" + Name + "';");
                sqlDB.execSQL("UPDATE prod_List "+"SET prod_link ='" + Link +
                        "' WHERE prod_name = '" + Name + "';");
                sqlDB.execSQL("UPDATE prod_List "+"SET prod_note ='" + Note +
                        "' WHERE prod_name = '" + Name + "';");
                sqlDB.execSQL("UPDATE prod_List "+"SET prod_star =" + Star +
                        " WHERE prod_name = '" + Name + "';");
                if(Image_byte != null) {
                    SQLiteStatement p = sqlDB.compileStatement("UPDATE prod_List SET prod_img = ? WHERE prod_name = '"
                            + Name + "';");
                    p.bindBlob(1, Image_byte);
                    p.execute();
                }
            }
        }
    }
    //===========Intent 응답 ==============


    //=====================임의 기존 데이터 설정(데이터 베이스)================
    private void getData() {
        // 임의의 데이터입니다.
        sqlDB = db.getReadableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM prod_List;", null);
        while (cursor.moveToNext()) {
            tmp = cursor.getString(0);
        }
        if (tmp == null) {
            data_get.setVisibility(View.VISIBLE);
            data_get.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getInitialData();
                }
            });
        }
        //sqlDB = db.getReadableDatabase();
        //Cursor cursor;

        cursor = sqlDB.rawQuery("SELECT * FROM prod_List;", null);

        while (cursor.moveToNext()) {
            ProdData data = new ProdData(null, null, null, null, -1, null);
            data.setProd_name(cursor.getString(0));
            data.setProd_price(cursor.getString(1));
            data.setProd_link(cursor.getString(2));
            data.setProd_note(cursor.getString(3));
            data.setProd_star(cursor.getInt(4));
            data.setProd_date(cursor.getString(5));
            byte[] image = cursor.getBlob(6);
            if (image != null) {
                Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
                data.setProd_bit_image(bm);
            } else {
                data.setProd_bit_image(null);
            }
            prodDataSet.add(data);
        }

        //adapter에 값이 변경되었다는 것을 알려줍니다.
        prodAdapter.notifyDataSetChanged(); //새로고침
    }

    //=====================임의 기존 데이터 설정(데이터 베이스)===============


    //=====================메뉴 동작 ===================
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo){

        super.onCreateContextMenu(menu, view, menuInfo);

        MenuInflater mmInflater = getMenuInflater();
        if(view == main_menu){
            mmInflater.inflate(R.menu.main_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.itemlatest: //최신순
                Comparator<ProdData> latest = new Comparator<ProdData>() {
                    @Override
                    public int compare(ProdData item1, ProdData item2) {
                        int ret;
                        int year1, mon1, day1;
                        int year2, mon2, day2;

                        year1 = Integer.parseInt(item1.getProd_date()) / 10000;
                        year2 = Integer.parseInt(item2.getProd_date()) / 10000;

                        mon1 = (Integer.parseInt(item1.getProd_date()) % 10000) / 100;
                        mon2 = (Integer.parseInt(item2.getProd_date()) % 10000) / 100;

                        day1 = (Integer.parseInt(item1.getProd_date()) % 10000) % 100;
                        day2 = (Integer.parseInt(item2.getProd_date()) % 10000) % 100;
                        // 년/월/일로 비교하기

                        if (year1 != year2) {
                            if (year1 < year2)
                                ret = 1;
                            else if (year1 == year2)
                                ret = 0;
                            else
                                ret = -1;
                            return ret;
                        } else if (mon1 != mon2) {
                            if (mon1 < mon2)
                                ret = 1;
                            else if (mon1 == mon2)
                                ret = 0;
                            else
                                ret = -1;
                            return ret;
                        } else {
                            if (day1 < day2)
                                ret = 1;
                            else if (day1 == day2)
                                ret = 0;
                            else
                                ret = -1;
                            return ret;
                        }
                    }
                } ;
                Collections.sort(prodDataSet, latest);
                prodAdapter.notifyDataSetChanged();
                return true;

            case R.id.itemoldest: //오래된순
                Comparator<ProdData> oldest = new Comparator<ProdData>() {
                    @Override
                    public int compare(ProdData item1, ProdData item2) {
                        int ret;
                        int year1, mon1, day1;
                        int year2, mon2, day2;

                        year1 = Integer.parseInt(item1.getProd_date()) / 10000;
                        year2 = Integer.parseInt(item2.getProd_date()) / 10000;

                        mon1 = (Integer.parseInt(item1.getProd_date()) % 10000) / 100;
                        mon2 = (Integer.parseInt(item2.getProd_date()) % 10000) / 100;

                        day1 = (Integer.parseInt(item1.getProd_date()) % 10000) % 100;
                        day2 = (Integer.parseInt(item2.getProd_date()) % 10000) % 100;

                        if (year1 != year2) {
                            if (year1 < year2)
                                ret = -1;
                            else if (year1 == year2)
                                ret = 0;
                            else
                                ret = 1;
                            return ret;
                        } else if (mon1 != mon2) {
                            if (mon1 < mon2)
                                ret = -1;
                            else if (mon1 == mon2)
                                ret = 0;
                            else
                                ret = 1;
                            return ret;
                        } else {
                            if (day1 < day2)
                                ret = -1;
                            else if (day1 == day2)
                                ret = 0;
                            else
                                ret = 1;
                            return ret;
                        }
                    }
                } ;
                Collections.sort(prodDataSet, oldest);
                prodAdapter.notifyDataSetChanged();
                return true;

            case R.id.itemlow: //가격 낮은순
                Comparator<ProdData> noAsc = new Comparator<ProdData>() {
                    @Override
                    public int compare(ProdData item1, ProdData item2) {
                        int ret ;

                        if (Integer.parseInt(StringReplace(item1.getProd_price())) < Integer.parseInt(StringReplace(item2.getProd_price())))
                            ret = -1 ;
                        else if (Integer.parseInt(StringReplace(item1.getProd_price())) == Integer.parseInt(StringReplace(item2.getProd_price())))
                            ret = 0 ;
                        else
                            ret = 1 ;

                        return ret ;
                    }
                } ;
                Collections.sort(prodDataSet, noAsc);
                prodAdapter.notifyDataSetChanged();
                return true;

            case R.id.itemhigh: //가격 높은순
                Comparator<ProdData> noDesc = new Comparator<ProdData>() {
                    @Override
                    public int compare(ProdData item1, ProdData item2) {
                        int ret = 0 ;

                        if (Integer.parseInt(StringReplace(item1.getProd_price())) < Integer.parseInt(StringReplace(item2.getProd_price())))
                            ret = 1 ;
                        else if (Integer.parseInt(StringReplace(item1.getProd_price())) == Integer.parseInt(StringReplace(item2.getProd_price())))
                            ret = 0 ;
                        else
                            ret = -1 ;

                        return ret ;
                    }
                } ;
                Collections.sort(prodDataSet, noDesc) ;
                prodAdapter.notifyDataSetChanged() ;
                return true;
            case R.id.itemattention:    //관심목록
                //관심목록 상단출력
                Comparator<ProdData> statusStar = new Comparator<ProdData>() {
                    @Override
                    public int compare(ProdData item1, ProdData item2) {
                        int ret ;

                        if (item1.getProd_star() < item2.getProd_star())
                            ret = 1 ;
                        else if (item1.getProd_star() == item2.getProd_star())
                            ret = 0 ;
                        else
                            ret = -1 ;

                        return ret ;
                    }
                } ;
                Collections.sort(prodDataSet, statusStar);
                prodAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }
    //=====================메뉴 동작 ===================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //====어플리케이션 이름 숨기기=====
        getSupportActionBar().hide();
        //====어플리케이션 이름 숨기기=====

        data_get = (Button) findViewById(R.id.data_get);
        
        //====리사이클러뷰 필수 ==========
        tmpContext = (Context) this;    //컨텍스트
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);  //리사이클러뷰
        
        //레이아웃 매니저 생성
        linearLayoutManager = new LinearLayoutManager(tmpContext);
        recyclerView.setLayoutManager(linearLayoutManager);  // LayoutManager 설정

        db = new ProdDB(this);


        //어댑터 생성
        prodAdapter = new ProdAdapter(getApplicationContext(), prodDataSet, db, new ProdAdapter.OnItemClickListener() {
            @Override
            //===========상품 클릭시===================
            public void onItemClick(View v, int position) {
                Intent intent2 = new Intent( getApplicationContext(),ActivityCheckProd.class);

                //이미지 가져오기
                Bitmap prod_Bitmap = prodDataSet.get(position).getProd_bit_image();

                if(prod_Bitmap ==null){
                    byte[] byteArray = null;
                    intent2.putExtra("image",byteArray);
                }else {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    prod_Bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent2.putExtra("image",byteArray);
                }

                String prod_name = prodDataSet.get(position).getProd_name();
                String prod_price = prodDataSet.get(position).getProd_price();
                String prod_note = prodDataSet.get(position).getProd_note();

                //이름,가격,메모,링크,이미지,관심,날짜,포지션
                intent2.putExtra("name",prod_name);
                intent2.putExtra("price",prod_price);
                intent2.putExtra("note", prod_note);

                String prod_link1 = prodDataSet.get(position).getProd_link();
                intent2.putExtra("link",prod_link1);

                int prod_star1 = prodDataSet.get(position).getProd_star();
                intent2.putExtra("star",prod_star1);

                String prod_date1 = prodDataSet.get(position).getProd_date();
                intent2.putExtra("date",prod_date1);

                //intent2.putExtra("image",byteArray);

                intent2.putExtra("position",position);

                startActivityForResult(intent2,CHECK_PROD);
            }
            //===========상품 클릭시===================
        });
        recyclerView.setAdapter(prodAdapter); // 어댑터 설정, 그 담아져있는 데이터 어댑터를 리사이클러뷰의 세팅해줘라
        //====리사이클러뷰 필수 ==========

        //====설명서 만들기=====
        ImageButton manual = (ImageButton) findViewById(R.id.manual);
        manual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                View dialogView = (View) View.inflate(MainActivity.this, R.layout.dialog, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                ImageView ivPoster = (ImageView) dialogView.findViewById(R.id.ivPoster);
                ivPoster.setImageResource(R.drawable.add1);
                dlg.setView(dialogView);
                dlg.setIcon(R.mipmap.ic_andro_round);
                dlg.setNegativeButton("cancel", null);
                dlg.setPositiveButton("next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        View dialogView = (View) View.inflate(MainActivity.this, R.layout.dialog, null);
                        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                        ImageView ivPoster = (ImageView) dialogView.findViewById(R.id.ivPoster);
                        ivPoster.setImageResource(R.drawable.add2);
                        dlg.setView(dialogView);
                        dlg.setIcon(R.mipmap.ic_andro_round);
                        dlg.setNegativeButton("cancel", null);
                        dlg.setPositiveButton("next", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                View dialogView = (View) View.inflate(MainActivity.this, R.layout.dialog, null);
                                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                                ImageView ivPoster = (ImageView) dialogView.findViewById(R.id.ivPoster);
                                ivPoster.setImageResource(R.drawable.add3);
                                dlg.setView(dialogView);
                                dlg.setIcon(R.mipmap.ic_andro_round);
                                dlg.setNegativeButton("cancel", null);
                                dlg.show();
                            }
                        });
                        dlg.show();
                    }
                });
                dlg.show();
            }
        });
        //====설명서 만들기=====

        //=====메뉴 동작 ===================
        main_menu =(ImageButton) findViewById(R.id.btnmenu);
        registerForContextMenu(main_menu);
        //=====메뉴 동작 ===================

        //=====등록창 동작 ===================
        prod_reg = (ImageButton) findViewById(R.id.add_product);
        prod_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prodDataSet.size() == 0) {
                    Toast.makeText(getApplicationContext(), "초기 데이터 생성을 우선 해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ActivityRegProd.class);// 보여지는 엑티비티 자바 이름
                    startActivityForResult(intent, REG_CODE_FROM_MAIN);
                }
            }
        });
        //=====등록창 동작 ===================

        //=====출력 리스트 모양 동작 ===================
        album_list = (ImageButton) findViewById(R.id.btnalbum);
        vertical_list = (ImageButton) findViewById(R.id.btnvertical);

        album_list.setOnClickListener(new View.OnClickListener() {  //앨범형
            @Override
            public void onClick(View view) {
                prodAdapter.setItemViewType(ProdAdapter.ALBUM_LIST);    //ViewType 전송
                gridLayoutManager = new GridLayoutManager(tmpContext, 2); //그리드 레이아웃매니저 설정
                recyclerView.setLayoutManager(gridLayoutManager);   
            }
        });

        vertical_list.setOnClickListener(new View.OnClickListener() {   //목록형
            @Override
            public void onClick(View view) {
                prodAdapter.setItemViewType(ProdAdapter.VERTICAL_LIST);
                linearLayoutManager = new LinearLayoutManager(tmpContext);
                recyclerView.setLayoutManager(linearLayoutManager);  //리니어 LayoutManager 설정
            }
        });
        //=====출력 리스트 모양 동작 ===================


        //=====검색 동작 ===================
        searchET = findViewById(R.id.search_view); //써치뷰
        filteredList=new ArrayList<>(); //필터리스트 생성

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = searchET.getText().toString();
                searchFilter(searchText);
                if(searchText.replace(" ", "").equals("")) {    //공백 발견 시
                    searchET.clearFocus();
                    prodAdapter.filterList(prodDataSet);
                }
            }
        });
        //=====검색 동작 ===================


        getData();  //데이터 값들 가져오기
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*===========검색필터 설정=============*/
    public void searchFilter(String searchText) {
        filteredList.clear();
        hs = new HashSet(filteredList);

        /*상품이름으로 검색*/
        for (int i = 0; i < prodDataSet.size(); i++) {
            if (prodDataSet.get(i).getProd_name().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(prodDataSet.get(i));
                hs.add(filteredList);
            }
        }
        /*가격으로 검색*/
        for (int i = 0; i < prodDataSet.size(); i++) {
            if (prodDataSet.get(i).getProd_price().toLowerCase().contains(searchText.toLowerCase())) {
                if(!(hs.contains(filteredList))){   //중복 확인
                    filteredList.add(prodDataSet.get(i));
                    hs.addAll(filteredList);
                }
            }
        }
        /*메모로 검색*/
        for (int i = 0; i < prodDataSet.size(); i++) {
            if (prodDataSet.get(i).getProd_note().toLowerCase().contains(searchText.toLowerCase())) {
                if(!hs.contains(filteredList)) {
                    filteredList.add(prodDataSet.get(i));
                    hs.addAll(filteredList);
                }
            }
        }
        prodAdapter.filterList(filteredList);
    }
    /*===========검색필터 설정=============*/

    /*===========특수문자 제거 함수=============*/
    public static String StringReplace(String str){
        String match = "[^\uAC00-\uD7A30-9a-zA-Z]";
        str = str.replaceAll(match, "");
        return str;
    }
    /*===========특수문자 제거 함수=============*/

    public byte[] bitmapToByteArray( Bitmap bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }

    private void restart(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    public void getInitialData(){
        List<String> listName = Arrays.asList(
                "ARCH TEE",
                "시그니처 로고 반팔티",
                "미니멀프로젝트 로버스트 헤비 오버핏 반팔티셔츠",
                "Durgod 토러스 K320KR 텐키리스 투톤 키보드",
                "바밀로 고래 VA87M 텐키리스 키보드",
                "콕스 COX CK87 게이트론 텐키리스 키보드",
                "뉴발란스 CM878MC1",
                "나이키 에어 포스107"
        );

        List<String> listPrice = Arrays.asList(
                "18,000", "19,900", "19,000", "129,500", "168,000", "49,900", "129,000", "119,000"
        );
        List<String> listNote = Arrays.asList(
                "반팔, 검정",
                "반팔, 흰색",
                "반팔, 회색",
                "키보드, 검정, 흰색",
                "키보드, 하늘색, 흰색",
                "키보드, 검정색, 빨간색",
                "신발, 회색",
                "신발, 흰색"
        );
        List<Integer> listImgId = Arrays.asList(
                R.drawable.sp1,
                R.drawable.sp2,
                R.drawable.sp3,
                R.drawable.sp4,
                R.drawable.sp5,
                R.drawable.sp6,
                R.drawable.sp7,
                R.drawable.sp8
        );
        List<Integer> listStar = Arrays.asList(
                1, 0, 1, 0, 1, 1, 0, 0
        );
        List<String> listlink = Arrays.asList(
                "http://mss.kr/2409894?_gf=A",
                "https://www.hiver.co.kr/products/b/66706556",
                "https://www.hiver.co.kr/products/b/15819478",
                "http://naver.me/GazatNbY",
                "http://naver.me/G5I7IrFb",
                "http://naver.me/GxOtp3oe",
                "https://www.nbkorea.com/product/productDetail.action?styleCode=NBP7CS130I&colCode=16&cIdx=1385",
                "https://www.nike.com/kr/ko_kr/t/men/fw/nike-sportswear/CW2288-111/avbt44/air-force-1-07"
        );
        List<String> listDate = Arrays.asList(
                "220503", "220501", "220504", "220507", "220405", "220431", "220427", "220421"
        );


        for (int i = 0; i < listName.size(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Bitmap get_prod_image = BitmapFactory.decodeResource(getApplicationContext().getResources(), listImgId.get(i));   //drawable 사진 비트맵 변환
            Image_byte = bitmapToByteArray(get_prod_image);
            ProdData data = new ProdData(listName.get(i), listPrice.get(i), listlink.get(i), listNote.get(i), listStar.get(i), listDate.get(i));
            data.setProd_bit_image(get_prod_image);
//
            sqlDB = db.getWritableDatabase();
            SQLiteStatement p = sqlDB.compileStatement("INSERT INTO prod_List VALUES(?,?,?,?,?,?,?);");
            p.bindString(1, listName.get(i));
            p.bindString(2, listPrice.get(i));
            p.bindString(3, listlink.get(i));
            p.bindString(4, listNote.get(i));
            p.bindLong(5, listStar.get(i));
            p.bindString(6, listDate.get(i));
            p.bindBlob(7, Image_byte);
            p.execute();
        }
        //  Toast.makeText(tmpContext, "어플리케이션을 완전히 종료했다가 \n다시 켜주세요", Toast.LENGTH_LONG).show();
        restart(getApplicationContext());
    }
}