# 📅 Next Meeting 

A lil desktop widget that shows your next Google Calendar meeting, a live countdown, and a quick join link — so you don't have to keep switching to your calendar tab.

## ✨ Features

- Shows your next meeting's title, start time, and a live countdown
- One-click join link (Google Meet / Zoom / Teams / stream links)
- Shows what's on after your current meeting, and a heads-up for tomorrow's first meeting
- Skip button for meetings that finished early or got cancelled
- Compact and full view modes keep you on track on a single screen, or across monitors
- Custom themes (!!) and space for a lil GIF to keep you motivated
- Auto-refreshes every 5 minutes, and immediately when the current meeting ends

## 🪐 Requirements

- **A JDK (17 or later)** — needed to build and run the app.
- **[gws](https://www.npmjs.com/package/@google/gws)** — the Google Workspace CLI, used to read your calendar.
- **A work Google Workspace account** — calendar authentication is built to align with an internal work account's OAuth setup, so this won't work with a personal Google account.

## 🐹 Install

### 1. Install a JDK

Any JDK 17+ works! If you don't already have one, [Eclipse Temurin](https://adoptium.net/) is a solid choice on both macOS and Windows.

### 2. Install the `gws` CLI

Requires [Node.js](https://nodejs.org/) (npm comes bundled with it).

```bash
npm install -g @google/gws
```

Then authenticate:

```bash
gws auth login -s calendar
```

This opens a browser window — sign in with your work Google account and approve the requested permissions.

### 3. Get the code

```bash
git clone <your-repo-url>
cd next-meeting
```

### 4. Run the app

**macOS / Linux:**

```bash
./gradlew run
```

**Windows:**

```bat
gradlew.bat run
```

The first run downloads Gradle and all dependencies automatically — no separate Gradle install needed.

## 🐸 Usage

- The app checks your calendar on startup and refreshes automatically.
- Click the settings icon to toggle what you see - the countdown, meeting title, show and pick a custom GIF, or switch themes.
- If your calendar fails to load, use **Re-authenticate** to run `gws auth login` again.

## 🐌 Troubleshooting

**"gws not found"**
Make sure `npm install -g @google/gws` succeeded and that npm's global bin directory is on your `PATH`. Restart your terminal after installing.

**Authentication fails / "you don't have access"**
You need to be signed in with a work Google Workspace account whose OAuth client this app is configured to use — a personal Google account won't be authorized.

## 💐 Building a distributable app

To package a native installer:

```bash
./gradlew packageDmg   # macOS only — see build.gradle.kts
```

There's currently no Windows (`.msi`/`.exe`) packaging target configured — running via `gradlew.bat run` is the supported way to use the app on Windows.
