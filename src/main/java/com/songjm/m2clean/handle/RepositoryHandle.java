package com.songjm.m2clean.handle;

import com.songjm.m2clean.config.CleanConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Title: com.songjm.m2clean.handle.RepositoryHandle
 * <p>
 * Description:
 * Maven 目录处理，删除本地 maven 仓库 .m2 中以下文件:
 * 	1.无效的文件: 下载失败的无效版本文件
 * 	2.过期 SNAPSHOT 文件: 有对应正式包的 SNAPSHOT 目录
 * 	3.最后更新日期相比当前超过指定的有效期天数的 SNAPSHOT 目录
 * </p>
 *
 * @Author: songjm
 * @CreateTime: 2019/8/6 09:44
 */
@Component
@Log4j2
public class RepositoryHandle {
    
    // 无效目标列表
    private static final List<String> INVALID_REPOSITORY = new ArrayList<>();

    // 过期 Snapshot 目录列表
    private static final List<String> EXPIRED_SNAPSHOT_REPOSITORY = new ArrayList<>();

    // 已经有 Release 包的 Snapshot 目录列表
    private static final List<String> RELEASED_SNAPSHOT_REPOSITORY = new ArrayList<>();

    // Snapshot 目录名后缀
    private static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT";

    // 一天的毫秒值
    private static final long DAY_MILLS = 24 * 3600 * 1000L;

    @Autowired
    CleanConfig cleanConfig;

    /**
     * 处理入口
     */
    public void handle() {
        // 检查 .m2 repository 是否有效
        if (cleanConfig.getM2Repository() == null
                || cleanConfig.getM2Repository().isEmpty()) {
            throw new RuntimeException("Maven repository path is null!!!");
        }

        File repository = new File(cleanConfig.getM2Repository());
        if (!(repository.exists() && repository.isDirectory())) {
            throw new RuntimeException("Maven repository (" + cleanConfig.getM2Repository() + ") is invalid!!!");
        }

        for (File file : repository.listFiles()) {
            checkValid(file);
        }

        log.info("INVALID_REPOSITORY ===================== {}", INVALID_REPOSITORY.size());
        if (!INVALID_REPOSITORY.isEmpty()) {
            INVALID_REPOSITORY.forEach(repositoryPath -> {
                boolean deleteFlag = deleteFile(new File(repositoryPath));
                if (deleteFlag) {
                    log.info("Repository[{}] delete success", repositoryPath);
                } else {
                    log.error("Repository[{}] delete failed", repositoryPath);
                }
            });

        }
        log.info("INVALID_REPOSITORY =====================");

        log.info("EXPIRED_SNAPSHOT_REPOSITORY ===================== {}", EXPIRED_SNAPSHOT_REPOSITORY.size());
        if (!EXPIRED_SNAPSHOT_REPOSITORY.isEmpty()) {
            EXPIRED_SNAPSHOT_REPOSITORY.forEach(repositoryPath -> {
                boolean deleteFlag = deleteFile(new File(repositoryPath));
                if (deleteFlag) {
                    log.info("Repository[{}] delete success", repositoryPath);
                } else {
                    log.error("Repository[{}] delete failed", repositoryPath);
                }
            });
        }
        log.info("EXPIRED_SNAPSHOT_REPOSITORY =====================");

        log.info("RELEASED_SNAPSHOT_REPOSITORY ===================== {}", RELEASED_SNAPSHOT_REPOSITORY.size());
        if (!RELEASED_SNAPSHOT_REPOSITORY.isEmpty()) {
            RELEASED_SNAPSHOT_REPOSITORY.forEach(repositoryPath -> {
                boolean deleteFlag = deleteFile(new File(repositoryPath));
                if (deleteFlag) {
                    log.info("Repository[{}] delete success", repositoryPath);
                } else {
                    log.error("Repository[{}] delete failed", repositoryPath);
                }
            });
        }
        log.info("RELEASED_SNAPSHOT_REPOSITORY =====================");
    }

    /*
     * 判断目录是否需要清理，如果需要清理则 add 到相应的清理目录
     *
     * @param file 检查的文件
     * @return true-有效，false-无效
     */
    public boolean checkValid(File file) {
        boolean isValid = false;

        if (file == null || !file.isDirectory()) {
            return isValid;
        }

        if (cleanConfig.getIgnorFiles() != null && cleanConfig.getIgnorFiles().contains(file.getName())) {
            return true;
        }

        // 判断目录是否为 SNAPSHOT 目录
        if (file.getName().endsWith(SNAPSHOT_VERSION_SUFFIX)) {
            String releaseVersion = file.getName().substring(0, file.getName().length() - SNAPSHOT_VERSION_SUFFIX.length());

            // 判断 Snapshot 版本同目录是否有相应的正是包
            String[] releaseFileNames = file.getParentFile().list((versionFile, versionFileName) -> versionFileName.equals(releaseVersion));
            if (releaseFileNames != null && releaseFileNames.length > 0) {
                if (cleanConfig.isCleanReleasedSnapshot()) {
                    RELEASED_SNAPSHOT_REPOSITORY.add(file.getAbsolutePath());
                }
                return false;
            } else {
                // 判断是否过期的 Snapshot 目录
                if (cleanConfig.isCleanExpiredSnapshot()) {
                    long validTime = System.currentTimeMillis() - file.lastModified();
                    if (validTime / DAY_MILLS > cleanConfig.getSnapshotExpiredDays()) {
                        EXPIRED_SNAPSHOT_REPOSITORY.add(file.getAbsolutePath());
                    }
                }
            }

        }

        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            boolean innerIsValid;
            for (File innerFile : files) {
                if (innerFile.getName().endsWith(".pom")) {
                    isValid = true;
                    break;
                }

                if (innerFile.getName().endsWith(".jar")) {
                    isValid = true;
                    break;
                }

                if (innerFile.isDirectory()) {
                    innerIsValid = checkValid(innerFile);
                    if (!isValid && innerIsValid) {
                        isValid = true;
                    }
                }
            }
        }

        if (!isValid && cleanConfig.isCleanInvalidRepository()) {
            INVALID_REPOSITORY.add(file.getAbsolutePath());
        }

        return isValid;
    }

    /**
     * 删除文件
     *
     * @param file 要删除的文件
     * @return false-删除失败，true-删除成功
     */
    public boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        } else {
            if (file.listFiles() != null && file.listFiles().length > 0) {
                boolean deleteFlag = true;
                for (File innerFile : file.listFiles()) {
                    if (!deleteFile(innerFile)) {
                        deleteFlag = false;
                        break;
                    }
                }
                return deleteFlag;
            } else {
                return file.delete();
            }
        }
    }
}
