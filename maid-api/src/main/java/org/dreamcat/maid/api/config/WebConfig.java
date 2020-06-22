package org.dreamcat.maid.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Create by tuke on 2020/6/16
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        SynchronossPartHttpMessageReader partReader = new SynchronossPartHttpMessageReader();
        partReader.setMaxParts(1);
        partReader.setMaxDiskUsagePerPart(4 * (1L << 30));
        partReader.setEnableLoggingRequestDetails(true);

        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);
        multipartReader.setEnableLoggingRequestDetails(true);

        configurer.defaultCodecs().multipartReader(multipartReader);
    }
}
