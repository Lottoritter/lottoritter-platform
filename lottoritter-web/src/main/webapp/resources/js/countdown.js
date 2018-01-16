/* Copyright 2017 Ulrich Cech & Christopher Schmidt
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
* @author Christopher Schmidt
*/
function initCountDown(nextDrawing, selectorHours, selectorMinutes, selectorSeconds) {
    var countDownDate = new Date(nextDrawing).getTime();

    var x = setInterval(function () {
        var now = new Date().getTime();
        var distance = countDownDate - now;

        var days = Math.floor(distance / (1000 * 60 * 60 * 24));
        var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)) + (days * 24);
        var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = Math.floor((distance % (1000 * 60)) / 1000);

        var $jsHours = $(selectorHours);
        var $jsMinutes = $(selectorMinutes);
        var $jsSeconds = $(selectorSeconds);
        $jsHours.text(hours);
        $jsMinutes.text(minutes);
        $jsSeconds.text(seconds);

        if (distance < 0) {
            clearInterval(x);
            $jsHours.text('00');
            $jsMinutes.text('00');
            $jsSeconds.text('00');
        }
    }, 1000);
}