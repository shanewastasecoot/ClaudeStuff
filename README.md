# Heart Meter

An 8-bit adventure-style Android home-screen widget that shows how much of your
Claude plan limits you have left.

![widget preview](docs/preview.png)

- **Hearts**: what's left of your 5-hour session limit. Each heart is 10%, in quarter-heart steps.
- **Green meter**: what's left of your weekly limit.
- Countdowns on the right show when each limit resets.
- The frame turns red when you're low (session ≤ 20% or weekly ≤ 10%).
- Tap the widget to refresh. It also refreshes about every 15 minutes.

> The usage data comes from `api.anthropic.com/api/oauth/usage`, the endpoint
> Claude Code's `/usage` screen uses. It isn't a documented public API, so it
> could change or break at any time.

## Install

1. Open the **Actions** tab of this repo, pick the latest green **Build APK** run,
   and download the `heart-meter-apk` artifact. Unzip it to get `app-debug.apk`.
2. Copy it to your phone and open it. Android will ask you to allow installs from
   that app (Files or Chrome); allow it.
3. Open **Heart Meter**, paste a token (see below), tap **Save and fetch**.
4. Long-press the home screen → **Widgets** → **Heart Meter**, and drag it out.
   It's resizable; 3×2 or 4×2 cells looks best.

Or build it yourself: open the folder in Android Studio, or run `./gradlew assembleDebug`.

## Getting a token

The token is stored only inside the app's private storage on your phone.
Treat it like a password.

**Option A: long-lived token (try this first).** On a computer with Claude Code:

```sh
claude setup-token
```

Paste the `sk-ant-oat01-…` token it prints into **Access token**, leave the
refresh token empty. It lasts about a year. If the widget shows `NO ACCESS`, this
token type isn't allowed to read usage; use option B.

**Option B: a separate login for the phone.** Copying the tokens from your
normal Claude Code login would work until the phone refreshes them, and then your
computer gets logged out, because refresh tokens are single-use. So make a
dedicated login instead:

```sh
CLAUDE_CONFIG_DIR=~/.claude-phone claude   # then run /login inside it
cat ~/.claude-phone/.credentials.json
```

Paste `accessToken` and `refreshToken` from that file into the app. The app
refreshes the access token on its own when it expires. (On macOS the credentials
go into the Keychain instead: open Keychain Access and search for "Claude Code".)

## Widget messages

| Message | Meaning |
|---|---|
| `NO TOKEN` | Open the app and paste a token. |
| `TOKEN EXPIRED` / `RELOGIN NEEDED` | Get a new token (see above). |
| `NO ACCESS` | That token can't read usage; try option B. |
| `OFFLINE` | No network; it'll retry. |
| `REST 2H14M` | Session limit used up; that's when it comes back. |

## Tweaking the look

`tools/preview.py` renders the same HUD with Pillow, so you can try colors or
layout changes without a phone:

```sh
python3 tools/preview.py out.png 66 61   # session % left, weekly % left
```

The real drawing code is in `app/src/main/java/dev/heartmeter/HudRenderer.kt`.
