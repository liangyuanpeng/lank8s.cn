package cn.lank8s.springboot.region;

import java.util.List;

public interface ImageRegionProvider {

    List<String> getRegionList(String path);
    boolean check(String repo,String tag);
    String getName();
}
