# GameIndex
### Sistema de Preservación y Gestión de Videojuegos

> Proyecto Integrador - Estructuras de Datos / Indexación en Disco

---

## Integrantes
- Yeferson Alexis Salcedo Preciado
- Samuel Velásquez Berrio
- Jonathan Alzate Castaño

---

## ¿Qué es GameIndex?

GameIndex es un sistema que permite registrar, consultar, actualizar y archivar información detallada sobre títulos de videojuegos a lo largo de la historia de la industria. A diferencia de una base de datos convencional, implementa todas sus estructuras de almacenamiento, búsqueda e indexación desde cero, sin depender de ningún motor externo.

---

## Tecnologías

- **Java**
- **Swing** - interfaz gráfica de escritorio
- **Maven** - gestión de dependencias
- **Árbol B+** - indexación y búsqueda eficiente
- **RandomAccessFile** - persistencia en disco

---

## Estructura del Proyecto

El punto de entrada de la aplicación es la clase `VentanaPrincipal`, desde donde se inicializa la interfaz gráfica y se orquesta la interacción con las demás capas del sistema.

```
src/
└── main/
    └── java/
        └── gameindex/
            └── gui/
                └── VentanaPrincipal.java   <-- Clase principal (main)
```

---
## Ejecutable

Si no deseas compilar el proyecto, puedes usar directamente el JAR precompilado incluido en el repositorio:

```bash
java -jar GameIndex.jar
```
> Asegúrate de tener instalado **Java 21+** y **Maven 3.6+**.

---
## Funcionalidades

| Operación | Descripción |
|---|---|
| **Insertar** | Registrar un nuevo videojuego en el sistema |
| **Buscar** | Búsqueda exacta por título |
| **Buscar rango** | Búsqueda por rango alfabético o prefijo |
| **Actualizar** | Modificar un registro existente |
| **Eliminar** | Eliminación lógica sin pérdida física de datos |
| **Listar** | Ver todos los registros activos del catálogo |