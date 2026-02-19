# Bitácora de Desarrollo: El Oído del Abuelo

## 🚀 Inicio del Proyecto | 19-Feb-2026
### 📜 El Problema
Necesitamos establecer una base sólida para el proyecto 'El Oído del Abuelo', asegurando compatibilidad estricta con Android 10 (API 29) y un entorno limpio.

### 🛠️ La Solución
Se ha inicializado el proyecto con la siguiente estructura:
- **Gradle**: Configuración optimizada para API 29.
- **Manifest**: Permisos de Audio/Storage/PhoneState y `legacyExternalStorage` activado.
- **MainActivity**: Implementación de solicitud de permisos en tiempo de ejecución.
- **Git**: `.gitignore` configurado con reglas estándar y específicas del agente.

### 🎓 Lecciones Aprendidas
- La importancia de `requestLegacyExternalStorage` en Android 10 para evitar Scoped Storage complejo innecesariamente en este MVP.
- La necesidad de aislar el entorno de compilación (Gradle Wrapper) para reproducibilidad.

## 🚀 Compilación Exitosa v1.0-dev.2 | 19-Feb-2026
### 📜 El Problema
La compilación inicial falló debido a una incompatibilidad entre el JDK 17 del sistema y Gradle 6.7.1, además de la falta de configuración para AndroidX.

### 🛠️ La Solución
1. **Upgrade de Infraestructura**:
   - Gradle Wrapper actualizado a 7.5.
   - Android Gradle Plugin actualizado a 7.2.2.
   - `android.useAndroidX=true` añadido a `gradle.properties`.
2. **Validación**:
   - Build exitoso en 32s.
   - APK generado: 3.1MB.

### 🎓 Lecciones Aprendidas
- **JDK 17 vs Gradle**: Las versiones antiguas de Gradle (6.x) no soportan clases Java 61 (JDK 17). Es mandatorio usar Gradle 7.3+ para entornos modernos.
- **AndroidX**: Aunque AGP moderno suele implicarlo, la ausencia explícita de `gradle.properties` puede causar fallos de classpath en builds limpios.
