package com.koiti.caja;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Sesiones {

    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
    private String usuario;

    public Sesiones() {
    }

    public Sesiones(String usuario) {
        this.usuario = usuario;
    }

    public Map<String, String> usuarios() {
        @SuppressLint("UseSparseArrays") Map<String, String> mapUsuarios = new HashMap<>();
        String user, password;

        Cursor c = db.query(
                "tb_usuarios",
                new String[]{"usu_codigo", "usu_password"},
                null,
                null,
                null,
                null,
                null);

        while (c.moveToNext()) {
            user = c.getString(c.getColumnIndex("usu_codigo"));
            password = c.getString(c.getColumnIndex("usu_password"));

            user = user.replaceAll("\\s", "");

            mapUsuarios.put(user, password);
        }

        c.close();

        return mapUsuarios;
    }

    public String fhInicioSesion() {

        String fecha = null;

        Cursor c = db.query(
                "tb_sesiones",
                new String[]{"ses_fh_inicio"},
                "ses_usuario" + " LIKE ?",
                new String[]{usuario},
                null,
                null,
                null);

        while (c.moveToNext()) {
            if (c.isLast())
                fecha = c.getString(c.getColumnIndex("ses_fh_inicio"));
        }
        c.close();

        return fecha;
    }
}