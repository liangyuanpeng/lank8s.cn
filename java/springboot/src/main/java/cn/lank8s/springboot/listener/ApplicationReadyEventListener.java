package cn.lank8s.springboot.listener;

import cn.lank8s.springboot.bean.LocationServerConfig;
import cn.lank8s.springboot.helper.ThreadHelper;
import cn.lank8s.springboot.region.ConfigMapConfigWatcher;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    LocationServerConfig locationServerConfig;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("locationServerConfig:{}",locationServerConfig);
        ThreadHelper.executorService.execute(()->{
            ConfigMapConfigWatcher configMapConfigWatcher = new ConfigMapConfigWatcher();
            try {
                configMapConfigWatcher.watch();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("application started!!");
    }

}