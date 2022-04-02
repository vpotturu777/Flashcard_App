package com.example.flashcardapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView flashcardQuestion;
    TextView flashcardAnswer;

    FlashcardDatabase flashcardDatabase;
    List<Flashcard> allFlashcards;
    int currentCardDisplayedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashcardQuestion = findViewById(R.id.flashcard_question_textview);
        flashcardAnswer = findViewById(R.id.flashcard_answer_textview);

        flashcardDatabase = new FlashcardDatabase(getApplicationContext());
        allFlashcards = flashcardDatabase.getAllCards();

        if (allFlashcards != null && allFlashcards.size() > 0) {
            ((TextView) findViewById(R.id.flashcard_question_textview)).setText(allFlashcards.get(0).getQuestion());
            ((TextView) findViewById(R.id.flashcard_answer_textview)).setText(allFlashcards.get(0).getAnswer());
        }

        findViewById(R.id.flashcard_next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (allFlashcards.size() == 0)
                    return;

                currentCardDisplayedIndex++;

                if(currentCardDisplayedIndex >= allFlashcards.size()) {
                    Snackbar.make(v,
                            "You've reached the end of the cards, going back to start.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                    currentCardDisplayedIndex = 0;
                }

                allFlashcards = flashcardDatabase.getAllCards();
                Flashcard flashcard = allFlashcards.get(currentCardDisplayedIndex);

                flashcardQuestion.setText(flashcard.getQuestion());
                flashcardQuestion.setText(flashcard.getAnswer());

                flashcardQuestion.setVisibility(View.VISIBLE);
                flashcardAnswer.setVisibility(View.INVISIBLE);

                final Animation leftOutAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.left_out);
                final Animation rightInAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.right_in);

                leftOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        findViewById(R.id.flashcard_question_textview).startAnimation(rightInAnim);
                        Flashcard flashcard = allFlashcards.get(currentCardDisplayedIndex);

                        flashcardQuestion.setText(flashcard.getQuestion());
                        flashcardQuestion.setText(flashcard.getAnswer());

                        flashcardQuestion.setVisibility(View.VISIBLE);
                        flashcardAnswer.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // we don't need to worry about this method
                    }
                });

                flashcardQuestion.startAnimation(leftOutAnim);
            }
        });

        flashcardQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) findViewById(R.id.flashcard_question_textview)).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.flashcard_answer_textview)).setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "I CLICKED THIS!", Toast.LENGTH_LONG).show();

                // get the center for the clipping circle
                int cx = flashcardAnswer.getWidth() / 2;
                int cy = flashcardAnswer.getHeight() / 2;

                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);

                // create the animator for this view (the start radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(flashcardAnswer, cx, cy, 0f, finalRadius);

                // hide the question and show the answer to prepare for playing the animation!
                flashcardQuestion.setVisibility(View.INVISIBLE);
                flashcardAnswer.setVisibility(View.VISIBLE);

                anim.setDuration(3000);
                anim.start();
            }
        });

        findViewById(R.id.flashcard_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddCardActivity.class);
                MainActivity.this.startActivityForResult(intent, 100);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String question = data.getExtras().getString("question");
            String answer = data.getExtras().getString("answer");
            flashcardQuestion.setText(question);
            flashcardAnswer.setText(answer);

            flashcardDatabase.insertCard(new Flashcard(question, answer));
            allFlashcards = flashcardDatabase.getAllCards();
        }
    }
}