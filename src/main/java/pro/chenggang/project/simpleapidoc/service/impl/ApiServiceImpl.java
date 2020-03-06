package pro.chenggang.project.simpleapidoc.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.FetchResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties;
import pro.chenggang.project.simpleapidoc.service.ApiService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final File htmlFile;
    private final File repositoryFile;

    @Override
    public void generateHtml() {
        List<File> htmlFileList = getFilesBySuffix(htmlFile, ".html");
        List<File> apibFiles = getFilesBySuffix(repositoryFile,".apib");
        boolean alreadyGenerated = false;
        if(htmlFileList.size() == apibFiles.size()){
            alreadyGenerated = true;
        }
        try{
            Ref call = git.checkout().setName(apiSystemProperties.getGitBranch()).call();
            log.debug("Git Checkout :{}",call.getName());
            git.clean().call();
            PullResult pullResult = git.pull().call();
            FetchResult fetchResult = pullResult.getFetchResult();
            MergeResult mergeResult = pullResult.getMergeResult();
            RebaseResult rebaseResult = pullResult.getRebaseResult();
            boolean nothingFetched = Objects.isNull(fetchResult) || StringUtils.isEmpty(fetchResult.getMessages());
            boolean nothingMerged = Objects.isNull(mergeResult) || MergeResult.MergeStatus.ALREADY_UP_TO_DATE.equals(mergeResult.getMergeStatus());
            boolean nothingRebased = Objects.isNull(rebaseResult);
            if(nothingFetched && nothingMerged && nothingRebased && alreadyGenerated){
                log.debug("Git Repository Nothing Changed");
                return;
            }
            if(Objects.nonNull(fetchResult)){
                log.debug("Git Fetch :{}", fetchResult.getMessages());
            }
            if(Objects.nonNull(mergeResult) || Objects.nonNull(rebaseResult)){
                log.debug("Git Repository Update :{}", pullResult.isSuccessful());
            }
        }catch (Exception e){
            log.error("Checkout Git Error,Nothing to do,Error Message:{}",e.getMessage());
            return;
        }
        if(!apibFiles.isEmpty()){
            apibFiles.forEach(item->{
                log.info("Load Apib File :{}",item.getPath());
            });
        }
        apibFiles.forEach(item->{
            String path = item.getPath();
            String targetHtmlFileName = path.replace(API_REPOSITORY_DIR_NAME, API_HTML_DIR_NAME).replace(".apib", ".html");
            String command = "aglio --theme-full-width -i " + path + " -o " + targetHtmlFileName;
            this.execCommand(command,true);
            log.info("Generate HTML :{}",targetHtmlFileName);
        });
    }

    private List<File> getFilesBySuffix(File parentFile,String suffix){
        File[] listFiles = parentFile.listFiles((file, name) -> name.endsWith(suffix));
        if(Objects.isNull(listFiles) || listFiles.length ==0){
            return Collections.emptyList();
        }
        return Stream.of(listFiles)
                .map(item->{
                    if(item.isDirectory()){
                        return getFilesBySuffix(item,suffix);
                    }
                    return Collections.singletonList(item);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Async
    @Override
    public void execApiMockSupport(){
        String apiLocation = apiSystemProperties.getLocation() + API_HTML_DIR_NAME;
        String command = "drakov -f "+apiLocation+"/*.apib -p 3000 --stealthmode --public --watch ";
        this.execCommand(command,true);
    }

    private void execCommand(String command,boolean sysout){
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command,null,null);
            if(sysout){
                InputStream stderr =  proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stderr, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String line="";
                while ((line = br.readLine()) != null) {
                    log.debug(line);
                }
            }
        }catch (Exception e){
            log.error("Exec Command Error,Message:{}",e.getMessage());
        }
    }
}
