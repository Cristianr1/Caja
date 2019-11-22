package com.koiti.caja;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;
import java.util.ArrayList;

public class Tarifas {


    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();

    public Tarifas() {
    }

    public ArrayList<String> tfaCodigo() {

        Cursor cursor = db.query(
                "tb_tarifas",
                new String[]{"tfa_codigo"},
                null,
                null,
                null,
                null,
                null);

        ArrayList<String> tfacodigo = new ArrayList<>();

        while (cursor.moveToNext()) {
            tfacodigo.add(cursor.getString(cursor.getColumnIndex("tfa_codigo")));
        }

        cursor.close();

//        Collections.sort(tfacodigo);

        return tfacodigo;
    }

    public ArrayList<String> tfaNombre() {

        Cursor cursor = db.query(
                "tb_tarifas",
                new String[]{"tfa_nombre"},
                null,
                null,
                null,
                null,
                null);

        ArrayList<String> tfanombre = new ArrayList<>();

        while (cursor.moveToNext()) {
            tfanombre.add(cursor.getString(cursor.getColumnIndex("tfa_nombre")));
        }
        cursor.close();

        return tfanombre;
    }

    public int tfaAjuste(String tfa_codigo) {

        int ajuste = 0;

        Cursor cursor = db.query(
                "tb_tarifas",
                new String[]{"tfa_ajuste"},
                "tfa_codigo" + " LIKE ?",
                new String[]{tfa_codigo},
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            String tfa_ajuste = cursor.getString(cursor.getColumnIndex("tfa_ajuste"));
            ajuste = Integer.parseInt(tfa_ajuste);
        }
        cursor.close();

        return ajuste;
    }

    public String tdgLiquidacion(String tfa_codigo) {

        String tdg = "";

        Cursor cursor = db.query(
                "tb_tarifas",
                new String[]{"tfa_tiempo_gracia_liq", "tfa_tiempo_gracia_sal"},
                "tfa_codigo" + " LIKE ?",
                new String[]{tfa_codigo},
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            tdg = cursor.getString(cursor.getColumnIndex("tfa_tiempo_gracia_liq"));
        }
        cursor.close();

        return tdg;
    }

    public String tdgSalida(String tfa_codigo) {

        String tdg = "";

        Cursor cursor = db.query(
                "tb_tarifas",
                new String[]{"tfa_tiempo_gracia_sal"},
                "tfa_codigo" + " LIKE ?",
                new String[]{tfa_codigo},
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            tdg = cursor.getString(cursor.getColumnIndex("tfa_tiempo_gracia_sal"));
        }
        cursor.close();

        return tdg;
    }

}

