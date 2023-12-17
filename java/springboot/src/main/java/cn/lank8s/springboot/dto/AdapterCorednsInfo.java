package cn.lank8s.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdapterCorednsInfo {
    private String repo;
    private boolean adapter;

    public static AdapterCorednsInfo adapter(String repo){
        return new AdapterCorednsInfo(repo,true);
    }

    public static AdapterCorednsInfo origin(String repo){
        return new AdapterCorednsInfo(repo,false);
    }

}
