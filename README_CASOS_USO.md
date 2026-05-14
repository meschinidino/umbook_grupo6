# Documentación de Implementación - Módulo: Gestión de Amigos

Este documento detalla cómo se han implementado a nivel de código los casos de uso correspondientes al módulo de **Gestión de Amigos** en el proyecto Umbook.

---

## 1. CU0007: Crear grupo de amigos

Permite a los usuarios organizar a sus amigos en grupos para luego asignarles permisos específicos.

- **Rutas HTTP:**
  - `GET /groups/create`: Muestra el formulario de creación.
  - `POST /groups/create`: Procesa la creación del grupo.
- **Controlador:** `GroupController.handleCreateGroup`
- **Servicio:** `GroupService.createGroup`
- **Modelo/Entidad:** `Group`
- **DTO de entrada:** `CreateGroupRequest`

### Detalles de Implementación:
- **Validaciones:** Se verifica que el nombre del grupo no esté vacío y que el usuario creador no tenga ya otro grupo activo con el mismo nombre.
- **Creación:** Se instancia la entidad `Group` asignándole el usuario creador, la descripción y los permisos base (`postPermission`, `commentPermission`, `invitePermission`).
- **Estados (State Machine):** Al instanciarse, el grupo nace por defecto en estado `CON_MIEMBROS`, debido a que la lógica de negocio añade automáticamente al creador como el primer miembro del grupo.

---

## 2. CU0008: Asignar amigo a grupo

Permite ubicar a uno o más amigos dentro de un grupo ya existente.

- **Rutas HTTP:**
  - `GET /groups/{groupId}/add-members`: Muestra la lista de amigos disponibles.
  - `POST /groups/{groupId}/add-members`: Añade los amigos seleccionados al grupo.
- **Controlador:** `GroupController.handleAddMembers`
- **Servicio:** `GroupService.addMembersToGroup`
- **Vista principal:** `add_members_to_group.html`

### Detalles de Implementación:
- **Filtrado Visual:** En la vista, el sistema se encarga de filtrar la lista de amigos del usuario para mostrar únicamente aquellos que **no** pertenezcan actualmente al grupo.
- **Validaciones:** Se exige que el usuario seleccione al menos a un amigo (la lista `memberIds` no puede estar vacía).
- **Asignación:** Los usuarios seleccionados se añaden a la colección `members` de la entidad `Group`.
- **Estados (State Machine):** Si el grupo se encontraba en un estado `SIN_MIEMBROS`, esta acción actualiza su estado automáticamente a `CON_MIEMBROS`.

---

## 3. CU0009: Asignar permisos por grupo

Permite definir qué acciones (postear, comentar, invitar) están permitidas para un grupo.

- **Rutas HTTP:**
  - `GET /groups/{groupId}/edit-permissions`: Muestra el formulario de edición de permisos.
  - `POST /groups/{groupId}/edit-permissions`: Guarda los nuevos permisos.
- **Controlador:** `GroupController.handleEditGroupPermissions`
- **Servicio:** `GroupService.setGroupPermissions`

### Detalles de Implementación:
- **Niveles de Permiso:** Los permisos se gestionan a través del enumerador `GroupPermission` (ej. `MEMBERS_ONLY`, `ADMIN_ONLY`).
- **Seguridad:** Existe una validación estricta para asegurar que únicamente el creador original del grupo tenga los privilegios necesarios para modificar estos permisos.

---

## 4. CU0010: Crear Álbum

Permite definir un nuevo álbum de fotos, asignándole un nombre, descripción y políticas de acceso.

- **Rutas HTTP:**
  - `GET /albums/create`: Muestra el formulario de creación de álbum.
  - `POST /albums/create`: Procesa la creación del álbum.
- **Controlador:** `AlbumController.handleCreateAlbum`
- **Servicio:** `AlbumService.createAlbum`
- **Modelo/Entidad:** `Album`

### Detalles de Implementación:
- **Configuración de Acceso:** Durante la creación, el usuario puede seleccionar grupos específicos de amigos a los que se les otorgará permiso para **Ver** (`permittedToView`) y/o **Comentar** (`permittedToComment`) el álbum.
- **Validaciones:** El sistema impide que un usuario tenga dos álbumes con exactamente el mismo nombre.
- **Estados (State Machine):** Un álbum recién creado nace por defecto en estado `VACIO`.

---

## 5. CU0011: Subir fotos al álbum

Permite subir imágenes a un álbum previamente creado.

- **Rutas HTTP:**
  - `GET /albums/{albumId}/upload`: Muestra el formulario de subida de fotos.
  - `POST /albums/{albumId}/upload`: Procesa la subida del archivo de imagen.
- **Controlador:** `AlbumController.handleUploadPhoto`
- **Servicio:** `PhotoService.uploadPhoto`
- **Modelo/Entidad:** `Photo`

### Detalles de Implementación:
- **Restricciones Técnicas:** Se valida estrictamente que el archivo subido sea una imagen en formato JPG, PNG o GIF, y que su tamaño no exceda los 5MB.
- **Seguridad:** Solo el propietario del álbum puede subir fotos a él.
- **Almacenamiento:** El archivo binario de la imagen se persiste directamente en la base de datos utilizando el tipo de columna `LONGBLOB`.
- **Estados (State Machine):** Al subirse la primera foto con éxito, el álbum transita de su estado `VACIO` al estado `CON_FOTO`.

---

## 6. CU0012: Eliminar foto del álbum

Permite a los usuarios eliminar fotos.

- **Rutas HTTP:**
  - `GET /photos/{photoId}/delete`: Ejecuta la acción de eliminación (requiere `albumId` como parámetro de consulta).
- **Controlador:** `AlbumController.handleDeletePhoto`
- **Servicio:** `PhotoService.deletePhoto`

### Detalles de Implementación:
- **Seguridad:** El sistema valida que la acción de borrado solo pueda ser ejecutada por el usuario que subió la foto originalmente, o en su defecto, por el dueño del álbum.
- **Borrado:** La foto se elimina físicamente de la base de datos y se desvincula de la colección del álbum.
- **Estados (State Machine):** Si tras eliminar la foto, la colección de fotos del álbum queda vacía, el estado del álbum retrocede automáticamente al estado `VACIO`.