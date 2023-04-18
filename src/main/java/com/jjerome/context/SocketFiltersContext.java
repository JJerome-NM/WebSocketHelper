package com.jjerome.context;

import com.jjerome.annotations.FilteringOrder;
import com.jjerome.annotations.SocketMappingFilters;
import com.jjerome.exceptions.ExceptionMessage;
import com.jjerome.filters.FiltersComparator;
import com.jjerome.filters.SocketMethodFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class SocketFiltersContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketFiltersContext.class);

    private final ApplicationContext context;

    public Set<SocketMethodFilter> findAllMethodFilters(Method method){
        Set<SocketMethodFilter> methodFilters = new TreeSet<>(new FiltersComparator<>());

        if (method.isAnnotationPresent(SocketMappingFilters.class)){
            for (Class<? extends SocketMethodFilter> filter :
                    method.getDeclaredAnnotation(SocketMappingFilters.class).filters()){

                if (!filter.isAnnotationPresent(FilteringOrder.class)){
                    LOGGER.warn(filter.getName() + " "
                            + ExceptionMessage.CLASS_DONT_HAVE_FILTERING_ORDER.getMessage());
                }

                methodFilters.add(this.context.getBean(filter));
            }
        }

        return methodFilters;
    }
}
