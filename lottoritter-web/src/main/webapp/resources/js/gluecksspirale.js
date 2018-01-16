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
var viewmodel = {
    ticket: {
        lotteryIdentifier: 'gluecksspirale',
        fields: [],
        drawingType: 'gluecksspiralesa',
        embeddedTickets: [],
        permaTicket: false,
        durationOrBillingPeriod: 1,
        number: [1, 2, 3, 4, 5, 6, 7]
    },
    price: {
        lotto: 0,
        super6: 0,
        spiel77: 0,
        gluecksspirale: 0,
        feeFirstDrawing: 0,
        feeSecondDrawing: 0,
        feeGluecksspirale: 0,
        total: 0
    }
};

var gluecksspirale = {
    togglePermaTicketOption: function (elem) {
        var message;

        if ($(elem).hasClass('jsCheckedPermaTicket')) {
            message = messages['lottery.gluecksspirale.options.duration.header.1'];
            $(elem).removeClass('jsCheckedPermaTicket');
            viewmodel.ticket.permaTicket = false;
        } else {
            message = messages['lottery.gluecksspirale.options.duration.header.2'];
            $(elem).addClass('jsCheckedPermaTicket');
            viewmodel.ticket.permaTicket = true;
        }

        $('.jsDurationHeader').text(message);

        updatePrice();
    },
    ticketNumber: {
        up: function (idx) {
            var number = viewmodel.ticket.number[idx];
            var newVal = (number + 1) % 10;
            viewmodel.ticket.number[idx] = newVal;

            updateTicketNumber();
        },
        down: function (idx) {
            var newVal;
            var number = viewmodel.ticket.number[idx];

            if (number - 1 < 0) {
                newVal = 9;
            } else {
                newVal = number - 1;
            }

            viewmodel.ticket.number[idx] = newVal;

            updateTicketNumber();
        },
        random: function () {
            initTicketNumber();
        }
    },
    changeDuration: function (val) {
        viewmodel.ticket.durationOrBillingPeriod = val;
        updatePrice();
    },
    toggleAdditionalLottery: function (lotteryId) {
        if ($.inArray(lotteryId, viewmodel.ticket.additionalLotteries) > -1) {
            viewmodel.ticket.additionalLotteries.splice($.inArray(lotteryId, viewmodel.ticket.additionalLotteries), 1);
        } else {
            viewmodel.ticket.additionalLotteries.push(lotteryId);
        }
        updatePrice();
    }
};

function getRandomInRange(max, min) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function updatePrice() {
    viewmodel.price.feeGluecksspirale = prices.feeGluecksspirale / 100;
    viewmodel.price.gluecksspirale = prices.gluecksspirale / 100;

    var totalLotto = viewmodel.price.gluecksspirale * viewmodel.ticket.durationOrBillingPeriod;
    var totalFee = viewmodel.price.gluecksspirale > 0 ? viewmodel.price.feeGluecksspirale : 0;

    var total = totalLotto + totalFee;
    viewmodel.price.total = total;


    $('.jsLottoPrice').text(totalLotto.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsFeePrice').text(totalFee.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsTotalPrice').text(total.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
}

function initTicketNumber() {
    for (var i = 0; i < 7; ++i) {
        viewmodel.ticket.number[i] = getRandomInRange(9, 0);
    }

    updateTicketNumber();
}

function updateTicketNumber() {
    $('.jsFirstTicketNumber').text(viewmodel.ticket.number[0]);
    $('.jsSecondTicketNumber').text(viewmodel.ticket.number[1]);
    $('.jsThirdTicketNumber').text(viewmodel.ticket.number[2]);
    $('.jsFourthTicketNumber').text(viewmodel.ticket.number[3]);
    $('.jsFifthTicketNumber').text(viewmodel.ticket.number[4]);
    $('.jsSixthTicketNumber').text(viewmodel.ticket.number[5]);
    $('.jsSeventhTicketNumber').text(viewmodel.ticket.number[6]);
}

function initNextDrawing() {
    var fr = messages['lottery.gluecksspirale.options.nextdrawing.fr'];
    var date = new Date(nextDrawing);
    var frFullText = fr + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    $('.jsNextDrawingText').text(frFullText);
}

function submitTicket() {
    viewmodel.ticket.fields[0].selectedNumbers = viewmodel.ticket.number;
    var ticketSubmitForm = document.getElementById("ticketSubmitForm");
    var ticketJsonField = document.getElementById("ticketSubmitForm:ticketJSON");
    ticketJsonField.value = JSON.stringify(viewmodel.ticket);
    document.getElementById("ticketSubmitForm:submitTicketBtn").click();
}


var init = function init() {
    for (var i = 0; i < 1; ++i) {
        var field = {
            fieldNumber: i + 1,
            selectedNumbers: [],
            selectedAdditionalNumbers: []
        };
        viewmodel.ticket.fields.push(field);
    }

    initTicketNumber();
    initNextDrawing();
    initCountDown(nextDrawing, '.jsHours', '.jsMinutes', '.jsSeconds');
    updatePrice();
};