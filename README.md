# Bluetooth Sync 🎧🔗

**Compensador de latencia Bluetooth para Android**

Aplicación que permite sincronizar dispositivos Bluetooth con diferentes latencias usando un delay ajustable manualmente. Ahora con **captura de audio del sistema** para aplicar delay a cualquier app (YouTube, Spotify, Netflix, juegos, etc.).

## ✨ Características

### Modo Metrónomo (Calibración)
- 🔍 **Detección automática** de dispositivo Bluetooth conectado
- 🎛️ **Perilla grande** para ajuste fino de delay (0-500ms)
- 📊 **Visualización en tiempo real** de las ondas de audio
- 🎵 **Metrónomo de prueba** para calibrar a oído
- 👆 **Test de click** para verificar sincronización

### Modo Sistema (Captura global) 🆕
- 🎮 **Captura audio de cualquier app** (YouTube, Spotify, Netflix, juegos)
- 🔄 **Aplica delay en tiempo real** mientras reproduces
- 📱 **Funciona en segundo plano** con servicio de accesibilidad
- 🔔 **Notificación persistente** con control del delay

### Base de datos
- 💾 **50+ dispositivos** con latencias típicas por marca
- 🏷️ **Marcas soportadas**: Apple, Samsung, Sony, Bose, JBL, Sennheiser, Soundcore, Xiaomi, Google, Nothing, OnePlus, Huawei, Edifier, Gaming
- 🎯 **Presets por codec**: aptX LL, Samsung SSC, AAC, LDAC, SBC

## 🏗️ Arquitectura

```
app/
├── src/main/java/com/bluetoothsync/app/
│   ├── MainActivity.kt              # UI principal (Jetpack Compose)
│   ├── CaptureActivity.kt           # Actividad para iniciar MediaProjection
│   ├── SyncViewModel.kt             # Lógica de negocio + estados
│   ├── BluetoothSyncApp.kt          # Application class
│   ├── audio/
│   │   ├── AudioEngine.kt           # Motor de audio (metrónomo + delay)
│   │   ├── AudioPlaybackCapture.kt  # Captura de audio del sistema (Android 10+)
│   │   └── AudioDelayProcessor.kt   # Procesador de delay con ring buffer
│   ├── data/
│   │   ├── BluetoothDeviceProfile.kt
│   │   └── LatencyDatabase.kt       # 50+ dispositivos por marca
│   ├── service/
│   │   ├── AudioDelayService.kt     # Servicio en segundo plano (metrónomo)
│   │   └── BluetoothSyncAccessibilityService.kt  # Servicio de accesibilidad
│   └── ui/components/
│       ├── BigKnob.kt               # Perilla giratoria táctil
│       ├── WaveVisualizer.kt        # Ondas animadas en tiempo real
│       └── DeviceCard.kt            # Selector de dispositivo + búsqueda
└── src/main/res/
    └── xml/accessibility_service_config.xml
```

## 🚀 Cómo compilar

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 35 (compileSdk)
- Dispositivo con Android 10+ (API 29+) para captura del sistema

### Pasos

1. **Abrir en Android Studio**
   ```bash
   # Descomprimir el ZIP
   cd BluetoothSync
   ```

2. **Sincronizar Gradle**
   - Android Studio detectará automáticamente el proyecto
   - Haz clic en "Sync Now" cuando aparezca la barra amarilla
   - Espera a que descargue todas las dependencias

3. **Compilar APK de debug**
   ```bash
   ./gradlew assembleDebug
   ```
   El APK se generará en:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Compilar APK de release**
   ```bash
   ./gradlew assembleRelease
   ```
   El APK se generará en:
   ```
   app/build/outputs/apk/release/app-release-unsigned.apk
   ```
   Para firmar el APK de release, necesitas crear un keystore:
   ```bash
   keytool -genkey -v -keystore bluetoothsync.keystore -alias bluetoothsync -keyalg RSA -keysize 2048 -validity 10000
   ```

5. **Instalar en dispositivo**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 📱 Cómo usar

### Modo Metrónomo (Calibración básica)

1. Conecta tus auriculares Bluetooth
2. Abre la app y selecciona tu dispositivo (o déjala detectarlo)
3. Pulsa **"Metrónomo"** → **"Reproducir"**
4. Gira la perilla hasta que el sonido de los auriculares y el altavoz interno suenen al unísono
5. ¡Listo! El delay ajustado compensa la latencia Bluetooth

