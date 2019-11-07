package com.koiti.caja;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;
import org.jumpmind.symmetric.android.SymmetricService;
import org.jumpmind.symmetric.common.ParameterConstants;

import java.util.Objects;
import java.util.Properties;

public class DbCajaProvider extends ContentProvider {

    private final String REGISTRATION_URL = "http://192.168.1.11:31415/sync/publicadorcajero";
    private final String NODE_ID = "movil1";
    private final String NODE_GROUP = "nodos_mdb";

    public static final String VEHICULOS = "CREATE TABLE IF NOT EXISTS  " + "tb_vehiculos" +
            " (" + "veh_id" + " INT PRIMARY KEY, " + "veh_fh_entrada" + " VARCHAR(255) ," + "veh_tipo_id" + " VARCHAR(255) ,"
            + "veh_usuario" + " VARCHAR(255) ," + "veh_estacion" + " VARCHAR(255) ," + "veh_codigo" + " VARCHAR(225),"
            + "veh_tipo" + " VARCHAR(255) ," + "veh_fh_salida" + " VARCHAR(255) ," + "veh_robado" + " VARCHAR(255) ,"
            + "veh_fe_entrada" + " VARCHAR(255) ," + "veh_fe_salida" + " VARCHAR(255) ," + "veh_sitio_entrada" + " VARCHAR(255) ,"
            + "veh_sitio_salida" + " VARCHAR(255) ," + "veh_dir_entrada" + " VARCHAR(255) ," + "veh_dir_salida" + " VARCHAR(255) ,"
            + "veh_purgado" + " VARCHAR(255) ," + "veh_purga_ses_estacion" + " VARCHAR(255) ," + "veh_purga_ses_usuario" + " VARCHAR(255) ,"
            + "veh_purga_ses_fh_inicio" + " VARCHAR(255) ," + "veh_purga_fecha_hora" + " VARCHAR(255) ," + "veh_clase" + " VARCHAR(255) ,"
            + "veh_lote" + " VARCHAR(255) ," + "veh_placa" + " VARCHAR(255) ," + "veh_documento" + " VARCHAR(255) ,"
            + "veh_nombre" + " VARCHAR(255) ," + "veh_minutos" + " VARCHAR(255) ," + "veh_rotacion" + " VARCHAR(255));";

    public static final String IMPUESTOS = "CREATE TABLE IF NOT EXISTS  " + "tb_impuestos" +
            " (" + "imp_codigo" + " VARCHAR(255) , " + "imp_nombre" + " VARCHAR(255) ," + "imp_porcentaje" + " VARCHAR(255));";

    public static final String TARIFAS = "CREATE TABLE IF NOT EXISTS  " + "tb_tarifas" +
            " (" + "tfa_codigo" + " VARCHAR(255) , " + "tfa_nombre" + " VARCHAR(255) ," + "tfa_max_24horas" + " VARCHAR(255) , " + "tfa_valor_24horas" + " NUMERIC , " +
            "tfa_tiempo_gracia_liq" + " TIME , " + "tfa_tiempo_gracia_sal" + " TIME, " + "tfa_aplicar_impuesto" + " CHAR , " + "tfa_modo_impuesto" + " VARCHAR(255), " +
            "tfa_valor_perdida_tiq" + " NUMERIC, " + "tfa_automatica" + " VARCHAR(255), " + "tfa_automatica_desde" + " DATE, "
            + "tfa_max24horas_corte_medianoche" + " VARCHAR(255), " + "tfa_ajuste" + " NUMERIC, " + "tfa_bloqueo" + " VARCHAR(255), " + "tfa_local" + " VARCHAR(255), " +
            "tfa_gracia_salida" + " VARCHAR(255), " + "tfa_localx" + " VARCHAR(255), " + "tfa_gracia_segmento" + " VARCHAR(255), " + "tfa_reliquida_continuo" + " VARCHAR(255), " +
            "tfa_valor_mostrar" + " VARCHAR(255), " + "tfa_texto_mostrar" + " VARCHAR(255));";

    public static final String HORARIOS = "CREATE TABLE IF NOT EXISTS  " + "tb_horarios" +
            " (" + "hor_codigo" + " VARCHAR(255) , " + "hor_nombre" + " VARCHAR(255) ," + "hor_desde_1" + " VARCHAR(255), " + "hor_hasta_1" + " VARCHAR(255), "
            + "hor_desde_2" + " VARCHAR(255), " + "hor_hasta_2" + " VARCHAR(255));";

