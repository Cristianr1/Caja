package com.koiti.caja;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.text.SimpleDateFormat;
import es.dmoral.toasty.Toasty;


public class Login extends AppCompatActivity {

    private EditText user, password;
    private Button login, settings;
    private Context context;
    private Sesiones sesiones = new Sesiones();
    private ConfigStorage config = new ConfigStorage();
    private Intent intent;

    ConfigStorage configuracion = new ConfigStorage();

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
    private String startSession, usuario, estacion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        Toasty.Config.getInstance().setTextSize(24).apply();

        user = findViewById(R.id.userId);
        password = findViewById(R.id.passId);

        login = findViewById(R.id.loginId);
        settings = findViewById(R.id.config_Id);

        login.setOnClickListener(mListener);
        settings.setOnClickListener(mListener);

        boolean sesion = config.getValueBoolean("sesion", context);

        if (sesion) {
            intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSession = date.format(new Date());
                someHandler.postDelayed(this, 1000);
            }
        }, 10);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loginId:
                    Map<String, String> mapUsuarios = sesiones.usuarios();

                    if (Objects.equals(mapUsuarios.get(String.valueOf(user.getText())),
                            String.valueOf(password.getText()))) {
                        config.save(true, "sesion", context);
                        usuario = String.valueOf(user.getText());
                        estacion = configuracion.getValueString("estacion", context);
                        config.save(usuario, "usuario", context);
                        addData();
                        intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toasty.error(Login.this, "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.config_Id:
                    intent = new Intent(context, Password.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @SuppressLint("Recycle")
    public void addData() {
        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getWritableDatabase();
        ContentValues registerInsert = new ContentValues();

//        Cursor cursor = db.rawQuery("SELECT changes()", null); //Evaluar si existen cambios en la DB

        Cursor cursor = db.rawQuery("SELECT EXISTS(SELECT *FROM tb_sesiones WHERE ses_usuario = " + quote(usuario) + " AND ses_fh_final IS NULL)", null);
        cursor.move(1);

        if (cursor.getString(0).equals("0")) {
            registerInsert.put("ses_usuario", usuario);
            registerInsert.put("ses_estacion", estacion);
            registerInsert.put("ses_fh_inicio", startSession);
            registerInsert.put("ses_fe_inicio", startSession);
            registerInsert.put("ses_automatico", "N");
            db.insert("tb_sesiones", null, registerInsert);
        }
        cursor.close();
    }

    public static String quote(String s) {
        return new StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        user.setText("");
        password.setText("");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
