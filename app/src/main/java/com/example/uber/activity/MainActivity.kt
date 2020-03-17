package com.example.uber.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.uber.R
import com.example.uber.helper.Permissoes
import com.example.uber.helper.UsuarioFirebase


class MainActivity: AppCompatActivity() {

    private var permissoes = arrayOf<String?>(Manifest.permission.ACCESS_FINE_LOCATION)

    private val positiveButtonClick = {
        dialog: DialogInterface, which: Int -> finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        Permissoes.validarPermissoes(permissoes, this, 1)

    }

    fun abrirTelaLogin(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun abrirTelaCadastro(view: View) {
        startActivity(Intent(this, CadastroActivity::class.java))
    }

    override fun onStart() {

        super.onStart()
        UsuarioFirebase.redirecionaUsuarioLogado(this@MainActivity)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for ( permissaoResultado in grantResults ) {

            if ( permissaoResultado == PackageManager.PERMISSION_DENIED ) {

                alertaValidacaoPermissao()

            }

        }

    }

    private fun alertaValidacaoPermissao() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("Permissões negadas")
        builder.setMessage("Para utilizar o app é necessário aceitas as permissões")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirmar", DialogInterface.OnClickListener(function = positiveButtonClick))

        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

}