package com.ourvoiceourrights;

import com.ourvoiceourrights.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(AppProperties.class)
public class OurVoiceOurRightsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OurVoiceOurRightsApplication.class, args);
	}
}
