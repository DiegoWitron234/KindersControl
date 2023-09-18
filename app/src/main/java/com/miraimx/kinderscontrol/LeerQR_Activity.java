package com.miraimx.kinderscontrol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Map;

public class LeerQR_Activity extends AppCompatActivity {
    Button btnScan;
    String idTutor = "8PalsQD1XmMSEELuEh8x8maxqdv2", idEmpleado;
    EditText txtResultado;
    private List<Tutorizacion.Usuario> alumnoLista = new ArrayList<>();
    private RecyclerAdapter2 recyclerAdapterAlumnos;
    private Button btnRegistrarEntrada;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leer_qr);
        idEmpleado = getIntent().getStringExtra("id");
        btnScan = findViewById(R.id.btnScan);
        txtResultado = findViewById(R.id.txtResultado);
        btnRegistrarEntrada = findViewById(R.id.registrarIn);
        btnRegistrarEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnRegistrarEntrada();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                options.setPrompt("Leer código QR");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(true);
                barcodeLauncher.launch(options);
            }
        });

        RecyclerView recyclerAlumnos = findViewById(R.id.rvCheckAlumno);
        initRecyclerView(recyclerAlumnos);
        cargarDatos(idTutor);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Lectura cancelada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                idTutor = result.getContents();
                txtResultado.setText(idTutor);
                cargarDatos(idTutor);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(LeerQR_Activity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LeerQR_Activity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });

    private void initRecyclerView(RecyclerView ryAlumnos) {
        LinearLayoutManager managerAlumnos = new LinearLayoutManager(this);

        ryAlumnos.setLayoutManager(managerAlumnos);
        recyclerAdapterAlumnos = new RecyclerAdapter2(alumnoLista, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLister();
            }
        });
        ryAlumnos.setAdapter(recyclerAdapterAlumnos);

    }

    private void selectLister() {
        boolean esAlumnoSeleccionado = false;

        for (Tutorizacion.Usuario alumno : alumnoLista) {
            if (alumno.getSeleccionado()) {
                esAlumnoSeleccionado = true;
                break;
            }
        }
        btnRegistrarEntrada.setEnabled(esAlumnoSeleccionado);
    }

    private void cargarDatos(final String cUserId) {
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
                                        Tutorizacion.Usuario usuarioDatos = new Tutorizacion.Usuario(id, nombreAlumno, false,"");
                                        alumnoLista.add(usuarioDatos);
                                    }
                                }

                                cantidadAlumnos[0]++;

                                if (cantidadAlumnos[0] == snapshot.getChildrenCount()) {
                                    // Notificar al adaptador sobre los cambios
                                    recyclerAdapterAlumnos.notifyDataSetChanged();
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

    private void btnRegistrarEntrada(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        List<String> alumnoSeleccion = new ArrayList<>();

        builder.setTitle("Confirmación");
        builder.setMessage("¿Desea realizar la asignación de los alumnos?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes agregar la lógica para agregar al niño al tutor
                Toast.makeText(LeerQR_Activity.this, "El niño ha entrado", Toast.LENGTH_SHORT).show();

                DatabaseReference chekinRef = FirebaseDatabase.getInstance().getReference("checkin").push();
                HashMap<String, Object> checkInfo = new HashMap<>();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String fechaHoraActual = dateFormat.format(calendar.getTime());

                int y = 0;

                for (Tutorizacion.Usuario alumno : alumnoLista) {
                    if (alumno.getSeleccionado()) {
                        alumnoSeleccion.add(alumno.getId());
                    }
                }

                while (y < alumnoSeleccion.size()) {
                    checkInfo.put("matricula", alumnoSeleccion.get(y));
                    checkInfo.put("tutor_id", idTutor);
                    checkInfo.put("empleado_id", idEmpleado);
                    checkInfo.put("in_out", "in");
                    checkInfo.put("horafecha_check", fechaHoraActual);
                    y++;
                }

                chekinRef.setValue(checkInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LeerQR_Activity.this, "Operación exitosa", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LeerQR_Activity.this, "No se pudo realizar la operación", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                while (y < alumnoSeleccion.size()) {
                    alumnoLista.get(y).setSeleccionado(false);
                }
                recyclerAdapterAlumnos.notifyDataSetChanged();
                btnRegistrarEntrada.setEnabled(false);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes agregar la lógica si el usuario elige "No"
                Toast.makeText(LeerQR_Activity.this, "No se agregó al niño al tutor", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}