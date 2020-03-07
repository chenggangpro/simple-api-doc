package pro.chenggang.project.simpleapidoc.configuration;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties;

import java.io.File;
import java.io.IOException;

import static pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties.API_HTML_DIR_NAME;
import static pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties.API_MOCK_DIR_NAME;
import static pro.chenggang.project.simpleapidoc.properties.ApiSystemProperties.API_REPOSITORY_DIR_NAME;

/**
 * @author: chenggang
 * @date 2020-02-25.
 */
@Slf4j
@EnableAsync
@EnableScheduling
@Configuration
public class SystemConfiguration {


    @ConfigurationProperties(prefix = ApiSystemProperties.PREFIX)
    @Bean
    public ApiSystemProperties apiSystemProperties(){
        return new ApiSystemProperties();
    }

    @Bean
    public Git git(ApiSystemProperties apiSystemProperties){
        File file = new File(apiSystemProperties.getLocation()+ API_REPOSITORY_DIR_NAME+"/");
        try{
            Git git;
            if(file.exists()){
                git = Git.open(file);
                git.checkout().setName(apiSystemProperties.getGitBranch()).call();
                git.clean().call();
                git.pull().call();
            }else{
                file.mkdirs();
                CloneCommand cloneCommand = Git.cloneRepository()
                        .setBranch(apiSystemProperties.getGitBranch())
                        .setURI(apiSystemProperties.getGitUrl())
                        .setDirectory(file);
                if(!StringUtils.isEmpty(apiSystemProperties.getGitUsername()) && !StringUtils.isEmpty(apiSystemProperties.getGitPassword())){
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(apiSystemProperties.getGitUsername(), apiSystemProperties.getGitPassword()));
                }
                git = cloneCommand.call();
            }
            log.info("Load Api Git Repository Success");
            return git;
        } catch (InvalidRemoteException e) {
            log.error("Remote Git Is Invalid,Message:{}",e.getMessage());
        } catch (IOException e) {
            log.error("Git Dir IO Exception,Message:{}",e.getMessage());
        } catch (TransportException e) {
            log.error("Git Transport Exception,Message:{}",e.getMessage());
        } catch (GitAPIException e) {
            log.error("Git API Exception,Message:{}",e.getMessage());
        }
        throw new UnsupportedOperationException("Can Not Load Api Repository ,Please Check Git Settings");
    }

    @Bean
    public File repositoryFile(ApiSystemProperties apiSystemProperties){
        String apiLocation = apiSystemProperties.getLocation() + API_REPOSITORY_DIR_NAME + "/";
        return new File(apiLocation);
    }

    @Bean
    public File htmlFile(ApiSystemProperties apiSystemProperties){
        String apiLocation = apiSystemProperties.getLocation() + API_HTML_DIR_NAME + "/";
        File file =  new File(apiLocation);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    @Bean
    public File mockFile(ApiSystemProperties apiSystemProperties){
        String mockLocation = apiSystemProperties.getLocation() + API_MOCK_DIR_NAME + "/";
        File file =  new File(mockLocation);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }
}
