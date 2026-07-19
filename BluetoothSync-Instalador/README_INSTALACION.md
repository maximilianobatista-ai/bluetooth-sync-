# 📦 Bluetooth Sync - Paquete de Instalación

**Compensador de latencia Bluetooth para Android**

Este paquete contiene todo lo necesario para compilar e instalar la app.

---

## 🚀 Opción 1: Compilación Local (Más rápido después de la primera vez)

### Requisitos
- **Windows 10/11** o **Mac/Linux**
- **Android Studio** instalado (~1GB)
  - Descarga: [developer.android.com/studio](https://developer.android.com/studio)
- **BluetoothSync.zip** (código fuente incluido en este paquete)

### Windows

1. Instala Android Studio (solo la primera vez)
2. Extrae este ZIP en cualquier carpeta
3. Doble clic en **`compilar.bat`**
4. Espera 2-5 minutos
5. El APK aparece en `BluetoothSync/app/build/outputs/apk/debug/`
6. Copia `app-debug.apk` a tu teléfono e instálalo

### Mac / Linux

1. Instala Android Studio (solo la primera vez)
2. Extrae este ZIP en cualquier carpeta
3. Abre Terminal en esa carpeta
4. Ejecuta:
   ```bash
   chmod +x compilar.sh
   ./compilar.sh
   ```
5. Espera 2-5 minutos
6. El APK aparece en `BluetoothSync/app/build/outputs/apk/debug/`
7. Copia `app-debug.apk` a tu teléfono e instálalo

---

## ☁️ Opción 2: GitHub Actions (Sin instalar nada)

### Requisitos
- Cuenta gratis en [GitHub](https://github.com)
- Navegador web

### Pasos

1. Crea un repositorio nuevo en GitHub (público, gratis)
2. Sube todos los archivos del ZIP `BluetoothSync/` (arrastra y suelta en la web)
3. Ve a la pestaña **"Actions"** en tu repositorio
4. Haz clic en **"Run workflow"** → **"Run workflow"**
5. Espera 5-10 minutos
6. Descarga el APK desde **Artifacts**

📖 Lee **`GUÍA_GITHUB_ACTIONS.md`** para instrucciones detalladas con imágenes.

---

## 📱 Instalación en el teléfono

### Método 1: USB + ADB (Recomendado para desarrolladores)
```bash
adb install app-debug.apk
```

### Método 2: Transferencia manual (Más fácil)
1. Envía el APK a tu teléfono por:
   - Email
   - Telegram/WhatsApp (como documento)
   - Google Drive
   - Cable USB
2. En tu teléfono, abre el archivo APK
3. Si aparece "Fuentes desconocidas":
   - Ve a **Ajustes → Seguridad → Fuentes desconocidas** → Actívalo
   - O toca **"Configuración"** en el diálogo que aparece
4. Toca **"Instalar"**

---

## 📋 Contenido de este paquete

| Archivo | Descripción |
|---------|-------------|
| `BluetoothSync.zip` | Código fuente completo del proyecto |
| `compilar.bat` | Script automático para Windows |
| `compilar.sh` | Script automático para Mac/Linux |
| `GUÍA_GITHUB_ACTIONS.md` | Tutorial paso a paso para compilar en la nube |
| `README_INSTALACION.md` | Este archivo |

---

## 🎯 ¿Qué hace la app?

Bluetooth Sync compensa la latencia de los auriculares Bluetooth retrasando el canal más rápido (cableado o altavoz interno) hasta que ambos suenen sincronizados.

### Modos de uso

| Modo | Para qué sirve | Cómo usar |
|------|---------------|-----------|
| **🎵 Metrónomo** | Calibrar "a oído" antes de usar apps | Reproduce clicks, gira la perilla hasta que suenen al unísono |
| **📱 Sistema** | Aplicar delay a cualquier app (YouTube, Spotify, Netflix) | Activa accesibilidad, selecciona modo Sistema, abre tu app favorita |

### Marcas soportadas (50+ dispositivos)

Apple, Samsung, Sony, Bose, JBL, Sennheiser, Soundcore, Xiaomi, Google, Nothing, OnePlus, Huawei, Edifier, Gaming (SteelSeries, Razer, HyperX, Logitech)

---

## ⚠️ Requisitos del teléfono

| Característica | Versión mínima |
|---------------|----------------|
| Android | 8.0 (API 26) para modo metrónomo |
| Android | 10 (API 29) para captura del sistema |
| Bluetooth | Cualquier versión |

---

## 🆘 Ayuda

### "No compila"
- Asegúrate de que Android Studio esté instalado
- El script detecta automáticamente el SDK, pero si falla, abre el proyecto en Android Studio y compila desde ahí

### "No puedo instalar el APK"
- Activa **"Fuentes desconocidas"** en Ajustes → Seguridad
- Algunos teléfonos requieren activar "Instalar apps de esta fuente" específicamente

### "La app no captura audio del sistema"
- Ve a **Ajustes → Accesibilidad → Bluetooth Sync** → Actívalo
- Acepta el permiso de captura de pantalla cuando aparezca
- Solo funciona en Android 10+

### "El delay no se siente"
- Usa auriculares Bluetooth + altavoz cableado (o altavoz interno del teléfono)
- El delay solo es noticeable si hay dos fuentes de sonido simultáneas

---

## 📄 Licencia

MIT License - Libre para uso personal y comercial.

Hecho con ❤️ para sincronizar el mundo Bluetooth.
