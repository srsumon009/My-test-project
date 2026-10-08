# Add project specific ProGuard rules here.
# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# Keep model classes (Room entities) so reflection-based generated code keeps working.
-keep class com.zakattracker.app.data.model.** { *; }
