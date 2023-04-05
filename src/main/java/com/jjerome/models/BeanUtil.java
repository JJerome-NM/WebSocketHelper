package com.jjerome.models;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BeanUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    private final ApplicationContext context;

    public BeanUtil(ApplicationContext context){
        this.context = context;
    }


    public Set<Class<?>> findSpringBootApplicationBeanClass(){
        String[] beanNames = this.context.getBeanNamesForAnnotation(SpringBootApplication.class);

        if (beanNames.length > 1) {
            LOGGER.warn("OMGGGGGGGGGGGGGGG You have more than two SpringBootApplications, this can cause errors");
        } else if (beanNames.length < 1) {
            LOGGER.error("Mot find");
            return null;
        }

        Reflections reflections = new Reflections(context.getBean(beanNames[0]).getClass().getPackageName());
        return reflections.getTypesAnnotatedWith(SpringBootApplication.class);
    }

    public ApplicationContext getContext() {
        return context;
    }
}
