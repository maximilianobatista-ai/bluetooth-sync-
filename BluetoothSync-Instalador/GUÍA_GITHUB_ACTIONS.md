# GitHub Actions - Compilación Automática en la Nube

Esta configuración permite compilar el APK automáticamente en los servidores de GitHub, **sin instalar nada en tu computadora**.

## ✅ Ventajas

- **Sin instalar Android Studio** en tu PC
- **Compilación gratis** (GitHub Actions es gratuito para proyectos públicos)
- **APK listo en 5-10 minutos**
- **Descarga directa** desde GitHub

## 🚀 Pasos para usar

### 1. Crear un repositorio en GitHub

1. Ve a [github.com](https://github.com) e inicia sesión (o crea cuenta gratis)
2. Haz clic en el botón **"+"** → **"New repository"**
3. Nombra el repositorio: `bluetooth-sync`
4. Selecciona **"Public"** (gratis)
5. Haz clic en **"Create repository"**

### 2. Subir el código

#### Opción A: Por web (más fácil)
1. En tu repositorio nuevo, haz clic en **"uploading an existing file"**
2. Arrastra todos los archivos del ZIP descomprimido
3. Escribe como mensaje: "Initial commit"
4. Haz clic en **"Commit changes"**

#### Opción B: Por línea de comandos (si tienes git)
```bash
cd BluetoothSync
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/bluetooth-sync.git
git push -u origin main
```

### 3. Ejecutar la compilación

1. En tu repositorio de GitHub, ve a la pestaña **"Actions"**
2. Verás el workflow **"Build Bluetooth Sync APK"**
3. Haz clic en **"Run workflow"** → **"Run workflow"** (botón verde)
4. Espera 5-10 minutos

### 4. Descargar el APK

1. Ve a la pestaña **"Actions"** → selecciona el workflow que acaba de terminar
2. Desplázate hacia abajo hasta **"Artifacts"**
3. Descarga **"BluetoothSync-Debug-APK"**
4. Descomprime el ZIP → obtienes `app-debug.apk`

### 5. Instalar en tu teléfono

1. Transfiere el APK a tu teléfono (USB, email, Telegram, etc.)
2. En tu teléfono, abre el archivo APK
3. Si pide permiso, ve a **Ajustes → Seguridad → Fuentes desconocidas** y actívalo
4. Instala la app

## 🔄 Compilación automática

Cada vez que subas cambios al repositorio, GitHub compilará automáticamente un nuevo APK. También puedes:

- Ir a **Actions** → **Run workflow** → seleccionar `debug` o `release`
- Los APK se guardan en **Artifacts** por 30 días

## ⚠️ Notas

- El repositorio debe ser **público** para usar GitHub Actions gratis
- Si quieres privado, necesitas GitHub Pro ($4/mes)
- La primera compilación tarda más porque descarga dependencias
- Las siguientes compilaciones son más rápidas (usa cache)

## 🆘 Solución de problemas

### "Workflow no aparece"
- Asegúrate de que el archivo `.github/workflows/build.yml` esté en el repositorio
- La carpeta `.github` debe empezar con punto (es oculta)

### "La compilación falla"
- Ve a **Actions** → selecciona el workflow fallido → **"build"** → revisa los logs
- Los errores comunes son: versión de Java incorrecta, dependencias faltantes

### "No puedo descargar el APK"
- Debes estar logueado en GitHub para descargar artifacts
- Si no funciona, usa la opción de Release (sección "Releases" del repositorio)
