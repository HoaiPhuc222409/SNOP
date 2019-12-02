package com.example.snop.notification;

import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAeq2IZ3Y:APA91bFjc40gJgNPoWB1UtCpLAKPB6hjZTDhRRkH4pTDxlw9LJS-jkvG5UbMJpcrcNv_kmxGYaFekcu03ojtui1vrJUox_qoIMISnSWhs52ADcGUgaLEuGT25-giorfKqSwc_7I__PZ8"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
