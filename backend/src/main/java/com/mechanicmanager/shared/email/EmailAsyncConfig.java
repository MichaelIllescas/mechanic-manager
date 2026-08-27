package com.mechanicmanager.shared.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Pool propio para el envio de emails: la cola vive en memoria y el hilo que
 * atiende la peticion HTTP no espera al servidor SMTP.
 */
@Slf4j
@Configuration
@EnableAsync
public class EmailAsyncConfig implements AsyncConfigurer {
    
    public static final String EMAIL_EXECUTOR = "emailExecutor";
    
    @Bean(EMAIL_EXECUTOR)
    public TaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("email-");
        
        // Al apagar la aplicacion, esperamos a que se vacie la cola en vez de
        // descartar emails ya prometidos al usuario.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        return executor;
    }
    
    /**
     * Un metodo @Async que devuelve void no puede propagar su excepcion a nadie:
     * quien la origino ya recibio su respuesta HTTP. Solo queda registrarla.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Fallo una tarea asincrona. metodo={}", method.getName(), ex);
            new SimpleAsyncUncaughtExceptionHandler().handleUncaughtException(ex, method, params);
        };
    }
}
