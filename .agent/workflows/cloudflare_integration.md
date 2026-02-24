---
description: Integrar el Binario de Cloudflared en El Oído del Abuelo
---

Este archivo analiza la viabilidad térmica y técnica de inyectar Cloudflared en el Redmi 9C.

1. **Obtención del Binario:** Necesitamos conseguir el binario `cloudflared` compilado estáticamente para arquitectura `arm64-v8a` (la del MediaTek Helio G35 del Redmi 9C).
2. **Inyección en App:** Puesto que las aplicaciones Android no pueden ejecutar comandos binarios de `assets` directamente, en el `onCreate` de `OidoService` se debe:
    a. Mapear el archivo de `assets/cloudflared` a un archivo en memoria local (`getFilesDir().getAbsolutePath() + "/cloudflared"`).
    b. Otorgar permisos de ejecución (`chmod 755`).
    c. Lanzar el proceso `ProcessBuilder("./cloudflared", "tunnel", "--no-autoupdate", "run", "--token", "EL_TOKEN_DE_CLOUDFLARE_AQUI")`.
3. **Control del Ciclo de Vida:** El proceso de Linux secundario debe guardarse en una variable (`Process cloudflaredProcess;`) e invocar su método `.destroy()` en el `onDestroy()` del `OidoService` para evitar fugas y demonios zombies consumiendo RAM.
4. **Viabilidad Térmica:**
    - El binario de Cloudflared en ARM Linux suele ocupar ~30MB.
    - Su consumo base de RAM es de ~20MB, subiendo hasta ~50MB bajo carga.
    - El Xiaomi Redmi 9C tiene 2GB o 3GB. Está dentro del presupuesto de RAM.
    - Consumo Celular 4G: Al ser un túnel de capa de red mantenido por HTTP2/QuicMUX, sí gasta batería por puro *Heartbeat* (pings). Sin embargo, el consumo de red en reposo (cuando no se transmite audio, gracias a nuestro modo de "Reposo Absoluto") debería ser mínimo, manteniendo el móvil fresco.

## Plan de Ejecución

1. **Buscar y descargar** el release oficial de `cloudflared` para Linux ARM64 desde su repositorio de GitHub. No necesitamos la versión "Android" (que es una app APK por VPN service), sino el binario CLI puro de Linux (Static build).
2. **Modificar `OidoService.java`** para añadir la lógica de extracción y ejecución del binario.
3. **Revisar `AndroidManifest.xml`** para asegurar permisos para extraer al disco y para evitar problemas de compatibilidad del JNI/binarios en App Bundles modernos (usando `android:extractNativeLibs="true"` si fuera una `.so`, pero como es un asset raw, simplemente lo copiamos por stream de bytes).
