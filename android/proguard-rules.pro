# libGDX rules (based on the current gdx-liftoff template).
-verbose

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication

# Scene2D and skins are configured through reflection (uiskin.json).
-keep public class com.badlogic.gdx.scenes.scene2d.** { *; }
-keep public class com.badlogic.gdx.graphics.g2d.BitmapFont { *; }
-keep public class com.badlogic.gdx.graphics.Color { *; }

# The leaderboard is stored as JSON through libGDX's reflection-based Json serializer.
-keep class si.um.feri.backgammon.persistence.** { *; }

-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
