package com.angkorteam.mbaas.sdk.android.library;

import com.angkorteam.mbaas.sdk.android.library.request.asset.AssetCreateRequest;
import com.angkorteam.mbaas.sdk.android.library.request.device.DeviceRegisterRequest;
import com.angkorteam.mbaas.sdk.android.library.request.file.FileCreateRequest;
import com.angkorteam.mbaas.sdk.android.library.request.oauth2.OAuth2RefreshRequest;
import com.angkorteam.mbaas.sdk.android.library.request.security.SecurityLoginRequest;
import com.angkorteam.mbaas.sdk.android.library.request.security.SecuritySignUpRequest;
import com.angkorteam.mbaas.sdk.android.library.response.asset.AssetCreateResponse;
import com.angkorteam.mbaas.sdk.android.library.response.asset.AssetDeleteResponse;
import com.angkorteam.mbaas.sdk.android.library.response.device.DeviceMetricsResponse;
import com.angkorteam.mbaas.sdk.android.library.response.device.DeviceRegisterResponse;
import com.angkorteam.mbaas.sdk.android.library.response.device.DeviceUnregisterResponse;
import com.angkorteam.mbaas.sdk.android.library.response.file.FileCreateResponse;
import com.angkorteam.mbaas.sdk.android.library.response.file.FileDeleteResponse;
import com.angkorteam.mbaas.sdk.android.library.response.javascript.JavaScriptExecuteResponse;
import com.angkorteam.mbaas.sdk.android.library.response.monitor.MonitorTimeResponse;
import com.angkorteam.mbaas.sdk.android.library.response.oauth2.OAuth2AuthorizeResponse;
import com.angkorteam.mbaas.sdk.android.library.response.oauth2.OAuth2RefreshResponse;
import com.angkorteam.mbaas.sdk.android.library.response.security.SecurityLoginResponse;
import com.angkorteam.mbaas.sdk.android.library.response.security.SecuritySignUpResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by socheat on 4/6/16.
 */
interface IService {

    @POST("api/rest/registry/device")
    Call<DeviceRegisterResponse> deviceRegister(@Body DeviceRegisterRequest request);

    @DELETE("api/rest/registry/device/{deviceToken}")
    Call<DeviceUnregisterResponse> deviceUnregister(@Path("deviceToken") String deviceToken);

    @PUT("api/rest/registry/device/pushMessage/{messageId}")
    Call<DeviceMetricsResponse> sendMetrics(@Path("messageId") String messageId);

    @POST("api/oauth2/authorize")
    @FormUrlEncoded
    Call<OAuth2AuthorizeResponse> oauth2Authorize(@Field("client_id") String clientId,
                                                  @Field("client_secret") String clientSecret,
                                                  @Field("grant_type") String grantType,
                                                  @Field("redirect_uri") String redirectUri,
                                                  @Field("state") String state,
                                                  @Field("code") String code);

    @POST("api/oauth2/refresh")
    Call<OAuth2RefreshResponse> oauth2Refresh(@Body OAuth2RefreshRequest request);

    @POST("api/javascript/execute/{script}")
    Call<JavaScriptExecuteResponse> javascriptExecutePost(@Path("script") String script, @Body Map<String, Object> request);

    @GET("api/javascript/execute/{script}")
    Call<JavaScriptExecuteResponse> javascriptExecuteGet(@Path("script") String script);

    @GET("api/javascript/execute/{script}")
    Call<JavaScriptExecuteResponse> javascriptExecuteGet(@Path("script") String script, @QueryMap Map<String, Object> params);

    @PUT("api/javascript/execute/{script}")
    Call<JavaScriptExecuteResponse> javascriptExecutePut(@Path("script") String script);

    @PUT("api/javascript/execute/{script}")
    Call<JavaScriptExecuteResponse> javascriptExecutePut(@Path("script") String script, @Body Map<String, Object> request);

    @POST("api/file/create/{filename}")
    Call<FileCreateResponse> fileCreate(@Path(value = "filename", encoded = false) String filename, @Body FileCreateRequest request);

    @DELETE("api/file/delete/{fileId}")
    Call<FileDeleteResponse> fileDelete(@Path("fileId") String fileId);

    @POST("api/asset/create/{filename}")
    Call<AssetCreateResponse> assetCreate(@Path("filename") String filename, @Body AssetCreateRequest request);

    @DELETE("api/file/delete/{assetId}")
    Call<AssetDeleteResponse> assetDelete(@Path("assetId") String assetId);

    @POST("api/security/signup")
    Call<SecuritySignUpResponse> securitySignup(@Body SecuritySignUpRequest request);

    @POST("api/security/login")
    Call<SecurityLoginResponse> securityLogin(@Body SecurityLoginRequest request);

    @GET("api/monitor/time")
    Call<MonitorTimeResponse> monitorTime();

}
