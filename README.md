# Chronos

Scheduled WhatsApp messages. Kotlin + Jetpack Compose + Room + AlarmManager + Accessibility Service.

## Build
1. Android Studio (Ladybug or newer, JDK 17) -> Open the `Chronos` folder -> let Gradle sync.
   If it complains about the wrapper, use Android Studio's bundled Gradle (8.9).
2. Run on a physical device (Android 8.0+). WhatsApp must be installed and logged in.

## First-run setup (Settings screen)
- Allow notifications.
- Allow "Alarms & reminders" (exact alarms), otherwise sends can be a few minutes late.
- For auto-send: enable the Chronos accessibility service, and (Android 14+) full-screen alerts.

## How sending works
- Individual: opens `https://wa.me/<number>?text=<message>` in WhatsApp (the number is the recipient check).
  Confirm mode: you press Send. Auto mode: the accessibility service presses Send only if the composer holds your text.
- Group: WhatsApp's chat picker opens with text prefilled; you pick the group and press Send (always manual).

## Known limits
- No official silent-send API exists. WhatsApp UI updates can break the Send-button lookup;
  Chronos then reports a failure instead of guessing.
- Some phones (Xiaomi, Oppo, Vivo, Samsung battery "optimization") kill alarms or block background launches.
- "Sent" means Send was tapped; delivery can't be verified.
