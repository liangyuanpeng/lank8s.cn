package cn.lank8s.springboot.region;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class CheckHealth {

    List<String> list = Lists.newArrayList();
    public List<LocationHealthInfo> updateHealthLocationList(List<String> urls){
        HttpClient httpClient = HttpClient.newBuilder()
                .build();
        List<LocationHealthInfo> locationHealthInfoList = Lists.newArrayList();
        List<CompletableFuture<HttpResponse<String>>> cfs = Lists.newArrayList();
        for (String s : list) {
            cfs.add(httpClient.sendAsync(HttpRequest.newBuilder()
                            .uri(URI.create(s))
                            .GET()
                        .build(), HttpResponse.BodyHandlers.ofString()));
        }
        for (CompletableFuture<HttpResponse<String>> cf : cfs) {
            HttpResponse<String> resp = cf.join();
            LocationHealthInfo locationHealthInfo = new LocationHealthInfo();
            locationHealthInfoList.add(locationHealthInfo);
            locationHealthInfo.setUrl(resp.uri().getHost()+resp.uri().getPath());
            if(resp.statusCode()==200){
                locationHealthInfo.setHealth(true);
            }
        }

        return locationHealthInfoList;
    }

    Map<String,List<LocationHealthInfo>> datamap = Maps.newHashMap();

    public String getLocation(String host){
        List<LocationHealthInfo> locationHealthInfos = datamap.get(host);
        if(locationHealthInfos==null){
            return "";
        }
        List<String> collect = locationHealthInfos.stream().filter(info -> info.isHealth()).map(LocationHealthInfo::getUrl).collect(Collectors.toList());
        if(!collect.isEmpty()){
            return collect.get(RandomUtils.nextInt(0,collect.size()));
        }
        return "";
    }

    public void updateLocations(){
        Map<String,List<LocationHealthInfo>> datamapTmp = Maps.newHashMap();
        for (Map.Entry<String, List<LocationHealthInfo>> entry : datamap.entrySet()) {
            datamapTmp.put(entry.getKey(),updateHealthLocationList(entry.getValue().stream().map(LocationHealthInfo::getUrl).collect(Collectors.toList())));
        }
        datamap=datamapTmp;
    }

}
