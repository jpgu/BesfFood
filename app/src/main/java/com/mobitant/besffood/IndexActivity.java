package com.mobitant.besffood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobitant.besffood.item.MemberInfoItem;
import com.mobitant.besffood.lib.EtcLib;
import com.mobitant.besffood.lib.GeoLib;
import com.mobitant.besffood.lib.MyLog;
import com.mobitant.besffood.lib.RemoteLib;
import com.mobitant.besffood.lib.StringLib;
import com.mobitant.besffood.remote.RemoteService;
import com.mobitant.besffood.remote.ServiceGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



/**
 * Created by jp on 2019-03-04.
 */
public class IndexActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        context = this;

        if(!RemoteLib.getInstance().isConnected(context)){
            showNoService();
            return;
        }
    }

    /* 일정 시간(1.2초) 이후에 startTask() 메소드를 호출해서 서버에서 사용자 정보를 조회한다. */
    @Override
    protected void onStart(){
        super.onStart();

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run(){
                startTask();
            }
        }, 1200);
    }

    /* 현재 인터넷에 접속할 수 없기 때문에 서비스 사용 불가 메시지와 화면 종료 버튼을 보여준다. */
    private void showNoService(){
        TextView messageText = (TextView)findViewById(R.id.message);
        messageText.setVisibility(View.VISIBLE);

        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);
    }

    /* 현재 폰의 전화번호와 동일한 사용자 정보를 조회할 수 있도록 selectmemberInfo() 메소드를 호출 */
    public void startTask(){
        String phone = EtcLib.getInstance().getPhoneNumber(this);

        selectMemberInfo(phone);
        GeoLib.getInstance().setLastKnownLocation(this);
    }

    /* 레트로핏을 활용해서 서버로부터 사용자 정보를 조회 */
    public void selectMemberInfo(String phone){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<MemberInfoItem> call = remoteService.selectMemberInfo(phone);
        call.enqueue(new Callback<MemberInfoItem>() {
            @Override
            public void onResponse(Call<MemberInfoItem> call, Response<MemberInfoItem> response) {
                MemberInfoItem item = response.body();

                if (response.isSuccessful() && !StringLib.getInstance().isBlank(item.name)) {
                    MyLog.d(TAG, "success " + response.body().toString());
                    setMemberInfoItem(item);
                } else {
                    MyLog.d(TAG, "not success");
                    goProfileActivity(item);
                }
            }

            @Override
            public void onFailure(Call<MemberInfoItem> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    /* 전달받은 MemberInfoItem 을 Application 객체에 저장한다. */
    private void setMemberInfoItem(MemberInfoItem item){
        ((MyApp)getApplicationContext()).setMemberInfoItem(item);
        startMain();
    }

    /* MainActivity 를 실행하고 현재 액티비티를 종료  */

    public void startMain(){
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void goProfileActivity(MemberInfoItem item){
        if(item == null || item.seq <= 0){
            insertMemberphone();
        }

        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        Intent intent2 = new Intent(this, ProfileAcivity.class);
        startActivity(intent2);

        finish();
    }

    /* 폰의 전화번호를 서버에 저장 */
    private void insertMemberPhone(){

        String phone = EtcLib.getInstance().getPhoneNumber(context);
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertMemberPhone(phone);
        call.enqueue(new Callback<String>) {

            @Override
            public void onResponse(Call<String> call, Response<String> response){
                if(response.isSuccessful()){
                    MyLog.d(TAG, "success insert id " + response.body().toString());
                }
                else{
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    MyLog.d(TAG, " fail " + statusCode + errorBody.toString());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t){
                MyLog.d(TAG, "no internet connectivity");
            }
        });
    }
}




}
