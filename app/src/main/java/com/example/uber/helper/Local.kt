package com.example.uber.helper

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat

class Local {

    companion object {

        @JvmStatic
        fun cacularDistancia(latLngInicial: LatLng, latLngFinal: LatLng): Float {

            val localInicial = Location("Local inicial")
            localInicial.latitude = latLngInicial.latitude
            localInicial.longitude = latLngInicial.longitude

            val localFinal = Location("Local final")
            localFinal.latitude = latLngFinal.latitude
            localFinal.longitude = latLngFinal.longitude

            val distancia: Float
            distancia = localInicial.distanceTo(localFinal) / 1000

            return distancia

        }

        @JvmStatic
        fun formatarDistancia(distancia: Float): String {

            val distanciaFormatada: String
            val novaDistancia: Float
            val decimal = DecimalFormat("0.0")

            if ( distancia < 1 ) {

                novaDistancia = distancia * 1000
                distanciaFormatada = Math.round(novaDistancia).toString() + " m"

            } else {

                distanciaFormatada = decimal.format(distancia.toDouble()) + " Km"

            }

            return distanciaFormatada

        }

    }

}