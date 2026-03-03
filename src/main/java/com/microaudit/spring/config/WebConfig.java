package com.microaudit.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.microaudit.dao.ActionDAO;
import com.microaudit.dao.AuditLogDAO;
import com.microaudit.dao.CommitteeDAO;
import com.microaudit.dao.UserDAO;
import com.microaudit.analytics.DelayDetectionEngine;
import com.microaudit.analytics.StreamsAnalytics;

/**
 * Spring MVC Configuration — defines beans, view resolver, and component scan.
 * Covers: Spring Core DI, Spring MVC practical
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.microaudit.spring")
public class WebConfig implements WebMvcConfigurer {

    /**
     * JSP view resolver — maps logical view names to /WEB-INF/jsp/*.jsp
     */
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    /**
     * Serve static resources (CSS, JS) from /css/ and /js/ directories.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
        registry.addResourceHandler("/pages/**").addResourceLocations("/pages/");
    }

    // ── Dependency Injection — DAO Beans ─────
    @Bean
    public UserDAO userDAO() { return new UserDAO(); }

    @Bean
    public ActionDAO actionDAO() { return new ActionDAO(); }

    @Bean
    public AuditLogDAO auditLogDAO() { return new AuditLogDAO(); }

    @Bean
    public CommitteeDAO committeeDAO() { return new CommitteeDAO(); }

    // ── Analytics Beans ──────────────────────
    @Bean
    public StreamsAnalytics streamsAnalytics() { return new StreamsAnalytics(); }

    @Bean
    public DelayDetectionEngine delayDetectionEngine() { return new DelayDetectionEngine(); }
}
