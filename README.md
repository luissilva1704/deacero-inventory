## Servicio de Inventario Deacero

API REST con Spring Boot para gestionar productos e inventario entre tiendas. Soporta CRUD de productos, carga inicial de existencias, movimientos de entrada/salida, transferencias entre tiendas, alertas por bajo stock e historial de transacciones

### Funcionalidades
- **Productos**: crear, consultar, actualizar y eliminar con categoría, precio y SKU.
- **Inventario**: inventario por tienda, carga inicial, entradas, salidas y transferencias.
- **Alertas**: alertas de productos con bajo stock.
- **Historial**: historial de transacciones

### Tecnologías
- Java 17, Spring Boot
- Spring Web, Spring Data JPA, Hibernate
- PostgreSQL (configurable por variables de entorno)
- Maven Wrapper (`mvnw`)
+- Docker (build multi-stage)

## Requisitos
- Java 17+
- PostgreSQL 13+ (o compatible)
- Maven (o usar el wrapper `./mvnw`)
- Docker (opcional)

## Configuración
El servicio lee variables de entorno referenciadas en `application.properties`:

- `SPRING_DATASOURCE_URL` (ej.: `jdbc:postgresql://GCP_IP:5432/deacero_inventory_db`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT` (ej.: `8080`)

## Ejecución local
1) Exporta variables de entorno (ejemplo Linux/macOS):

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/inventory"
export SPRING_DATASOURCE_USERNAME="deacerouser1"
export SPRING_DATASOURCE_PASSWORD="Password123#"
export SERVER_PORT="8080"
```

2) Inicia la aplicación de manera local, ejecutando el servicio desde el ID y la base de datos desde docker

```bash
# usando Maven wrapper
Hay que setear las variables de entorno para que mvn pueda reconocerlas
set -a; source .env; set +a (teniendo claro el archivo .env)
./mvnw spring-boot:run

# o compila el jar y ejecútalo
./mvnw -q clean package
java -jar target/inventario-0.0.1-SNAPSHOT.jar
```

La API quedará disponible en `http://localhost:${SERVER_PORT}`.

## Ejecución con Docker (docker compose yaml)
Construye la imagen y levantar los contenedores con docker compose:

```bash
# build (el Dockerfile del repo se llama 'dockerfile')
1.- docker build -t inventario-api .
2.- docker compose up -d

## Referencia de la API
Ruta base: `http://localhost:${SERVER_PORT}/deacero/api/v1`

### Envoltura de respuesta
Todas las respuestas comparten la siguiente estructura:

```json
{
  "success": true,
  "message": "string",
  "data": {},
  "timestamp": "2025-01-01T00:00:00Z",
  "path": "/deacero/api/v1/...",
  "status": 200,
  "code": null
}
```

Los errores usan la misma estructura con `success=false` y `code` definido.

### Paginación
Los endpoints paginados usan los parámetros estándar de Spring:
- `page` (base 0), `size` (por defecto 20), `sort` (ej.: `name,asc`).

---

### Productos
- GET `/products`
  - Parámetros (opcionales): `category`, `minPrice`, `maxPrice`, `stock`, `page`, `size`, `sort`
  - Retorna: `Page<ProductResponse>`

- GET `/products/{id}`
  - Retorna: `ProductResponse`

- POST `/products`
  - Cuerpo `ProductRequest`:
    ```json
    {
      "name": "string",
      "description": "string (opcional)",
      "category": "string",
      "price": 123.45,
      "sku": "SKU-0001"
    }
    ```
  - Retorna: `ProductResponse` creado

- PUT `/products/{id}`
  - Cuerpo: `ProductRequest`
  - Retorna: `ProductResponse` actualizado

- DELETE `/products/{id}`
  - Retorna: `data` vacío

### Inventario y existencias
- GET `/stores/{id}/inventory`
  - Retorna: `List<InventoryItemResponse>` (inventario de una tienda)

- POST `/inventory/load`
  - Cuerpo `StockLoadRequest`:
    ```json
    {
      "productId": "uuid",
      "storeId": "string",
      "quantity": 100,
      "minStock": 10
    }
    ```
  - Para carga inicial de existencias (o establecer base según reglas de negocio).

