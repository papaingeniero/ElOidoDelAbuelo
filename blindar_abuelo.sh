#!/bin/bash
clear
echo "=========================================="
echo "🚀 CI/CD Y SUPER-BLINDAJE ADB: EL ABUELO 🛡️"
echo "=========================================="

echo "📡 IP del Abuelo (ENTER para 192.168.1.136):"
read input_ip
ABUELO_IP=${input_ip:-"192.168.1.136"}

echo "🔌 Conectando a $ABUELO_IP:5555..."
adb connect $ABUELO_IP:5555
sleep 2

echo ""
echo "📦 FASE 0: DESPLIEGUE Y EJECUCIÓN"
echo "---------------------------------------------------"
echo "⏳ Instalando la última compilación (APK)..."
adb install -r app/build/outputs/apk/release/app-release.apk
sleep 2

echo "▶️  Arrancando el motor principal (MainActivity)..."
adb shell am start -n com.david.eloidodelabuelo/.MainActivity
echo "⏳ Dando tiempo al sistema para asentar la app..."
echo "⏸️  Pulsa ENTER en el Mac cuando haya arrancado la app..."
read pausa_teclado

echo ""
echo "⚙️  FASE 1: INYECCIÓN DE COMANDOS SILENCIOSOS"
echo "---------------------------------------------------"
echo "🔋 1/4 Saltando Optimización de Android (Doze)..."
adb shell dumpsys deviceidle whitelist +com.david.eloidodelabuelo

echo "🚀 2/4 Configurando Standby Bucket como ACTIVE..."
adb shell am set-standby-bucket com.david.eloidodelabuelo active

echo "⚙️  3/4 Permitiendo RUN_IN_BACKGROUND total..."
adb shell cmd appops set com.david.eloidodelabuelo RUN_IN_BACKGROUND allow

echo "🌐 4/4 Inyectando Pase VIP de red (Datos sin restricción)..."
adb shell cmd netpolicy add restrict-background-whitelist com.david.eloidodelabuelo

echo "⚡ Extra: Activando Stay Awake (Pantalla encendida en Bypass)..."
adb shell settings put global stay_on_while_plugged_in 3

echo ""
echo "📱 FASE 2: ASISTENTE VISUAL EN EL MÓVIL"
echo "---------------------------------------------------"
echo "Atento a la pantalla del Xiaomi. Ve completando cada paso."

echo ""
echo "👉 PASO 1: Optimización de Batería"
adb shell am start -a android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -d "package:com.david.eloidodelabuelo"
echo "⏸️  Pulsa ENTER en el Mac tras darle a 'Permitir' (si sale el pop-up)..."
read pausa_teclado

echo ""
echo "👉 PASO 2: Datos sin restricción"
adb shell am start -a android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS -d "package:com.david.eloidodelabuelo"
echo "⏸️  Pulsa ENTER en el Mac cuando hayas activado el interruptor..."
read pausa_teclado

echo ""
echo "👉 PASO 3: Notificaciones"
adb shell am start -a android.settings.APP_NOTIFICATION_SETTINGS --es android.provider.extra.APP_PACKAGE com.david.eloidodelabuelo
echo "⏸️  Pulsa ENTER en el Mac cuando dejes SÓLO 'Mostrar' y apagues el resto..."
read pausa_teclado

echo ""
echo "👉 PASO 4: Inicio Automático (MIUI)"
adb shell am start -n com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity
echo "⏸️  Pulsa ENTER en el Mac tras activar el interruptor de Auto-inicio..."
read pausa_teclado

echo ""
echo "=========================================="
echo "✅ ¡HECHO! EL SERVIDOR ESTÁ 100% ACORAZADO"
echo "=========================================="
echo ""
echo "📋 AUDITORÍA FINAL (Tus comprobaciones manuales):"
echo "Check 1: AutoInicio en segundo plano: administrar aplicaciones, Autoinicio en segundo plano, asegurarse de que está la app habilitada"
echo "Check 2: Notificaciones: mirar en ajustes, aplicaciones, gestionar aplicaciones, buscar la app, Notificaciones, mostrar notificaciones=sí, las demás apagadas"
echo "Check 3: Ajustes, buscar: optimización de batería, pinchar en la app, asegurarse de que está en no optimizar"
echo "Check 3: Ajustes, buscar: optimización de batería, Datos sin restricción, buscar la app y habilitarla"

