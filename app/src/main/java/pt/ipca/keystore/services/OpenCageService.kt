package pt.ipca.keystore.services

import pt.ipca.keystore.data.api.GeocodeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenCageService {
    @GET("json")
    fun geocode(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("pretty") pretty: Int = 1
    ): Call<GeocodeResponse>
}
