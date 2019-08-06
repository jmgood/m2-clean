package com.songjm.m2clean.config;
import	java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Title: com.songjm.m2clean.config.CleanConfig
 * <p>
 * Description: 清理参数配置
 * </p>
 *
 * @Author: songjm
 * @CreateTime: 2019/8/6 10:21
 */
@Configuration
@ConfigurationProperties("clean.config")
@Data
public class CleanConfig {

    private String m2Repository;

    private boolean cleanInvalidRepository;

    private boolean cleanReleasedSnapshot;

    private boolean cleanExpiredSnapshot;

    private int snapshotExpiredDays;

    private List<String> ignorFiles;

}
