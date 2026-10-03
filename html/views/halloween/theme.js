/* SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-or-later */
/* Read-only display adapter for the dev branch's JSON/WebSocket API. */
(function () {
  var params = new URLSearchParams(window.location.search);
  var mode = document.body.dataset.display;
  if (params.get('background') === 'transparent') {
    document.documentElement.style.setProperty('--haunted-background', 'transparent');
  }
  if (params.get('background') === 'green') {
    document.documentElement.style.setProperty('--haunted-background', '#00ff00');
  }
  if (params.get('venue') === 'true') {
    document.documentElement.style.setProperty('--haunted-panel', 'linear-gradient(135deg,#2a1019,#100b10)');
  }
  if (params.get('motion') === 'off') {
    document.documentElement.style.setProperty('--haunted-motion', 'paused');
  }

  function all(selector, root) { return Array.from((root || document).querySelectorAll(selector)); }
  function text(selector, value, root) {
    all(selector, root).forEach(function (element) { element.textContent = value; });
  }
  function value(path) { return WS.state[path]; }
  function running(clock) { return isTrue(value('ScoreBoard.CurrentGame.Clock(' + clock + ').Running')); }
  function clockTime(clock) {
    var path = 'ScoreBoard.CurrentGame.Clock(' + clock + ')';
    return _timeConversions.msToMinSec(value(path + '.Time') || 0, isTrue(value(path + '.Direction')));
  }
  var lastNameLayout = '';
  function fitNames() {
    var signature = window.innerWidth + ':' + window.innerHeight + ':' + all('.board-team h2,.team-name').map(function (element) { return element.textContent; }).join('|');
    if (signature === lastNameLayout) return;
    lastNameLayout = signature;
    all('.board-team h2,.team-name').forEach(function (element) {
      element.style.fontSize = '';
      var size = parseFloat(getComputedStyle(element).fontSize);
      var minimum = size * .65;
      while ((element.scrollHeight > element.clientHeight + 1 || element.scrollWidth > element.clientWidth + 1) && size > minimum) {
        size -= 1;
        element.style.fontSize = size + 'px';
      }
    });
  }

  (function () {
    var pending = false;
    var view = params.get('preview') === 'true' ? 'Preview' : 'View';
    var nameContext = mode === 'broadcast' ? 'overlay' : 'scoreboard';
    function schedule() {
      if (pending) return;
      pending = true;
      requestAnimationFrame(function () { pending = false; render(); });
    }
    function render() {
      var active = running('Jam') ? 'Jam' : running('Timeout') ? 'Timeout' : running('Lineup') ? 'Lineup' : running('Intermission') ? 'Intermission' : 'NoClock';
      var interNumber = value('ScoreBoard.CurrentGame.Clock(Intermission).Number');
      var final = isTrue(value('ScoreBoard.CurrentGame.OfficialScore')) || (Number(interNumber) > 0 && Number(interNumber) === Number(value('ScoreBoard.CurrentGame.Rule(Period.Number)')));
      if (isTrue(value('ScoreBoard.CurrentGame.InJam'))) active = 'Jam';
      if (running('Lineup') && running('Timeout')) {
        var afterTimeout = value('ScoreBoard.Settings.Setting(ScoreBoard.' + view + '_ClockAfterTimeout)');
        active = afterTimeout === 'Timeout' ? 'Timeout' : 'Lineup';
      }
      if (mode === 'scoreboard') {
        document.getElementById('scoreboard').hidden = active === 'Intermission' || final;
        document.getElementById('intermission').hidden = !(active === 'Intermission' || final);
      }
      var swapped = isTrue(value('ScoreBoard.Settings.Setting(ScoreBoard.' + view + '_SwapTeams)'));
      var order = swapped ? [2, 1] : [1, 2];
      var cards = all('.board-team');
      var barTeams = all('.bar .team');
      var barScores = all('.bar > .score');
      var indicators = all('.bar .indicators');
      order.forEach(function (id, index) {
        var path = 'ScoreBoard.CurrentGame.Team(' + id + ')';
        var name = value(path + '.AlternateName(' + nameContext + ')') || value(path + '.Name') || '—';
        var score = value(path + '.Score') || '0';
        var timeouts = Number(value(path + '.Timeouts')) || 0;
        var reviews = Number(value(path + '.OfficialReviews')) || 0;
        var lead = isTrue(value(path + '.DisplayLead'));
        var starPass = isTrue(value(path + '.StarPass'));
        var lost = isTrue(value(path + '.Lost'));
        var marker = starPass ? 'STAR PASS' : lead ? 'LEAD JAMMER' : lost ? 'LEAD LOST' : '';
        var compactMarker = starPass ? 'SP' : lead ? 'LEAD' : lost ? 'LOST' : '';
        if (cards[index]) {
          text('h2', name, cards[index]);
          text('.board-score', score, cards[index]);
          text('.board-detail b', value(path + '.JamScore') || '0', cards[index]);
          text('.board-kicker', id === 1 ? 'HOME' : 'AWAY', cards[index]);
          text('.board-resources b', reviews + (isTrue(value(path + '.RetainedOfficialReview')) ? '*' : ''), cards[index]);
          cards[index].querySelector('.board-resources > span:last-child').classList.toggle('active-timeout', active === 'Timeout' && String(value('ScoreBoard.CurrentGame.TimeoutOwner') || '').slice(-1) === String(id) && isTrue(value('ScoreBoard.CurrentGame.OfficialReview')));
          var leadLabel = cards[index].querySelector('.lead');
          if (leadLabel) { leadLabel.hidden = !marker; leadLabel.textContent = marker; }
        }
        if (barTeams[index]) {
          text('.team-name', name, barTeams[index]);
          text('.team-label', id === 1 ? 'HOME' : 'AWAY', barTeams[index]);
        }
        if (barScores[index]) barScores[index].textContent = score;
        var indicator = indicators[index];
        if (indicator) {
          var review = indicator.querySelector('.review');
          review.classList.toggle('empty', reviews === 0);
          review.classList.toggle('retained', isTrue(value(path + '.RetainedOfficialReview')));
          review.classList.toggle('active-timeout', active === 'Timeout' && String(value('ScoreBoard.CurrentGame.TimeoutOwner') || '').slice(-1) === String(id) && isTrue(value('ScoreBoard.CurrentGame.OfficialReview')));
          var label = indicator.querySelector('.lead');
          if (label) { label.hidden = !compactMarker; label.textContent = compactMarker; }
        }
        [cards[index], indicator].forEach(function (root) {
          if (!root) return;
          all('.pip', root).forEach(function (pip, n) {
            pip.classList.toggle('empty', n >= timeouts);
            pip.classList.toggle('active-timeout', active === 'Timeout' && String(value('ScoreBoard.CurrentGame.TimeoutOwner') || '').slice(-1) === String(id) && n === timeouts && !isTrue(value('ScoreBoard.CurrentGame.OfficialReview')));
          });
        });
        var inter = document.getElementById('intermission');
        if (inter) {
          all('.inter-score .name', inter)[index].textContent = name;
          all('.inter-score strong', inter)[index].textContent = score;
        }
      });
      // Both teams need a live lead label; the original away mockup omitted one.
      text('.board-title span:last-child', (isTrue(value('ScoreBoard.CurrentGame.InOvertime')) ? 'OVERTIME' : 'PERIOD ' + value('ScoreBoard.CurrentGame.Clock(Period).Number')));
      var mainClock = all('.board-clock');
      if (mainClock.length) {
        text('.board-kicker', 'PERIOD ' + value('ScoreBoard.CurrentGame.Clock(Period).Number'), mainClock[0]);
        text('strong', clockTime('Period'), mainClock[0]);
        mainClock[1].querySelector('.board-kicker').textContent = (active === 'NoClock' ? 'COMING UP' : active.toUpperCase()) + (active === 'Jam' ? ' ' + value('ScoreBoard.CurrentGame.Clock(Jam).Number') : '');
        text('strong', active === 'NoClock' ? '—' : clockTime(active), mainClock[1]);
        text('.board-kicker:last-child', active === 'NoClock' ? 'BETWEEN JAMS' : active.toUpperCase() + (running(active) ? ' IN PROGRESS' : ' STOPPED'), mainClock[1]);
      }
      var clocks = all('.bar .clock');
      if (clocks.length) {
        text('small', (active === 'NoClock' ? 'COMING UP' : active.toUpperCase()) + (active === 'Jam' ? ' ' + value('ScoreBoard.CurrentGame.Clock(Jam).Number') : ''), clocks[0]);
        text('strong', active === 'NoClock' ? '—' : clockTime(active), clocks[0]);
        var periodClock = active === 'Intermission' ? 'Intermission' : 'Period';
        text('small', periodClock.toUpperCase() + (periodClock === 'Period' ? ' ' + value('ScoreBoard.CurrentGame.Clock(Period).Number') : ''), clocks[1]);
        text('strong', final ? 'FINAL' : clockTime(periodClock), clocks[1]);
        text('.bar-status', final ? 'FINAL' : active === 'NoClock' ? 'BETWEEN JAMS' : active.toUpperCase());
      }
      var stateLabel = final ? (isTrue(value('ScoreBoard.CurrentGame.OfficialScore')) ? 'Official' : 'Unofficial') : Number(interNumber) === 0 ? 'PreGame' : 'Intermission';
      var finalClock = isTrue(value('ScoreBoard.CurrentGame.OfficialScore')) && isTrue(value('ScoreBoard.CurrentGame.ClockDuringFinalScore'));
      if (finalClock) stateLabel = 'OfficialWithClock';
      text('.inter-subtitle', value('ScoreBoard.Settings.Setting(ScoreBoard.Intermission.' + stateLabel + ')') || stateLabel);
      text('.countdown', clockTime('Intermission'));
      all('.countdown,.return-label').forEach(function (element) { element.hidden = !!final && !finalClock; });
      text('.inter-content h2', final ? 'Final score.' : Number(interNumber) === 0 ? 'The bout awaits.' : "We'll be fright back.");
      rotateSponsors();
      fitNames();
    }
    // CurrentGame follows the game selected in the operator UI, including game changes.
    var game = 'ScoreBoard.CurrentGame';
    var paths = ['InJam', 'OfficialScore', 'ClockDuringFinalScore', 'InOvertime', 'TimeoutOwner', 'OfficialReview', 'Rule(Period.Number)'].map(function (field) { return game + '.' + field; });
    [1, 2].forEach(function (id) {
      ['Name', 'AlternateName(' + nameContext + ')', 'Score', 'JamScore', 'DisplayLead', 'StarPass', 'Lost', 'Timeouts', 'OfficialReviews', 'RetainedOfficialReview'].forEach(function (field) {
        paths.push(game + '.Team(' + id + ').' + field);
      });
    });
    ['Period', 'Jam', 'Lineup', 'Timeout', 'Intermission'].forEach(function (clock) {
      ['Running', 'Time', 'Number', 'Direction'].forEach(function (field) { paths.push(game + '.Clock(' + clock + ').' + field); });
    });
    paths.push('ScoreBoard.Settings.Setting(ScoreBoard.' + view + '_SwapTeams)', 'ScoreBoard.Settings.Setting(ScoreBoard.' + view + '_ClockAfterTimeout)', 'ScoreBoard.Settings.Setting(ScoreBoard.Intermission.*)');
    WS.Register(paths, { triggerBatchFunc: schedule });
    window.addEventListener('resize', schedule);

    // Same media collection and wall-clock 5-second cadence as the standard dev display.
    function rotateSponsors() {
      var prefix = 'ScoreBoard.Media.Format(images).Type(sponsor_banner).File(';
      var banners = Object.keys(WS.state).filter(function (key) {
        return key.indexOf(prefix) === 0 && key.endsWith(').Src') && WS.state[key];
      }).map(function (key) {
        return { src: WS.state[key], name: WS.state[key.slice(0, -4) + '.Name'] || key };
      }).sort(function (a, b) { return a.name < b.name ? -1 : a.name > b.name ? 1 : 0; });
      var index = Math.round((Date.now() / 5000) % banners.length) % banners.length;
      var currentSponsor = banners.length ? banners[index].src : '';
      all('.sponsor').forEach(function (slot) {
        var img = slot.querySelector('img');
        var band = slot.closest('.sponsor-band');
        slot.hidden = !currentSponsor;
        if (band) band.hidden = !currentSponsor;
        img.hidden = !currentSponsor;
        if (currentSponsor) { if (img.getAttribute('src') !== currentSponsor) img.src = currentSponsor; }
        else img.removeAttribute('src');
        // The home sponsor slot follows HOME if the teams are swapped.
        if (mode === 'broadcast') {
          var teams = all('.bar .team');
          var swapped = isTrue(value('ScoreBoard.Settings.Setting(ScoreBoard.' + view + '_SwapTeams)'));
          var home = teams[swapped ? 1 : 0];
          if (home && slot.parentNode !== home) home.appendChild(slot);
        }
      });
    }
    WS.AfterLoad(function () {
      render();
      rotateSponsors();
    });
    WS.Register('ScoreBoard.Media.Format(images).Type(sponsor_banner)', { triggerBatchFunc: rotateSponsors });
    setInterval(rotateSponsors, 5000);
  }());
}());
