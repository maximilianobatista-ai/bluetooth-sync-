#!/bin/bash
set -e

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     Bluetooth Sync - Compilador Automático v1.0          ║"
echo "║     Compensador de latencia Bluetooth                    ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que existe el proyecto
if [ ! -d "BluetoothSync" ]; then
    if [ ! -f "BluetoothSync.zip" ]; then
        echo -e "${RED}[ERROR]${NC} No se encontró BluetoothSync.zip"
        echo "Descarga el ZIP del proyecto primero."
        exit 1
    fi
    echo "[1/6] Descomprimiendo BluetoothSync.zip..."
    unzip -q BluetoothSync.zip
fi

cd BluetoothSync

# Verificar Android SDK
echo "[2/6] Buscando Android SDK..."
SDK_FOUND=false

for SDK_PATH in "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" "/usr/lib/android-sdk" "/opt/android-sdk"; do
    if [ -d "$SDK_PATH" ]; then
        export ANDROID_SDK="$SDK_PATH"
        export ANDROID_HOME="$SDK_PATH"
        export ANDROID_SDK_ROOT="$SDK_PATH"
        SDK_FOUND=true
        echo -e "    ${GREEN}SDK encontrado:${NC} $SDK_PATH"
        break
    fi
done

if [ "$SDK_FOUND" = false ]; then
    echo -e "${RED}[ERROR]${NC} Android SDK no encontrado"
    echo ""
    echo "Instala Android Studio desde:"
    echo "https://developer.android.com/studio"
    echo ""
    echo "Después de instalarlo, ejecuta este script de nuevo."
    exit 1
fi

# Verificar Java
echo "[3/6] Verificando Java..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} Java no encontrado. Android Studio incluye Java."
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo -e "    ${GREEN}Java OK:${NC} $JAVA_VERSION"

# Compilar
echo "[4/6] Compilando APK de debug..."
echo "    Esto puede tardar 2-5 minutos la primera vez..."
echo ""

chmod +x gradlew

if ! ./gradlew assembleDebug --console=plain; then
    echo ""
    echo -e "${YELLOW}[FIX]${NC} Intentando solucionar problemas comunes..."

    if ! command -v gradle &> /dev/null; then
        echo "Gradle no encontrado. Intentando descargar wrapper..."
        if command -v sdkmanager &> /dev/null; then
            sdkmanager "build-tools;35.0.0"
        fi
    fi

    echo "Reintentando compilación..."
    ./gradlew assembleDebug --console=plain || {
        echo -e "${RED}[ERROR]${NC} La compilación falló."
        echo "Abre el proyecto en Android Studio para ver los errores."
        exit 1
    }
fi

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  ✅ COMPILACIÓN EXITOSA                                  ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "📱 APK generado en:"
echo "    app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "🔧 Para instalar en tu teléfono:"
echo "    1. Activa 'Opciones de desarrollador' en tu teléfono"
echo "    2. Activa 'Depuración USB'"
echo "    3. Conecta el teléfono a la PC"
echo "    4. Ejecuta: adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "📧 O copia el APK manualmente al teléfono e instálalo."
echo ""

# Preguntar si instalar
read -p "¿Quieres instalar el APK ahora? (S/N): " INSTALL
if [[ $INSTALL =~ ^[Ss]$ ]]; then
    echo "Instalando..."
    if adb install app/build/outputs/apk/debug/app-debug.apk; then
        echo -e "${GREEN}✅ Instalado correctamente${NC}"
    else
        echo -e "${RED}❌ Error instalando.${NC} Conecta el teléfono y activa depuración USB."
    fi
fi
