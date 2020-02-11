package com.example.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.uber.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

  private GoogleMap mMap;
  private LocationManager locationManager;
  private LocationListener locationListener;
  private LatLng localMotorista;
  private LatLng localPassageiro;
  private Usuario motorista;
  private Usuario passageiro;
  private String idRequisicao;
  private Requisicao requisicao;
  private DatabaseReference firebaseRef;
  private Button buttonAceitarCorrida;
  private Marker marcadorMotorista;
  private Marker marcadorPassageiro;
  private String statusRequisicao;
  private Boolean requisicaoAtiva;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_corrida);

    inicializarComponentes();

    // Recupera dados do usuário
    if (getIntent().getExtras().containsKey("idRequisicao") && getIntent().getExtras().containsKey("motorista")) {
      Bundle extras = getIntent().getExtras();
      motorista = (Usuario) extras.getSerializable("motorista");
      localMotorista = new LatLng(
        Double.parseDouble(motorista.getLatitude()),
        Double.parseDouble(motorista.getLongitude())
      );
      idRequisicao = extras.getString("idRequisicao");
      requisicaoAtiva = extras.getBoolean("requisicaoAtiva");

      vericaStatusRequisicao();
    }

  }

  @Override
  public void onMapReady(GoogleMap googleMap) {

    mMap = googleMap;

    // Recuperar a localização do usuário
    recuperarLocalizacaoUsuario();

  }

  public void aceitarCorrida(View view) {

    // Configurar requisição
    requisicao = new Requisicao();
    requisicao.setId(idRequisicao);
    requisicao.setMotorista(motorista);
    requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

    requisicao.atualizar();

  }

  private void vericaStatusRequisicao() {

    DatabaseReference requisicoes = firebaseRef.child("requisicoes")
        .child(idRequisicao);
    requisicoes.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        // Recuperar requisição
        requisicao = dataSnapshot.getValue(Requisicao.class);

        if (requisicao != null) {
          passageiro = requisicao.getPassageiro();
          localPassageiro = new LatLng(
              Double.parseDouble(passageiro.getLatitude()),
              Double.parseDouble(passageiro.getLongitude())
          );

          statusRequisicao = requisicao.getStatus();
          alteraInterfaceStatusRequisicao(statusRequisicao);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

  }

  private void alteraInterfaceStatusRequisicao(String status) {

    switch (status) {
      case Requisicao.STATUS_AGUARDANDO :
        requisicaoAguardando();
        break;
      case Requisicao.STATUS_A_CAMINHO :
        requisicaoACaminho();
        break;
    }

  }

  private void requisicaoAguardando() {

    buttonAceitarCorrida.setText("Aceitar corrida");

    // Exibe marcador do motorista
    adicionaMarcadorMotorista(localMotorista, motorista.getNome());

    mMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(localMotorista, 20)
    );

  }

  private void requisicaoACaminho() {

    buttonAceitarCorrida.setText("A caminho do passageiro");

    // Exibe marcador do motorista
    adicionaMarcadorMotorista(localMotorista, motorista.getNome());

    // Exibe marcador do passageiro
    adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

    // Centralizar dois marcadores
    centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);

  }

  private void adicionaMarcadorMotorista(LatLng localizacao, String titulo) {

    if (marcadorMotorista != null) {
      marcadorMotorista.remove();
    }

    marcadorMotorista = mMap.addMarker(
        new MarkerOptions()
            .position(localizacao)
            .title(titulo)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
    );

  }

  private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo) {

    if (marcadorPassageiro != null) {
      marcadorPassageiro.remove();
    }

    marcadorPassageiro = mMap.addMarker(
        new MarkerOptions()
            .position(localizacao)
            .title(titulo)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.circulo_azul))
    );

  }

  private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2) {

    LatLngBounds.Builder builder = new LatLngBounds.Builder();

    builder.include(marcador1.getPosition());
    builder.include(marcador2.getPosition());

    LatLngBounds bounds = builder.build();

    int largura = getResources().getDisplayMetrics().widthPixels;
    int altura = getResources().getDisplayMetrics().heightPixels;
    int espacoInterno = (int) (largura * 0.30);

    mMap.moveCamera(
        CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno)
    );

  }

  private void recuperarLocalizacaoUsuario() {

    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    locationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        //recuperar latitude e longitude
        double latitute = location.getLatitude();
        double longitude = location.getLongitude();

        localMotorista = new LatLng(latitute, longitude);

        // Atualizar Geofire
        UsuarioFirebase.atualizarDadosLocalizacao(latitute, longitude);

        alteraInterfaceStatusRequisicao(statusRequisicao);
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {

      }

      @Override
      public void onProviderEnabled(String provider) {

      }

      @Override
      public void onProviderDisabled(String provider) {

      }
    };

    //Solicitar atualizações de localização
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      locationManager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER,
          5000,
          10,
          locationListener
      );
    }

  }

  private void inicializarComponentes() {

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("Iniciar corrida");

    buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);

    //Configuracoes iniciais
    firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

  }

  @Override
  public boolean onSupportNavigateUp() {

    if (requisicaoAtiva) {
      Toast.makeText(CorridaActivity.this, "Necessário encerrar a requisição atual",
          Toast.LENGTH_SHORT).show();
    } else {
      Intent i = new Intent(CorridaActivity.this, RequisicoesActivity.class);
      startActivity(i);
    }
    return false;

  }

}
