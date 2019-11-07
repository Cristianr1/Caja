package com.koiti.caja;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;

public class Password extends AppCompatActivity {

    private Context context;

    private EditText password;
    private Button btnSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toasty.Config.getInstance().setTextSize(24).apply();

        addListenerOnButton();
    }

    public void addListenerOnButton() {

        password = findViewById(R.id.txtPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String pass = password.getText().toString();

                Intent intent = null;
                switch (pass) {
                    case "B800530*":
                        intent = new Intent(context, Configuracion.class);
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        incorrect();
                        break;
                }
            }

        });
    }

    public void incorrect() {
        Toasty.error(Password.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
    }
}
