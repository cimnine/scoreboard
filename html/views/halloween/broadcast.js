/* SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-or-later */
// Keep old direct broadcast links working through the native interactive overlay.
(function () {
  var url = new URL(window.location.href);
  url.pathname = '/views/overlay/';
  url.searchParams.set('theme', '/themes/custom/Halloween.js');
  window.location.replace(url.href);
}());