    public static final String COMERCIOS = "CREATE TABLE IF NOT EXISTS  " + "tb_comercios" +
            " (" + "comercio_codigo" + " VARCHAR(255) , " + "comercio_nombre" + " VARCHAR(255) ," + "com_direccion" + " VARCHAR(255) , " + "com_telefono" + " VARCHAR(255) , " +
            "com_responsable" + " VARCHAR(255), " + "com_tipo_descuento" + " VARCHAR(255), " + "com_bloqueo" + " VARCHAR(255), " + "com_valor_bloqueo" + " VARCHAR(255), " +
            "com_tarifa" + " VARCHAR(255), " + "com_suma" + " VARCHAR(255), " + "com_nocaja" + " VARCHAR(255), " + "com_tarifa_moto" + " VARCHAR(255), " + "com_tarifa_camion" +
            " VARCHAR(255), " + "com_tarifa_bicicleta" + " VARCHAR(255), " + "com_tarifa_tractomula" + " VARCHAR(255));";

    public static final String PRODUCTOS = "CREATE TABLE IF NOT EXISTS  " + "tb_productos" +
            " (" + "pro_codigo" + " VARCHAR(255) , " + "pro_descripcion" + " VARCHAR(255) ," + "pro_clase" + " VARCHAR(255), " + "pro_imp_codigo1" + " VARCHAR(255), "
            + "pro_imp_codigo2" + " VARCHAR(255), " + "pro_imp_codigo3" + " VARCHAR(255), " + "pro_valor_unitario" + " VARCHAR(255), " + "pro_modo_impuesto" + " VARCHAR(255), " +
            "pro_descuento" + " VARCHAR(255));";

    public static final String TARIFAS_IMPUESTOS = "CREATE TABLE IF NOT EXISTS  " + "tb_tarifa_impuestos" +
            " (" + "tim_tfa_codigo" + " VARCHAR(255) , " + "tim_imp_codigo" + " VARCHAR(255));";

    public static final String TARIFAS_SEGMENTOS = "CREATE TABLE IF NOT EXISTS  " + "tb_tarifa_segmentos" +
            " (" + "tsg_tfa_codigo" + " VARCHAR(255) , " + "tsg_codigo" + " VARCHAR(255) ," + "tsg_dia_desde" + " VARCHAR(255), " + "tsg_hora_desde" + " VARCHAR(255), "
            + "tsg_dia_hasta" + " VARCHAR(255), " + "tsg_hora_hasta" + " VARCHAR(255));";

    public static final String TARIFAS_SUBSEGMENTOS = "CREATE TABLE IF NOT EXISTS  " + "tb_tarifa_subsegmentos" +
            " (" + "tss_tfa_codigo" + " VARCHAR(255) , " + "tss_tsg_codigo" + " VARCHAR(255) , " + "tss_codigo" + "  VARCHAR(255) ," + "tss_tiempo" + " VARCHAR(255), " + "tss_repetir" +
            " VARCHAR(255), " + "tss_siguiente" + " VARCHAR(255), " + "tss_valor" + " VARCHAR(255));";

    public static final String HORARIOS_DETALLE = "CREATE TABLE IF NOT EXISTS  " + "tb_horarios_detalle" +
            " (" + "hod_hor_codigo" + " VARCHAR(255) , " + "hod_dia" + " VARCHAR(255) ," + "hod_desde_1" + " VARCHAR(255), " + "hod_hasta_1" + " VARCHAR(255), "
            + "hod_desde_2" + " VARCHAR(255), " + "hod_hasta_2" + " VARCHAR(255));";

    public static final String USUARIOS = "CREATE TABLE IF NOT EXISTS  " + "tb_usuarios" +
            " (" + "usu_codigo" + " VARCHAR(255) , " + "usu_password" + " VARCHAR(255) ," + "usu_nombre" + " VARCHAR(255), " + "usu_tipo" + " VARCHAR(255), "
            + "usu_bloqueo" + " VARCHAR(255), " + "usu_eliminado" + " VARCHAR(255));";

