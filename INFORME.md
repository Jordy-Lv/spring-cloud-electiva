# Informe: Taller Spring Cloud & Spring Client

**Curso:** ELECTIVA I (clase 3281)
**Estudiante:** Yordy Pardo
**Repositorio:** https://github.com/Jordy-Lv/spring-cloud-electiva

---

## 1. Objetivo

Construir una aplicación que **no guarda su propia configuración**, sino que la pide a un servidor central al arrancar. Para lograrlo se crean dos programas:

- un **Config Server** (el servidor que guarda y entrega la configuración), y
- un **Config Client** llamado `loan-service` (la aplicación que la recibe y la usa).

Además, se comprueba que el mismo programa se comporta distinto según el **perfil** con el que se inicia: por defecto, desarrollo (`dev`) o pruebas de aceptación (`uat`).

## 2. ¿Qué es Spring Cloud Config?

Cuando una aplicación crece, es común tener varios programas y varios ambientes (desarrollo, pruebas, producción), y cada uno necesita su propia configuración: puertos, mensajes, direcciones, etc. Si esa información vive dentro de cada programa, cambiar algo significa tocar el código y volver a instalarlo.

Spring Cloud Config resuelve esto guardando toda la configuración **en un solo lugar**. Es como una biblioteca central: cada aplicación llega, dice su nombre y el ambiente en el que trabaja, y la biblioteca le entrega el archivo que le corresponde.

## 3. Cómo funciona lo que se construyó

```
 ┌──────────────────┐  lee   ┌──────────────────┐ 1. pide  ┌──────────────────┐
 │   config-repo/   │ ─────► │   Config Server  │ ◄─────── │   Config Client  │
 │ archivos         │        │   (puerto 8888)  │ 2. envía │   (loan-service) │
 │ .properties      │        │                  │ ───────► │                  │
 └──────────────────┘        └──────────────────┘          └──────────────────┘
```

1. El cliente arranca y le dice al servidor: "soy `loan-service` y trabajo en el perfil `dev`".
2. El servidor busca los archivos de `loan-service` en `config-repo/` y se los entrega.
3. El cliente usa esos valores (en este caso, **el puerto** en el que se levanta y **un mensaje**).

## 4. Herramientas usadas

| Herramienta | Para qué se usó |
|---|---|
| Java 21 | Lenguaje en el que están hechos los programas |
| Spring Boot 4.1.0 | Base para crear las aplicaciones web |
| Spring Cloud 2025.1.3 | Aporta el Config Server y el Config Client |
| Maven | Descarga las librerías y compila/ejecuta los proyectos |
| Git y GitHub | Control de versiones y entrega del código |

## 5. Desarrollo paso a paso

El trabajo se hizo por fases, y cada fase quedó registrada en un commit del repositorio.

### Fase 1: El Config Server

