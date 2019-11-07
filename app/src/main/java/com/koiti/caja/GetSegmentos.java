package com.koiti.caja;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;


public class GetSegmentos {

    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
    private String codigo;

    public GetSegmentos(String codigo){
        this.codigo = codigo;
    }


    public int numSegmentos(){
        Cursor cursor = db.query(
                "tb_tarifa_segmentos",
                null,
                "tsg_tfa_codigo" + " LIKE ?",
                new String[]{codigo},
                null,
                null,
                null);

        int rows = cursor.getCount();

        cursor.close();

        return rows;
    }

    public int numSubSegmentos(){
        Cursor c = db.query(
                "tb_tarifa_subsegmentos",
                null,
                "tss_tfa_codigo" + " LIKE ?",
                new String[]{codigo},
                null,
                null,
                null);

        int rows = c.getCount();

        c.close();

        return rows;
    }

}