### Modo Sistema (Captura de cualquier app) 🆕

1. **Activa el servicio de accesibilidad** (obligatorio):
   - Ve a **Ajustes → Accesibilidad → Servicios de accesibilidad**
   - Busca **"Bluetooth Sync"** y actívalo
   - Confirma los permisos

2. **En la app**, selecciona modo **"Sistema"**

3. **Android pedirá permiso de captura de pantalla**:
   - Acepta (la app solo captura audio, no video)

4. **Abre YouTube, Spotify o cualquier app**
   - El audio se captura automáticamente
   - El delay se aplica en tiempo real
   - Ajusta la perilla mientras escuchas hasta sincronizar

5. **Para detener**, vuelve a la app y pulsa **"Detener"**

## 🔧 Latencias típicas por codec

| Codec | Latencia típica | Uso | Dispositivos |
|-------|----------------|-----|-------------|
| aptX Low Latency | 40 ms | Gaming | SteelSeries, Razer, HyperX, Logitech |
| Samsung SSC | 120 ms | Galaxy Buds | Buds3 Pro, Buds2 Pro |
| AAC | 150 ms | Apple/Android | AirPods, Pixel Buds, Beats |
| aptX | 120 ms | Android | Sennheiser Momentum |
| LDAC | 200 ms | Sony Hi-Res | WH-1000XM5, WF-1000XM5 |
| SBC | 200-250 ms | Genérico | Bose, JBL, Edifier |

## ⚠️ Limitaciones y notas

### Técnicas
- **No modifica el stack Bluetooth** del sistema (imposible sin root)
- La compensación se hace **delayando el otro canal** (cableado o altavoz interno)
- La captura del sistema requiere **Android 10+** (API 29+)
- El servicio de accesibilidad es necesario para mantener la captura activa en segundo plano

### Permisos requeridos
- `RECORD_AUDIO` — Para detección automática de latencia por eco
- `BLUETOOTH_CONNECT` — Para detectar dispositivo conectado
- `FOREGROUND_SERVICE` — Para mantener la app activa
- `BIND_ACCESSIBILITY_SERVICE` — Para captura de audio del sistema
- `POST_NOTIFICATIONS` — Para notificación de servicio activo

### Compatibilidad
- **Android 8.0+** (API 26+) para modo metrónomo
- **Android 10+** (API 29+) para captura del sistema
- Algunas apps con DRM (Netflix, Disney+) pueden bloquear la captura de audio
- Apps de banca y pagos generalmente bloquean MediaProjection por seguridad

## 🔒 Privacidad

- **NO recopilamos información personal**
- El servicio de accesibilidad NO lee el contenido de la pantalla
- Solo captura el audio que TÚ estás reproduciendo
- Todos los datos se procesan localmente en el dispositivo
- Sin servidores, sin analytics, sin tracking

## 🐛 Solución de problemas

### "No aparece el servicio de accesibilidad"
- Ve a **Ajustes → Aplicaciones → Bluetooth Sync → Permisos**
- Asegúrate de que todos los permisos estén concedidos
- Reinicia el dispositivo si es necesario

### "La captura del sistema no funciona"
- Verifica que tienes **Android 10 o superior**
- Asegúrate de que el servicio de accesibilidad esté activo
- Algunas apps (Netflix, Spotify) pueden requerir que desactives "Protección de contenido" en ajustes de desarrollador

### "El delay no se aplica correctamente"
- Usa el modo **Metrónomo** primero para calibrar a oído
- La detección automática por eco funciona mejor en ambientes silenciosos
- Ajusta manualmente con los botones ±1ms y ±10ms

### "Audio distorsionado o con eco"
- Reduce el delay gradualmente
- Asegúrate de que solo un dispositivo de audio esté activo (Bluetooth o cableado, no ambos)
- Prueba con diferentes codecs Bluetooth si tu dispositivo lo permite

## 📄 Licencia

MIT License — Libre para uso personal y comercial.

```
Copyright (c) 2024 Bluetooth Sync Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

Hecho con ❤️ para sincronizar el mundo Bluetooth.

¿Preguntas o problemas? Abre un issue en el repositorio.
