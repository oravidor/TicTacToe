package com.example.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The lesson's LoginActivity shell. It deliberately stays Firebase-free so the project can run
 * before a class Firebase project and matching google-services.json are supplied.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private CheckBox rememberMe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.eTemail);
        rememberMe = findViewById(R.id.cBstayconnect);

        if (SessionStore.shouldRestore(this)) {
            openMenu();
        }
    }

    public void onLoginClick(View view) {
        String userName = email.getText() == null ? "" : email.getText().toString().trim();
        if (userName.isEmpty()) {
            email.setError(getString(R.string.email_hint));
            email.requestFocus();
            return;
        }
        SessionStore.signIn(this, userName, rememberMe.isChecked());
        openMenu();
    }

    public void onGoogleLoginClick(View view) {
        Toast.makeText(this,
                "Google Sign-In needs a Firebase project and matching google-services.json.",
                Toast.LENGTH_LONG).show();
    }

    private void openMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
