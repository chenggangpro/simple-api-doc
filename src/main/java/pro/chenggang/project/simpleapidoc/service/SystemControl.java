package pro.chenggang.project.simpleapidoc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author: chenggang
 * @date 2020-02-25.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemControl implements SmartInitializingSingleton {

    private final ApiService apiService;

    @Scheduled(fixedRateString = "#{@apiSystemProperties.refreshRate}")
    public void execApiDoc(){
        apiService.generateHtml();
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.execApiDoc();
        apiService.execApiMockSupport();
    }
}
