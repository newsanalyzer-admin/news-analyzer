package org.newsanalyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for handling Hibernate lazy-loaded proxies.
 *
 * This configuration registers the Hibernate6Module which properly handles
 * serialization of lazy-loaded entities and proxies, preventing
 * "ByteBuddyInterceptor" serialization errors.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // Don't force lazy loading during serialization - just serialize as null
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        // Serialize identifier for lazy not loaded objects
        module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        return module;
    }
}
