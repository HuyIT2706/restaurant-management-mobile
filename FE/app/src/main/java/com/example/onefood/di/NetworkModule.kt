package com.example.onefood.di

import com.example.onefood.data.api.ProductApiService
import com.example.onefood.data.api.TableApiService
import com.example.onefood.data.api.OrderApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import com.example.onefood.data.api.StatisticsApiService

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.111.17.241/BeMobie/restaurant-management-mobile/BE/"

    // Ktor HttpClient for Product API
    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

    @Provides
    @Singleton
    fun provideProductApiService(client: HttpClient): ProductApiService {
        return ProductApiService(client, BASE_URL)
    }

    @Provides
    @Singleton
    fun provideOrderApiService(client: HttpClient): OrderApiService {
        return OrderApiService(client, BASE_URL)
    }

    @Provides
    @Singleton
    fun providePromotionApiService(client: HttpClient): com.example.onefood.data.api.PromotionApiService {
        return com.example.onefood.data.api.PromotionApiService(client, BASE_URL)
    }

    @Provides
    @Singleton
    fun provideStatisticsApiService(client: HttpClient): StatisticsApiService {
        return StatisticsApiService(client, BASE_URL)
    }



    // Retrofit for Table API (keep existing)
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.221.4.241/BeMobie/restaurant-management-mobile/BE/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideTableApi(retrofit: Retrofit): TableApiService {
        return retrofit.create(TableApiService::class.java)
    }
}