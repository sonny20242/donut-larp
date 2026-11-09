DONUT LARP - Fabric 1.21.11 (Yarn)

Build:
 1. Install JDK 21.
 2. Go to https://fabricmc.net/develop/template  -> Minecraft 1.21.11, Yarn mappings, Java 21 -> download.
    (That template includes the gradle wrapper.)
 3. Replace its src/, and copy my build.gradle, gradle.properties, settings.gradle over the template's.
 4. Run:  gradlew build        (Windows)   or   ./gradlew build   (Mac/Linux)
 5. Put build/libs/donutlarp-1.0.0.jar (NOT the -sources jar) in .minecraft/mods
    together with Fabric API for 1.21.11.

Use:  RIGHT SHIFT opens the menu (rebind in Controls > Donut Larp).
      Config: config/donutlarp.json
