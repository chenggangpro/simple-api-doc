package pro.chenggang.project.simpleapidoc.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

    private String location = "./";
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
}
