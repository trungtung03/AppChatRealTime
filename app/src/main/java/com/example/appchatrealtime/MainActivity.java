package com.example.appchatrealtime;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ListView user, chat;
    EditText content;
    ImageButton add, send;

    Socket mSocket;

    List<String> listUser, listChat;
    ArrayAdapter adapterUser, adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        anhxa();

        try {
            mSocket = IO.socket("http://192.168.1.5:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();

        mSocket.on("tham-gia-chat", thamGia);

        mSocket.on("server-send-user", onRetriveUser);

        mSocket.on("server-send-result", onRetriveResult);

        mSocket.on("server-send-chat", onRetriveChat);

        listUser = new ArrayList<>();
        adapterUser = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, listUser);
        user.setAdapter(adapterUser);

        listChat = new ArrayList<>();
        adapterChat = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, listChat);
        chat.setAdapter(adapterChat);

        add.setOnClickListener(this);
        send.setOnClickListener(this);

    }

    private void anhxa() {
        user = findViewById(R.id.listUser);
        chat = findViewById(R.id.listChat);
        content = findViewById(R.id.edtContent);
        add = findViewById(R.id.imgAdd);
        send = findViewById(R.id.imgSend);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgAdd:
                add();
                break;
            case R.id.imgSend:
                send();
                break;
        }
    }

    public void add() {
        if (TextUtils.isEmpty(content.getText().toString().trim())) {
            Toast.makeText(MainActivity.this, "chưa nhập đủ thông tin", Toast.LENGTH_SHORT).show();
        } else {
            mSocket.emit("client-register-user", content.getText().toString().trim());
        }
    }

    public void send() {
        if (TextUtils.isEmpty(content.getText().toString().trim())) {
            Toast.makeText(MainActivity.this, "chưa nhập đủ thông tin", Toast.LENGTH_SHORT).show();
        } else {
            mSocket.emit("client-send-chat", content.getText().toString().trim());
        }
    }

    Emitter.Listener onRetriveUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("danhsach");
                        listUser.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            listUser.add(jsonArray.getString(i));
                            name = jsonArray.getString(i);
                        }
                        adapterUser.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    String name;
    Emitter.Listener onRetriveResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        boolean isCheck = jsonObject.getBoolean("ketqua");
                        if (isCheck) {
                            Toast.makeText(MainActivity.this, "tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "đăng ký thành công -> " + name, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    Emitter.Listener onRetriveChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        listChat.add(jsonObject.getString("chatContent"));
                        adapterChat.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    Emitter.Listener thamGia = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        Toast.makeText(MainActivity.this, jsonObject.getString("new"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}