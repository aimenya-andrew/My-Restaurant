package com.paidapp.laughtercafe.Remote;

import com.paidapp.laughtercafe.Model.FCMResponse;
import com.paidapp.laughtercafe.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGQunqR4:APA91bHH1AKxqRG8Vm4EqKUh-nq_ET8XjBuJ9igOQahJjcfWX3a-2j3-fVsH5fe1x2sEpDfcUPW0dIeHqqqCMCPDASyIQvy9ZQcRG5nWAT22OXCIHDGQAIZaYtJSn_VgJaVNxtt5HXhH"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
