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
var key = "accepted_cookiehint";

var cookiehint = {
    accept: function () {
        if (isBrowserSupportingStorage()) {
            localStorage.setItem(key, true);
        } else {
            setCookie(key, true)
        }

        $('#jsCookiehint').hide();
    }
};
function isBrowserSupportingStorage() {
    return typeof(Storage) !== "undefined";
}

$(document).ready(function () {
    if (isBrowserSupportingStorage()) {
        var accepted = localStorage.getItem(key);

        if (accepted) {
            $('#jsCookiehint').hide();
        } else {
            $('#jsCookiehint').show();
        }
    } else {
        var cookie = getCookie(key);

        if (cookie === "") {
            $('#jsCookiehint').show();
        } else {
            $('#cookiehint').hide();
        }
    }
});

function setCookie(cname, cvalue) {
    document.cookie = cname + "=" + cvalue + ";path=/";
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}