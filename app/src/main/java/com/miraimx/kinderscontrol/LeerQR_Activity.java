package com.miraimx.kinderscontrol;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LeerQR_Activity extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(LeerQR_Activity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LeerQR_Activity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    //idTutor = result.getContents();
                    cargarDatos(result.getContents());
                    cargarDatosTutor(result.getContents());

                }
            });
    Button btnScan;
    String idEmpleado, idTutor, estatusRegistro;
    //idTutor = "8PalsQD1XmMSEELuEh8x8maxqdv2",
    TextView txtNombre, txtTelefono, txtEmail, txtDireccion;
    private final List<Usuario> alumnoLista = new ArrayList<>();
    //private RecyclerAdapter2 recyclerAdapterAlumnos;
    private ArrayAdapter listViewAdapter;

    private Button btnRegistrarEntrada;

    private int posAnteriorAlumno = -1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leer_qr);
        idEmpleado = getIntent().getStringExtra("id");
        estatusRegistro = getIntent().getStringExtra("estatus");
        btnScan = findViewById(R.id.btnScan);
        txtNombre = findViewById(R.id.txtNombraTutorQR);
        txtTelefono = findViewById(R.id.txtTelefonoTutorQR);
        txtEmail = findViewById(R.id.txEmailTutorQR);
        txtDireccion = findViewById(R.id.txDireccionTutorQR);
        btnRegistrarEntrada = findViewById(R.id.registrarIn);
        btnRegistrarEntrada.setEnabled(false);
        modoOscuro();
        if (estatusRegistro.equals("In")){
            btnRegistrarEntrada.setText("Registrar Entrada");
        }else{
            btnRegistrarEntrada.setText("Registrar Salida");
        }

        btnRegistrarEntrada.setOnClickListener(view -> btnRegistrarEntrada());

        btnScan.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Leer código QR");
            options.setCameraId(0);  // Use a specific camera of the device
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            barcodeLauncher.launch(options);
        });

        ListView listViewAlumnos = findViewById(R.id.lsCheckAlumno);
        listViewAdapter = new ListViewUsuarioAdapter(this, alumnoLista);
        listViewAlumnos.setAdapter(listViewAdapter);
        listViewAlumnos.setOnItemClickListener((adapterView, view, i, l) -> {
            Usuario elementoSeleccionado = alumnoLista.get(i);
            elementoSeleccionado.setSeleccionado(true);
            if (posAnteriorAlumno != -1 && posAnteriorAlumno != i) {
                alumnoLista.get(posAnteriorAlumno).setSeleccionado(false);
            }
            posAnteriorAlumno = i;
            btnRegistrarEntrada.setEnabled(true);
        });
    }
    private void modoOscuro(){
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {/* si está activo el modo oscuro lo desactiva */
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Lectura cancelada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                idTutor = result.getContents();
                cargarDatos(idTutor);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void cargarDatosTutor(String cUserId){
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("tutores").orderByChild("tutor_id").equalTo(cUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datos: snapshot.getChildren()){
                    String nombreTutor = datos.child("nombre_tutor").getValue(String.class);
                    String telefono = datos.child("telefono_tutor").getValue(String.class);
                    String correo = datos.child("correo_tutor").getValue(String.class);
                    String direccion = datos.child("direccion_tutor").getValue(String.class);
                    if (nombreTutor != null && telefono != null && correo != null && direccion != null){
                        txtNombre.setText(nombreTutor);
                        txtTelefono.setText(telefono);
                        txtEmail.setText(correo);
                        txtDireccion.setText(direccion);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cargarDatos(String cUserId) {
        alumnoLista.clear();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("tutorizacion").orderByChild("tutor_id").equalTo(cUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int[] cantidadAlumnos = {0};

                for (DataSnapshot registro : snapshot.getChildren()) {
                    final String id = registro.child("matricula").getValue(String.class);

                    if (id != null) {
                        Query queryAlumno = database.child("alumnos").orderByChild("matricula").equalTo(id);

                        queryAlumno.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot alumno : snapshot.getChildren()) {
                                    String nombreAlumno = alumno.child("nombre_alumno").getValue(String.class);

                                    if (nombreAlumno != null) {
                                        Usuario usuarioDatos = new Usuario(id, nombreAlumno, false, "");
                                        alumnoLista.add(usuarioDatos);
                                    }
                                }

                                cantidadAlumnos[0]++;

                                if (cantidadAlumnos[0] == snapshot.getChildrenCount()) {
                                    // Notificar al adaptador sobre los cambios
                                    listViewAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Manejar la cancelación si es necesario
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar la cancelación si es necesario
            }
        });
    }

    private void btnRegistrarEntrada() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        List<String> alumnoSeleccion = new ArrayList<>();

        builder.setTitle("Confirmación");
        builder.setMessage("¿Desea realizar la asignación de los alumnos?");

        builder.setPositiveButton("Sí", (dialog, which) -> {
            // Aquí puedes agregar la lógica para agregar al niño al tutor
            Toast.makeText(LeerQR_Activity.this, "Registro de "+estatusRegistro+" exitoso", Toast.LENGTH_SHORT).show();

            DatabaseReference chekinRef = FirebaseDatabase.getInstance().getReference("checkin").push();
            HashMap<String, Object> checkInfo = new HashMap<>();

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String fechaHoraActual = dateFormat.format(calendar.getTime());

            int y = 0;

            for (Usuario alumno : alumnoLista) {
                if (alumno.getSeleccionado()) {
                    alumnoSeleccion.add(alumno.getId());
                }
            }

            while (y < alumnoSeleccion.size()) {
                checkInfo.put("matricula", alumnoSeleccion.get(y));
                checkInfo.put("tutor_id", idTutor);
                checkInfo.put("empleado_id", idEmpleado);
                checkInfo.put("in_out", estatusRegistro);
                checkInfo.put("horafecha_check", fechaHoraActual);
                y++;
            }

            chekinRef.setValue(checkInfo).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LeerQR_Activity.this, "Operación exitosa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LeerQR_Activity.this, "No se pudo realizar la operación", Toast.LENGTH_SHORT).show();
                }
            });

            while (y < alumnoSeleccion.size()) {
                alumnoLista.get(y).setSeleccionado(false);
            }

            listViewAdapter.notifyDataSetChanged();

            btnRegistrarEntrada.setEnabled(false);
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // Aquí puedes agregar la lógica si el usuario elige "No"
            // Toast.makeText(LeerQR_Activity.this, "No registro la entrada del niño", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}