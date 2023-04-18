package com.jjerome.models;

import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BeanUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    private static final String SPRING_APP_COUNT_WARN_TEXT = """
            You have more than one @SpringBootApplication in your program, this can lead to errors,
            make sure you need it and add the "scanBasePackages" parameter to your @SpringBootApplication or\s
            use the @ComponentScan annotation, it is important to choose the name of the scanned packages because\s
            the socket application works only with them
            """;

    private static final String SPRING_APP_NOT_FIND_WARN_TEXT = """
            Your application is missing the @SpringBootApplication annotation, it is used as the beginning of\s
            the application without it my web sockets will not work correctly, so please add it.
            """;

    private final ApplicationContext context;


    public Set<Class<?>> findSpringBootApplicationBeanClass(){
        String[] beanNames = this.context.getBeanNamesForAnnotation(SpringBootApplication.class);

        if (beanNames.length > 1) {
            LOGGER.warn(SPRING_APP_COUNT_WARN_TEXT);
        } else if (beanNames.length < 1) {
            LOGGER.error(SPRING_APP_NOT_FIND_WARN_TEXT);
            return Collections.emptySet();
        }

        Reflections reflections = new Reflections(context.getBean(beanNames[0]).getClass().getPackageName());
        return reflections.getTypesAnnotatedWith(SpringBootApplication.class);
    }
}
