package com.example.uber.config

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ConfiguracaoFirebase {

    companion object {

        private var database: DatabaseReference? = null
        private var auth: FirebaseAuth? = null

        @JvmStatic
        fun getFirebaseDatabase(): DatabaseReference {

            if ( database == null ) {

                database = FirebaseDatabase.getInstance().getReference()

            }

            return this.database!!

        }

        @JvmStatic
        fun getFirebaseAutenticacao(): FirebaseAuth {

            if ( auth == null ) {

                auth = FirebaseAuth.getInstance()

            }

            return this.auth!!

        }

    }

}
