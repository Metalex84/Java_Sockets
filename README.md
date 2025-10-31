# JAVA SOCKETS

Una sencilla implementacion de una aplicacion cliente-servidor en Java.

## Ejecución del Proyecto
Para construir y ejecutar el proyecto, sigue estos pasos desde el directorio raíz (cliente-servidor-tcp/):

### Compilar y Empaquetar el Proyecto

```
mvn clean install
```
Esto generará los archivos .jar ejecutables para el cliente y el servidor en sus respectivas carpetas target/.

### Ejecutar el Servidor

```
java -jar server/target/server-1.0-SNAPSHOT.jar
```

### Ejecutar el Cliente (en otra terminal)

```
java -jar client/target/client-1.0-SNAPSHOT.jar
```