package pnj.jejaringsosial.chandrasa.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAECobNuE:APA91bEQI8dOUsEm_wbOyqYUguUdnZP8KIBxPiodU2pUjKsX9OYsnOc5Vo7fkwAWOvE_k_i1_0hyYLqxdeJAOCt2jTfLQ6Z2z125oHfLhDudjmzFVeAL7C3GQ_zkj8fPnr987EOZ5Xzf"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
