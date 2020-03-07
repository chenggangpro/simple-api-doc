package pro.chenggang.project.simpleapidoc.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

/**
 * @author: chenggang
 * @date 2020-02-25.
 */
@Getter
@Setter
@ToString
public class ApiSystemProperties {

    public static final String PREFIX = "api.doc";

    public static final String API_REPOSITORY_DIR_NAME = "api-repository";

    public static final String API_HTML_DIR_NAME = "api-html";

    public static final String API_MOCK_DIR_NAME = "api-mock";

    private String location = "/Users/evans/GitHub/simple-api-doc/";
    /**
     * git url
     */
    private String gitUrl;
    /**
     * git branch
     */
    private String gitBranch = "master";
    /**
     * username
     */
    private String gitUsername;
    /**
     * password
     */
    private String gitPassword;
    /**
     * refresh rate (default 1 minute)
     */
    private Long refreshRate = TimeUnit.MILLISECONDS.convert(1,TimeUnit.MINUTES);

}
