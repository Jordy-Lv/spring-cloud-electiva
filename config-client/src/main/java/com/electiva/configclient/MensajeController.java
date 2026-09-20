package com.electiva.configclient;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MensajeController {

    // Estos valores NO están en este proyecto: vienen del Config Server
    @Value("${app.mensaje}")
    private String mensaje;

    @Value("${server.port}")
    private String puerto;

    // Perfil con el que se arrancó el cliente (dev, uat, ...)
    @Value("${spring.profiles.active:default}")
    private String perfil;

    @GetMapping("/mensaje")
    public Map<String, String> mensaje() {
        return Map.of(
                "perfil", perfil,
                "puerto", puerto,
                "mensaje", mensaje);
    }
}
