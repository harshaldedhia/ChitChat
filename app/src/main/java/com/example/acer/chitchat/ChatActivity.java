package com.example.acer.chitchat;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    Button btnSend;
    ListView read_msg_box;
    EditText writeMsg;

    boolean owner;
    InetAddress serverAddress;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    ChatArrayAdapter chatArrayAdapter;
    List<ChatMessage> chats;

    static final int MESSAGE_READ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        owner = intent.getBooleanExtra("Owner?", false);
        serverAddress = (InetAddress)intent.getSerializableExtra("Owner Address");
        btnSend = findViewById(R.id.sendButton);
        read_msg_box = findViewById(R.id.readMsg);
        writeMsg = findViewById(R.id.writeMsg);
        chats = new ArrayList<ChatMessage>();



        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), chats);

        if (owner) {
            serverClass = new ServerClass();
            serverClass.start();
        } else {
            serverAddress = (InetAddress)intent.getSerializableExtra("Owner Address");
            clientClass = new ClientClass(serverAddress);
            clientClass.start();
        }

        //read_msg_box.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        read_msg_box.setAdapter(chatArrayAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                /*String curr_text = read_msg_box.getText().toString();
                String temp_text = curr_text + "\n" + "Outgoing: " + msg;
                read_msg_box.setText(temp_text);*/
                chats.add(new ChatMessage(true, msg));
                chatArrayAdapter.notifyDataSetChanged();
                writeMsg.setText("");
                sendReceive.write(msg.getBytes());
            }
        });

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    /*String curr_text = read_msg_box.getText().toString();
                    String temp_text = curr_text + "\n" + "Incoming: " + tempMsg;
                    read_msg_box.setText(temp_text);*/
                    chats.add(new ChatMessage(false, tempMsg));
                    chatArrayAdapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    });

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void interrupt() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.interrupt();
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket = skt;
            try {
                inputStream = skt.getInputStream();
                outputStream = skt.getOutputStream();
            }catch(IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket != null){
                try {
                    bytes = inputStream.read(buffer);
                    if(bytes > 0){
                        handler.obtainMessage(MESSAGE_READ, bytes,-1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(final byte[] bytes){
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            outputStream.write(bytes);
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
        }
    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void interrupt() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.interrupt();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(owner){
            serverClass.interrupt();
        }
        else{
            clientClass.interrupt();
        }
        Intent i = new Intent();
        i.setClass(ChatActivity.this, MainActivity.class);
        startActivity(i);
    }
}
