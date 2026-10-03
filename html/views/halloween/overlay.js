/* SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-or-later */
/* Keep all native overlay bindings, panels and keyboard controls. */
(function initialize() {
  // Theme scripts are fetched in parallel with core.js dependencies.
  if (typeof WS === 'undefined') { setTimeout(initialize, 25); return; }
  var params = new URLSearchParams(window.location.search);
  function updateBackground() {
    var background = params.get('background');
    var color = background === 'green' ? '#00ff00' : background === 'transparent' ? 'transparent' :
      WS.state['ScoreBoard.Settings.Setting(Overlay.Interactive.BackgroundColor)'] || 'transparent';
    document.documentElement.style.setProperty('--haunted-background', color);
  }
  if (params.get('motion') === 'off') document.documentElement.style.setProperty('--haunted-motion', 'paused');
  WS.Register('ScoreBoard.Settings.Setting(Overlay.Interactive.BackgroundColor)', { triggerBatchFunc: updateBackground });
  WS.AfterLoad(function () {
    updateBackground();
    var box = document.querySelector('.TeamBox');
    if (!box) return;
    box.querySelectorAll(':scope > [Team]').forEach(function (team) {
      var artwork = {"pumpkin": "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" class=\"haunted-decoration haunted-pumpkin\" aria-hidden=\"true\"><path d=\"M49 24 Q45 7 61 8\" fill=\"none\" stroke=\"#a87643\" stroke-width=\"7\" /><ellipse cx=\"32\" cy=\"59\" rx=\"24\" ry=\"33\" fill=\"#e46b16\" /><ellipse cx=\"69\" cy=\"59\" rx=\"24\" ry=\"33\" fill=\"#e46b16\" /><ellipse cx=\"50\" cy=\"60\" rx=\"23\" ry=\"35\" fill=\"#ff8a24\" /><path class=\"pumpkin-eyes\" d=\"M29 48 L42 53 L30 59 Z M70 48 L58 53 L69 59 Z\" /><path d=\"M46 63 L54 63 L50 57 Z M29 70 L42 75 L48 72 L55 76 L71 68 Q62 89 49 85 Q36 84 29 70\" fill=\"#281626\" /></svg>", "bat": "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 120 60\" class=\"haunted-decoration haunted-bat\" aria-hidden=\"true\"><path fill=\"currentColor\" d=\"M60 21L53 9L48 22Q24 7 4 3L11 30Q23 23 28 40Q41 32 50 48L60 56L70 48Q79 32 92 40Q97 23 109 30L116 3Q96 7 72 22L67 9Z\" /><path class=\"bat-eyes\" d=\"M56 29L58 31M64 29L62 31\" stroke-width=\"2.5\" stroke-linecap=\"round\" /></svg>"};
      Object.keys(artwork).forEach(function (name) { team.insertAdjacentHTML('beforeend', artwork[name]); });
    });
    var home = box.querySelector('[Team="1"]');
    if (!home) return;
    var slot = document.createElement('div'); slot.className = 'haunted-sponsor';
    var image = document.createElement('img'); image.alt = 'Home team sponsor';
    slot.appendChild(image); home.appendChild(slot);
    function rotateSponsors() {
      var prefix = 'ScoreBoard.Media.Format(images).Type(sponsor_banner).File(';
      var banners = Object.keys(WS.state).filter(function (key) {
        return key.indexOf(prefix) === 0 && key.endsWith(').Src') && WS.state[key];
      }).sort().map(function (key) { return WS.state[key]; });
      var src = banners.length ? banners[Math.round(Date.now() / 5000) % banners.length] : '';
      slot.hidden = !src;
      if (src && image.getAttribute('src') !== src) image.src = src;
      if (!src) image.removeAttribute('src');
    }
    WS.Register('ScoreBoard.Media.Format(images).Type(sponsor_banner)', { triggerBatchFunc: rotateSponsors });
    rotateSponsors();
    setInterval(rotateSponsors, 5000);
  });
}());