Es la carpeta `config-server/`. Su parte esencial es una sola anotación, `@EnableConfigServer`, que convierte una aplicación normal en un servidor de configuración:

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication { ... }
```

Su archivo `application.properties` le indica tres cosas:

```properties
server.port=8888                                                   # puerto en el que atiende
spring.profiles.active=native                                      # lee archivos de una carpeta local
spring.cloud.config.server.native.search-locations=file:../config-repo   # cuál carpeta
```

### Fase 2: Los archivos de configuración

Están en la carpeta `config-repo/`. Hay un archivo base y uno por ambiente. Los tres tienen las mismas dos propiedades, pero con valores diferentes:

| Archivo | Puerto | Mensaje |
|---|---|---|
| `loan-service.properties` | 8080 | Perfil por defecto |
| `loan-service-dev.properties` | 8081 | Perfil DEV de desarrollo |
| `loan-service-uat.properties` | 8082 | Perfil UAT de pruebas |

El nombre `loan-service` al inicio de cada archivo es importante: es la forma en que el servidor sabe a qué aplicación pertenece cada uno.

### Fase 3: El Config Client (`loan-service`)

Es la carpeta `config-client/`. Su `application.properties` es muy corto y **no contiene ni el puerto ni el mensaje**:

```properties
spring.application.name=loan-service
spring.profiles.active=dev
spring.config.import=optional:configserver:http://localhost:8888
```

- `spring.application.name`: el nombre con el que se presenta ante el servidor.
- `spring.profiles.active`: el ambiente en el que trabaja.
- `spring.config.import`: la dirección donde está el servidor.

Para comprobar que recibe la configuración, tiene un pequeño servicio web (`/mensaje`) que muestra el perfil, el puerto y el mensaje que le llegaron del servidor.

### Fase 4: Pruebas

Se levantaron ambos programas y se probaron los tres perfiles (ver sección 7).

## 6. Cómo ejecutar el proyecto

Se necesita Java 21 y Maven. Se usan **dos terminales**, y el servidor siempre debe iniciarse primero.

**Terminal 1: Config Server**

```
cd config-server
mvn spring-boot:run
```

**Terminal 2: Config Client** (con el perfil `dev` que trae por defecto)

```
cd config-client
mvn spring-boot:run
```

Para iniciarlo con otro perfil, sin editar ningún archivo:

```
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=uat"
```

## 7. Pruebas y evidencias

### 7.1 El servidor entrega la configuración de cada perfil

Con el servidor encendido, se abren estas direcciones en el navegador (o con `curl`):

- http://localhost:8888/loan-service/default
- http://localhost:8888/loan-service/dev
- http://localhost:8888/loan-service/uat

En `dev` y `uat` el servidor devuelve dos archivos: primero el del perfil y luego el base. Si una propiedad aparece en los dos, se usa la del perfil. Por eso el puerto de `dev` es 8081 y no 8080.

![Captura 1: Respuesta del servidor para el perfil dev](capturas/01-servidor-dev.png)

### 7.2 El cliente recibe la configuración y la usa

Al iniciar el cliente, en la consola se ve que pide la configuración al servidor y que arranca en el puerto que este le indicó:

```
The following 1 profile is active: "dev"
Fetching config from server at : http://localhost:8888
Tomcat started on port 8081 (http)
```

![Captura 2: Consola del cliente arrancando con el perfil dev](capturas/02-consola-cliente-dev.png)

### 7.3 Resultado por perfil

Al abrir `/mensaje` en el puerto correspondiente, se obtuvo:

| Perfil | Dirección | Respuesta |
|---|---|---|
| `dev` | http://localhost:8081/mensaje | `{"perfil":"dev","puerto":"8081","mensaje":"Hola desde loan-service - perfil DEV de desarrollo (puerto 8081)"}` |
| `uat` | http://localhost:8082/mensaje | `{"perfil":"uat","puerto":"8082","mensaje":"Hola desde loan-service - perfil UAT de pruebas (puerto 8082)"}` |
| `default` | http://localhost:8080/mensaje | `{"perfil":"default","puerto":"8080","mensaje":"Hola desde loan-service - perfil por defecto (puerto 8080)"}` |

![Captura 3: /mensaje con el perfil dev](capturas/03-mensaje-dev.png)

![Captura 4: /mensaje con el perfil uat](capturas/04-mensaje-uat.png)

![Captura 5: /mensaje con el perfil default](capturas/05-mensaje-default.png)

### 7.4 Observación

El perfil por defecto usa el puerto 8080, que es muy común. Si otro programa del computador ya lo está usando, el cliente no logra arrancar y muestra el error `Port 8080 was already in use`. Los perfiles `dev` y `uat` no tienen este problema porque usan otros puertos.

## 8. Conclusiones

- La configuración quedó **fuera del código** de la aplicación: `loan-service` no sabe qué puerto ni qué mensaje usará hasta que se lo pide al servidor.
- Cambiar de ambiente es tan simple como cambiar el nombre del perfil al iniciar; no hace falta modificar ni recompilar el cliente.
- Tener toda la configuración en un solo lugar (`config-repo/`) facilita mantenerla ordenada cuando hay varias aplicaciones y varios ambientes.
- El servidor debe estar encendido antes que el cliente. Si no lo está, el cliente no obtiene su configuración y falla al arrancar.
