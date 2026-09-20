# Spring Cloud Config - Taller Electiva I

Taller de configuración centralizada con Spring Cloud: un **Config Server** que sirve las propiedades y un **Config Client** (`loan-service`) que las consume.

## Estructura

- `config-server/`: servidor de configuración (puerto 8888)
- `config-client/`: cliente `loan-service`
- `config-repo/`: archivos `.properties` que sirve el servidor

## Ejecución

Requisitos: Java 21 y Maven.

1. Iniciar el servidor:
   ```
   cd config-server
   mvn spring-boot:run
   ```
2. En otra terminal, iniciar el cliente:
   ```
   cd config-client
   mvn spring-boot:run
   ```
