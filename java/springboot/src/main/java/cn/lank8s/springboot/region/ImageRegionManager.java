package cn.lank8s.springboot.region;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ImageRegionManager {
    AliYunImageRegionProvider aliYunImageRegionProvider = new AliYunImageRegionProvider();

    public List<String> updateRegionListForImage(String path,String repo,String tag){
        if(aliYunImageRegionProvider.check(repo,tag)){
            return aliYunImageRegionProvider.getRegionList(path);
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) {
        String path = "";
        ImageRegionManager regionManager = new ImageRegionManager();
        List<String> strings = regionManager.updateRegionListForImage("/v2/kube-proxy/manifests/v1.10.1","kube-proxy", "v1.10.1");
        log.info("region list :{}", strings);
        System.out.println(strings);
        if(strings.isEmpty()){
            strings = Lists.newArrayList();
            strings.add("https://lank8s.cn"+path);
        }
    }

}
