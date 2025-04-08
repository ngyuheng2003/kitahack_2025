package com.example.kitahack2025;


import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.*;
import com.google.gson.*;

import java.io.IOException;
import java.util.List;

import okhttp3.*;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    private static final String GEMINI_API_KEY = "YOUR_API_KEY";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;


    FirebaseFirestore db;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        db = FirebaseFirestore.getInstance();
        client = new OkHttpClient();

        analyzeFeedbacks();
    }

    private void analyzeFeedbacks() {
        db.collection("feedback")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<DocumentSnapshot> documents = querySnapshots.getDocuments();
                    for (DocumentSnapshot doc : documents) {
                        String feedbackText = doc.getString("text");
                        if (feedbackText != null && !feedbackText.trim().isEmpty()) {
                            analyzeWithGemini(doc.getReference(), feedbackText);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching feedbacks", e));
    }

    private void analyzeWithGemini(DocumentReference docRef, String feedbackText) {
        String prompt = "Analyze the sentiment of this user feedback and return only one word: Positive, Neutral, or Negative.\n\nFeedback: \"" + feedbackText + "\"";

        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);

        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", parts);

        JsonArray contentArray = new JsonArray();
        contentArray.add(contentObj);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentArray);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Gemini API call failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Log the error code and the error body to get more details about the failure
                    Log.e(TAG, "Gemini API error: " + response.code());
                    Log.e(TAG, "Error body: " + response.body().string());  // log the actual error response body
                    return;
                }

                // Read and parse the successful response
                String responseStr = response.body().string();
                JsonObject resJson = JsonParser.parseString(responseStr).getAsJsonObject();

                try {
                    // Parse the sentiment
                    final String sentiment = resJson
                            .getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString()
                            .trim()
                            .replaceAll("[^a-zA-Z]", ""); // inline cleanup

                    // Update Firestore with the sentiment result
                    docRef.update("sentiment", sentiment)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated sentiment: " + sentiment))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update Firestore", e));

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing Gemini response", e);
                }
            }

        });
    }
}

