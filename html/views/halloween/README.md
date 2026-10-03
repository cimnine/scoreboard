# Haunted Rink · Halloween display theme

This package targets the **dev** branch's JSON/WebSocket display API. It was verified on dev commit `95286c14` (v2027.1 development). The required API also exists on dev commit `54416822` (v2025.6 development). It does not use the legacy master XML API.

## Install and open

1. Extract the package into your scoreboard installation folder. The result must be `html/views/halloween/scoreboard.html` alongside the existing `html/views/standard/` directory.
2. Start the scoreboard normally and select the current game in the operator screen.
3. Open one of these URLs (replace the hostname/port if your server uses another address):

| Screen | URL |
| --- | --- |
| Venue scoreboard, including automatic intermission/final score | `http://localhost:8000/views/halloween/scoreboard.html?venue=true` |
| Compact broadcast overlay | `http://localhost:8000/views/halloween/broadcast.html?background=transparent` |
| Broadcast intermission overlay | `http://localhost:8000/views/halloween/intermission.html?background=transparent` |
| Full scoreboard over live footage, with automatic intermission | `http://localhost:8000/views/halloween/scoreboard.html?background=transparent` |

These are dedicated display views; they do not add a theme option to the standard view's settings menu. Use the venue URL in the projector browser, and the broadcast URLs as OBS browser sources. All operator controls continue to work through the normal scoreboard UI. The theme only reads live data.

The scoreboard automatically changes to intermission while its clock runs, then to final score when the final intermission or official-score state is reached. The standalone intermission URL always displays the intermission/final layout, so OBS can control its visibility separately.

Use 1920×1080 or 1920×1200 for broadcast sources. The venue layout adapts to a true 1024×768 viewport with larger labels and scores. Use a modern Chromium-based browser/OBS browser source; the theme uses CSS container units and SVG. Resize the source itself to the desired resolution rather than squeezing a 16:9 source into a 4:3 projector.

## OBS background and motion controls

Paste this into the browser source's **Custom CSS**:

```css
:root {
  --haunted-background: transparent !important;
  --haunted-motion: paused !important;
}
```

Remove the motion line, or set it to `running`, to enable motion. `paused` stops every decorative animation, including bat eyes, pumpkin eyes, and ghost opacity. Live game clocks and sponsor rotation continue to work. The theme also respects the browser's reduced-motion preference.

The background variable controls the entire page canvas, including the full scoreboard and intermission. Panel translucency remains intact. For venue use, leave the default background and use `?venue=true` for solid panels. You can also edit `theme.css` directly. To change panel translucency separately:

```css
:root {
  --haunted-panel: linear-gradient(135deg, #2a1019d9, #100b10b8) !important;
}
```

Optional URL controls work on every screen: `background=transparent`, `background=green`, `motion=off`, `venue=true`, and `preview=true` (uses the standard Preview swap/clock settings). Join multiple parameters with `&`.

## Sponsors and live data

Upload sponsors through the normal scoreboard media controls under **sponsor banners**. The theme reads the same `ScoreBoard.Media.Format(images).Type(sponsor_banner)` collection as the standard dev display, orders banners by filename, and rotates them every five seconds. Uploaded artwork keeps its original colors and proportions on a white backing. The broadcast sponsor slot stays with HOME when teams swap; full scoreboard and intermission use the larger sponsor band. Slots disappear when no banners are configured.

The theme follows the current game, scoreboard/overlay alternate names, scores, jam points, lead/lost/star-pass state, timeouts, official reviews, retained reviews, clock direction, and overtime. A retained review is marked by an asterisk on the venue display and an orange review outline in the broadcast overlay. It uses the standard View/Preview team swap and post-timeout clock settings. A `Both` post-timeout setting uses the lineup clock in this compact layout. The native connection-status warning remains enabled.

Decorations stay away from sponsor artwork. Broadcast indicators sit in a separate row beneath the team panels, with a margin from the bottom edge.

## Artwork and licensing

See `ARTWORK.md`. The SVG artwork and theme source are original code created for this theme and provided under the repository's Apache-2.0 OR GPL-3.0-or-later license choice. License texts are included in the downloadable package. YouTube footage, thumbnails, sample sponsors, and generated concept images are not bundled.

## Verification

Verified against an isolated running dev server: score updates, alternate names, team swap with HOME sponsor following, active timeout, sponsor rotation, automatic intermission, and transparent/paused CSS variables overridden using `!important`. Browser layouts were checked at 1024×768, 1920×1080, and 1920×1200. This is browser verification; physical projector and OBS compositing checks remain for your venue setup.
