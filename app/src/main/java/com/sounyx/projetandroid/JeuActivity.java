package com.sounyx.projetandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.sounyx.projetandroid.database.ScoreDatabaseHelper;

public class JeuActivity extends AppCompatActivity {

    private TextView tvLives;
    private TextView tvScore;
    private TextView tvOperation;
    private EditText etAnswer;
    private Button btnSubmit;

    private int score = 0;
    private int lives = 3;
    private ScoreDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        dbHelper = new ScoreDatabaseHelper(this);

        tvLives = findViewById(R.id.tv_lives);
        tvScore = findViewById(R.id.tv_score);
        tvOperation = findViewById(R.id.tv_operation);
        etAnswer = findViewById(R.id.et_answer);
        btnSubmit = findViewById(R.id.btn_submit);

        updateUI();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }

    private void updateUI() {
        tvLives.setText(getString(R.string.lives_label, lives));
        tvScore.setText(getString(R.string.score_label, score));
        // TODO: Generate operation
        tvOperation.setText("10 + 5 = ?");
    }

    private void checkAnswer() {
        String answerStr = etAnswer.getText().toString().trim();
        if (answerStr.isEmpty()) {
            return;
        }

        // Logic check placeholder
        boolean correct = true; // TODO: Implement validation

        if (correct) {
            score += 10;
            Toast.makeText(this, R.string.correct_msg, Toast.LENGTH_SHORT).show();
        } else {
            lives--;
            Toast.makeText(this, R.string.wrong_msg, Toast.LENGTH_SHORT).show();
        }

        etAnswer.setText("");
        updateUI();

        if (lives <= 0) {
            gameOver();
        }
    }

    private void gameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_over_title);
        builder.setMessage(getString(R.string.game_over_msg, score));

        final EditText etName = new EditText(this);
        etName.setHint(R.string.name_hint);
        builder.setView(etName);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                name = "Joueur";
            }
            dbHelper.insertScore(name, score);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }
}
