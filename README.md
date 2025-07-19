# 📷 UMbook - Aplicación de Red Social

UMbook es una aplicación web de red social construida con **Spring Boot**, **Thymeleaf**, **Spring Security** y **MySQL**. Permite a los usuarios registrarse, gestionar amigos, crear grupos y compartir álbumes de fotos.

---

## ✨ Características Principales

### 🔐 Autenticación de Usuarios
- Sistema completo de registro e inicio de sesión.
- Contraseñas encriptadas.
- Integración con Spring Security.

### 👥 Gestión de Grupos
- Crear grupos con nombre, descripción y permisos.
- Ciclo de vida del grupo:
    - `Activo`: grupo funcional con miembros.
    - `Sin Miembros`: grupo sin participantes.
    - `Eliminado`: grupo marcado como inactivo.
- Agregar y visualizar miembros de un grupo.
- Control de permisos para:
    - Quién puede postear.
    - Quién puede comentar.
    - Quién puede invitar a otros usuarios.

### 🖼️ Gestión de Álbumes de Fotos
- Crear álbumes personalizados con nombre y descripción.
- Subida de fotos a cada álbum.
- Ciclo de vida del álbum:
    - `Vacío`: sin fotos.
    - `Con Fotos`: contiene imágenes.
    - `Eliminado`: marcado como inactivo.
- Permisos por grupo para visualizar o comentar en los álbumes.

### 🤝 Sistema de Amigos
- Solicitudes de amistad entre usuarios.
- Aceptación y eliminación de amistades.

---

## 🚀 Cómo Ejecutar la Aplicación

### 1. Clonar el repositorio
\`\`\`bash
git clone https://github.com/tu_usuario/umbook.git
cd umbook
\`\`\`

### 2. Configurar la base de datos
Asegurate de tener un servidor **MySQL** activo y accesible.

### 3. Editar el archivo \`application.properties\`
Ubicado en \`src/main/resources/application.properties\`, modificá con tus credenciales de MySQL.

\`\`\`properties
# --- Configuración de MySQL ---
spring.datasource.url=jdbc:mysql://localhost:3306/umbook_db?createDatabaseIfNotExist=true
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña


# --- Configuración para subida de archivos ---
server.tomcat.max-parameter-count=10000
spring.servlet.multipart.max-file-size=10MB
\`\`\`

### 4. Ejecutar la aplicación
Desde tu IDE o por consola con Maven:
\`\`\`bash
./gradlew bootRun
\`\`\`

### 5. Acceder a la aplicación
Abrí tu navegador en:  
http://localhost:8080

---

## 🧱 Tecnologías Utilizadas

- **Java 21**
- **Spring Boot**
- **Spring Security**
- **Thymeleaf**
- **MySQL**

---