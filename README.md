# Shape Pong för Android

Ett snabbt Pong-spel mot datorn, byggt med Kotlin och en egenritad Android View.

## Spela

- Dra fingret i sidled för att styra det gröna racket.
- Välj **Platt**, **Båge** eller **Vinkel** längst ned när som helst.
- Platt ger klassisk studs, bågen ger snabbare och rakare returer, och vinkeln ger kraftig sidostyrning beroende på träffpunkten.
- Första till valfri poängsumma – matchen fortsätter tills du lämnar spelet.

## Förutsättningar

- Android Studio Ladybug eller senare (enklast), alternativt JDK 17 och Android SDK.
- Android SDK Platform 35 och Build Tools 35.0.0.
- Internetåtkomst vid första bygget för Gradle och projektberoenden.

Gradle behöver inte installeras separat; `gradlew` hämtar låst version 8.9 vid första körningen.

## Bygg en körbar APK

### Android Studio

Öppna mappen `ShapePong`, låt Gradle synkronisera och tryck **Run**. Välj en emulator eller en Android-telefon med USB-felsökning aktiverad.

För att skapa en APK: **Build → Build APK(s)**. APK-filen hamnar normalt i `app/build/outputs/apk/debug/`.

### Terminal (macOS/Linux)

```bash
./build-debug.sh
```

På Windows kan du köra `gradlew.bat :app:assembleDebug` från en terminal där `ANDROID_HOME` är satt.

Den färdiga filen är `app/build/outputs/apk/debug/app-debug.apk`. Installera den på en ansluten telefon med:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Automatisk byggkontroll

Arbetsflödet `.github/workflows/android.yml` installerar rätt JDK och Android SDK, kör Android Lint och bygger en APK. På GitHub går APK:n sedan att hämta som artefakten `ShapePong-debug-apk`.
