# 🎬 JD Media Converter

**JD Media Converter** is a desktop application developed in **Java 25** and **JavaFX** to analyze, optimize, and convert media libraries for servers like **Jellyfin**, **Plex**, and **Emby**.

Its goal is to automate the conversion of movies and TV shows through a simple graphical interface, using **FFmpeg** and **FFprobe** as the analysis and conversion engine.

---

# ✨ Features

- 📂 Recursive scanning of movie and TV show libraries.

- 🎥 Comprehensive analysis using **FFprobe**:

- Resolution

- Video codec

- Audio codec

- Duration

- Bitrate

- Languages

- Subtitles
- 🔊 Selection of a single audio track for each conversion.

- 📝 Subtitle management (keep or delete).

- 🔄 Conversion to **MP4 (H.264 + AAC)** using FFmpeg.

⚙️ Configurable conversion quality and speed.

⏹️ Cancellation of ongoing conversions.

💾 Persistent configuration using JSON.

🗄️ Conversion history stored in SQLite.

---

# 🖥️ Technologies used

- Java 25
- JavaFX
- Maven
- FFmpeg
- FFprobe
- SQLite
- Gson

---

# 📦 Requirements

To run the project from source code, you need:

- JDK 25
- Maven 3.9 or higher

It is not necessary to install FFmpeg manually.

The application includes the **FFmpeg** and **FFprobe** executables for ease of use by the end user.

---

# ▶️ Run the project

```bash
mvn javafx:run
```

---

# 🏗️ Architecture

The project follows a modular architecture organized by responsibilities:

```
controller/
model/
service/
util/
resources/
```

- **controller** → JavaFX controllers and navigation.

- **service** → Business logic, FFmpeg, FFprobe, history, and configuration.

- **model** → Domain objects.

- **util** → Common utilities.

- **resources** → FXML interfaces, CSS, icons, and resources.

Controllers only coordinate the interface.

All business logic resides in the **service** layer.

---

# 🚧 Project Status

The application is currently under active development.

The first version includes:

- H.264 + AAC conversion
- Audio selection
- Basic subtitle management
- History
- Persistent settings

Future versions will add:

- More conversion profiles
- Automatic rules per language
- More output formats
- Improved progress information
- Conversion statistics

---

# ⚖️ Third-Party Software

This project uses **FFmpeg**, **FFprobe**, and **FFplay**, developed by the FFmpeg project.

- Official website:

https://ffmpeg.org/

The FFmpeg executables included in the application retain their original license.

JD Media Converter is a standalone application that uses FFmpeg as an external tool for media analysis and conversion.

All information regarding the FFmpeg license can be found at:

https://ffmpeg.org/legal.html

## 📄 License

This project is distributed under the **MIT** license.

See the **LICENSE** file for more information.

JD Media Converter uses FFmpeg, FFprobe, and FFplay, which maintain their own independent licenses.

---

# 👨‍💻 Author

**Jose David Plaza**

GitHub:

https://github.com/Jplamec

LinkedIn:

https://www.linkedin.com/in/jos%C3%A9-david-plaza-meca-67203a275
