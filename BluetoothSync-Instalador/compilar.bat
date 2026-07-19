@echo off
chcp 65001 >nul
cls
echo ╔══════════════════════════════════════════════════════════╗
echo ║     Bluetooth Sync - Compilador Automatico v1.0          ║
echo ║     Compensador de latencia Bluetooth                    ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

:: Verificar que existe el proyecto
if not exist "BluetoothSync" (
    if not exist "BluetoothSync.zip" (
        echo [ERROR] No se encontro BluetoothSync.zip
        echo Descarga el ZIP del proyecto primero.
        pause
        exit /b 1
    )
    echo [1/6] Descomprimiendo BluetoothSync.zip...
    tar -xf BluetoothSync.zip
    if %ERRORLEVEL% neq 0 (
        powershell -Command "Expand-Archive -Path 'BluetoothSync.zip' -DestinationPath '.' -Force"
    )
)

cd BluetoothSync

:: Verificar Android SDK
echo [2/6] Buscando Android SDK...
if exist "%LOCALAPPDATA%\Android\Sdk" (
    set "ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk"
    echo     SDK encontrado: %ANDROID_SDK%
) else if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk" (
    set "ANDROID_SDK=C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
    echo     SDK encontrado: %ANDROID_SDK%
) else (
    echo [ERROR] Android SDK no encontrado
    echo.
    echo Instala Android Studio desde:
    echo https://developer.android.com/studio
    echo.
    echo Despues de instalarlo, ejecuta este script de nuevo.
    pause
    exit /b 1
)

set "ANDROID_HOME=%ANDROID_SDK%"
set "ANDROID_SDK_ROOT=%ANDROID_SDK%"

:: Verificar Java
echo [3/6] Verificando Java...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java no encontrado. Android Studio incluye Java.
    echo Asegurate de que Android Studio este instalado correctamente.
    pause
    exit /b 1
)
echo     Java OK

:: Compilar
echo [4/6] Compilando APK de debug...
echo     Esto puede tardar 2-5 minutos la primera vez...
echo.

call gradlew.bat assembleDebug --console=plain

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La compilacion fallo.
    echo Intentando solucionar problemas comunes...
    echo.

    :: Intentar con wrapper
    if not exist "gradlew.bat" (
        echo [FIX] Descargando Gradle wrapper...
        gradle wrapper
    )

    echo Reintentando compilacion...
    call gradlew.bat assembleDebug --console=plain

    if %ERRORLEVEL% neq 0 (
        echo [ERROR] La compilacion fallo de nuevo.
        echo Revisa los errores arriba o abre el proyecto en Android Studio.
        pause
        exit /b 1
    )
)

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║  ✅ COMPILACION EXITOSA                                 ║
echo ╚══════════════════════════════════════════════════════════╝
echo.
echo 📱 APK generado en:
echo    app\build\outputs\apk\debug\app-debug.apk
echo.
echo 🔧 Para instalar en tu telefono:
echo    1. Activa "Opciones de desarrollador" en tu telefono
echo    2. Activa "Depuracion USB"
echo    3. Conecta el telefono a la PC
echo    4. Ejecuta: adb install app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📧 O copia el APK manualmente al telefono e instalalo.
echo.

:: Preguntar si instalar
set /p INSTALL="¿Quieres instalar el APK ahora? (S/N): "
if /I "%INSTALL%"=="S" (
    echo Instalando...
    adb install app\build\outputs\apk\debug\app-debug.apk
    if %ERRORLEVEL% == 0 (
        echo ✅ Instalado correctamente
    ) else (
        echo ❌ Error instalando. Conecta el telefono y activa depuracion USB.
    )
)

pause
