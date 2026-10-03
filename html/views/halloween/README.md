# Haunted Rink · Halloween display theme

This package targets the **dev** branch's JSON/WebSocket display API. It was verified on dev commit `95286c14` (v2027.1 development). The required API also exists on dev commit `54416822` (v2025.6 development). It does not use the legacy master XML API.

## Install and open

1. Extract the package into your scoreboard installation folder. The result must include `html/views/halloween/scoreboard.html` and `html/themes/custom/Halloween.js`. Both are included in the package.
2. Start the scoreboard normally and select the current game in the operator screen.
3. Refresh the main menu and choose **Halloween** in the **Theme** selector. **Main Scoreboard** opens the Halloween venue layout; **Broadcast Overlay** opens the Halloween overlay, using the background configured in Overlay Admin. Roster, Penalty Whiteboard, and Penalty Clocks also receive the theme. **Roster Display**, **Penalty Whiteboard Display**, and **Penalty Clocks Display** also use the Halloween theme, retaining their native live data and controls. The selected theme travels in the display link URL, as with the other themes.
4. For a separate OBS intermission source, or explicit display/background options, use one of these URLs (replace the hostname/port if your server uses another address):

| Screen | URL |
| --- | --- |
| Venue scoreboard, including automatic intermission/final score | `http://localhost:8000/views/halloween/scoreboard.html?venue=true` |
| Compact broadcast overlay | `http://localhost:8000/views/overlay/?theme=/themes/custom/Halloween.js` |
| Broadcast intermission overlay | `http://localhost:8000/views/halloween/intermission.html?background=transparent` |
| Full scoreboard over live footage, with automatic intermission | `http://localhost:8000/views/halloween/scoreboard.html?background=transparent` |

The native theme selector discovers the `Halloween.js` entry through the standard themes media collection. It routes the standard scoreboard to the dedicated layout and styles the native interactive broadcast, roster, penalty whiteboard, and penalty-clock displays, preserving URL options; the roster, whiteboard, and penalty clocks receive a scoped stylesheet on their native pages. Menus and operator panels keep their usual layout. Use the venue URL in the projector browser, and the broadcast URLs as OBS browser sources. All operator controls continue to work through the normal scoreboard UI. The theme only reads live data.

The scoreboard automatically changes to intermission while its clock runs, then to final score when the final intermission or official-score state is reached. The standalone intermission URL always displays the intermission/final layout, so OBS can control its visibility separately.

Use 1920×1080 or 1920×1200 for broadcast sources. The venue layout adapts to a true 1024×768 viewport with larger labels and scores. Use a modern Chromium-based browser/OBS browser source; the theme uses CSS container units and SVG. Resize the source itself to the desired resolution rather than squeezing a 16:9 source into a 4:3 projector.

## Roster, whiteboard, and penalty clocks

Choose Halloween in the main menu and use the usual display links. Direct URLs also work:

- `/views/roster/?theme=/themes/custom/Halloween.js`
- `/views/wb/?theme=/themes/custom/Halloween.js`
- `/views/box/?theme=/themes/custom/Halloween.js`

These views share the Halloween palette, header pumpkins, bats, and spiderwebs. The decorations gently glow/drift and use the same `--haunted-motion` variable and reduced-motion preference as the main displays. `--haunted-background` and the optional `background`/`motion` URL controls apply here too.

The roster keeps numbers, names, pronouns, roles, logos, and officials. Role highlights and team logo proportions are preserved. Empty roster metadata rows collapse, while long names wrap and additional rows remain scrollable. The whiteboard keeps penalty codes, jam references, serving/unserved markers, foul-out warnings, and annotations. Its number column accommodates four-digit numbers at 1024×768. Fonts scale up for larger displays. Penalty clocks retain the distinct Sit/Stand/Done instructions and urgency colors, with large countdown numbers.

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

Optional URL controls for dedicated scoreboard/intermission screens: `background=transparent`, `background=green`, `motion=off`, `venue=true`, and `preview=true` (uses the standard Preview swap/clock settings). Join multiple parameters with `&`. Native broadcast links support `background=transparent`, `background=green`, and `motion=off`; otherwise their background follows Overlay Admin. OBS `!important` overrides take priority. Old direct `broadcast.html` links redirect to the native themed overlay.

## Sponsors and live data

Upload sponsors through the normal scoreboard media controls under **sponsor banners**. The theme reads the same `ScoreBoard.Media.Format(images).Type(sponsor_banner)` collection as the standard dev display, orders banners by filename, and rotates them every five seconds. Uploaded artwork keeps its original colors and proportions on a white backing. The broadcast sponsor slot remains attached to native Team 1 (HOME); full scoreboard and intermission use the larger sponsor band. Slots disappear when no banners are configured.

The theme follows the current game, scoreboard/overlay alternate names, scores, jam points, lead/lost/star-pass state, timeouts, official reviews, retained reviews, clock direction, and overtime. A retained review is marked by an asterisk on the venue display and an orange review outline in the broadcast overlay. The full scoreboard uses the standard View/Preview team swap and post-timeout clock settings. The broadcast uses native `Overlay.Interactive` controls, including score/clock visibility, jammers, lineups, names, penalty clocks, team colors, scaling, background, clock after timeout, and all extra panels. Overlay Admin forwards the selected theme into its preview iframe. A `Both` post-timeout setting uses the lineup clock in this compact layout. The native connection-status warning remains enabled.

Decorations stay away from sponsor artwork. Broadcast indicators sit in a separate row beneath the team panels, with a margin from the bottom edge.

## Artwork and licensing

See `ARTWORK.md`. The SVG artwork and theme source are original code created for this theme and provided under the repository's Apache-2.0 OR GPL-3.0-or-later license choice. License texts are included in the downloadable package. YouTube footage, thumbnails, sample sponsors, and generated concept images are not bundled.

## Verification

Verified native theme discovery, selection, and routing of the main scoreboard and broadcast links. Roster, whiteboard, and penalty clocks were checked with populated live rosters and penalties at VGA and full HD, including a live Sit-to-Stand transition, warning colors, four-digit numbers, and paused decorative motion. Verified against an isolated running dev server: score updates, alternate names, full-scoreboard team swap, active timeout, sponsor rotation, automatic intermission, and transparent/paused CSS variables overridden using `!important`. The native interactive overlay was additionally checked through Overlay Admin for score/clock toggles, full lineups, scaling, green background, roster/lower-third panels, sponsor aspect ratio, theme propagation to the preview, and OBS `!important` background/motion overrides. Auxiliary displays were checked with 15 skaters per team and serving/unserved/foul-out/SIT/STAND/DONE states. Browser layouts were checked at 1024×768, 1920×1080, and 1920×1200. This is browser verification; physical projector and OBS compositing checks remain for your venue setup.

## Auxiliary displays

Choose Halloween in the menu before opening Roster, Penalty Whiteboard, or Penalty Clocks. These retain their native live data and configured team identity. Numbers and clocks use large type, with bats, pumpkins and webs confined to header margins. Serving/unserved colors, foul-out warnings, and SIT/STAND/DONE states retain their meaning. Long rosters remain scrollable; no skaters or officials are dropped to fit the viewport. Shared operator components are not themed. The same background/motion CSS variables work here.
