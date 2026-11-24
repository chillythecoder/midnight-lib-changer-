# Midnight lib changer

Android Studio project for **Midnight lib changer** by PENG.LOL.

## Open and build
1. Extract the ZIP.
2. Open the `MidnightLibChanger` folder in Android Studio.
3. Let Gradle sync. Android Studio may download the Android Gradle Plugin and SDK 35.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

## Features
- Import and export `.so` / binary library files using Android's document picker.
- Scan printable ASCII strings and perform length-preserving replacements in memory.
- Menu title and credits edit-plan fields.
- Menu theme presets and JSON export.
- TTF/OTF font import and live preview, plus font preview preset export.
- Settings for animated night sky, background motion, and click sounds.

## Important technical limits
This is a safe starter editor, not a universal native-library decompiler. It can only discover printable ASCII runs and replace a string when the new text fits in the original byte length. Shorter replacements are null-padded. It does not modify compiled machine instructions, ELF relocation tables, encrypted/compressed resources, or arbitrary theme data inside unknown binaries. Always keep an untouched backup and only modify libraries you own or are authorized to edit. Theme and font presets are exported as JSON; they are not automatically injected into a target app.
