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
    url.pathname = '/views/halloween/broadcast.html';
    if (!url.searchParams.has('background')) {
      url.searchParams.set('background', 'transparent');
    }
  } else {
    // Keep menus, operator panels, overlay admin, and the theme's own views on their current pages.
    return;
  }
  window.location.replace(url.href);
}());
