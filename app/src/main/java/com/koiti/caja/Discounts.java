package com.koiti.caja;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.util.Map;
import java.util.TreeMap;


/**
 * Tiempo = 0
 * Porcentaje = 1
 */
class Discounts {
    private Map<Integer, int[]> mapDiscountsValues = new TreeMap<>();
    private Map<Integer, String> mapDiscountsNames = new TreeMap<>();

    int[] getArrayDiscountValue(int key) {
        return mapDiscountsValues.get(key);
    }

    String getArrayDiscountName(int key) {
        return mapDiscountsNames.get(key);
    }

    Discounts() {
        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
        String com_codigo, com_tipo_descuento, com_valor_descuento, com_nombre;
        int type;

        Cursor cursor = db.query(
                "tb_comercios",
                new String[]{"com_codigo", "com_nombre", "com_tipo_descuento", "com_valor_descuento"},
                null,
                null,
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            com_codigo = cursor.getString(cursor.getColumnIndex("com_codigo"));
            com_nombre = cursor.getString(cursor.getColumnIndex("com_nombre"));
            com_tipo_descuento = cursor.getString(cursor.getColumnIndex("com_tipo_descuento"));
            com_valor_descuento = cursor.getString(cursor.getColumnIndex("com_valor_descuento"));

            type = com_tipo_descuento.equals("Porcentaje") ? 1 : 0;

            mapDiscountsValues.put(Integer.parseInt(com_codigo),
                    new int[]{type, Integer.parseInt(com_valor_descuento)});

            mapDiscountsNames.put(Integer.parseInt(com_codigo), com_nombre);
        }

        cursor.close();
    }
}
