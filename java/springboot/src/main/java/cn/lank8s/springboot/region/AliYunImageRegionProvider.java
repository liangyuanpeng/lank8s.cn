package cn.lank8s.springboot.region;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AliYunImageRegionProvider implements ImageRegionProvider{
    
    @Override
    public List<String> getRegionList(String path) {
        List<String> list = Lists.newArrayList();
        if(!path.equalsIgnoreCase("/v2/")){
            path = path.replaceAll("/v2/","/v2/lan-k8s/");
        }
        list.add("https://registry.aliyuncs.com"+path);
        return list;
    }

    @Override
    public boolean check(String repo, String tag) {
        List<String> needCheckList = Lists.newArrayList();
        needCheckList.add("https://registry.aliyuncs.com/v2/lan-k8s/"+repo+"/manifests/"+tag);
        List<CompletableFuture<HttpResponse<byte[]>>> cfs = Lists.newArrayList();

        HttpClient httpClient = HttpClient.newBuilder()
                .build();

        for (String s : needCheckList) {
            cfs.add(httpClient.sendAsync(HttpRequest.newBuilder()
                    .uri(URI.create(s))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.ofByteArray()));
        }

        List<String> regionList = Lists.newArrayList();
        Map<String,List<String>> map = Maps.newHashMap();
        Map<String,String> authHeaderTokenUrlMap = Maps.newHashMap();
        for (CompletableFuture<HttpResponse<byte[]>> cf : cfs) {
            HttpResponse<byte[]> resp = cf.join();
            if(resp.statusCode()==200){
                regionList.add(resp.uri().getHost()+resp.uri().getPath());
                return true;
            }else{
                List<String> authHeader = resp.headers().allValues("www-authenticate");
                System.out.println(authHeader);
                String tokenUrl = "";
                if(resp.statusCode()==401 && authHeader!=null && !authHeader.isEmpty()){
                    if(authHeaderTokenUrlMap.containsKey(authHeader.get(0))){
                        continue;
                    }
                    String[] strs = authHeader.get(0).split(",");
                    for (int i = 0; i < strs.length; i++) {
                        if(strs[i].contains("Bearer")){
                            tokenUrl = strs[i].split("=")[1].replaceAll("\"","")+"?";
                        }else{
                            tokenUrl+=strs[i].replaceAll("\"","");
                            if(i!=strs.length-1){
                                tokenUrl+="&";
                            }
//                            tokenUrl+=strs[i].split("=")[0];
//                            tokenUrl+=strs[i].split("=")[1];
                        }
                    }
                    authHeaderTokenUrlMap.put(authHeader.get(0),tokenUrl);
                    List<String> urls = Collections.emptyList();
                    if(!map.containsKey(tokenUrl)){
                        urls = Lists.newArrayList();
                        map.put(tokenUrl,urls);
                    }
                    urls.add(resp.uri().getHost()+resp.uri().getPath());
                    System.out.println("tokenUrl:"+tokenUrl);
//                    map.put()
                }
                System.out.println(resp.uri().getHost()+resp.uri().getPath()+"*********"+resp.statusCode());
            }
        }
        List<CompletableFuture<HttpResponse<byte[]>>> completableFutures = retryWithAuth(map);
        for (CompletableFuture<HttpResponse<byte[]>> cf : completableFutures) {
            HttpResponse<byte[]> resp = cf.join();
            if(resp.statusCode()==200){
                regionList.add(resp.uri().getHost()+resp.uri().getPath());
                return true;
            }else {
                System.out.println("wrong url:"+resp.uri().getHost()+resp.uri().getPath()+"|"+resp.statusCode());
            }
        }
        return false;
    }

    public List<CompletableFuture<HttpResponse<byte[]>>>  retryWithAuth(Map<String,List<String>> tokenAPIUrlsMap){
        List<CompletableFuture<HttpResponse<byte[]>>> cfs = Lists.newArrayList();
        HttpClient httpClient = HttpClient.newBuilder()
                .build();
        for (Map.Entry<String, List<String>> entry : tokenAPIUrlsMap.entrySet()) {
            for (String s : entry.getValue()) {
                cfs.add(httpClient.sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create("https://"+s))
                        .headers("Authorization","Bearer "+token(entry.getKey()))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build(), HttpResponse.BodyHandlers.ofByteArray()));
            }
        }
        return cfs;
    }

    public String token(String url){
        return "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjRSSU06SEhMNDpHU1MyOjdaQ0w6QkNMRDpKN0ZIOlVPNzM6Q1FETzpNUUg1OjdNQ1E6T0lQUTpYQlk1In0.eyJpc3MiOiJkb2NrZXJhdXRoLmFsaXl1bmNzLmNvbSIsImF1ZCI6InJlZ2lzdHJ5LmFsaXl1bmNzLmNvbTpjbi1oYW5nemhvdToyNjg0MiIsInN1YiI6IiIsImlhdCI6MTY3MzkyODAwMiwianRpIjoiTkZ5dWtpUGtoRExyU3g2VUM4Z2Z3dyIsIm5iZiI6MTY3MzkyNzcwMiwiZXhwIjoxNjczOTI4NjAyLCJhY2Nlc3MiOlt7Im5hbWUiOiJsYW4tazhzL2t1YmUtcHJveHkiLCJ0eXBlIjoicmVwb3NpdG9yeSIsImFjdGlvbnMiOlsicHVsbCJdfV19.rzJ0EXPDnkn_EUtl7lQA5f48xulmpyMpNX4d8nPPxez-exOz2b9Mj7jyc0rhdKODdXlXhpvpI4Lys9t4pC6lnZ5WPqddkJXEtZNC6QIkRQzDakX9I4kqKZ58a4IggN5687W73KmW7UaAK77pvOoLLqLgJm4IDFk33D3h5LqWxXho1AHFBV4eXKmWGc3-hc31gHuMTIxu2OmMMcMz3mvJin3pBbeXwj9TN8RPwyOfddmsrm9v5YpRz0YoNSWuRcNdxaRm_rbG7GaAFKQLVIRSEG2QUEEFiHxPwqS9_MMwv25XZdWyUZ1MfwvSsu6z62_taCEQyspucQ78hsSUfTZ7Gg";
    }

    @Override
    public String getName() {
        return "aliyun";
    }
}
