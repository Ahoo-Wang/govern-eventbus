package me.ahoo.eventbus.spring.support;

import me.ahoo.eventbus.spring.annotation.EnableEventBus;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author : ahoo wang
 * @see EnableEventBus
 */
public class EventBusConfigurationSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]
                {
                        EventBusBootstrapConfiguration.class.getName()
                };
    }
}
