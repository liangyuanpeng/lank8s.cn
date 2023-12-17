package cn.lank8s.springboot.region;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConfigMapConfigWatcher {

    public ConfigMapConfigWatcher(){

    }

    public void watch() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        // infinite timeout
        OkHttpClient httpClient =
                client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Watch<V1ConfigMap> watch =
                Watch.createWatch(
                        client,
                        api.listNamespacedConfigMapCall("default", "", false,
                                null, null, null, null, null, null, null, true, new ApiCallback() {
                                    @Override
                                    public void onFailure(ApiException e, int statusCode, Map responseHeaders) {

                                    }

                                    @Override
                                    public void onSuccess(Object result, int statusCode, Map responseHeaders) {

                                    }

                                    @Override
                                    public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

                                    }

                                    @Override
                                    public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

                                    }
                                }),
                        new TypeToken<Watch.Response<V1ConfigMap>>() {}.getType());

        try {
            for (Watch.Response<V1ConfigMap> item : watch) {
                log.info("{}|{}|{}",item.type,item.object.getMetadata().getNamespace(), item.object.getMetadata().getName());
            }
        } finally {
            watch.close();
        }
    }

}
