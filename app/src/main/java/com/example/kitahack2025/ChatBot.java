package com.example.kitahack2025;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBot {


    public static void PromptChat(){
        Executor executor = Executors.newSingleThreadExecutor();
        // Initialize the Vertex AI service and create a `GenerativeModel` instance
        // Specify a model that supports your use case
        GenerativeModel gm = FirebaseVertexAI.getInstance()
                .generativeModel("gemini-2.0-flash");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Provide a prompt that contains text
        Content prompt = new Content.Builder()
                .addText("Write a story about a magic backpack.")
                .build();

        // To generate text output, call generateContent with the text input
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                System.out.println(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);
    }

}
