package com.example.jobappclientside.di

import android.content.Context
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.other.Constants.BASE_URL
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.DispatchersProvider
import com.example.jobappclientside.remote.HttpApi
import com.example.jobappclientside.repositories.AbstractRepository
import com.example.jobappclientside.repositories.DefaultRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Singleton
    @Provides
    fun provideHttpApi(okHttpClient: OkHttpClient): HttpApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(HttpApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDefaultRepository(httpApi: HttpApi): AbstractRepository
        = DefaultRepositoryImpl(httpApi)

    @Singleton
    @Provides
    fun provideDispatcherProvider(): AbstractDispatchers
        = DispatchersProvider()


    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    fun provideDataStoreUtil(context: Context) =
        DataStoreUtil(context)
}