package pro.chenggang.project.simpleapidoc.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties;
import pro.chenggang.project.simpleapidoc.service.ApiService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties.API_HTML_DIR_NAME;
import static pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties.API_REPOSITORY_DIR_NAME;

/**
 * @author: chenggang
 * @date 2020-02-25.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

    private final ApiSystemProperties apiSystemProperties;
    private final Git git;

    @Override
    public void generateHtml() {
        git.checkout();
        git.clean();
        git.pull();
        String apiLocation = apiSystemProperties.getLocation() + API_REPOSITORY_DIR_NAME + "/";
        String command = "find "+apiLocation+" -name \"*.apib\" | sed 's/.apib//' | xargs -i -t aglio -i {}.apib `echo $aglio` -o {}.html";
        this.execCommand(command);
        File file = new File(apiLocation);
        File targetFile = new File(apiSystemProperties.getLocation()+API_HTML_DIR_NAME +"/");
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        try {
            FileSystemUtils.copyRecursively(file,targetFile);
        } catch (IOException e) {
            log.error("Copy File To Target Location Error,Message:{}",e.getMessage());
        }
    }

    @Async
    @Override
    public void execApiMockSupport(){
        String apiLocation = apiSystemProperties.getLocation() + API_HTML_DIR_NAME;
        String command = "drakov -f "+apiLocation+"/*.apib -p 3000 --stealthmode --public --watch ";
        this.execCommand(command);
    }

    private void execCommand(String command){
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command,null,null);
            InputStream stderr =  proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stderr, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String line="";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }catch (Exception e){
            log.error("Exec Command Error,Message:{}",e.getMessage());
        }
    }
}