    public static final String SESIONES = "CREATE TABLE IF NOT EXISTS `tb_sesiones` (" +
            "  `ses_usuario` varchar(10)," +
            "  `ses_estacion` varchar(16)," +
            "  `ses_fh_inicio` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'," +
            "  `ses_fh_final` datetime DEFAULT NULL," +
            "  `ses_liq_canceladas` int(11) DEFAULT '0'," +
            "  `ses_barrera_entrada` int(11) DEFAULT '0'," +
            "  `ses_barrera_salida` int(11) DEFAULT '0'," +
            "  `ses_fe_inicio` date DEFAULT NULL," +
            "  `ses_dinero_en_caja` decimal(15,2) DEFAULT '0.00'," +
            "  `ses_dinero_reportado` decimal(15,2) DEFAULT '0.00'," +
            "  `ses_diferencia` double DEFAULT '0'," +
            "  `ses_numero` int(11) DEFAULT '0'," +
            "  `ses_automatico` char(1));";


    public static final String CAJA = "CREATE TABLE IF NOT EXISTS `tb_caja` (`caj_ses_usuario` varchar(10), `caj_ses_estacion` varchar(16), " +
            "`caj_ses_fh_inicio` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `caj_fh_movimiento` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'," +
            " `caj_tipo_movimiento` char(1), `caj_fac_numero` int(11) DEFAULT NULL, `caj_forma_pago` varchar(10), `caj_documento_pago` varchar(20)," +
            " `caj_fe_movimiento` date DEFAULT NULL, `caj_fac_prefijo` varchar(10) DEFAULT '', `caj_valor` decimal(15,2) DEFAULT NULL," +
            " `caj_dinero_entregado` decimal(15,2) DEFAULT NULL, `caj_cambio` decimal(15,2) DEFAULT NULL,`caj_aprobacion` varchar(10)," +
            "`caj_tarjeta` varchar(10),`caj_franquicia` varchar(10),`caj_cuotas` varchar(10),`caj_tipotarjeta` varchar(20));";
    
    public static final String FACTURA_DETALLE =  "CREATE TABLE IF NOT EXISTS `tb_factura_detalle` (`fdt_fac_numero` int(11) NOT NULL, " +
            "`fdt_pro_codigo` varchar(4), `fdt_pro_descripcion` varchar(20)," +
            "`fdt_cantidad` int(11) DEFAULT NULL,`fdt_fac_prefijo` varchar(10)," +
            "`fdt_subtotal` decimal(15,2) DEFAULT NULL, `fdt_impuestos` decimal(15,2) DEFAULT NULL, `fdt_total` decimal(15,2) DEFAULT NULL," +
            "`fdt_ajuste` decimal(15,2) DEFAULT NULL, `fdt_desde` datetime DEFAULT NULL, `fdt_hasta` datetime DEFAULT NULL, `fdt_bruto` decimal(15,1) DEFAULT '0.0');";

    public static final String FACTURAS = "CREATE TABLE IF NOT EXISTS `tb_facturas` (" +
            "  `fac_numero` int(11) NOT NULL," +
            "  `fac_ses_usuario` varchar(10)," +
            "  `fac_ses_estacion` varchar(16)," +
            "  `fac_ses_fh_inicio` datetime DEFAULT NULL," +
            "  `fac_fh_emision` datetime DEFAULT NULL," +
            "  `fac_fe_emision` date DEFAULT NULL," +
            "  `fac_prefijo` varchar(10)DEFAULT ''," +
            "  `fac_total_bruto` decimal(15,2) DEFAULT NULL," +
            "  `fac_descuentos` decimal(15,2) DEFAULT '0,00'," +
            "  `fac_subtotal` decimal(15,2) DEFAULT NULL," +
            "  `fac_impuestos` decimal(15,2) DEFAULT NULL," +
            "  `fac_total` decimal(15,2) DEFAULT NULL," +
            "  `fac_ajuste` decimal(15,2) DEFAULT NULL," +
            "  `fac_nombre` varchar(50)," +
            "  `fac_placa` varchar(10)," +
            "  `fac_documento` varchar(20)," +
            "  `fac_anulada` smallint(6) DEFAULT '0'," +
            "  `fac_fe_anulada` date DEFAULT NULL);";

