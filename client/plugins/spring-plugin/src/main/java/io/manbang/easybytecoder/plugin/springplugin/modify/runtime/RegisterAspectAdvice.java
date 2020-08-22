package io.manbang.easybytecoder.plugin.springplugin.modify.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author xujie
 */
public class RegisterAspectAdvice {
    public static void registerAspect( ConfigurableListableBeanFactory beanFactory) {
        Logger log = LoggerFactory.getLogger("RegisterAspectAdvice");
        log.info("注册参数切面");
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition("io.manbang.easybytecoder.plugin.springplugin.modify.runtime.ArgumentPrintAspect").getBeanDefinition();
        registry.registerBeanDefinition("argumentPrintAspect", definition);
    }
}
