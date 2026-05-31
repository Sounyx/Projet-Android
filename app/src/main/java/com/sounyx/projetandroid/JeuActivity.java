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
import java.util.Random;

public class JeuActivity extends AppCompatActivity {

    private TextView tvLives;
    private TextView tvScore;
    private TextView tvOperation;
    private EditText etAnswer;
    private Button btnSubmit;

    private int score = 0;
    private int lives = 3;
    private int correctResult = 0;
    private String currentOperationText = "";
    private Random random = new Random();
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

        generateOperation();
        updateUI();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }

    private void generateOperation() {
        int operatorType = random.nextInt(4); // 0: +, 1: -, 2: *, 3: /
        int a, b;

        switch (operatorType) {
            case 0: // Addition
                a = random.nextInt(50) + 1; // 1 to 50
                b = random.nextInt(50) + 1; // 1 to 50
                correctResult = a + b;
                currentOperationText = a + " + " + b + " = ?";
                break;
            case 1: // Subtraction
                a = random.nextInt(50) + 1; // 1 to 50
                b = random.nextInt(50) + 1; // 1 to 50
                // Ensure positive results
                if (a < b) {
                    int temp = a;
                    a = b;
                    b = temp;
                }
                correctResult = a - b;
                currentOperationText = a + " - " + b + " = ?";
                break;
            case 2: // Multiplication
                a = random.nextInt(10) + 1; // 1 to 10
                b = random.nextInt(10) + 1; // 1 to 10
                correctResult = a * b;
                currentOperationText = a + " × " + b + " = ?";
                break;
            case 3: // Integer Division
            default:
                correctResult = random.nextInt(10) + 1; // 1 to 10 (Result)
                b = random.nextInt(10) + 1; // 1 to 10 (Divisor)
                a = correctResult * b; // Dividend
                currentOperationText = a + " ÷ " + b + " = ?";
                break;
        }
    }

    private void updateUI() {
        tvLives.setText(getString(R.string.lives_label, lives));
        tvScore.setText(getString(R.string.score_label, score));
        tvOperation.setText(currentOperationText);
    }

    private void checkAnswer() {
        String answerStr = etAnswer.getText().toString().trim();
        if (answerStr.isEmpty()) {
            return;
        }

        try {
            int userAnswer = Integer.parseInt(answerStr);
            boolean correct = (userAnswer == correctResult);

            if (correct) {
                score += 10;
                Toast.makeText(this, R.string.correct_msg, Toast.LENGTH_SHORT).show();
            } else {
                lives--;
                Toast.makeText(this, R.string.wrong_msg, Toast.LENGTH_SHORT).show();
            }

            etAnswer.setText("");

            if (lives <= 0) {
                updateUI(); // Show 0 lives
                gameOver();
            } else {
                generateOperation();
                updateUI();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Entrée invalide", Toast.LENGTH_SHORT).show();
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
