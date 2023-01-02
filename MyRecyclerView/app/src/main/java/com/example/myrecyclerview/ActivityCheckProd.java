package com.example.myrecyclerview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityCheckProd  extends AppCompatActivity {
    //==================req, res 코드=============================
    public static final int PROD_DATA_CHANGE = 108; //상품 수정
    public static final int PROD_DATA_DELETE = 105; //상품 삭제

    String Name;    //이름
    String Price;   //가격
    String Link;    //링크
    String Note;    //메모
    String Date, Date_p;    //날짜, 출력 날짜
    byte[] image_arr;   //이미지
    int Star;      //관심
    int Pos; //위치값

    Button ok_btn, modify_btn,delete_btn;  //확인버튼,수정,삭제
    Context context;
    //푸시알림 관련
    private NotificationHelper notificationHelper;
    private Button btn_alarm;
    RadioGroup rPush;
    RadioButton sec10,day7,day14;
    int push_msec;
    int push_ctr;
    String push_msg;

    //날짜 관련(푸시알림)
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyMMdd");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PROD_DATA_CHANGE) {   //수정완료 시 지나침 Mod -> Check -> Main
            //이름,가격,링크,메모,관심,날짜,이미지,포지션
            Name = data.getStringExtra("name");
            Price =data.getStringExtra("price");
            Link = data.getStringExtra("link");
            Note = data.getStringExtra("note");
            image_arr = data.getByteArrayExtra("image");
            Pos = data.getIntExtra("position",0);
            Date = data.getStringExtra("date");
            Star = data.getIntExtra("star",0);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);   //Check -> Main
            //이름,가격,링크,메모,관심,날짜,이미지,포지션
            intent.putExtra("name", Name);     // 키값은 name
            intent.putExtra("price", Price);
            intent.putExtra("link", Link);
            intent.putExtra("note", Note);
            intent.putExtra("position",Pos);
            intent.putExtra("star",Star);
            intent.putExtra("image",image_arr);
            intent.putExtra("date",Date);
            setResult(PROD_DATA_CHANGE,intent);

            finish();
        }
    }


    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prod_activity_check);
        //====어플리케이션 이름 숨기기=====
        getSupportActionBar().hide();
        //====어플리케이션 이름 숨기기=====

        context = this;
        //텍스트 작업
        TextView prod_name = (TextView) findViewById(R.id.prod_name);
        TextView prod_price = (TextView) findViewById(R.id.prod_price);
        TextView prod_link = (TextView) findViewById(R.id.prod_link);
        TextView prod_note = (TextView) findViewById(R.id.prod_note);
        TextView prod_date = (TextView) findViewById(R.id.prod_date);
        Intent intent2 = getIntent();
        //이름,가격,링크,메모,관심,날짜,이미지,포지션
        Pos = intent2.getIntExtra("position",0);
        Name = intent2.getStringExtra("name");
        Price = intent2.getStringExtra("price");
        Link = intent2.getStringExtra("link");
        Note = intent2.getStringExtra("note");
        Date = intent2.getStringExtra("date");
        //yyMMdd 형태
        Date_p = "20"+Date.substring(0,2)+"."+Date.substring(2,4)+"."+Date.substring(4,Date.length());
        //20yy.MM.dd 형태로 출력(print)

        //===========푸시알림===================================
        rPush = (RadioGroup) findViewById(R.id.rPush);
        sec10 = (RadioButton) findViewById(R.id.sec10);
        day7 = (RadioButton) findViewById(R.id.day7);
        day14 = (RadioButton) findViewById(R.id.day14);
        String today = getTime();   //현재 시간
        btn_alarm = findViewById(R.id.btn_alarm);
        notificationHelper = new NotificationHelper(this);
        String date_alarm = Date.substring(2,4)+"월"+Date.substring(4,Date.length())+"일"; //MM월dd일 형태
        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (rPush.getCheckedRadioButtonId()) {
                    case R.id.sec10:
                        push_msec = 10000;
                        push_msg = "10초";
                        push_ctr = 0;
                        break;
                    case R.id.day7:
                        push_msec = 604800000;     //7일 1000(1초)*60(1분)*60(1시)*24(1일)*7(7일)
                        push_msg = "7일";
                        push_ctr = 0;
                        break;
                    case R.id.day14:
                        push_msec = 1209600000;
                        push_msg = "14일";
                        push_ctr = 0;
                        break;
                    default:
                        push_msec = -1;
                        Toast.makeText(getApplicationContext(), "푸시 알림 시간을 선택해주세요", Toast.LENGTH_SHORT).show();
                }
                if (push_msec != -1 && push_ctr!=1) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendOnChannel1(
                                    Name + "구매하셨나요?",
                                    "상품 담은 " + date_alarm + "로 부터 " + 7 + "일 지났습니다!!");
                        }
                    }, push_msec);    //라디오버튼만큼 delay
                    push_ctr = 1;   //같은 창에서 여러번 클릭 시에도 한번만 울리게 함
                    Toast.makeText(getApplicationContext(), push_msg + " 뒤에 알림이 갑니다", Toast.LENGTH_LONG).show();
                }
            }
        });
        //===========푸시알림===================================

        prod_name.setText(Name);                                             // 이름 출력
        prod_price.setText(Price);                                             // 가격 출력
        prod_link.setText(Link);                                             // 링크 출력
        prod_note.setText(Note);                                             // 텍스트 출력
        prod_date.setText(Date_p);                                            //날짜 출력
        //관심도 작업
        ImageView prod_star = (ImageView) findViewById(R.id.prod_star);
        Star = intent2.getIntExtra("star",0);
        if(Star == 1){          //관심 출력
            prod_star.setImageResource(R.drawable.full_star);
        }else
            prod_star.setImageResource(R.drawable.empty_star);

        //이미지 작업
        byte[] arr = intent2.getByteArrayExtra("image");    //비트맵이미지 받기
        if(arr != null) {
            Bitmap image = BitmapFactory.decodeByteArray(arr, 0, arr.length);  //바이트배열 -> 비트맵
            ImageView prod_img = (ImageView) findViewById(R.id.prod_img);
            prod_img.setImageBitmap(image); //비트맵으로 이미지 출력
        }
        else if(arr == null){
            ImageView prod_img = (ImageView) findViewById(R.id.prod_img);
            prod_img.setImageResource(R.drawable.image);
        }

        //확인버튼 동작
        ok_btn = findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //=============수정=============
        modify_btn = findViewById(R.id.btn_modify);
        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityModProd.class);  //Check -> Mod
                //이름,가격,메모,링크,이미지,관심,날짜,포지션
                intent.putExtra("position",Pos);
                intent.putExtra("name",Name);
                intent.putExtra("price",Price);
                intent.putExtra("note",Note);
                intent.putExtra("link",Link);
                intent.putExtra("date",Date);
                intent.putExtra("star",Star);
                intent.putExtra("image",arr);
                startActivityForResult(intent, PROD_DATA_CHANGE);
            }
        });
        //=============수정=============

        //===========삭제==============
        delete_btn = findViewById(R.id.btn_delete);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityCheckProd.this, MainActivity.class); //Check -> Main
                intent.putExtra("position",Pos);
                setResult(PROD_DATA_DELETE,intent);
                finish();
            }
        });
    }   //===========삭제==============

    //푸시 알림 설정
    public void sendOnChannel1(String title, String message){
        NotificationCompat.Builder nb = notificationHelper.getChannel1Notification(title, message);
        notificationHelper.getManager().notify(1, nb.build());
    }
    //현재 시간
    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}




