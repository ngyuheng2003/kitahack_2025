package com.example.kitahack2025;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;
import com.google.firebase.vertexai.type.GenerationConfig;
import com.google.firebase.vertexai.type.HarmCategory;
import com.google.firebase.vertexai.type.SafetySetting;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBotActivity extends AppCompatActivity {

    ArrayList<ChatModel> chatList;
    RecyclerView recyclerView;
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_bot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton sendButt = findViewById(R.id.sendButt);
        EditText userInput = findViewById(R.id.chatInput);
        sendButt.setOnClickListener(v->{
            inputChat(userInput.getText().toString().trim(),ChatModel.SENT_BY_USER);
            userInput.setText("");
        });
        chatList = new ArrayList<>();
        recyclerView = findViewById(R.id.myRecyclerView);
        adapter = new ChatAdapter(chatList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void inputChat(String message,String sender){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatList.add(new ChatModel(message,sender));
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
                if(sender.equals(ChatModel.SENT_BY_USER)){
                    callAPI(message);
                }
            }
        });
    }

    private void addResponse(String response){
        chatList.remove(chatList.size()-1);
        inputChat(response,ChatModel.SENT_BY_AI);
    }

    //
    private void callAPI(String question) {
        chatList.add(new ChatModel("Typing......",ChatModel.SENT_BY_AI));

        GenerativeModel gm = FirebaseVertexAI.getInstance()
                .generativeModel("gemini-2.0-flash");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("Answer in 50 words:" + question)
                .build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        addResponse(resultText);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        addResponse(t.getMessage());
                    }
                },
                executor);
    }
}