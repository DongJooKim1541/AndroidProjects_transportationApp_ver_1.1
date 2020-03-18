package com.example.busappver11;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private ListView myListView;
    public static EditText cityName,busNum;
    private Button searchBtn;
    private CityCodeListParser cParser;
    private RouteNumberListSearchParser rParser;
    private ArrayList<BusItems> list,cityList;
    private RouteNumberListSearchViewModelAdapter adapter;
    public static String cityCode=null;

    int pageNo=1;   //검색을 시작할 page 번호
    ProgressDialog dialog_progress; //로딩을 위한 다이얼로그

    //스크롤링을 통한 추가 로드를 위해 필요한 변수
    LayoutInflater mInflater;
    View footerView;
    boolean mLockListView=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //다이얼로그 객체 준비
        dialog_progress = new ProgressDialog(this);
        dialog_progress.setMessage("Loading.....");

        cityName = (EditText) findViewById(R.id.cityName);    //도시명
        busNum = (EditText) findViewById(R.id.busNum);    //노선 번호

        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //한글자 이상 입력받은 경우에만 검색
                if (cityName.getText().toString().trim().length() > 0 && busNum.getText().toString().trim().length() > 0) {

                    pageNo=1;

                    //객체 검색
                    myListView = (ListView) findViewById(R.id.myListView);

                    //리스트뷰의 오버스크롤 제거
                    myListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

                    cityList=new ArrayList<BusItems>();
                    list = new ArrayList<BusItems>();

                    adapter = null;

                    //android.util.Log.d("KDJ","--------cityAsync 실행--------");
                    new cityAsync().execute();

                }
            }
        });
        cParser=new CityCodeListParser();
        rParser = new RouteNumberListSearchParser();
        //로딩을 표시하기 위해 미리 만들어둔 footer를 등록하기 위한 준비
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerView = mInflater.inflate(R.layout.footer, null);
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

    class cityAsync extends AsyncTask<String, String, ArrayList<BusItems>>{
        @Override
        protected ArrayList<BusItems> doInBackground(String... strings) {
            return cParser.cityCodeListApi(cityList);
        }

        @Override
        protected void onPostExecute(ArrayList<BusItems> busItems) {
            //doInBackground에서 통신을 마친 결과가 onPostExcute의 Items 넘어온다.
            for(int i=0;i<busItems.size();i++){
                if(busItems.get(i).getCityName().trim().equalsIgnoreCase(cityName.getText().toString().trim())){
                    cityCode=busItems.get(i).getCityCode().trim();
                    dialog_progress.show(); //로딩 시작
                    //BusAsync 실행
                    //android.util.Log.d("KDJ","--------BusAsync 실행--------");
                    new BusAsync().execute();
                }
            }
            if(cityCode==null){
                Toast.makeText(getApplicationContext(), "검색한 도시가 없습니다.", Toast.LENGTH_SHORT).show();
                myListView.removeFooterView(footerView);
                dialog_progress.dismiss();
                return;
            }
        }
    }

    class BusAsync extends AsyncTask<String, String, ArrayList<BusItems>> {

        @Override
        protected ArrayList<BusItems> doInBackground(String... strings) {
            return rParser.connectRouteNumberListSearchApi(list,pageNo);
        }

        @Override
        protected void onPostExecute(ArrayList<BusItems> BusItems) {
            //doInBackground에서 통신을 마친 결과가 onPostExcute의 Items 넘어온다. 리스트뷰의 화면을 갱신

            if (adapter == null) {
                adapter = new RouteNumberListSearchViewModelAdapter(MainActivity.this,R.layout.all_bus_item, BusItems, myListView);

                //리스트 뷰에 스크롤 리스너 등록
                myListView.setOnScrollListener(scrollListener);

                //리스트 뷰에 footer등록
                myListView.addFooterView(footerView);

                //리스트 뷰에 adapter 세팅
                myListView.setAdapter(adapter);

            }

            if (BusItems.size() == 0) {
                Toast.makeText(getApplicationContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                //footer 제거
                myListView.removeFooterView(footerView);
                dialog_progress.dismiss();
                return;
            }

            //리스트뷰의 변경사항 갱신
            adapter.notifyDataSetChanged();
            mLockListView = false;
            dialog_progress.dismiss();

            watchListOfBusByRoute();
        }
    }

    //리스트 뷰의 스크롤 감시자
    AbsListView.OnScrollListener scrollListener=new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //현재 리스트뷰의 상태를 알려주는 메소드
            //scrollState - 상태값은 총 3가지

            //SCROLL_STATE_FLING (2)
            // - 터치 후 손을 뗀 상태에서 아직 스크롤 되고 있는 상태

            //SCROLL_STATE_IDLE (0)
            // - 스크롤이 종료 되어 어떠한 애니메이션도 발생하지 않는 상태

            //SCROLL_STATE_TOUCH_SCROLL (1)
            // - 스크린에 터치를 한 상태에서 스크롤하는 상태
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //스크롤이 발생하는 동안 지속적으로 호출되는 메소드
            //firstVisibleItem - 현재 보여지는 리스트뷰의 최상단 아이템 인덱스
            //visibleItemCount - 현재 보여지는 리스트뷰의 아이템 개수
            //totalItemCount - 현재 리스트뷰의 총 아이템 개수

            //현재 보여지는 처음보이는 항목의 인덱스 값고 아이템 카운트를 더했을 때 totalItemCount와 같으면 가장 아래로 스크롤 되었다.
            int count=totalItemCount-visibleItemCount;

            if(firstVisibleItem>=count && totalItemCount!=0 && mLockListView==false){
                mLockListView=true;
                android.util.Log.d("KDJ","Integer.parseInt(rParser.getBi().getTotalCount()): "+Integer.parseInt(rParser.getBi().getTotalCount()));
                if(pageNo*10 < Integer.parseInt(rParser.getBi().getTotalCount()) && list.size()>=10){

                    footerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pageNo+=1;
                            new BusAsync().execute();
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"더 불러올 내용이 없습니다.",Toast.LENGTH_SHORT).show();
                    myListView.removeFooterView(footerView);
                }
            }
        }
    };

    private void watchListOfBusByRoute(){

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BusItems busItems=(BusItems)adapter.getItem(position);
                Toast.makeText(getApplicationContext(), busItems.getCityCode()+", "+busItems.getRouteId(), Toast.LENGTH_SHORT).show();

                Intent i=new Intent(getApplicationContext(),SubActivity.class);

                i.putExtra("cityCode",busItems.getCityCode());
                i.putExtra("routeId",busItems.getRouteId());

                startActivity(i);

            }
        });
    }
}