    public static final String FACTURAIMPUESTOS = "CREATE TABLE IF NOT EXISTS `tb_factura_impuestos` (" +
            "  `fim_fac_numero` int(11) NOT NULL," +
            "  `fim_imp_codigo` varchar(4)," +
            "  `fim_imp_nombre` varchar(20)," +
            "  `fim_fdt_pro_codigo` varchar(4)," +
            "  `fim_fac_prefijo` varchar(10)," +
            "  `fim_imp_porcentaje` decimal(15,2) DEFAULT NULL," +
            "  `fim_valor` decimal(15,2) DEFAULT NULL);";

    public static final String LIQDESCUENTOS = "CREATE TABLE IF NOT EXISTS `tb_liq_descuentos` (" +
            "  `lds_liq_veh_id` varchar(10)," +
            "  `lds_liq_veh_fh_entrada` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'," +
            "  `lds_liq_fh_liquidacion` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'," +
            "  `lds_com_codigo` varchar(4)," +
            "  `lds_com_nombre` varchar(20)," +
            "  `lds_secuencia` int(11) DEFAULT NULL," +
            "  `lds_valor` decimal(15,2) DEFAULT NULL," +
            "  `lds_documento` varchar(30)," +
            "  `lds_tiempo` decimal(15,2) DEFAULT NULL);";

    public static final String LIQUIDACIONES = "CREATE TABLE IF NOT EXISTS `tb_liquidaciones` (" +
            "  `liq_veh_id` varchar(10)," +
            "  `liq_veh_fh_entrada` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'," +
            "  `liq_fh_liquidacion` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'," +
            "  `liq_ses_usuario` varchar(10)," +
            "  `liq_ses_estacion` varchar(16)," +
            "  `liq_ses_fh_inicio` datetime DEFAULT NULL," +
            "  `liq_fh_desde` datetime DEFAULT NULL," +
            "  `liq_fh_hasta` datetime DEFAULT NULL," +
            "  `liq_tfa_codigo` varchar(4)," +
            "  `liq_tfa_nombre` varchar(20)," +
            "  `liq_fac_numero` int(11) DEFAULT NULL," +
            "  `liq_tipo` varchar(15)," +
            "  `liq_en_contingencia` char(1)DEFAULT 'N'," +
            "  `liq_salida_contingencia` char(1)DEFAULT 'N'," +
            "  `liq_fe_liquidacion` date DEFAULT NULL," +
            "  `liq_fac_prefijo` varchar(10)," +
            "  `liq_tiempo_extra` int(11) DEFAULT '0'," +
            "  `liq_valor_bruto` decimal(15,2) DEFAULT NULL," +
            "  `liq_multa_perdido` decimal(15,2) DEFAULT '0.00'," +
            "  `liq_gracia_salida` int(11) DEFAULT NULL);";

    public static final String MENSUALES = "CREATE TABLE IF NOT EXISTS `tb_mensuales` (" +
            "  `men_nombre` varchar(50) DEFAULT NULL," +
            "  `men_direccion` varchar(50) DEFAULT NULL," +
            "  `men_telefono` varchar(20) DEFAULT NULL," +
            "  `men_id` varchar(10) NOT NULL," +
            "  `men_placa` varchar(10) DEFAULT NULL," +
            "  `men_desde` datetime DEFAULT NULL," +
            "  `men_hasta` datetime DEFAULT NULL," +
            "  `men_fac_numero` int(11) DEFAULT '0'," +
            "  `men_vip` char(1) DEFAULT 'N'," +
            "  `men_lote` varchar(4) DEFAULT 'l01'," +
            "  `men_facility_code` int(11) DEFAULT '0'," +
            "  `men_placa2` varchar(10) DEFAULT NULL," +
            "  `men_fac_prefijo` varchar(10) DEFAULT NULL," +
            "  `men_id2` varchar(10) DEFAULT NULL," +
            "  `men_facility_code2` int(11) DEFAULT NULL," +
            "  `men_valor` decimal(15,2) DEFAULT NULL," +
            "  `men_horario` varchar(4) DEFAULT 'siem'," +
            "  `men_documento` varchar(20) DEFAULT NULL," +
            "  `men_fecha_locatario` date DEFAULT NULL," +
            "  `men_antipassback` char(1) DEFAULT 'N'," +
            "  `men_postpago` char(1) DEFAULT 'N'," +
            "  `men_clase` varchar(10) DEFAULT NULL," +
            "  `men_locatario` char(1) DEFAULT 'N'," +
            "  `men_horas` int(11) DEFAULT '0'," +
            "  `men_picoplaca` varchar(25) DEFAULT NULL," +
            "  `men_bloqueado` char(1) DEFAULT 'N'," +
            "  `men_multiple` char(1) DEFAULT 'N'," +
            "  `men_actualizado` date DEFAULT NULL," +
            "  `men_renovar_0` char(1) DEFAULT 'N'," +
            "  `men_renovar0` char(1) DEFAULT 'N');";


