/* SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-or-later */
/* The main menu discovers this entry through the standard themes media collection. */
(function () {
  var url = new URL(window.location.href);
  var path = url.pathname.replace(/\/$/, '').replace(/\/index\.html$/, '');
  if (path === '/views/standard') {
    url.pathname = '/views/halloween/scoreboard.html';
    if (!url.searchParams.has('background') && !url.searchParams.has('venue')) {
      url.searchParams.set('venue', 'true');
    }
  } else if (path === '/views/overlay') {
    document.documentElement.classList.add('haunted-overlay');
    _include('/views/halloween/overlay.css');
    _include('/views/halloween/overlay.js');
    return;
  } else if (path === '/views/roster' || path === '/views/wb' || path === '/views/box') {
    var root = document.documentElement;
    root.classList.add('haunted-auxiliary');
    root.classList.add(path === '/views/roster' ? 'haunted-roster' : path === '/views/wb' ? 'haunted-whiteboard' : 'haunted-clocks');
    if (url.searchParams.get('background') === 'transparent') root.style.setProperty('--haunted-background', 'transparent');
    if (url.searchParams.get('background') === 'green') root.style.setProperty('--haunted-background', '#00ff00');
    if (url.searchParams.get('motion') === 'off') root.style.setProperty('--haunted-motion', 'paused');
    _include('/views/halloween/auxiliary.css');
    $(function () { $('#sbIndexLink').attr('href', '/?theme=' + encodeURIComponent(url.searchParams.get('theme'))); });
    return;
  } else {
    // Keep menus, operator panels, overlay admin, and the theme's own views on their current pages.
    return;
  }
  window.location.replace(url.href);
}());
