package com.example.busappver11;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SubActivity extends AppCompatActivity {


    private ListView mySubListView;
    private ListOfBusLocationsByRouteParser parser;
    private ArrayList<BusItems> list;
    private ListOfBusLocationsByRouteViewModelAdapter adapter;
    private String cityCode,routeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //객체 검색
        mySubListView = (ListView) findViewById(R.id.mySubListView);

        //리스트뷰의 오버스크롤 제거
        mySubListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        parser=new ListOfBusLocationsByRouteParser();

        Intent i=getIntent();
        cityCode=i.getExtras().getString("cityCode");
        routeId=i.getExtras().getString("routeId");

        if(cityCode!=null && routeId!=null){

            list = new ArrayList<BusItems>();

            adapter = null;

            //BusAsync 실행
            new BusAsync2().execute();
        }
    }

    //AsyncTask
    // - Thread개념
    // - BackGround작업을 심플하게 할수 있도록 만들어주는 클래스
    // - 앱이 실행되면 안드로이드 시스템은 메인 쓰레드를 생성한다.
    // - 백그라운드 쓰레드(UI쓰레드)와 메인쓰레드와의 커뮤니케이션을 위해 사용된다
    // - 백그라운드 쓰레드에서 작업 종료 후 결과를 메인쓰레드에 통보하거나 중간에 UI쓰레드 처리 요청 등을 쉽게 할수 있도록 만들어졌다.

    // - 객체가 처음 생성되면 최초 호출 메소드 - onPreExecute가 실행된다.
    // - onPreExecute - 백그라운드 작업을 수행하기 전에 필요한 초기화 작업등을 담당
    // - 두번째로 호출되는 메소드: doInBackground가 실행된다.
    // - doInBackground - 각종 반복이나 제어 등 주된 처리 로직을 담당한다.
    // - 세번째로 호출되는 메소드 doPostExecute가 실행된다.
    //  - 최종적으로 처리되는 메소드

    //AsyncTask클래스에 들어가는 세가지의 제너릭 타입
    //첫번째는 doInBackground의 파라미터로 전달될 값의 형태
    //두번쨰는 UI진행 상태를 관리하는 onProgressUpdate가 오버라이딩 되어 있는 경우 이 메소드에서 사용할 자료형을 지정
    //세번쨰는 Sync 스레드의 작업 결과를 반영하는 doPostExcute로 전달될 객체

    class BusAsync2 extends AsyncTask<String, String, ArrayList<BusItems>> {

        @Override
        protected ArrayList<BusItems> doInBackground(String... strings) {
            return parser.ListOfBusLocationsByRouteApi(cityCode,routeId,list);
        }

        @Override
        protected void onPostExecute(ArrayList<BusItems> BusItems) {
            //doInBackground에서 통신을 마친 결과가 onPostExcute의 Items 넘어온다. 리스트뷰의 화면을 갱신

            if (adapter == null) {
                adapter = new ListOfBusLocationsByRouteViewModelAdapter(SubActivity.this,R.layout.each_bus_item, BusItems, mySubListView);



                //리스트 뷰에 adapter 세팅
                mySubListView.setAdapter(adapter);

            }
            //리스트뷰의 변경사항 갱신
            adapter.notifyDataSetChanged();
        }
    }
}

