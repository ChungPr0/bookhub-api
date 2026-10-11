package com.chungpr0.bookhub.modules.catalog.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookhub.media")
public class MediaProperties {

    /**
     * Maximum upload file size in megabytes (MB). Default: 5 MB.
     */
    private int maxFileSizeMb = 5;

    /**
     * Maximum number of files allowed in a single batch upload. Default: 10.
     */
    private int maxBatchFiles = 10;

    /**
     * Base CDN URL for static media assets. Default: https://cdn.bookhub.vn.
     */
    private String cdnBaseUrl = "https://cdn.bookhub.vn";
}

