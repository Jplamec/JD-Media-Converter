# 🎬 JD Media Converter

**JD Media Converter** es una aplicación de escritorio desarrollada en **Java 25** y **JavaFX** para analizar, optimizar y convertir bibliotecas multimedia destinadas a servidores como **Jellyfin**, **Plex** y **Emby**.

Su objetivo es automatizar la conversión de películas y series mediante una interfaz gráfica sencilla, utilizando **FFmpeg** y **FFprobe** como motor de análisis y conversión.

---

# ✨ Características

- 📂 Exploración recursiva de bibliotecas de películas y series.
- 🎥 Análisis completo mediante **FFprobe**:
  - Resolución
  - Códec de vídeo
  - Códec de audio
  - Duración
  - Bitrate
  - Idiomas
  - Subtítulos
- 🔊 Selección de una única pista de audio para cada conversión.
- 📝 Gestión de subtítulos (mantener o eliminar).
- 🔄 Conversión a **MP4 (H.264 + AAC)** utilizando FFmpeg.
- ⚙️ Calidad y velocidad de conversión configurables.
- ⏹️ Cancelación de conversiones en curso.
- 💾 Configuración persistente mediante JSON.
- 🗄️ Historial de conversiones almacenado en SQLite.

---

# 🖥️ Tecnologías utilizadas

- Java 25
- JavaFX
- Maven
- FFmpeg
- FFprobe
- SQLite
- Gson

---

# 📦 Requisitos

Para ejecutar el proyecto desde el código fuente se necesita:

- JDK 25
- Maven 3.9 o superior

No es necesario instalar FFmpeg manualmente.

La aplicación incluye los ejecutables de **FFmpeg** y **FFprobe** para facilitar su uso al usuario final.

---

# ▶️ Ejecutar el proyecto

```bash
mvn javafx:run
```

---

# 🏗️ Arquitectura

El proyecto sigue una arquitectura modular organizada por responsabilidades:

```
controller/
model/
service/
util/
resources/
```

- **controller** → Controladores JavaFX y navegación.
- **service** → Lógica de negocio, FFmpeg, FFprobe, historial y configuración.
- **model** → Objetos del dominio.
- **util** → Utilidades comunes.
- **resources** → Interfaces FXML, CSS, iconos y recursos.

Los controladores únicamente coordinan la interfaz.

Toda la lógica de negocio se encuentra en la capa **service**.

---

# 🚧 Estado del proyecto

Actualmente la aplicación se encuentra en desarrollo activo.

La primera versión incluye:

- Conversión H.264 + AAC
- Selección de audio
- Gestión básica de subtítulos
- Historial
- Configuración persistente

Futuras versiones añadirán:

- Más perfiles de conversión
- Reglas automáticas por idioma
- Más formatos de salida
- Mejor información de progreso
- Estadísticas de conversión

---

# ⚖️ Software de terceros

Este proyecto utiliza **FFmpeg** , **FFprobe** y **FFplay**, desarrollados por el proyecto FFmpeg.

- Sitio oficial:
  https://ffmpeg.org/

Los ejecutables de FFmpeg incluidos en la aplicación mantienen su licencia original.

JD Media Converter es una aplicación independiente que utiliza FFmpeg como herramienta externa para el análisis y conversión multimedia.

Toda la información relativa a la licencia de FFmpeg puede consultarse en:

https://ffmpeg.org/legal.html

## 📄 Licencia

Este proyecto está distribuido bajo la licencia **MIT**.

Consulta el archivo **LICENSE** para más información.

JD Media Converter utiliza FFmpeg, FFprobe y FFplay, que mantienen sus propias licencias independientes.
---

# 👨‍💻 Autor

**Jose David Plaza**

GitHub:
https://github.com/Jplamec

LinkedIn:
https://www.linkedin.com/in/jos%C3%A9-david-plaza-meca-67203a275