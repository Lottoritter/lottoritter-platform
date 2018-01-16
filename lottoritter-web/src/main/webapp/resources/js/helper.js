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
var messageFormatter = function (message) {
    var args = Array.prototype.slice.call(arguments), key = args[0];

    for (var i = 0; i < args.length; i++) {
        regExStr = '\\{' + i + '}';
        regEx = new RegExp(regExStr);
        var parameter = args[i + 1];
        message = message.replace(regEx, parameter);
    }
    return message;
};

var showError = function (what, why, howtosolve) {
    $('.error .what').text(what);
    $('.error .why').text(why);
    $('.error .how-to-solve').text(howtosolve);
    $('.error').show();
};

var hideError = function() {
    $('.error').hide();
};