The CRG ScoreBoard is a browser-based scoreboard solution that also provides overlays for video production and the ability to track full game data and export it to a WFTDA statsbook.

The topics on the [Scoreboard Wiki Main Page](https://github.com/rollerderby/scoreboard/wiki/) are the primary documentation for the scoreboard. In order to reach out to the developers, it's best to use the [Github Issues Page](https://github.com/rollerderby/scoreboard/issues).

A mailing list and wiki were available on SourceForge (the original location for this project) but they are not currently used. Subscribing to the SourceForge mailing list and consulting the wiki there is not recommended.

# Installing the Scoreboard Software

These are instructions for getting the software installed and running on a standalone computer to provide a functioning scoreboard. If you have already done this, see [Setting up the Scoreboard](#setting-up-the-scoreboard) below.

## Hardware Requirements

Most Apple or Windows computers that have been manufactured in the last ten years should be able to handle the scoreboard well on a standalone setup. In general, a machine with at least a dual-core 64-bit processor and 2 gigabytes of RAM should be sufficient. Using the scoreboard to provide video overlays or in a networked setup that includes penalty or lineup tracking typically requires more computing power.

Chromebooks that have been modified to run Linux distributions have been used to host the scoreboard but hardware limitations (lack of a suitable display output or low-powered CPUs) may cause issues.

## Software Requirements

The scoreboard should be unzipped into a folder on the local machine. The user running the software requires write access to this folder. Do not put the scoreboard in a folder that requires administrator privileges to write to unless you intend to run the software as an administrator.

### Web Browser

[Google Chrome](https://www.google.com/chrome/) and [Microsoft Edge](https://www.microsoft.com/edge/) (as well as their open-source parent [Chromium](http://www.chromium.org/) or other browsers derived from it) are recommended for running the software. Some known issues may occur when using Mozilla Firefox or Apple Safari. Microsoft Internet Explorer is not recommended.

### Java

Java is required for providing a Java Runtime Environment (JRE) version 17 or newer. Installing the latest version of Oracle's Java is recommended.

- Windows users can install the standard Java for Windows package that is available when clicking on Free Java Download from [Oracle’s Java site](https://java.com/).

- Apple users must install the complete [Java Platform (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html), which includes the JRE, to run the scoreboard properly.

- Linux users may already have a JRE from the OpenJDK project installed, if not, OpenJDK can be obtained from [their repositories](http://openjdk.java.net/install/).

## Downloading the Scoreboard

The project is currently hosted on GitHub, and ZIP files can be downloaded from the [GitHub Releases Page](https://github.com/rollerderby/scoreboard/releases). It is recommended that you use the version labeled "Latest release" (green box). The "Pre-release" (orange box) versions are currently in development and testing, and are not recommended for sanctioned games or tournaments.

Please note that an older version of the project is still hosted on SourceForge and it is no longer maintained there.

## Setting up the Scoreboard

Once Chrome and Java are installed, use your file manager to navigate to the scoreboard folder and run the scoreboard background script by double-clicking on it.

- Windows users: Run scoreboard-Windows.exe to start the script.

- Apple users: Run scoreboard.sh to start the script. (If clicking doesn't work, try pressing command+i (or right click on the file and select "Get info"). In the new info dialog in section "open with" select Terminal.app. (If it's not listed, choose other and navigate to /Applications/Utilities/Terminal.app.)

- Linux users: Run scoreboard.sh to start the script. If you are unable to start it, you may have to allow script files to be executable as programs.

Once it starts successfully, the scoreboard script will open a new window and display a series of status messages. You must keep this script running in order for the scoreboard to function, so do not close the window. You may minimize the window without effect.

In your file manager, open start.html with the recommended browser. You may need to right-click on the file and choose the **Open With** option. The browser will open to localhost:8000 where several options are presented.

Assuming that your scoreboard computer is set up with a monitor/laptop screen as a primary display for the operator, and a separate projector as a second display, right-click on the second link for **Main Scoreboard** and choose **Open link in new window**. Drag the new window with the main scoreboard onto the second display, click inside the window, and press the F11 key to make the window full screen. In the first browser window that you opened on the primary display, click on one of the documentation links. It will open in a new tab. Back in the original tab click on **Main Operator Control Panel**.

When the control panel displays, it will ask you for an operator name. Enter your name and click Login. This operator name is used to store your personalized settings such as key controls.

Now you can go to the tab with the documentation and either go to the Quick Start Guide or dive in deep right away and proceed with the section on the Controls page.


## Development with Gradle

Use Java SDK 17 or newer to run the wrapper. Gradle uses a Java 17 toolchain
and can download it automatically. No system Gradle installation is needed.

```shell
./gradlew run        # start the server without the GUI from the project root
./gradlew build      # compile, test, and package
./gradlew classes    # compile production sources only
./gradlew installDist # create a runnable distribution under build/install/
```

Import the repository as a Gradle project in your IDE. Production Java sources
stay in `src/`, tests stay in `tests/`, and classpath properties files stay beside
the sources in `src/`. Explicit source sets preserve this layout; generated
version properties and build outputs are written only under `build/`.

Application distributions include the existing `html/` and `config/` directories
and Gradle-generated launchers in `bin/`. The launchers change to the distribution
root before starting the server. ZIP and TAR distributions are in
`build/distributions/`; a standalone executable JAR is in `build/libs/` and needs
`html/` and `config/` beside its working directory.

Ant and its checked-in dependencies remain available during the transition.
Gradle resolves dependencies from Maven Central, using the same dependency
versions as the current Ant build. The Gradle runtime requires Java 17.

Gradle generates the same version metadata fields as Ant: `release`,
`release.commit`, `release.user`, `release.time`, and `release.host`. Development
builds append a timestamp to the Git description; use `-PisRelease=true` when
building a release to omit that suffix. The executable JAR keeps one entry per
path, merges service registrations, and combines dependency license notices.

## Native installers and releases

Native installers bundle a Java 17 runtime.
JReleaser builds DMG on macOS, DEB and RPM on Linux, and EXE on Windows.
Each format must be built on its target operating system.
Run the packaging command with JDK 17.
Linux requires `fakeroot` and `rpm`.
Windows requires WiX 3 with `candle.exe` and `light.exe` on `PATH`.

```shell
./gradlew jreleaserAssemble -PisRelease=true -PinstallerVersion=2027.1
```

Installers are written to `build/jreleaser/assemble/scoreboard/jpackage/`.
The installer version must be numeric, such as `2027.1` or `2027.1.0`.
Windows maps calendar years to years since 2000, so `2027.1` becomes `27.1`.
This keeps the installer version within Windows version limits.
Without an explicit version, the build extracts the numeric version from the Git description.
Native launches keep configuration, autosaves, uploads, and web files in `~/.crg-scoreboard`.
Bundled web files are refreshed on launch.
Existing configuration and user data are preserved.

Pushing a numeric version tag such as `v2027.1` runs the release workflow.
It builds the installers on macOS, Linux, and Windows.
A final job collects all four formats and publishes them with the executable JAR through JReleaser.
The workflow uses `GITHUB_TOKEN` with write access to repository contents for publication.


## Container build

With Podman running, build a local image using JReleaser:

```shell
./gradlew buildContainer
```

The image is `localhost/crg/scoreboard:latest`.
This task builds locally and does not log in or publish to a registry.
It includes the complete application distribution, web assets, configuration, and a Java 17 runtime.
No source files are moved.
Ordinary `./gradlew build` does not require a container runtime.
`./gradlew prepareContainer` generates the Dockerfile under
`build/jreleaser/prepare/container/docker/` without building an image.

```shell
podman run --name scoreboard --init -p 8000:8000 \
  -v scoreboard-autosave:/opt/scoreboard/config/autosave \
  -v scoreboard-games:/opt/scoreboard/html/game-data \
  localhost/crg/scoreboard:latest
```

Open <http://localhost:8000/>.
The server runs as UID/GID `10001:10001`.
Named volumes preserve autosaves and game exports across container replacement.
For bind mounts, ensure this user can write to the mounted directories.
Uploaded logos, sponsor banners, and custom themes can also be persisted with volumes mounted
at their corresponding paths under `/opt/scoreboard/html/`.

The default arguments are `--nogui --nodiscovery --import=`.
mDNS discovery is disabled for bridge networking.
Importing from adjacent installations is disabled.
Arguments after the image name replace these defaults; for example,
use `--network=host` and `--nogui --import=` to enable discovery on a Linux host.
