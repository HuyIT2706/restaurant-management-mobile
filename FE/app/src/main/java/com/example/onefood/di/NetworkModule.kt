package com.example.onefood.di

import android.content.Context
import com.example.onefood.data.api.ProductApiService
import com.example.onefood.data.api.TableApiService
import com.example.onefood.data.api.OrderApiService
import com.example.onefood.data.api.UserApiService
import com.example.onefood.data.repository.UserRepository
import com.example.onefood.data.api.StatisticsApiService
import com.example.onefood.data.cache.ApiCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://onefood.id.vn/BE/"

    // Ktor HttpClient for Product API
    @Provides
    @Singleton
    fun provideKtorClient(@ApplicationContext context: Context): HttpClient {
        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            // Only enable logging in debug mode
            if (isDebug) {
                install(Logging) {
                    level = LogLevel.BODY
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideUserApiService(client: HttpClient): UserApiService {
        return UserApiService(client, BASE_URL)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userApiService: UserApiService): UserRepository {
        return UserRepository(userApiService)
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

    // Retrofit for Table API (keep existing)
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
        
        // Only add logging interceptor in debug mode
        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        
        return builder.build()
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
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideTableApi(retrofit: Retrofit): TableApiService {
        return retrofit.create(TableApiService::class.java)
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
    
}