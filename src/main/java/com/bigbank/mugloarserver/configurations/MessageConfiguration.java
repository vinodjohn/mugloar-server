package com.bigbank.mugloarserver.configurations;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Configuration class for message source, enabling the application to use externalized messages from 'messages
 * .properties'.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Configuration
public class MessageConfiguration {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}