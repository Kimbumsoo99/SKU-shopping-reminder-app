package com.example.myrecyclerview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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

public class ActivityModProd extends AppCompatActivity {
    //==================req, res 코드=============================
    public static final int PROD_DATA_CHANGE = 108; //상품 수정
    private static final int IMAGE_REQUEST_CODE = 111; //이미지 받아오기

    ImageView imageView;
    TextView prod_name;     // 상품 이름
    EditText prod_price;   // 가격
    EditText prod_link;     // 링크
    EditText prod_note;     // 메모를 입력하세요
    CheckBox prod_star;

    int Star;          // 중요도
    int Pos;
    String Date;
    byte[] change_image_arr;
    byte[] get_image_arr;

    Button btn_cancel, btn_ok;

    //숫자표현 특수문자
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");       // 3자리 숫자마다 , 표시
    private String result="";

    int change_img; //img 변화 확인 변수

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prod_activity_modify);
        //====어플리케이션 이름 숨기기=====
        getSupportActionBar().hide();
        //====어플리케이션 이름 숨기기=====

        prod_name = (TextView) findViewById(R.id.mod_name);     // 상품 이름
        prod_price = (EditText) findViewById(R.id.mod_price);   // 가격
        prod_link = (EditText) findViewById(R.id.mod_link);     // 링크
        prod_note = (EditText) findViewById(R.id.mod_note);     // 메모
        prod_star = (CheckBox) findViewById(R.id.mod_star);
        imageView = (ImageView) findViewById(R.id.mod_img);
        Intent intent = getIntent();


        //이름,가격,링크,메모,관심,날짜,이미지,포지션
        String Name = intent.getStringExtra("name");
        String Price = intent.getStringExtra("price");
        String Link = intent.getStringExtra("link");
        String Note = intent.getStringExtra("note");
        Pos = intent.getIntExtra("position",0);
        Star = intent.getIntExtra("star",0);
        Date = intent.getStringExtra("date");

        final CheckBox cb_star = (CheckBox)findViewById(R.id.mod_star);
        //관심인터페이스
        if(Star == 1)   //관심 상품이면
            cb_star.setChecked(true);   //체크 상태
        else
            cb_star.setChecked(false); //un 체크 상태

        cb_star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){    //상품 체크 시
                    Star = 1;
                    Toast.makeText(getApplicationContext(), "관심 설정\n", Toast.LENGTH_SHORT).show();   // 토스트 메시지 출력
                }else{
                    Star = 0;
                }
            }
        });

        //EditText 출력
        prod_name.setText(Name);                                             // 이름 출력
        prod_price.setText(Price);                                             // 가격 출력
        prod_price.addTextChangedListener(watcher);                     // 3자리 숫자마다 , 적기 리스너
        prod_link.setText(Link);                                             // 링크 출력
        prod_note.setText(Note);

        //비트맵으로 이미지 출력
        get_image_arr = intent.getByteArrayExtra("image");
        if(get_image_arr != null) {
            Bitmap image = BitmapFactory.decodeByteArray(get_image_arr, 0, get_image_arr.length);  //바이트배열 -> 비트맵
            ImageView prod_img = (ImageView) findViewById(R.id.mod_img);
            prod_img.setImageBitmap(image);
            //비트맵으로 이미지 출력
        }
        else if(get_image_arr == null){
            ImageView prod_img = (ImageView) findViewById(R.id.mod_img);
            prod_img.setImageResource(R.drawable.image);
        }

        //=================사진클릭========================
        imageView.setOnClickListener(new View.OnClickListener() {   // 이미지뷰 눌렀을 때 갤러리 열기
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);        // 다른앱에서 파일 검색할 수 있게 하기
                startActivityForResult(intent, IMAGE_REQUEST_CODE);       // 갤러리에 요청 코드 보내기
            }
        });
        //=================사진클릭========================

        //=================취소=========================
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "수정 취소", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //=================취소========================
        //=================수정완료========================
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Name = prod_name.getText().toString();
                String Price =  prod_price.getText().toString();
                String Link = prod_link.getText().toString();
                String Note = prod_note.getText().toString();

                Intent intent1 = new Intent(ActivityModProd.this, ActivityCheckProd.class); //Mod -> Check
                //이름,가격,링크,메모,관심,날짜,이미지,포지션
                intent1.putExtra("name", Name);     // 키값은 name
                intent1.putExtra("position",Pos);
                intent1.putExtra("price", Price);
                intent1.putExtra("link", Link);
                intent1.putExtra("note", Note);
                intent1.putExtra("star",Star);
                intent1.putExtra("date",Date);
                if(change_img==1)   //이미지 변경 시
                    intent1.putExtra("image",change_image_arr);
                else
                    intent1.putExtra("image",get_image_arr);
                setResult( PROD_DATA_CHANGE ,intent1);
                //Toast.makeText(getApplicationContext(), "수정 완료", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //=================수정완료========================
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
                    change_image_arr = stream.toByteArray();

                    change_img = 1; //이미지 변경

                    imageView.setImageBitmap(bitmap);              // img 를 이미지뷰에 출력
                    Toast.makeText(getApplicationContext(), "사진 불러오기 성공", Toast.LENGTH_SHORT).show();   // 토스트 메시지 출력
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {             // 뒤로가기로 작업 취소한때
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
        //==========갤러리 동작=============
    }

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

}
