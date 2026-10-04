/* SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-or-later */
/* Keep all native overlay bindings, panels and keyboard controls. */
(function initialize() {
  // Theme scripts are fetched in parallel with core.js dependencies.
  if (typeof WS === 'undefined') { setTimeout(initialize, 25); return; }
  function updateBackground() {
    var background = _getUrlParam('background');
    var color = background === 'green' ? '#00ff00' : background === 'transparent' ? 'transparent' :
      WS.state['ScoreBoard.Settings.Setting(Overlay.Interactive.BackgroundColor)'] || 'transparent';
    document.documentElement.style.setProperty('--haunted-background', color);
  }
  if (_getUrlParam('motion') === 'off') document.documentElement.style.setProperty('--haunted-motion', 'paused');
  function updateMotion() {
    var root = document.documentElement;
    root.classList.toggle('haunted-motion-paused', getComputedStyle(root).getPropertyValue('--haunted-motion').trim() === 'paused');
  }
  new MutationObserver(updateMotion).observe(document.documentElement, { attributes: true, attributeFilter: ['style'] });
  new MutationObserver(updateMotion).observe(document.head, { childList: true, subtree: true, characterData: true });
  window.addEventListener('load', updateMotion);
  updateMotion();
  WS.Register('ScoreBoard.Settings.Setting(Overlay.Interactive.BackgroundColor)', { triggerBatchFunc: updateBackground });
  WS.AfterLoad(function () {
    updateBackground();
    var box = document.querySelector('.TeamBox');
    if (!box) return;
    var bar = document.createElement('div'); bar.className = 'haunted-scorebar';
    box.parentNode.insertBefore(bar, box); bar.appendChild(box);
    var clock = document.querySelector('.ClockBox'); bar.appendChild(clock);
    box.querySelectorAll(':scope > [Team]').forEach(function (team) {
      var panel = document.createElement('div'); panel.className = 'haunted-name-panel';
      var row = team.querySelector('.Team'); row.prepend(panel);
      var label = document.createElement('div'); label.className = 'haunted-team-label';
      label.textContent = team.getAttribute('Team') === '1' ? 'HOME' : 'AWAY'; panel.appendChild(label);
      panel.appendChild(row.querySelector('.Name'));
      var indicators = document.createElement('div'); indicators.className = 'haunted-indicators';
      panel.appendChild(indicators);
      indicators.appendChild(team.querySelector('.Indicator'));
      indicators.appendChild(row.querySelector('.DotTimeouts'));
      indicators.appendChild(row.querySelector('.Mini'));
      var extras = document.createElement('div'); extras.className = 'haunted-team-extras';
      panel.appendChild(extras); extras.appendChild(team.querySelector('.JammerWrapper'));
    });
    var periodCard = document.createElement('div'); periodCard.className = 'haunted-clock-card haunted-period-clock';
    var activeCard = document.createElement('div'); activeCard.className = 'haunted-clock-card haunted-active-clock';
    clock.appendChild(activeCard); clock.appendChild(periodCard);
    var names = clock.querySelectorAll('.ClockNames > .Name');
    periodCard.appendChild(names[0]); activeCard.appendChild(names[1]);
    activeCard.appendChild(clock.querySelector('.ClockDescription'));
    var times = clock.querySelectorAll('.ClockBarBottom > .Time');
    periodCard.appendChild(times[0]);
    Array.from(times).slice(1).forEach(function (time) { activeCard.appendChild(time); });
    function formatClocks() {
      times.forEach(function (time) {
        var path = 'ScoreBoard.CurrentGame.' + time.getAttribute('sbContext');
        time.textContent = _timeConversions.msToMinSec(WS.state[path + '.Time'] || 0, isTrue(WS.state[path + '.Direction']));
      });
    }
    WS.Register(['ScoreBoard.CurrentGame.Clock(*).Time', 'ScoreBoard.CurrentGame.Clock(*).Direction'], { triggerBatchFunc: formatClocks });
    formatClocks();
    var artwork = document.createElement('div'); artwork.className = 'haunted-art-layer';
    artwork.innerHTML = "<svg style=\"position:absolute;width:0;height:0\" aria-hidden=\"true\"><defs>\n<symbol id=\"bat\" viewBox=\"0 0 120 60\"><path fill=\"currentColor\" d=\"M60 21L53 9L48 22Q24 7 4 3L11 30Q23 23 28 40Q41 32 50 48L60 56L70 48Q79 32 92 40Q97 23 109 30L116 3Q96 7 72 22L67 9Z\"/><path class=\"bat-eyes\" d=\"M56 29L58 31M64 29L62 31\" stroke-width=\"2.5\" stroke-linecap=\"round\"/></symbol><symbol id=\"blood\" viewBox=\"0 0 400 50\" preserveAspectRatio=\"none\"><path fill=\"currentColor\" d=\"M0 0H400V9H358V21Q358 31 351 31Q344 31 344 21V9H306V40Q306 50 299 50Q292 50 292 40V9H91V28Q91 38 84 38Q77 38 77 28V9H46V17Q46 27 39 27Q32 27 32 17V9H0Z\"/></symbol><symbol id=\"pumpkin\" viewBox=\"0 0 100 100\"><path d=\"M49 24 Q45 7 61 8\" fill=\"none\" stroke=\"#a87643\" stroke-width=\"7\"/><ellipse cx=\"32\" cy=\"59\" rx=\"24\" ry=\"33\" fill=\"#e46b16\"/><ellipse cx=\"69\" cy=\"59\" rx=\"24\" ry=\"33\" fill=\"#e46b16\"/><ellipse cx=\"50\" cy=\"60\" rx=\"23\" ry=\"35\" fill=\"#ff8a24\"/><path class=\"pumpkin-eyes\" d=\"M29 48 L42 53 L30 59 Z M70 48 L58 53 L69 59 Z\"/><path d=\"M46 63 L54 63 L50 57 Z M29 70 L42 75 L48 72 L55 76 L71 68 Q62 89 49 85 Q36 84 29 70\" fill=\"#281626\"/></symbol>\n<symbol id=\"ghost\" viewBox=\"0 0 100 100\"><path d=\"M20 85 V42 C20 1 80 1 80 42 V85 L68 77 L57 89 L46 78 L33 89 Z\" fill=\"currentColor\"/><ellipse cx=\"40\" cy=\"42\" rx=\"5\" ry=\"8\" fill=\"#201624\"/><ellipse cx=\"61\" cy=\"42\" rx=\"5\" ry=\"8\" fill=\"#201624\"/><ellipse cx=\"51\" cy=\"61\" rx=\"6\" ry=\"8\" fill=\"#201624\"/></symbol>\n<symbol id=\"web\" viewBox=\"0 0 100 100\"><g fill=\"none\" stroke=\"currentColor\" stroke-width=\"3\"><path d=\"M0 0 L100 12 M0 0 L93 45 M0 0 L72 73 M0 0 L43 95 M0 0 L10 100 M23 3 Q19 8 22 11 Q15 13 17 17 Q9 17 10 23 Q3 21 2 25 M47 6 Q39 15 44 22 Q30 25 34 34 Q18 33 22 48 Q6 41 5 50 M73 9 Q60 23 67 33 Q46 36 53 53 Q27 52 33 74 Q9 65 8 76 M98 12 Q80 30 90 44 Q62 49 72 73 Q37 71 44 98 Q13 86 10 100\"/></g></symbol>\n</defs></svg>\n<svg class=\"web broadcast-corner-web\" aria-hidden=\"true\"><use href=\"#web\"/></svg><svg class=\"bat broadcast-left-bat\" aria-hidden=\"true\"><use href=\"#bat\"/></svg><svg class=\"bat broadcast-bat\" aria-hidden=\"true\"><use href=\"#bat\"/></svg><svg class=\"bat broadcast-bat satellite left\" aria-hidden=\"true\"><use href=\"#bat\"/></svg><svg class=\"bat broadcast-bat satellite right\" aria-hidden=\"true\"><use href=\"#bat\"/></svg><svg class=\"pumpkin broadcast-pumpkin\" aria-hidden=\"true\"><use href=\"#pumpkin\"/></svg><svg class=\"pumpkin broadcast-pumpkin right\" aria-hidden=\"true\"><use href=\"#pumpkin\"/></svg>\n<svg class=\"ghost\" aria-hidden=\"true\"><use href=\"#ghost\"/></svg>\n";
    document.getElementById('sb').appendChild(artwork);
    function fitNames() {
      box.querySelectorAll('.haunted-name-panel > .Name').forEach(function (name) {
        name.style.fontSize = '';
        var size = parseFloat(getComputedStyle(name).fontSize);
        var minimum = size * .65;
        while ((name.scrollHeight > name.clientHeight + 1 || name.scrollWidth > name.clientWidth + 1) && size > minimum) {
          size -= 1; name.style.fontSize = size + 'px';
        }
      });
    }
    function updateLayout() {
      var prefix = 'ScoreBoard.Settings.Setting(Overlay.Interactive.';
      artwork.classList.toggle('Show', isTrue(WS.state[prefix + 'Score)']));
      var scaling = Number(WS.state[prefix + 'Scaling)']) || 100;
      document.documentElement.style.setProperty('--haunted-scale', scaling / 100);
      names[0].textContent = 'PERIOD ' + (WS.state['ScoreBoard.CurrentGame.Clock(Period).Number'] || 1);
      names[1].textContent = 'JAM ' + (WS.state['ScoreBoard.CurrentGame.Clock(Jam).Number'] || 0);
      box.querySelectorAll(':scope > [Team]').forEach(function (team) {
        var path = 'ScoreBoard.CurrentGame.Team(' + team.getAttribute('Team') + ').Color(overlay.';
        team.classList.toggle('haunted-star-pass', isTrue(WS.state['ScoreBoard.CurrentGame.Team(' + team.getAttribute('Team') + ').StarPass']));
        var namePanel = team.querySelector('.haunted-name-panel');
        namePanel.style.background = WS.state[path + 'bg)'] || '';
        namePanel.style.color = WS.state[path + 'fg)'] || '';
      });
      fitNames();
    }
    WS.Register(['ScoreBoard.Settings.Setting(Overlay.Interactive.Score)',
      'ScoreBoard.Settings.Setting(Overlay.Interactive.Scaling)', 'ScoreBoard.CurrentGame.Clock(*).Number',
      'ScoreBoard.CurrentGame.Team(*).Color(overlay.*)', 'ScoreBoard.CurrentGame.Team(*).Name',
      'ScoreBoard.CurrentGame.Team(*).AlternateName(overlay)', 'ScoreBoard.CurrentGame.Team(*).StarPass'], { triggerBatchFunc: updateLayout });
    updateLayout();
    window.addEventListener('resize', fitNames);
    var home = box.querySelector('[Team="1"]');
    if (!home) return;
    var slot = document.createElement('div'); slot.className = 'haunted-sponsor';
    var image = document.createElement('img'); image.alt = 'Home team sponsor';
    slot.appendChild(image); home.querySelector('.haunted-team-extras').prepend(slot);
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