    public static final String DATABASE_NAME = "caja.db";

    // Handle to a new DatabaseHelper.
    private DatabaseHelper mOpenHelper;

    /**
     * This class helps open, create, and upgrade the database file. Set to package visibility
     * for testing purposes.
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            // calls the super constructor, requesting the default cursor factory.
            super(context, DATABASE_NAME, null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    /**
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is called
     * automatically when Android creates the provider in response to a resolver request from a
     * client.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onCreate() {
        ConfigStorage config = new ConfigStorage();
        String url, node, nodeGroup;
        // Creates a new helper object. Note that the database itself isn't opened until
        // something tries to access it, and it's only created if it doesn't already exist.
        mOpenHelper = new DatabaseHelper(getContext());

        // Init the DB here
        mOpenHelper.getWritableDatabase().execSQL(VEHICULOS);
        mOpenHelper.getWritableDatabase().execSQL(IMPUESTOS);
        mOpenHelper.getWritableDatabase().execSQL(TARIFAS);
        mOpenHelper.getWritableDatabase().execSQL(HORARIOS);
        mOpenHelper.getWritableDatabase().execSQL(COMERCIOS);
        mOpenHelper.getWritableDatabase().execSQL(PRODUCTOS);
        mOpenHelper.getWritableDatabase().execSQL(TARIFAS_IMPUESTOS);
        mOpenHelper.getWritableDatabase().execSQL(TARIFAS_SUBSEGMENTOS);
        mOpenHelper.getWritableDatabase().execSQL(TARIFAS_SEGMENTOS);
        mOpenHelper.getWritableDatabase().execSQL(HORARIOS_DETALLE);
        mOpenHelper.getWritableDatabase().execSQL(USUARIOS);
        mOpenHelper.getWritableDatabase().execSQL(SESIONES);
        mOpenHelper.getWritableDatabase().execSQL(CAJA);
        mOpenHelper.getWritableDatabase().execSQL(FACTURA_DETALLE);
        mOpenHelper.getWritableDatabase().execSQL(FACTURAIMPUESTOS);
        mOpenHelper.getWritableDatabase().execSQL(FACTURAS);
        mOpenHelper.getWritableDatabase().execSQL(LIQDESCUENTOS);
        mOpenHelper.getWritableDatabase().execSQL(LIQUIDACIONES);
        mOpenHelper.getWritableDatabase().execSQL(MENSUALES);

        // Register the database helper, so it can be shared with the SymmetricService
        SQLiteOpenHelperRegistry.register(DATABASE_NAME, mOpenHelper);

        Intent intent = new Intent(getContext(), SymmetricService.class);

        url = config.getValueString("url", Objects.requireNonNull(getContext()));
        node = config.getValueString("node", getContext());
        nodeGroup = config.getValueString("group", getContext());

        intent.putExtra(SymmetricService.INTENTKEY_SQLITEOPENHELPER_REGISTRY_KEY, DATABASE_NAME);
        intent.putExtra(SymmetricService.INTENTKEY_REGISTRATION_URL, url);
        intent.putExtra(SymmetricService.INTENTKEY_EXTERNAL_ID, node);
        intent.putExtra(SymmetricService.INTENTKEY_NODE_GROUP_ID, nodeGroup);
        intent.putExtra(SymmetricService.INTENTKEY_START_IN_BACKGROUND, true);

        Properties properties = new Properties();
        properties.put(ParameterConstants.FILE_SYNC_ENABLE, "true");
        properties.put("start.file.sync.tracker.job", "true");
        properties.put("start.file.sync.push.job", "true");
        properties.put("start.file.sync.pull.job", "true");
        properties.put("job.file.sync.pull.period.time.ms", "10000");

        intent.putExtra(SymmetricService.INTENTKEY_PROPERTIES, properties);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            getContext().startForegroundService(intent);
//        }else{
        getContext().startService(intent);
//        }

        // Assumes that any failures will be reported by a thrown exception.
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public String getType(Uri uri) {
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}