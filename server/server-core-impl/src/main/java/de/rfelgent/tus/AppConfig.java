package de.rfelgent.tus;

import de.rfelgent.tus.service.AssetFactory;
import de.rfelgent.tus.service.AssetLocker;
import de.rfelgent.tus.service.AssetLockerInMemory;
import de.rfelgent.tus.service.AssetStorage;
import de.rfelgent.tus.service.AssetStorageInMemory;
import de.rfelgent.tus.service.ExpirationService;
import de.rfelgent.tus.service.ExpirationServiceDays;
import de.rfelgent.tus.service.IdGenerator;
import de.rfelgent.tus.service.IdGeneratorUuid;
import de.rfelgent.tus.service.LocationResolver;
import de.rfelgent.tus.service.LocationResolverAbsolute;
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
    @ConditionalOnMissingBean(LocationResolver.class)
    public LocationResolver locationResolver() {
        return new LocationResolverAbsolute();
    }

    @Bean
    @ConditionalOnMissingBean(AssetStorage.class)
    public AssetStorage assetStorage() {
        return new AssetStorageInMemory();
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator() {
        return new IdGeneratorUuid();
    }

    @Bean
    @ConditionalOnMissingBean(AssetFactory.class)
    public AssetFactory assetFactory() {
        return new AssetFactory();
    }

    @Bean
    @ConditionalOnMissingBean(AssetLocker.class)
    public AssetLocker assetUploadLocker() {
        return new AssetLockerInMemory();
    }

    @Bean
    @ConditionalOnMissingBean(ExpirationService.class)
    public ExpirationService expirationService() {
        return new ExpirationServiceDays();
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
