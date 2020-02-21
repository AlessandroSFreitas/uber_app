package com.example.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Destino;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
  private Marker marcadorDestino;
  private String statusRequisicao;
  private Boolean requisicaoAtiva;
  private FloatingActionButton fabRota;
  private Destino destino;

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

      verificaStatusRequisicao();
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

  private void verificaStatusRequisicao() {

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
          destino = requisicao.getDestino();
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
      case Requisicao.STATUS_VIAGEM :
        requisicaoViagem();
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
    fabRota.setVisibility(View.VISIBLE);

    // Exibe marcador do motorista
    adicionaMarcadorMotorista(localMotorista, motorista.getNome());

    // Exibe marcador do passageiro
    adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

    // Centralizar dois marcadores
    centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);

    // Iniciar monitoramento do motorista/passageiro
    iniciarMonitoramentoCorrida(passageiro, motorista);

  }

  private void requisicaoViagem() {

    // Alterar a interface
    fabRota.setVisibility(View.VISIBLE);
    buttonAceitarCorrida.setText("A caminho do destino");

    // Exibe marcador do motorista
    adicionaMarcadorMotorista(localMotorista, motorista.getNome());

    // Exibe marcador de destino
    LatLng localDestino = new LatLng(
        Double.parseDouble(destino.getLatitude()),
        Double.parseDouble(destino.getLongitude())
    );
    adicionaMarcadorDestino(localDestino, "Destino");

    // Centraliza marcadores motorista / destino
    centralizarDoisMarcadores(marcadorMotorista, marcadorDestino);

  }

  private void iniciarMonitoramentoCorrida(Usuario p, Usuario m) {

    // Inicializar Geofire
    DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase()
        .child("local_usuario");
    GeoFire geoFire = new GeoFire(localUsuario);

    // Adicionar círculo no passageiro
    final Circle circulo = mMap.addCircle(
        new CircleOptions()
        .center(localPassageiro)
        .radius(50) // em metros
        .fillColor(Color.argb(90, 255, 153, 0))
        .strokeColor(Color.argb(190, 255, 153, 0))
    );

    final GeoQuery geoQuery = geoFire.queryAtLocation(
        new GeoLocation(localPassageiro.latitude, localPassageiro.longitude),
        0.05 // (0.05 km == 50 metros)
    );

    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
      @Override
      public void onKeyEntered(String key, GeoLocation location) {
        if (key.equals(motorista.getId())) {
          //Log.d("onKeyEntered", "onKeyEntered: motorista esta dentro da area");
          // Alterar o status da requisição
          requisicao.setStatus(Requisicao.STATUS_VIAGEM);
          requisicao.atualizarStatus();

          // Remover listeners
          geoQuery.removeAllListeners();
          circulo.remove();
        }
      }

      @Override
      public void onKeyExited(String key) {

      }

      @Override
      public void onKeyMoved(String key, GeoLocation location) {

      }

      @Override
      public void onGeoQueryReady() {

      }

      @Override
      public void onGeoQueryError(DatabaseError error) {

      }
    });

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

  private void adicionaMarcadorDestino(LatLng localizacao, String titulo) {

    if (marcadorPassageiro != null) {
      marcadorPassageiro.remove();
    }

    if (marcadorDestino != null) {
      marcadorDestino.remove();
    }

    marcadorDestino = mMap.addMarker(
        new MarkerOptions()
            .position(localizacao)
            .title(titulo)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
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

    // Adicionar evento de clique no fabRota
    fabRota = findViewById(R.id.fabRota);
    fabRota.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String status = statusRequisicao;
        if (status != null && !status.isEmpty()) {

          String lat = "";
          String lon = "";

          switch (status) {
            case Requisicao.STATUS_A_CAMINHO :
              lat = String.valueOf(localPassageiro.latitude);
              lon = String.valueOf(localPassageiro.longitude);
              break;
            case Requisicao.STATUS_VIAGEM :
              lat = destino.getLatitude();
              lon = destino.getLongitude();
              break;
          }

          // Abrir rota
          String latLong = lat + "," + lon;
          Uri uri = Uri.parse("google.navigation:q="+latLong+"&mode=d");
          Intent i = new Intent(Intent.ACTION_VIEW, uri);
          i.setPackage("com.google.android.apps.maps");
          startActivity(i);

        }

      }
    });

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
