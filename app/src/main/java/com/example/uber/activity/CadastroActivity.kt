package com.example.uber.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uber.R
import com.example.uber.config.ConfiguracaoFirebase
import com.example.uber.helper.UsuarioFirebase
import com.example.uber.model.Usuario
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.*

class CadastroActivity: AppCompatActivity() {

    private lateinit var campoNome: TextInputEditText
    private lateinit var campoEmail: TextInputEditText
    private lateinit var campoSenha: TextInputEditText
    private lateinit var switchTipoUsuario: Switch
    private lateinit var autenticacao: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        //Inicializar os componentes
        inicializarComponentes()

    }

    fun validarCadastroUsuario(view: View) {

        //Recuperar textos dos campos
        val textoNome = campoNome.text.toString()
        val textoEmail = campoEmail.text.toString()
        val textoSenha = campoSenha.text.toString()
        val usuario = Usuario()

        if ( !textoNome.isEmpty() ) {

            if ( !textoEmail.isEmpty() ) {

                if ( !textoSenha.isEmpty() ) {

                    usuario.nome = textoNome
                    usuario.email = textoEmail
                    usuario.senha = textoSenha
                    usuario.tipo = verificaTipoUsuario()

                    cadastrarUsuario(usuario)

                } else {

                    Toast.makeText(this@CadastroActivity,
                            "Preencha a senha!",
                            Toast.LENGTH_SHORT).show()

                }

            } else {

                Toast.makeText(this@CadastroActivity,
                        "Preencha o email!",
                        Toast.LENGTH_SHORT).show()

            }

        } else {

            Toast.makeText(this@CadastroActivity,
                    "Preencha o nome!",
                    Toast.LENGTH_SHORT).show()

        }

    }

    fun verificaTipoUsuario(): String {

        return if (switchTipoUsuario.isChecked) "M" else "P"

    }

    fun cadastrarUsuario(usuario: Usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao()

        autenticacao.createUserWithEmailAndPassword(usuario.email, usuario.senha)
                .addOnCompleteListener { task: Task<AuthResult> ->

                    if ( task.isSuccessful ) {

                        try {

                            val idUsuario: String? = task.result?.user?.uid
                            usuario.id = idUsuario
                            usuario.salvar()

                            UsuarioFirebase.atualizarNomeUsuario(usuario.nome)

                            if (verificaTipoUsuario() == "P") {

                                startActivity(Intent(this, PassageiroActivity::class.java))
                                finish()

                                Toast.makeText(this@CadastroActivity,
                                        "Sucesso ao cadastrar o passageiro!",
                                        Toast.LENGTH_SHORT).show()

                            } else {

                                startActivity(Intent(this, RequisicoesActivity::class.java))
                                finish()

                                Toast.makeText(this@CadastroActivity,
                                        "Sucesso ao cadastrar o motorista!",
                                        Toast.LENGTH_SHORT).show()

                            }

                        } catch ( e: Exception ) {

                            e.printStackTrace()

                        }

                    } else {

                        var excecao: String? = ""

                        try {

                            throw task.exception!!

                        } catch ( e: FirebaseAuthWeakPasswordException) {

                            excecao = "Digite uma senha mais forte!"

                        } catch ( e: FirebaseAuthInvalidCredentialsException) {

                            excecao = "Por favor, digite um e-mail válido!"

                        } catch ( e: FirebaseAuthUserCollisionException) {

                            excecao = "Esta conta já foi cadastrada!"

                        } catch ( e: Exception ) {

                            excecao = "Erro ao cadastrar usuário: " + e.message
                            e.printStackTrace()

                        }

                        Toast.makeText(this@CadastroActivity,
                                excecao, Toast.LENGTH_SHORT).show()

                    }

                }

    }

    fun inicializarComponentes() {

        campoNome = findViewById(R.id.editCadastroNome)
        campoEmail = findViewById(R.id.editCadastroEmail)
        campoSenha = findViewById(R.id.editCadastroSenha)
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario)

    }

}