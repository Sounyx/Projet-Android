package com.sounyx.projetandroid;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.sounyx.projetandroid.database.ScoreDatabaseHelper;
import java.util.Random;

public class JeuActivity extends AppCompatActivity {

    private static final int TIMER_SECONDS = 10;
    private static final int MAX_LEVEL = 10000; // Drawable level max

    private TextView tvLives;
    private TextView tvScore;
    private TextView tvOperation;
    private EditText etAnswer;
    private Button btnSubmit;
    private TextView tvFeedback;
    private TextView tvTimer;
    private TextView tvCombo;
    private ImageView timerRing;

    private int score = 0;
    private int lives = 3;
    private int correctResult = 0;
    private int comboStreak = 0;  // number of consecutive correct answers
    private String currentOperationText = "";
    private Random random = new Random();
    private ScoreDatabaseHelper dbHelper;

    private CountDownTimer countDownTimer;
    private ObjectAnimator ringAnimator;
    private boolean gameEnded = false;

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
        tvFeedback = findViewById(R.id.tv_feedback);
        tvTimer = findViewById(R.id.tv_timer);
        timerRing = findViewById(R.id.timer_ring);
        tvCombo = findViewById(R.id.tv_combo);

        generateOperation();
        updateUI();
        startTimer();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });

        // Hide feedback text when the user starts typing their next answer
        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && tvFeedback.getVisibility() == View.VISIBLE) {
                    tvFeedback.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void startTimer() {
        // Cancel any existing timer
        cancelTimer();

        // Reset ring to full
        setRingLevel(MAX_LEVEL, false);
        tvTimer.setText(String.valueOf(TIMER_SECONDS));
        updateTimerColor(TIMER_SECONDS);

        countDownTimer = new CountDownTimer(TIMER_SECONDS * 1000L, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                tvTimer.setText(String.valueOf(secondsLeft));
                updateTimerColor(secondsLeft);

                // Update ring level proportional to time remaining
                int level = (int) ((millisUntilFinished / (TIMER_SECONDS * 1000.0)) * MAX_LEVEL);
                setRingLevel(level, false);
            }

            @Override
            public void onFinish() {
                setRingLevel(0, false);
                tvTimer.setText("0");
                updateTimerColor(0);
                onTimeExpired();
            }
        };
        countDownTimer.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (ringAnimator != null) {
            ringAnimator.cancel();
            ringAnimator = null;
        }
    }

    private void setRingLevel(int level, boolean animate) {
        Drawable drawable = timerRing.getDrawable();
        if (drawable instanceof RotateDrawable) {
            RotateDrawable rotateDrawable = (RotateDrawable) drawable;
            if (animate) {
                ringAnimator = ObjectAnimator.ofInt(rotateDrawable, "level", rotateDrawable.getLevel(), level);
                ringAnimator.setDuration(100);
                ringAnimator.setInterpolator(new LinearInterpolator());
                ringAnimator.start();
            } else {
                rotateDrawable.setLevel(level);
            }
        }
    }

    private void updateTimerColor(int secondsLeft) {
        int color;
        if (secondsLeft > 5) {
            color = getColor(R.color.accent_color); // Blue/Indigo - plenty of time
        } else if (secondsLeft > 2) {
            color = getColor(R.color.warning); // Orange - getting low
        } else {
            color = getColor(R.color.danger); // Red - urgent
        }
        tvTimer.setTextColor(color);

        // Also tint the ring
        timerRing.setColorFilter(color);
    }

    private void onTimeExpired() {
        if (gameEnded) return;
        comboStreak = 0;  // Reset combo on timeout
        updateComboUI();
        lives--;
        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText("⏰ Temps écoulé !");
        tvFeedback.setTextColor(getColor(R.color.danger));

        etAnswer.setText("");

        if (lives <= 0) {
            updateUI();
            gameOver();
        } else {
            generateOperation();
            updateUI();
            startTimer();
        }
    }

    /** Returns the points multiplier based on the current combo streak */
    private int getMultiplier() {
        if (comboStreak >= 7) return 4;
        if (comboStreak >= 5) return 3;
        if (comboStreak >= 3) return 2;
        return 1;
    }

    /** Show or hide the combo badge based on current streak */
    private void updateComboUI() {
        if (comboStreak >= 3) {
            int multiplier = getMultiplier();
            tvCombo.setText(getString(R.string.combo_label, multiplier));
            if (tvCombo.getVisibility() != View.VISIBLE) {
                tvCombo.setVisibility(View.VISIBLE);
                // Pop-in animation
                tvCombo.setScaleX(0.5f);
                tvCombo.setScaleY(0.5f);
                tvCombo.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            } else {
                // Pulse animation on combo increase
                tvCombo.animate()
                        .scaleX(1.15f)
                        .scaleY(1.15f)
                        .setDuration(100)
                        .withEndAction(() -> tvCombo.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();
            }
        } else {
            tvCombo.setVisibility(View.GONE);
        }
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
        tvLives.setText(getString(R.string.lives_label) + " " + getHeartsString(lives));
        tvScore.setText(getString(R.string.score_label, score));
        tvOperation.setText(currentOperationText);
    }

    private String getHeartsString(int currentLives) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < currentLives) {
                sb.append("❤️ ");
            } else {
                sb.append("🤍 ");
            }
        }
        return sb.toString().trim();
    }

    private void checkAnswer() {
        String answerStr = etAnswer.getText().toString().trim();
        if (answerStr.isEmpty()) {
            return;
        }

        try {
            int userAnswer = Integer.parseInt(answerStr);
            boolean correct = (userAnswer == correctResult);

            // Stop the current timer
            cancelTimer();

            tvFeedback.setVisibility(View.VISIBLE);
            if (correct) {
                comboStreak++;
                int multiplier = getMultiplier();
                int points = 10 * multiplier;
                score += points;
                updateComboUI();
                if (multiplier > 1) {
                    tvFeedback.setText(getString(R.string.correct_msg) + " (×" + multiplier + " = +" + points + " pts)");
                } else {
                    tvFeedback.setText(R.string.correct_msg);
                }
                tvFeedback.setTextColor(getColor(R.color.success));
            } else {
                comboStreak = 0;
                updateComboUI();
                lives--;
                tvFeedback.setText(R.string.wrong_msg);
                tvFeedback.setTextColor(getColor(R.color.danger));
            }

            etAnswer.setText("");

            if (lives <= 0) {
                updateUI(); // Show 0 lives
                gameOver();
            } else {
                generateOperation();
                updateUI();
                startTimer(); // Start a fresh timer for the new question
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Entrée invalide", Toast.LENGTH_SHORT).show();
        }
    }

    private void gameOver() {
        gameEnded = true;
        cancelTimer();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gameEnded) {
            startTimer();
        }
    }
}
