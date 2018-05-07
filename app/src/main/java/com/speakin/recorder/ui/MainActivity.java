package com.speakin.recorder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.speakin.recorder.R;
import com.speakin.recorder.module.control.MasterControlManager;
import com.speakin.recorder.module.control.SlaveControlManager;
import com.speakin.recorder.utils.AnswerSignal;
import com.speakin.recorder.utils.Book;
import com.speakin.recorder.utils.IpUtil;
import com.speakin.recorder.utils.PullAnswerSignalParser;
import com.speakin.recorder.utils.PullBookParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static MainActivity instance = null;

    private TextView textView;
    private TextView textView2;

    private MasterControlManager masterControlManager;
    private SlaveControlManager slaveControlManager;
    private boolean isMaster = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        refreshIP();
        instance = this;
    }

    private void initData() {
        masterControlManager = new MasterControlManager();
        slaveControlManager = new SlaveControlManager();

        masterControlManager.setControlManagerCallback(new MasterControlManager.MasterControlManagerCallback() {
            @Override
            public void onServerError(Exception ex) {
                textView2.setText("server error: " + ex.getLocalizedMessage());
            }

            @Override
            public void onClientConnected(String clientSocket) {
                textView2.setText("client: " + clientSocket);
            }

            @Override
            public void onClientDisconnect(String clientSocket) {
                textView2.setText("disconnected: " + clientSocket);
            }

            @Override
            public void onMessageReceive(String clientSocket, String message) {
                textView2.setText("message: " + message);

                onMathActivityRightAnswerSignalReceived(message);
            }

            @Override
            public void onReceiveFile(String clientSocket, String filePath) {
                Toast.makeText(MainActivity.this, "receive file" + filePath, Toast.LENGTH_SHORT).show();
            }
        });

        slaveControlManager.setControlManagerCallback(new SlaveControlManager.SlaveControlManagerCallback() {
            @Override
            public void onFoundMaster(String masterIp, JSONObject masterInfo) {
                textView2.setText(masterIp + " " + masterInfo.toString());
            }

            @Override
            public void onConnectedMaster(String masterIp, Exception ex) {
                textView2.setText(masterIp + " connected");
            }

            @Override
            public void onDisconnectMaster(String masterIp, Exception ex) {
                textView2.setText(masterIp + " disconnected");
            }

            @Override
            public void onReceiveMessage(String message) {
                textView2.setText("message: " + message);
                onMathActivityMessageReceived(message);
            }
        });


    }

    private void initView() {
        findViewById(R.id.masterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                masterControlManager.start();
                isMaster = true;

            }
        });

        findViewById(R.id.slaveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slaveControlManager.start();
            }
        });

        textView = (TextView) findViewById(R.id.text1);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshIP();
            }
        });

        textView2 = (TextView) findViewById(R.id.text2);

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMaster) {
                    masterControlManager.send("Hello, I am server :" + System.currentTimeMillis());
                } else {
                    slaveControlManager.send("Hello, I am client :"  + System.currentTimeMillis());
                }
            }
        });
        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMaster) {
                    masterControlManager.stop();
                } else {
                    slaveControlManager.stop();
                }
            }
        });
        findViewById(R.id.sendeFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //slaveControlManager.sendFile("/storage/emulated/0/wakeupIn/record/detected_1505991013.wav");
//                LayoutInflater inflater = getLayoutInflater();
//                View tempview = inflater.inflate(R.layout.math_pk, (ViewGroup)findViewById(R.id.pkll));
//                setContentView(tempview);

                Intent intent = new Intent(getApplicationContext(), MathPkActivity.class);


                Bundle bundle = new Bundle();
                if(isMaster){
                    bundle.putInt("isMaster", 1);
                }else {
                    bundle.putInt("isMaster", 0);
                }
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }

    private void refreshIP() {
        String ip1 = IpUtil.getHostIP();

        textView.setText("本机IP: " + ip1 );
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        slaveControlManager.stop();
        masterControlManager.stop();
    }

    public void sendBook(Book book){
        try {
            PullBookParser parser = new PullBookParser();
            List<Book> books = new ArrayList<Book>();

            books.add(book);

            String strBook = parser.serialize(books);

            if (isMaster) {
                masterControlManager.send(strBook);
            } else {
                slaveControlManager.send(strBook);
            }

        } catch (Exception e) {
            Log.e("KenHong", e.getMessage());
        }
    }

    public void sendRightAnswerSignal(){

        Calendar CD = Calendar.getInstance();
        AnswerSignal answerSignal = new AnswerSignal();
        answerSignal.setHour(CD.get(Calendar.HOUR));
        answerSignal.setMinute(CD.get(Calendar.MINUTE));
        answerSignal.setSecond(CD.get(Calendar.SECOND));
        answerSignal.setMillisecond(CD.get(Calendar.MILLISECOND));

        try{
            PullAnswerSignalParser parser = new PullAnswerSignalParser();
            List<AnswerSignal> answerSignals = new ArrayList<AnswerSignal>();

            answerSignals.add(answerSignal);

            String strBook = parser.serialize(answerSignals);

            if (!isMaster) {
                slaveControlManager.send(strBook);
            }

        }catch (Exception e){
            Log.e("KenHong", e.getMessage());
        }


    }

    private void onMathActivityMessageReceived(String xmlStr){
        if(MathPkActivity.instance != null){
            PullBookParser parser = new PullBookParser();
            List<Book> books;

            try {
                books = parser.parse(xmlStr);
                for(Book book : books ){
                    MathPkActivity.instance.onBookReceived(book);
                }

            }catch (Exception e) {
                Log.e("KenHong", e.getMessage());
            }
        }
    }

    private void onMathActivityRightAnswerSignalReceived(String xmlStr){

        if(MathPkActivity.instance != null){
            PullAnswerSignalParser parser = new PullAnswerSignalParser();
            List<AnswerSignal> answerSignals;

            try {
                answerSignals = parser.parse(xmlStr);
                for(AnswerSignal answerSignal : answerSignals ){
                    MathPkActivity.instance.onRightAnswerSignalReceived(answerSignal);
                }

            }catch (Exception e) {
                Log.e("KenHong", e.getMessage());
            }
        }
    }
}
