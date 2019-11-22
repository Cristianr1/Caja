package com.koiti.caja;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;
import org.jumpmind.symmetric.android.SymmetricService;


public class Configuracion extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private Button out, sync;
    EditText codeInput, idInput, estacionInput, ipInput, nodeInput, nodegroupInput, publicadorInput,
            startNum, endNum, preFac, nextFac;

    private String ip = "";
    private String node = "";
    private String publicador = "";
    private String nodeGroup = "";

    Spinner carroTfaCodigo, motoTfaCodigo, biciTfaCodigo;
    ArrayAdapter<String> adapter;

    ConfigStorage config = new ConfigStorage();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        context = this;
        setTitle("Configuración");

        codeInput = findViewById(R.id.codeInput);
        idInput = findViewById(R.id.idInput);
        estacionInput = findViewById(R.id.estacionInput);

        startNum = findViewById(R.id.startNum_id);
        endNum = findViewById(R.id.endNum_id);
        preFac = findViewById(R.id.prefijoFac_id);
        nextFac = findViewById(R.id.nextFac_id);

        ipInput = findViewById(R.id.ipInput);
        nodeInput = findViewById(R.id.nodeIdInput);
        nodegroupInput = findViewById(R.id.nodeGroupInput);
        publicadorInput = findViewById(R.id.publicadorInput);

        codeInput.addTextChangedListener(numberTextWatcher);
        idInput.addTextChangedListener(numberTextWatcher);
        startNum.addTextChangedListener(numberTextWatcher);
        endNum.addTextChangedListener(numberTextWatcher);

        estacionInput.addTextChangedListener(stringTextWatcher);
        preFac.addTextChangedListener(stringTextWatcher);
        ipInput.addTextChangedListener(stringTextWatcher);
        nodeInput.addTextChangedListener(stringTextWatcher);
        nodegroupInput.addTextChangedListener(stringTextWatcher);
        publicadorInput.addTextChangedListener(stringTextWatcher);

        out = findViewById(R.id.exit_Id);
        sync = findViewById(R.id.sync_Id);
        out.setOnClickListener(mListener);
        sync.setOnClickListener(mListener);

        int code = config.getValueInt("code", context);
        int id = config.getValueInt("id", context);
        int start = config.getValueInt("start", context);
        int end = config.getValueInt("end", context);
        int next = config.getValueInt("next", context) + start;
        String estacion = config.getValueString("estacion", context);
        String prefijo = config.getValueString("prefijo", context);

        ip = config.getValueString("ip", context);
        node = config.getValueString("node", context);
        publicador = config.getValueString("publicador", context);
        nodeGroup = config.getValueString("group", context);

        codeInput.setText(Integer.toString(code));
        idInput.setText(Integer.toString(id));
        estacionInput.setText(estacion);

        startNum.setText(Integer.toString(start));
        endNum.setText(Integer.toString(end));
        preFac.setText(prefijo);
        nextFac.setText(Integer.toString(next));

        ipInput.setText(ip);
        nodeInput.setText(node);
        publicadorInput.setText(publicador);
        nodegroupInput.setText(nodeGroup);

        carroTfaCodigo = findViewById(R.id.carro_tfa_codigo);
        motoTfaCodigo = findViewById(R.id.moto_tfa_codigo);
        biciTfaCodigo = findViewById(R.id.bicicleta_tfa_codigo);
        carroTfaCodigo.setOnItemSelectedListener(this);
        motoTfaCodigo.setOnItemSelectedListener(this);
        biciTfaCodigo.setOnItemSelectedListener(this);

        Tarifas tarifas = new Tarifas();
        ArrayList<String> tfacodigo = tarifas.tfaNombre();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tfacodigo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Cargo el spinner con los datos
        carroTfaCodigo.setAdapter(adapter);
        motoTfaCodigo.setAdapter(adapter);
        biciTfaCodigo.setAdapter(adapter);

        //Persiste la selección
        carroTfaCodigo.setSelection(config.getValueInt("carro_tfa_codigo", context));
        motoTfaCodigo.setSelection(config.getValueInt("moto_tfa_codigo", context));
        biciTfaCodigo.setSelection(config.getValueInt("bici_tfa_codigo", context));
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked
            switch (v.getId()) {
                case R.id.exit_Id:
                    finish();
                    break;
                case R.id.sync_Id:
                    sincronizar();
                    break;
            }
        }
    };

    private final TextWatcher numberTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }

            if (codeInput.getText().hashCode() == s.hashCode())
                config.save(value, "code", context);
            else if (idInput.getText().hashCode() == s.hashCode())
                config.save(value, "id", context);
            else if (startNum.getText().hashCode() == s.hashCode())
                config.save(value, "start", context);
            else if (endNum.getText().hashCode() == s.hashCode())
                config.save(value, "end", context);
        }
    };

    private final TextWatcher stringTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();

            if (estacionInput.getText().hashCode() == s.hashCode())
                config.save(text, "estacion", context);
            else if (ipInput.getText().hashCode() == s.hashCode())
                config.save(text, "ip", context);
            else if (nodeInput.getText().hashCode() == s.hashCode())
                config.save(text, "node", context);
            else if (nodegroupInput.getText().hashCode() == s.hashCode())
                config.save(text, "group", context);
            else if (publicadorInput.getText().hashCode() == s.hashCode())
                config.save(text, "publicador", context);
            else
                config.save(text, "prefijo", context);
        }
    };

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using

        switch (parent.getId()) {
            case R.id.carro_tfa_codigo:
                config.save(pos, "carro_tfa_codigo", context);
                break;
            case R.id.moto_tfa_codigo:
                config.save(pos, "moto_tfa_codigo", context);
                break;
            case R.id.bicicleta_tfa_codigo:
                config.save(pos, "bici_tfa_codigo", context);
                break;
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void sincronizar() {
        ip = config.getValueString("ip", context);
        node = config.getValueString("node", context);
        publicador = config.getValueString("publicador", context);
        nodeGroup = config.getValueString("group", context);

        String url = "http://" + ip + ":31415/sync/" + publicador;

        config.save(url, "url", context);

        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getWritableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tablas = new ArrayList<>();

        while (c.moveToNext()) {
            tablas.add(c.getString(0));
        }

        tablas.remove("sqlite_sequence");
        tablas.remove("android_metadata");

        c.close();

        for (String tabla : tablas) {
            String dropQuery = "DROP TABLE IF EXISTS " + tabla;
            db.execSQL(dropQuery);
            Log.d("ipSync", tabla);
        }

        Toast.makeText(context, "Sincronizando...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Configuracion.this, SymmetricService.class);
        stopService(intent);

        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        finish();
        System.exit(0);
    }
}
