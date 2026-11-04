# Pago Peones — Calculadora de Pago para Peones Agrícolas

App nativa de Android (Kotlin + XML, sin Jetpack Compose) para calcular el
pago de jornales de peones agrícolas en colones costarricenses (₡).
Funciona **100% sin internet**, no pide ningún permiso especial y guarda la
configuración en el propio celular.

## Estructura del proyecto

```
app/src/main/java/com/finca/pagopeones/
  calculo/           -> Lógica pura de cálculo (sin dependencias de Android)
    Modelos.kt         - TipoJornada, ModoRedondeo, ConfigPago, ResultadoPago
    PayCalculator.kt    - Reglas de cálculo y redondeo
  util/
    Prefs.kt            - Guarda/lee la configuración con SharedPreferences
    MoneyFormatter.kt    - Formato ₡12 000
  MainActivity.kt        - Pantalla principal (calculadora)
  ConfiguracionActivity.kt - Pantalla de configuración (+ PIN opcional)
  ManualActivity.kt       - Manual de usuario con ejemplos en vivo

app/src/test/java/.../calculo/PayCalculatorTest.kt  -> Pruebas unitarias JUnit
```

## Paso 1: Abrir el proyecto en Android Studio

1. Instale **Android Studio** (versión reciente, ej. Android Studio
   Jack Jack o superior) desde https://developer.android.com/studio si aún
   no lo tiene.
2. Abra Android Studio → **Open** → seleccione la carpeta
   `Cal_HorasLaborales` (la raíz del proyecto, donde está `settings.gradle.kts`).
3. Android Studio detectará que falta el archivo binario
   `gradle/wrapper/gradle-wrapper.jar` (no se puede generar como texto).
   Cuando lo pida, acepte la opción de **"Use Gradle from: 'wrapper'"** o,
   si no aparece el aviso automático, genere el jar manualmente así:
   - Si ya tiene Gradle instalado en su PC, abra una terminal en la carpeta
     del proyecto y ejecute:
     ```
     gradle wrapper --gradle-version 8.4
     ```
   - Si no tiene Gradle instalado, abra el proyecto igual: Android Studio
     incluye su propio Gradle embebido y, al sincronizar, le ofrecerá crear
     el wrapper automáticamente (botón **"OK"** en el aviso que aparece
     arriba del editor).
4. Espere a que termine el **Gradle Sync** (barra de progreso abajo).

## Paso 2: Probar que compila y corre las pruebas

1. En Android Studio, abra la pestaña **Terminal** (abajo) y ejecute:
   ```
   ./gradlew test
   ```
   (en Windows: `gradlew.bat test`)
   Debe mostrar todas las pruebas de `PayCalculatorTest` en verde.
2. Para probar la app en un celular o emulador conectado:
   ```
   ./gradlew installDebug
   ```

## Paso 3: Generar el APK firmado (release)

1. En el menú superior: **Build → Generate Signed Bundle / APK…**
2. Seleccione **APK** → **Next**.
3. En **Key store path**, haga clic en **Create new…** (solo la primera vez)
   y complete:
   - Ruta donde guardar el archivo `.jks` (guárdelo en un lugar seguro,
     lo necesitará para futuras actualizaciones).
   - Contraseña del almacén de claves (**guárdela**, sin ella no podrá
     actualizar la app en el futuro).
   - Alias y contraseña de la clave.
   - Datos del certificado (nombre, organización — pueden ser los de la
     finca o cualquiera de los 3 hermanos).
4. Click **Next**. Marque la variante **release**.
5. Marque las casillas **V1** y **V2** de firma → **Finish**.
6. Android Studio compilará con R8 (minify) y `shrinkResources` activados
   (ya configurado en `app/build.gradle.kts`) y generará el APK en:
   ```
   app/release/app-release.apk
   ```
7. Verifique el tamaño del archivo (clic derecho → Propiedades). Con estas
   reglas activas y sin librerías pesadas, debería quedar muy por debajo
   de 2 MB.

> **Importante:** guarde el archivo `.jks` y sus contraseñas. Todas las
> futuras versiones de la app deben firmarse con la misma clave; si la
> pierde, no podrá actualizar la app ya instalada en los celulares, sino
> reinstalarla desde cero.

## Paso 4: Instalar el APK en el celular (sin Play Store)

### Opción A: Transferir el archivo directamente
1. Copie `app-release.apk` al celular (cable USB, o subiéndolo a una nube
   y descargándolo).
2. En el celular, abra el archivo APK desde el Explorador de archivos.
3. Android pedirá activar **"Instalar apps desconocidas"** para la app que
   use para abrir el archivo (Explorador de archivos, Chrome, etc.):
   - Vaya a **Ajustes → Aplicaciones → Acceso especial → Instalar apps
     desconocidas** (el nombre varía según la marca del celular: Samsung,
     Xiaomi, etc.).
   - Active el interruptor para la app desde la que va a instalar.
4. Vuelva a abrir el APK y toque **Instalar**.

### Opción B: Compartir por WhatsApp con los demás hermanos
1. Envíese el APK a usted mismo por WhatsApp (ej. en "Mensajes a mí mismo"
   o un chat de prueba), o compártalo directo en el chat familiar.
2. Cada hermano, al recibir el archivo en WhatsApp:
   - Toca el archivo `.apk` recibido.
   - Si aparece el aviso de **"Instalar apps desconocidas"**, lo activa
     para WhatsApp (mismo camino de Ajustes mencionado arriba).
   - Toca **Instalar**.
3. Listo: la app queda instalada y funciona sin internet.

> Nota: WhatsApp puede bloquear el envío de archivos `.apk` en algunos
> casos. Si eso pasa, comprima el APK en un `.zip` antes de enviarlo, o
> use Google Drive / Bluetooth / cable USB para pasarlo.

## Reglas de cálculo implementadas

Ver `indicaciones.md` para el detalle completo. Resumen rápido:

- Jornada **Normal** (8h): horas × ₡1 200.
- Jornada **Dura** (8h): horas × ₡1 200 + ₡2 000 de reconocimiento.
- Con horas extra: se paga la jornada completa + horas extra × tarifa de
  hora extra (₡1 300 en normal, ₡2 000 en dura). En jornada dura con
  extras, el reconocimiento de ₡2 000 **no** se suma por defecto (hay un
  interruptor en Configuración para activarlo).
- Menos de 8 horas: horas × ₡1 200 (sin reconocimiento, también
  configurable).
- El total final se redondea al millar más cercano (configurable: activar/
  desactivar, unidad ₡100/₡500/₡1 000, y modo más cercano / siempre arriba
  / siempre abajo).

Todos estos valores son editables desde **Configuración**, con un PIN
opcional de 4 dígitos para evitar cambios accidentales, y un botón para
restaurar los valores por defecto.

## Pruebas unitarias

`PayCalculatorTest.kt` cubre los casos obligatorios del enunciado
(normal/dura 8h y 10h, redondeo de 12 500, normal 6h) además de casos de
validación (horas negativas, vacías, mayores a 16), medias horas, y los
tres modos de redondeo. Corra `./gradlew test` para verlas pasar.
