package com.example.tictactoe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/** The clean duplicate route prepared for the Firebase RTDB lessons. */
public class Main2Activity extends BaseGameActivity {
    private DatabaseReference gameRef;
    private ValueEventListener gameListener;

    @Override
    protected String gameModeLabel() {
        return "RTDB game · synced via 'tictactoe'";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameRef = FirebaseDatabase.getInstance("https://android-practise-d0b1c-default-rtdb.firebaseio.com/").getReference("tictactoe");
        findViewById(R.id.button_new_game).setOnClickListener(view -> resetFirebaseState());
        
        setupFirebaseListener();
    }

    private void setupFirebaseListener() {
        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    resetFirebaseState();
                    return;
                }

                String firebasePlayer = snapshot.child("currentPlayer").getValue(String.class);
                Boolean firebaseFinished = snapshot.child("gameFinished").getValue(Boolean.class);

                model.resetGame();
                for (int id : BUTTON_IDS) {
                    Button button = findViewById(id);
                    String[] position = button.getTag().toString().split(",");
                    int r = Integer.parseInt(position[0]);
                    int c = Integer.parseInt(position[1]);

                    String cellVal = snapshot.child("board").child(r + "_" + c).getValue(String.class);
                    if (cellVal != null && !cellVal.isEmpty()) {
                        model.setMove(r, c, cellVal);
                        button.setText(cellVal);
                        button.setTextColor(ContextCompat.getColor(Main2Activity.this,
                                "X".equals(cellVal) ? R.color.x_mark : R.color.o_mark));
                    } else {
                        button.setText("");
                    }
                }

                if (firebasePlayer != null) {
                    while (!model.getCurrentPlayer().equals(firebasePlayer)) {
                        model.changePlayer();
                    }
                }

                if (firebaseFinished != null) {
                    gameFinished = firebaseFinished;
                }

                if (model.checkWin()) {
                    gameFinished = true;
                    // Find out who won by checking the board marks
                    String winner = findWinner();
                    gameStatus.setText("Player " + winner + " wins!");
                } else if (model.isTie()) {
                    gameFinished = true;
                    gameStatus.setText("It is a tie!");
                } else {
                    updateStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Main2Activity.this, "Sync error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        gameRef.addValueEventListener(gameListener);
    }

    private String findWinner() {
        if (checkLine(0, 0, 0, 1, 0, 2)) return model.getCell(0, 0);
        if (checkLine(1, 0, 1, 1, 1, 2)) return model.getCell(1, 0);
        if (checkLine(2, 0, 2, 1, 2, 2)) return model.getCell(2, 0);
        if (checkLine(0, 0, 1, 0, 2, 0)) return model.getCell(0, 0);
        if (checkLine(0, 1, 1, 1, 2, 1)) return model.getCell(0, 1);
        if (checkLine(0, 2, 1, 2, 2, 2)) return model.getCell(0, 2);
        if (checkLine(0, 0, 1, 1, 2, 2)) return model.getCell(0, 0);
        if (checkLine(0, 2, 1, 1, 2, 0)) return model.getCell(0, 2);
        return "";
    }

    private boolean checkLine(int r1, int c1, int r2, int c2, int r3, int c3) {
        String first = model.getCell(r1, c1);
        return !first.isEmpty() && first.equals(model.getCell(r2, c2)) && first.equals(model.getCell(r3, c3));
    }

    @Override
    public void onCellClick(View view) {
        if (gameFinished) {
            return;
        }

        Button button = (Button) view;
        String[] position = button.getTag().toString().split(",");
        int row = Integer.parseInt(position[0]);
        int col = Integer.parseInt(position[1]);
        if (!model.isLegal(row, col)) {
            return;
        }

        String player = model.getCurrentPlayer();
        gameRef.child("board").child(row + "_" + col).setValue(player);

        // Update model to check for next state
        model.makeMove(row, col);
        if (model.checkWin() || model.isTie()) {
            gameRef.child("gameFinished").setValue(true);
        } else {
            model.changePlayer();
            gameRef.child("currentPlayer").setValue(model.getCurrentPlayer());
        }
    }

    private void resetFirebaseState() {
        gameRef.child("currentPlayer").setValue("X");
        gameRef.child("gameFinished").setValue(false);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                gameRef.child("board").child(r + "_" + c).setValue("");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameRef != null && gameListener != null) {
            gameRef.removeEventListener(gameListener);
        }
    }
}
