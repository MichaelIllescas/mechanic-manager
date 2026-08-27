package com.mechanicmanager.shared.email;

public interface EmailSender {
    
    /**
     * Encola el envio. Retorna de inmediato: el email sale en un hilo aparte y
     * un fallo posterior no llega a quien lo pidio, solo al log.
     */
    
    void send(EmailMessage message);
}