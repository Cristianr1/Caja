package com.koiti.caja;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class CalSubsegmentos {

    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CalSubsegmentos() {

    }

    public int[] getEntrada(String codigo, String tsg_codigo, int difMinutos) {

        String bloquesRepetir, bloquesValor, bloquesCodigo, bloquesTiempo;
        int resultado = 0;
        int acumulador = 0;
        int[] datos = new int[3];
        Map<Integer, int[]> mapBloques = new TreeMap<>();

        Cursor c = db.query(
                "tb_tarifa_subsegmentos",
                null,
                "tss_tfa_codigo" + " LIKE ?" + " AND " + "tss_tsg_codigo" + " LIKE ?",
                new String[]{codigo, tsg_codigo},
                null,
                null,
                null);

        while (c.moveToNext()) {
            bloquesRepetir = c.getString(c.getColumnIndex("tss_repetir"));
            bloquesValor = c.getString(c.getColumnIndex("tss_valor"));
            bloquesCodigo = c.getString(c.getColumnIndex("tss_codigo"));
            bloquesTiempo = c.getString(c.getColumnIndex("tss_tiempo"));

            try {
                Date tiempo = formato.parse(bloquesTiempo);
                mapBloques.put(Integer.parseInt(bloquesCodigo),
                        new int[]{Integer.parseInt(bloquesRepetir),
                                (int) Double.parseDouble(bloquesValor),
                                tiempo.getHours() * 60 + tiempo.getMinutes()});
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        c.close();
        c.close();

        Iterator<Integer> iterador = mapBloques.keySet().iterator();
        while (iterador.hasNext()) {
            Integer key = iterador.next();
            datos = mapBloques.get(key);
            if (datos != null) {
                acumulador += datos[0];
                if (difMinutos % acumulador != difMinutos) {
                    resultado += datos[0] * datos[1];
                } else {
                    resultado += difMinutos * datos[1];
                    break;
                }
            }
        }

        assert datos != null;

        return new int[]{resultado, datos[2]};
    }

    public int getDiaCompleto(String codigo, ArrayList<String> tsgCodigo) {

        String bloquesRepetir, bloquesValor;
        int resultado = 0;

        for (int i = 0; i < tsgCodigo.size(); i++) {
            Cursor c = db.query(
                    "tb_tarifa_subsegmentos",
                    null,
                    "tss_tfa_codigo" + " LIKE ?" + " AND " + "tss_tsg_codigo" + " LIKE ?",
                    new String[]{codigo, tsgCodigo.get(i)},
                    null,
                    null,
                    null);

            while (c.moveToNext()) {
                bloquesRepetir = c.getString(c.getColumnIndex("tss_repetir"));
                bloquesValor = c.getString(c.getColumnIndex("tss_valor"));

                resultado += Integer.parseInt(bloquesRepetir) * ((int) Double.parseDouble(bloquesValor));
            }
            c.close();
        }
        return resultado;
    }
}
