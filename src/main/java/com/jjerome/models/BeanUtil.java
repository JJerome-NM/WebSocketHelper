package com.jjerome.models;

import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class BeanUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    private final ApplicationContext context;


    public Set<Class<?>> findSpringBootApplicationBeanClass(){
        String[] beanNames = this.context.getBeanNamesForAnnotation(SpringBootApplication.class);

        if (beanNames.length > 1) {
            LOGGER.warn("OMGGGGGGGGGGGGGGG You have more than two SpringBootApplications, this can cause errors");
        } else if (beanNames.length < 1) {
            LOGGER.error("I can't find the SpringBootApplication, I don't know how you started me, " +
                    "but I need the SpringBootApplication");
            return null;
        }

        Reflections reflections = new Reflections(context.getBean(beanNames[0]).getClass().getPackageName());
        return reflections.getTypesAnnotatedWith(SpringBootApplication.class);
    }
}
