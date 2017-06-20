package de.rfelgent.tus;

import de.rfelgent.tus.service.AssetFactory;
import de.rfelgent.tus.service.ExpirationService;
import de.rfelgent.tus.service.ExpirationService7Days;
import de.rfelgent.tus.service.UploadLocker;
import de.rfelgent.tus.service.UploadLockerInMemory;
import de.rfelgent.tus.web.MethodOverrideFilter;
import de.rfelgent.tus.web.ProtocolFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * @author rfelgentraeger
 */
@SpringBootApplication
public class AppConfig {

    @Bean
    @ConditionalOnMissingBean(AssetFactory.class)
    public AssetFactory assetFactory() {
        return new AssetFactory();
    }

    @Bean
    @ConditionalOnMissingBean(UploadLocker.class)
    public UploadLocker assetUploadLocker() {
        return new UploadLockerInMemory();
    }

    @Bean
    @ConditionalOnMissingBean(ExpirationService.class)
    public ExpirationService expirationService() {
        return new ExpirationService7Days();
    }

    @Bean
    public FilterRegistrationBean protocolFilterRegistration() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(protocolFilter());
        bean.setName("protocolFilter");
        bean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        bean.addUrlPatterns("/files/*");
        bean.setOrder(1);
        return bean;
    }

    @Bean
    protected ProtocolFilter protocolFilter() {
        ProtocolFilter protocolFilter = new ProtocolFilter();
        return protocolFilter;
    }

    @Bean
    public FilterRegistrationBean methodOverrideFilterRegistration() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(methodOverrideFilter());
        bean.setName("methodOverrideFilter");
        bean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        bean.addUrlPatterns("/files/*");
        bean.setOrder(2);
        return bean;
    }

    @Bean
    protected MethodOverrideFilter methodOverrideFilter() {
        MethodOverrideFilter methodOverrideFilter = new MethodOverrideFilter();
        return methodOverrideFilter;
    }
}