- POST `/inventory/in`
  - Cuerpo `MovementRequest`:
    ```json
    {
      "productId": "uuid",
      "storeId": "string",
      "quantity": 5
    }
    ```
  - Registra entrada de existencias a una tienda.

- POST `/inventory/out`
  - Cuerpo `MovementRequest` (igual que arriba)
  - Registra salida de existencias de una tienda.

- POST `/inventory/transfer`
  - Cuerpo `TransferRequest`:
    ```json
    {
      "productId": "uuid",
      "sourceStoreId": "string",
      "targetStoreId": "string",
      "quantity": 3
    }
    ```
  - Transfiere existencias entre tiendas.

- GET `/inventory/alerts`
  - Retorna: `List<LowStockProductResponse>` (productos por debajo del mínimo)

### Historial de transacciones
- GET `/inventory/history`
  - Parámetros (opcionales): `productId`, `storeId`, `page`, `size`, `sort`
  - Retorna: `Page<TransactionResponse>`

## Pruebas
Ejecuta las pruebas unitarias:

```bash
./mvnw -q test
```

## Notas
- El servicio usa validaciones; payloads inválidos devolverán errores con `success=false` y un `code` descriptivo.
- Si habilitas OpenAPI/Swagger en tu entorno, el controlador ya incluye anotaciones para soportar documentación generada. La ruta exacta de Swagger UI depende de tu configuración de SpringDoc (no incluida por defecto).
- En este caso local: http://localhost:8080/swagger-ui/index.html

## Estructura del proyecto (alto nivel)
- `controller/` endpoints REST (`InventoryController`)
- `service/` lógica de negocio
- `repository/` repositorios Spring Data JPA
- `entities/` entidades JPA
- `models/` tipos de petición/respuesta y envoltura de respuesta
- `exception/` excepciones personalizadas y manejador global
- `resources/` propiedades y configuración de logging




## Despliegue en la nube

Desplieuge a GCP:
- configuracion BD
  - configurar base de datos (deacero_inventory_db) postgresql en cloudSql Service (definir ips permitidas para conexión)
  - cargar archivo init.sql
    - psql -h <PUBLIC_IP> -U postgres -d deacero_inventory_db -f init.sql

- cargar contenedor en artifact registry
  - `Instalar` sdk gcp en el SO (ubuntu en este caso) "sudo snap install google-cloud-sdk --classic"
  - `Iniciar sesion` en gcp desde la consola "gcloud auth login"
  - `Situarse` en el proyecto "gcloud config set project [project_name]", "gcloud auth configure-docker us-central1-docker.pkg.dev"
  - `Construir` imagen que se subirá: "docker build -t deacero-inventario-api ."
  - `Definir` etiqueta "docker tag deacero-inventario-api:latest us-central1-docker.pkg.dev/[project_name]/inventory-deacero/deacero-inventario-api:latest
    `Docker push` us-central1-docker.pkg.dev/[project_name]/inventory-deacero/deacero-inventario-api:latest"
  - `Pushear` a artifact registry "" docker tag deacero-inventario-api:latest us-central1-docker.pkg.dev/[project_name]/inventory-deacero/deacero-inventario-api:latest
    docker push us-central1-docker.pkg.dev/[project_name]/inventory-deacero/deacero-inventario-api:latest
  - `Crear` servicio en cloud run y seleccionar el contenedor de artifact registry que acabamos de crear.


## Ejecucion de unit test
```basg
  mvn clean test
  ```
  ir a la ruta target/site/jacoco/index.html para visualizar el reporte

## Ejecucion test profiling
```bash
# situado en la raiz
1.- ejecutar "k6 run src/test/perf/test-products-get-10rps.js"
  ``` 
   Resultado esperado
   
    TOTAL RESULTS 
    checks_total.......: 168     5.599803/s
    checks_succeeded...: 100.00% 168 out of 168
    checks_failed......: 0.00%   0 out of 168
    ✓ status 200
    ...

## Documentacion swagger open api
https://deacero-inventario-api-368147415867.us-central1.run.app/swagger-ui/index.html


## Colection postman
- setear una variable con alcance de coleccion llamada **baseURL** con valor **localhost:8080** o **https://deacero-inventario-api-368147415867.us-central1.run.app**

