package com.example.myrecyclerview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityRegProd extends AppCompatActivity {
    // 아래는 사용 request code
    private static final int PROD_REG_CODE = 103;   //상품 등록 코드
    private static final int IMAGE_REQUEST_CODE = 111; //이미지 받아오기

    ImageView imageView;
    byte[] byteArray;
    EditText prod_name;     // 상품 이름
    EditText prod_price;   // 가격
    EditText prod_link;     // 링크
    EditText prod_note;     // 메모를 입력하세요

    int prod_star1;          //관심

    boolean img_ok;

    //숫자표현 특수문자
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");       // 3자리 숫자마다 , 표시
    private String result="";

    //날짜관련
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyMMdd");
    //날짜관련

    String prod_date1;          //날짜

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prod_activity_reg);
        setTitle("목록 추가");

        final CheckBox cb_star = (CheckBox)findViewById(R.id.prod_star);

        cb_star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                if(checked){    //관심 체크 시
                    prod_star1 = 1;
                    Toast.makeText(getApplicationContext(), "관심 설정", Toast.LENGTH_SHORT).show();   // 토스트 메시지 출력
                }else{
                    prod_star1 = 0;
                }
            }
        });


        imageView = findViewById(R.id.prod_img);                       // 이미지뷰 id

        prod_price = (EditText) findViewById(R.id.prod_price);
        prod_price.addTextChangedListener(watcher);                     // 3자리 숫자마다 , 적기 리스너

        imageView.setOnClickListener(new View.OnClickListener() {   // 이미지뷰 눌렀을 때 갤러리 열기
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);        // 다른앱에서 파일 검색할 수 있게 하기
                startActivityForResult(intent, IMAGE_REQUEST_CODE);       // 갤러리에 요청 코드 보내기
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //==========갤러리 동작=============
        if (requestCode == IMAGE_REQUEST_CODE) {       //사진받아오기
            if (resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    Bitmap bitmap = BitmapFactory.decodeStream(in); //이미지 파일을 변환할 때는 BitmapFactory 클래스의 decodeStream 함수
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byteArray = stream.toByteArray();
                    img_ok = true;
                    imageView.setImageBitmap(bitmap);              // img 를 이미지뷰에 출력
                    Toast.makeText(getApplicationContext(), "사진 불러오기 성공", Toast.LENGTH_SHORT).show();   // 토스트 메시지 출력
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {             // 뒤로가기로 작업 취소한때
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                if(img_ok != true)img_ok=false;
            }
        }
        //==========갤러리 동작=============
    }

    //=================메뉴 동작======================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {             // 액션바에 저장 취소버튼 추가
        getMenuInflater().inflate(R.menu.reg_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_btn:// 액션바 저장버튼 눌렀을때

                prod_date1 = getTime();  //현재 날짜 yymmdd 꼴

                Intent intent = new Intent(ActivityRegProd.this, MainActivity.class);     // 보여지는 엑티비티 자바 이름

                prod_name = (EditText) findViewById(R.id.prod_name);     // 상품 이름
                prod_price = (EditText) findViewById(R.id.prod_price);   // 가격
                prod_link = (EditText) findViewById(R.id.prod_link);     // 링크
                prod_note = (EditText) findViewById(R.id.prod_note);     // 메모

                intent.putExtra("name", prod_name.getText().toString());     // 키값은 name
                intent.putExtra("price", prod_price.getText().toString());
                intent.putExtra("link", prod_link.getText().toString());
                intent.putExtra("note", prod_note.getText().toString());
                intent.putExtra("star",prod_star1);
                intent.putExtra("date",prod_date1);
                if(img_ok == true) {
                    intent.putExtra("image", byteArray);
                }
                else if(img_ok == false){
                    byte[] tmp = null;
                    intent.putExtra("image",tmp);
                }
                setResult(PROD_REG_CODE,intent);    //Resultcode 설정
                Toast.makeText(getApplicationContext(), "저장완료했습니다\n"+
                        "20"+prod_date1.substring(0,2)+"."+prod_date1.substring(2,4)+"."+prod_date1.substring(4,prod_date1.length()),
                        Toast.LENGTH_SHORT).show();
                finish(); //등록창 -> 메인창

                return true;
            case R.id.return_btn:   //취소버튼 동작
                Toast.makeText(getApplicationContext(), "취소합니다", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //숫자 표기
    TextWatcher watcher = new TextWatcher() {                               // 글자 변경을 감지해줌 (3자리 숫자마다 , 나오기)
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(!TextUtils.isEmpty(charSequence.toString()) && !charSequence.toString().equals(result)){                         // EditText 가 비어있지 않을 때만 실행
                result = decimalFormat.format(Double.parseDouble(charSequence.toString().replaceAll(",","")));  //
                prod_price.setText(result);
                prod_price.setSelection(result.length());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {               // 가격입력시 3자리 마다 , 를 생기기 위해 쓰는 함수

        }
    };
    //현재 시각
    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}

