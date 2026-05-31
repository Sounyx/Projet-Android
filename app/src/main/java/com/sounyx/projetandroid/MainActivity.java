package com.sounyx.projetandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.os.LocaleListCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.btn_play);
        Button btnHighscore = findViewById(R.id.btn_highscore);
        Button btnLanguage = findViewById(R.id.btn_language);

        // Update language button text based on current locale
        String currentLang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        if (currentLang.startsWith("en")) {
            btnLanguage.setText("🇬🇧 EN ▾");
        } else {
            btnLanguage.setText("🇫🇷 FR ▾");
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, JeuActivity.class);
                startActivity(intent);
            }
        });

        btnHighscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HighscoreActivity.class);
                startActivity(intent);
            }
        });

        btnLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageMenu(v);
            }
        });

        Button btnAbout = findViewById(R.id.btn_about);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.btn_close, null)
                .show();
    }

    private void showLanguageMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 1, 0, "Français 🇫🇷");
        popup.getMenu().add(0, 2, 1, "English 🇬🇧");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) {
                    setLocale("fr");
                    return true;
                } else if (item.getItemId() == 2) {
                    setLocale("en");
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void setLocale(String langCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}
