package cn.lank8s.springboot.service;

import cn.lank8s.springboot.dto.AdapterCorednsInfo;
import cn.lank8s.springboot.dto.LocationOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class OCIService {
    
    @Autowired
    CacheService cacheService;
    
    public AdapterCorednsInfo adapterCoredns(String repo, String tag){
        if(repo.equalsIgnoreCase("coredns")){
            if(!tag.contains("v")){
                return AdapterCorednsInfo.origin(repo);
            }
            //TODO tag is sha256:xxx
            String tagt = tag.replaceAll("v","");
            String[] strs = tagt.split("\\.");
            if(Integer.valueOf(strs[0])<1){
                return AdapterCorednsInfo.origin(repo);
            }
            if(Integer.valueOf(strs[0])>1){
                return AdapterCorednsInfo.adapter("coredns/coredns");
            }
            if(Integer.valueOf(strs[1])>7){
                return AdapterCorednsInfo.adapter("coredns/coredns");
            }
        }
        return AdapterCorednsInfo.origin(repo);
    }

    public String getLocation(String host,String path){
        LocationOption locationOption = new LocationOption();
        locationOption.setHost(host);
        locationOption.setTag("tag");
        locationOption.setRepo(path);
        String location = cacheService.getLocation(locationOption);
        log.info("location:{}",location);
        Random random = new Random();
        String url = "";
        if(random.nextInt(100)<5){
            url = "";
        }
        url = "http://123:10015"+path;
        
        cacheService.cacheLocation(locationOption,url);
        return url;
    }

}
