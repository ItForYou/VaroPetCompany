package util.retrofit;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitService {

    @FormUrlEncoded
    @POST("/adm/json/query.php")
    Call<ServerPost> getMenu(
            @FieldMap Map<String, String> option
    );
    @FormUrlEncoded
    @POST("/adm/json/query.php")
    Call<ServerPost> getPosition(
            @FieldMap Map<String, String> option
    );
}