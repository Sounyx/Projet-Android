package com.sounyx.projetandroid;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.sounyx.projetandroid.database.ScoreDatabaseHelper;
import java.util.ArrayList;

public class HighscoreActivity extends AppCompatActivity {

    private ListView lvHighscores;
    private ScoreDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        lvHighscores = findViewById(R.id.lv_highscores);
        dbHelper = new ScoreDatabaseHelper(this);

        loadHighscores();
    }

    private void loadHighscores() {
        Cursor cursor = dbHelper.getTopScores();
        ArrayList<String> scoreList = new ArrayList<>();

        if (cursor != null) {
            int indexName = cursor.getColumnIndex(ScoreDatabaseHelper.COLUMN_NAME);
            int indexScore = cursor.getColumnIndex(ScoreDatabaseHelper.COLUMN_SCORE);

            int rank = 1;
            while (cursor.moveToNext()) {
                String name = (indexName >= 0) ? cursor.getString(indexName) : "Unknown";
                int score = (indexScore >= 0) ? cursor.getInt(indexScore) : 0;
                
                String rankDisplay;
                if (rank == 1) {
                    rankDisplay = "🥇 ";
                } else if (rank == 2) {
                    rankDisplay = "🥈 ";
                } else if (rank == 3) {
                    rankDisplay = "🥉 ";
                } else {
                    rankDisplay = rank + ". ";
                }
                
                scoreList.add(rankDisplay + name + " : " + score + " pts");
                rank++;
            }
            cursor.close();
        }

        if (scoreList.isEmpty()) {
            scoreList.add(getString(R.string.no_scores_msg));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                scoreList
        );
        lvHighscores.setAdapter(adapter);
    }
}
