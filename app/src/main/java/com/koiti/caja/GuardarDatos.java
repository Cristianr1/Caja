package com.koiti.caja;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class GuardarDatos {
    private String vehId, fechaEntrada, tfaCodigo, tfaNombre, facturaFHEmision, facturaEmision, facturaNumero, usuario,
            fechaInicioSesion, estacion, prefijo, type, subtotal, impuesto, encabezado, piepagina, liqTipo, veh_tipo, nameDiscount;
    private float total, ajuste;
    private int entregado, cambio, codeDiscount;

    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getWritableDatabase();


    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat fe = new SimpleDateFormat("yyyy-MM-dd");

    //constructor Facturas
    GuardarDatos(String vehId, String fechaEntrada, String tfaCodigo, String tfaNombre, String facturaNumero, String facturaFHEmision, String facturaEmision,
                 String usuario, String estacion, String fechaInicioSesion, String prefijo, float total, float subtotal, float impuesto,
                 int entregado, int cambio, String type, String encabezado, String piepagina, String liqTipo, String veh_tipo, int codeDiscount, String nameDiscount) {
        this.vehId = vehId;
        this.fechaEntrada = fechaEntrada;
        this.tfaCodigo = tfaCodigo;
        this.tfaNombre = tfaNombre;
        this.facturaNumero = facturaNumero;
        this.facturaFHEmision = facturaFHEmision;
        this.facturaEmision = facturaEmision;
        this.usuario = usuario;
        this.estacion = estacion;
        this.fechaInicioSesion = fechaInicioSesion;
        this.prefijo = prefijo;
        this.total = total;
        this.subtotal = new DecimalFormat("##.##").format(subtotal);
        this.impuesto = new DecimalFormat("##.##").format(impuesto);
        this.entregado = entregado;
        this.cambio = cambio;
        this.type = type;
        this.ajuste = 0;
        this.encabezado = encabezado;
        this.piepagina = piepagina;
        this.liqTipo = liqTipo;
        this.veh_tipo = veh_tipo;
        this.codeDiscount = codeDiscount;
        this.nameDiscount = nameDiscount;
    }

    @SuppressLint("DefaultLocale")
    public void tbFacturas() {
        ContentValues registerInsert = new ContentValues();
        registerInsert.put("fac_numero", facturaNumero);
        registerInsert.put("fac_ses_usuario", usuario);
        registerInsert.put("fac_ses_estacion", estacion);
        registerInsert.put("fac_ses_fh_inicio", fechaInicioSesion);
        registerInsert.put("fac_fh_emision", facturaFHEmision);
        registerInsert.put("fac_fe_emision", facturaEmision);
        registerInsert.put("fac_prefijo", prefijo);
        registerInsert.put("fac_total_bruto", total);
        registerInsert.put("fac_subtotal", subtotal);
        registerInsert.put("fac_impuestos", impuesto);
        registerInsert.put("fac_total", total);
        registerInsert.put("fac_ajuste", ajuste);
        db.insert("tb_facturas", null, registerInsert);
    }

    @SuppressLint("DefaultLocale")
    public void tbFacturaDetalle() {
        ContentValues registerInsert = new ContentValues();
        registerInsert.put("fdt_fac_numero", facturaNumero);
        registerInsert.put("fdt_pro_codigo", "PARQ");
        registerInsert.put("fdt_pro_descripcion", "Parqueadero");
        registerInsert.put("fdt_cantidad", 1);
        registerInsert.put("fdt_fac_prefijo", prefijo);
        registerInsert.put("fdt_subtotal", subtotal);
        registerInsert.put("fdt_impuestos", impuesto);
        registerInsert.put("fdt_total", total);
        registerInsert.put("fdt_bruto", total);
//        registerInsert.put("fdt_bruto", String.format("%,.2f", total)); //formato ej. 1.200,00
        db.insert("tb_factura_detalle", null, registerInsert);
    }

    @SuppressLint("DefaultLocale")
    public void tbFacturaImpuestos() {
        ContentValues registerInsert = new ContentValues();
        registerInsert.put("fim_fac_numero", facturaNumero);
        registerInsert.put("fim_imp_codigo", "I01");
        registerInsert.put("fim_imp_nombre", "IVA");
        registerInsert.put("fim_fdt_pro_codigo", "PARQ");
        registerInsert.put("fim_fac_prefijo", prefijo);
        registerInsert.put("fim_imp_porcentaje", 19);
        registerInsert.put("fim_valor", impuesto);
        db.insert("tb_factura_impuestos", null, registerInsert);
    }

    @SuppressLint("DefaultLocale")
    public void tbLiquidaciones() {
        ContentValues registerInsert = new ContentValues();
        registerInsert.put("liq_veh_id", vehId);
        registerInsert.put("liq_veh_fh_entrada", fechaEntrada);
        registerInsert.put("liq_fh_liquidacion", facturaFHEmision);
        registerInsert.put("liq_ses_usuario", usuario);
        registerInsert.put("liq_ses_estacion", estacion);
        registerInsert.put("liq_ses_fh_inicio", fechaInicioSesion);
        registerInsert.put("liq_fh_desde", fechaEntrada);
        registerInsert.put("liq_fh_hasta", facturaFHEmision);
        registerInsert.put("liq_tfa_codigo", tfaCodigo);
        registerInsert.put("liq_tfa_nombre", tfaNombre);
        registerInsert.put("liq_fac_numero", facturaNumero);
        registerInsert.put("liq_tipo", liqTipo);
        registerInsert.put("liq_fe_liquidacion", facturaEmision);
        registerInsert.put("liq_fac_prefijo", prefijo);
        registerInsert.put("liq_valor_bruto", total);
        db.insert("tb_liquidaciones", null, registerInsert);
    }

    public void tbVehiculos() {
        ContentValues register = new ContentValues();
        register.put("veh_id", vehId);
        register.put("veh_tipo_id", "Tarjeta");
        register.put("veh_fh_entrada", fechaEntrada);
        register.put("veh_estacion", estacion);
        register.put("veh_usuario", "SISTEMA");
        register.put("veh_tipo", veh_tipo);
        register.put("veh_robado", "N");
        register.put("veh_purgado", "N");
        register.put("veh_dir_entrada", "1");
        register.put("veh_fe_entrada", fechaEntrada);
        register.put("veh_clase", type);
        register.put("veh_rotacion", "N");
        db.insert("tb_vehiculos", null, register);
    }

    @SuppressLint("DefaultLocale")
    public void tbCaja() {
        ContentValues registerInsert = new ContentValues();
        registerInsert.put("caj_ses_usuario", usuario);
        registerInsert.put("caj_ses_estacion", estacion);
        registerInsert.put("caj_ses_fh_inicio", fechaInicioSesion);
        registerInsert.put("caj_fh_movimiento", facturaFHEmision);
        registerInsert.put("caj_tipo_movimiento", "P");
        registerInsert.put("caj_fac_numero", facturaNumero);
        registerInsert.put("caj_forma_pago", "Efectivo");
        registerInsert.put("caj_fe_movimiento", facturaEmision);
        registerInsert.put("caj_fh_movimiento", facturaFHEmision);
        registerInsert.put("caj_fac_prefijo", prefijo);
        registerInsert.put("caj_valor", total);
        registerInsert.put("caj_dinero_entregado", entregado);
        registerInsert.put("caj_cambio", cambio);
        db.insert("tb_caja", null, registerInsert);
    }

    public void tbLiqDescuentos() {
        ContentValues registerInsert = new ContentValues();
        registerInsert.put("lds_liq_veh_id", vehId);
        registerInsert.put("lds_liq_veh_fh_entrada", fechaEntrada);
        registerInsert.put("lds_liq_fh_liquidacion", facturaEmision);
        registerInsert.put("lds_com_codigo", codeDiscount);
        registerInsert.put("lds_com_nombre", nameDiscount);
        registerInsert.put("lds_valor", total);
        db.insert("tb_liq_descuentos", null, registerInsert);
    }
}
