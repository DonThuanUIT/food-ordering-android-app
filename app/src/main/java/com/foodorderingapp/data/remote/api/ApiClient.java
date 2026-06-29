package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.utils.constants.AppConstants;
import com.foodorderingapp.utils.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static AuthApiService authApiService = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .addInterceptor(chain -> {
                                Request.Builder builder = chain.request().newBuilder();
                                String token = TokenManager.getInstance().getAccessToken();
                                if (token != null && !token.isEmpty()) {
                                    builder.addHeader("Authorization", "Bearer " + token);
                                }
                                return chain.proceed(builder.build());
                            })
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(AppConstants.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    public static synchronized AuthApiService getAuthApiService() {
        if (authApiService == null) {
            authApiService = getRetrofit().create(AuthApiService.class);
        }
        return authApiService;
    }
}
