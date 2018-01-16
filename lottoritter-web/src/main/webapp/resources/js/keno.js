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
        lotteryIdentifier: 'keno',
        fields: [],
        drawingType: 'keno',
        embeddedTickets: [],
        permaTicket: false,
        durationOrBillingPeriod: 1,
        number: [1, 2, 3, 4, 5],
        bet: 100
    },
    price: {
        lotto: 0,
        plus5: 0,
        fee: 0,
        total: 0
    }
};

var plus5Model = {
    lotteryIdentifier: 'plus5',
    fields: [
        {
            fieldNumer: 1,
            selectedNumbers: []
        }
    ]
};

var quoteMatrix = [
    6, //max possible winning for type 2 in EUR
    16, //max possible winning for type 3 in EUR
    22, //max possible winning for type 4 in EUR
    100, //max possible winning for type 5 in EUR
    500, //max possible winning for type 6 in EUR
    1000, //max possible winning for type 7 in EUR
    10000, //max possible winning for type 8 in EUR
    50000, //max possible winning for type 9 in EUR
    100000 //max possible winning for type 10 in EUR
];

var keno = {
    selectNumber: function select(elem, number, fieldNumber) {
        var fields = getField(viewmodel.ticket.fields, fieldNumber);
        var header = $(elem).siblings('.header');

        if (fields[0].selectedNumbers.indexOf(number) === -1) {
            if (fields[0].selectedNumbers.length + 1 <= 10) {
                $(elem).addClass('marked');

                header.addClass('highlight');
                $(elem).siblings('.possibleWinnings').addClass('highlight');
                updateHeaderText(header, fields[0].selectedNumbers.length + 1);

                fields[0].selectedNumbers.push(number);
            }
        } else {
            var index = fields[0].selectedNumbers.indexOf(number);
            fields[0].selectedNumbers.splice(index, 1);
            $(elem).removeClass('marked');

            updateHeaderText(header, fields[0].selectedNumbers.length);

            if (fields[0].selectedNumbers.length === 0) {
                header.removeClass('highlight');
                $(elem).siblings('.possibleWinnings').removeClass('highlight');
            }
        }

        updatePrice();
        updatePossibleWinningsText($(header).siblings('.possibleWinnings').children('.jsPossibleWinningsAmount'), fields[0].selectedNumbers.length);
    },
    clearPick: function (elem, fieldNumber) {
        var fields = getField(viewmodel.ticket.fields, fieldNumber);
        var header = $(elem).siblings('.header');

        if (fields.length > 0) {
            $(elem).siblings('.tippbox').each(function () {
                $(this).removeClass('marked');
            });

            fields[0].selectedNumbers = [];
            updateHeaderText(header, 0);
            header.removeClass('highlight');
            $(header).siblings('.possibleWinnings').removeClass('highlight');
            updatePossibleWinningsText($(header).siblings('.possibleWinnings').children('.jsPossibleWinningsAmount'), 0);
        }

        updatePrice();
    },
    togglePermaTicketOption: function (elem) {
        var message;

        if ($(elem).hasClass('jsCheckedPermaTicket')) {
            message = messages['lottery.keno.options.duration.header.1'];
            $(elem).removeClass('jsCheckedPermaTicket');
            viewmodel.ticket.permaTicket = false;
        } else {
            message = messages['lottery.keno.options.duration.header.2'];
            $(elem).addClass('jsCheckedPermaTicket');
            viewmodel.ticket.permaTicket = true;
        }

        $('.jsDurationHeader').text(message);

        updatePrice();
    },
    quickPicks: function (cntFields) {
        var emptyFields = viewmodel.ticket.fields.filter(function (field) {
            return field.selectedNumbers.length === 0;
        });
        var sizeOfEmptyFields = emptyFields.length;
        var selectedType = $('.jsTypes option:selected').data('value');
        var headerElements = $('.header');

        var run = sizeOfEmptyFields < cntFields ? sizeOfEmptyFields : cntFields;

        for (var i = 0; i < run; ++i) {
            headerElements.each(function (index) {
                if (index + 1 === emptyFields[i].fieldNumber) {
                    quickpick($(this), emptyFields[i].fieldNumber, selectedType);
                }
            });
        }

        updatePrice();
    },
    ticketNumber: {
        up: function (elem) {
            var digit = $(elem).siblings('.digit');
            var current = parseInt(digit.text());
            var newVal = (current + 1) % 10;
            digit.text(newVal);

            updateTicketNumber();
        },
        down: function (elem) {
            var digit = $(elem).siblings('.digit');
            var current = parseInt(digit.text());
            var newVal;

            if (current - 1 < 0) {
                newVal = 9;
            } else {
                newVal = current - 1;
            }

            digit.text(newVal);

            updateTicketNumber();
        }
    },
    changeDuration: function (val) {
        viewmodel.ticket.durationOrBillingPeriod = val;
        updatePrice();
    },
    toggleAdditionalLottery: function (lotteryId) {
        var exists = viewmodel.ticket.embeddedTickets.filter(function (et) {
            return et.hasOwnProperty(lotteryId);
            // return et.super6 !== 'undefined'
        });
        var embeddedTicketModel = getEmbeddedTicketStructure(lotteryId);
        if (exists.length === 0) {
            embeddedTicketModel.fields[0].selectedNumbers = [];
            if (lotteryId === 'plus5') {
                embeddedTicketModel.fields[0].selectedNumbers.push(viewmodel.ticket.number[0], viewmodel.ticket.number[1], viewmodel.ticket.number[2], viewmodel.ticket.number[3],
                    viewmodel.ticket.number[4]);
            }
            var embeddedTicketToAdd = {};
            var embeddedTicketId = lotteryId;
            embeddedTicketToAdd[embeddedTicketId] = embeddedTicketModel;
            viewmodel.ticket.embeddedTickets.push(embeddedTicketToAdd);
        } else {
            var idx = 0;
            for (i = 0; i < viewmodel.ticket.embeddedTickets.length; i++) {
                if (viewmodel.ticket.embeddedTickets[i].hasOwnProperty(lotteryId)) {
                    idx = i;
                    break;
                }
            }
            viewmodel.ticket.embeddedTickets.splice(idx, 1);
        }
        updatePrice();
    },
    changeBet: function (val) {
        viewmodel.ticket.bet = val * 100;
        updatePrice();

        for (var i = 0; i < 5; ++i) {
            $('.header').each(function (index, elem) {
                if (index + 1 === viewmodel.ticket.fields[i].fieldNumber) {
                    updatePossibleWinningsText($(elem).siblings('.possibleWinnings').children('.jsPossibleWinningsAmount'), viewmodel.ticket.fields[i].selectedNumbers.length);
                }
            })
        }
    }
};

function quickpick(header, fieldNumber, type) {
    var fields = getField(viewmodel.ticket.fields, fieldNumber);

    if (fields.length === 0) {
        var newField = {
            fieldNumber: fieldNumber,
            selectedNumbers: getRandomField(type)
        };

        $(header).siblings('.tippbox').each(function () {
            var number = parseInt($(this).text());
            if ($.inArray(number, newField.selectedNumbers) > -1) {
                $(this).addClass('marked');
            }
        });

        viewmodel.ticket.fields.push(newField);
    } else {
        fields[0].selectedNumbers = getRandomField(type);

        $(header).siblings('.tippbox').each(function () {
            $(this).removeClass('marked');
        });

        $(header).siblings('.tippbox').each(function () {
            var number = parseInt($(this).text());
            if ($.inArray(number, fields[0].selectedNumbers) > -1) {
                $(this).addClass('marked');
            }
        });
    }

    updateHeaderText(header, type);
    header.addClass('highlight');
    $(header).siblings('.possibleWinnings').addClass('highlight');
    updatePossibleWinningsText($(header).siblings('.possibleWinnings').children('.jsPossibleWinningsAmount'), type);
    updatePrice();
}

function updatePossibleWinningsText(amountElem, type) {
    if (type < 2) {
        amountElem.text('-');
    }

    if (type >= 2) {
        var amount = quoteMatrix[type-2] * (viewmodel.ticket.bet / 100);
        amountElem.text(amount.toLocaleString('de', {minimumFractionDigits: 0}) + ' EUR');
    }
}

function updateHeaderText(header, selectedFieldsCnt) {
    var text;
    if (selectedFieldsCnt >= 2 && selectedFieldsCnt <= 10) {
        text = messages['lottery.keno.tipparea.header.type' + selectedFieldsCnt];
    }

    if (selectedFieldsCnt === 1) {
        text = messages['lottery.keno.tipparea.header.chooseone'];
    }

    if (selectedFieldsCnt === 0) {
        var message = messages['lottery.keno.tipparea.header.choose'];
        text = messageFormatter(message, 2);
    }

    header.text(text);
}


function getField(fields, fieldNumber) {
    return fields.filter(function (f) {
        return f.fieldNumber === fieldNumber;
    });
}

function getRandomInRange(max, min) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getRandomField(maxCnt) {
    var randomNumbers = [];
    for (var i = 0; i < maxCnt; ++i) {
        var rnd = getRandomInRange(70, 1);
        while ($.inArray(rnd, randomNumbers) > -1) {
            rnd = getRandomInRange(70, 1);
        }
        randomNumbers.push(rnd);
    }
    return randomNumbers;
}

function updatePrice() {
    viewmodel.price.lotto = viewmodel.ticket.fields.filter(function (field) {
        return field.selectedNumbers.length >= 2
    }).length * (viewmodel.ticket.bet / 100);
    var totalFee = 0;
    var totalPlus5 = 0;

    if (viewmodel.price.lotto > 0) {
        viewmodel.price.plus5 = isEmbeddedTicketPresent(viewmodel.ticket, 'plus5') ? prices.pricePlus5 / 100 : 0;
        viewmodel.price.fee = prices.feeFirstDrawing / 100;

        totalFee = viewmodel.price.fee;
        totalPlus5 = viewmodel.price.plus5 * viewmodel.ticket.durationOrBillingPeriod;
    }

    var totalLotto = viewmodel.price.lotto * viewmodel.ticket.durationOrBillingPeriod;

    var total = totalLotto + totalPlus5 + totalFee;
    viewmodel.price.total = total;


    $('.jsLottoPrice').text(totalLotto.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsPlus5Price').text(totalPlus5.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsFeePrice').text(totalFee.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsTotalPrice').text(total.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
}

function initTicketNumber() {
    for (var i = 0; i < 5; ++i) {
        viewmodel.ticket.number[i] = getRandomInRange(9, 0);
    }

    $('.jsFirstTicketNumber').text(viewmodel.ticket.number[0]);
    $('.jsSecondTicketNumber').text(viewmodel.ticket.number[1]);
    $('.jsThirdTicketNumber').text(viewmodel.ticket.number[2]);
    $('.jsFourthTicketNumber').text(viewmodel.ticket.number[3]);
    $('.jsFifthTicketNumber').text(viewmodel.ticket.number[4]);
}

function updateTicketNumber() {
    viewmodel.ticket.number[0] = parseInt($('.jsFirstTicketNumber').text());
    viewmodel.ticket.number[1] = parseInt($('.jsSecondTicketNumber').text());
    viewmodel.ticket.number[2] = parseInt($('.jsThirdTicketNumber').text());
    viewmodel.ticket.number[3] = parseInt($('.jsFourthTicketNumber').text());
    viewmodel.ticket.number[4] = parseInt($('.jsFifthTicketNumber').text());

    plus5Model.fields[0].selectedNumbers[0] = viewmodel.ticket.number[0];
    plus5Model.fields[0].selectedNumbers[1] = viewmodel.ticket.number[1];
    plus5Model.fields[0].selectedNumbers[2] = viewmodel.ticket.number[2];
    plus5Model.fields[0].selectedNumbers[3] = viewmodel.ticket.number[3];
    plus5Model.fields[0].selectedNumbers[4] = viewmodel.ticket.number[4];
}

function initNextDrawing() {
    var days = ['su', 'mo', 'tu', 'we', 'th', 'fr', 'sa'];
    var mo = messages['lottery.keno.options.nextdrawing.mo'];
    var tu = messages['lottery.keno.options.nextdrawing.tu'];
    var we = messages['lottery.keno.options.nextdrawing.we'];
    var th = messages['lottery.keno.options.nextdrawing.th'];
    var fr = messages['lottery.keno.options.nextdrawing.fr'];
    var sa = messages['lottery.keno.options.nextdrawing.sa'];
    var su = messages['lottery.keno.options.nextdrawing.su'];

    var date = new Date(nextDrawing);
    var moFullText = mo + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var tuFullText = tu + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var weFullText = we + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var thFullText = th + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var frFullText = fr + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var saFullText = sa + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var suFullText = su + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var drawingDay = days[date.getDay()];

    if (drawingDay === 'mo') {
        $('.jsNextDrawingText').text(moFullText);
    }
    if (drawingDay === 'tu') {
        $('.jsNextDrawingText').text(tuFullText);
    }
    if (drawingDay === 'we') {
        $('.jsNextDrawingText').text(weFullText);
    }
    if (drawingDay === 'th') {
        $('.jsNextDrawingText').text(thFullText);
    }
    if (drawingDay === 'fr') {
        $('.jsNextDrawingText').text(frFullText);
    }
    if (drawingDay === 'sa') {
        $('.jsNextDrawingText').text(saFullText);
    }
    if (drawingDay === 'su') {
        $('.jsNextDrawingText').text(suFullText);
    }
}

function userHasUncompletedFields() {
    return viewmodel.ticket.fields.filter(function (field) {
        return field.selectedNumbers.length > 0 && field.selectedNumbers.length < 2;
    }).length > 0;
}

function getAllUncompletedFields() {
    return viewmodel.ticket.fields.filter(function (field) {
        if (field.selectedNumbers.length > 0 && field.selectedNumbers.length < 2) {
            return field;
        }
    });
}

function noTippsWereMade() {
    return viewmodel.ticket.fields.filter(function (field) {
        return field.selectedNumbers.length === 0;
    }).length === 5;
}

function submitTicket() {
    if (noTippsWereMade()) {
        showError(messages['lottery.keno.error.title'], messages['lottery.keno.error.notipps.why'], messages['lottery.keno.error.notipps.howtosolve']);
        return;
    }

    if (userHasUncompletedFields()) {
        var uncompletedFields = getAllUncompletedFields();
        var why = messages['lottery.keno.error.missingtipp.why'];
        var howtosolve = messageFormatter(messages['lottery.keno.error.missingtipp.howtosolve'], uncompletedFields[0].fieldNumber);
        showError(messages['lottery.keno.error.title'], why, howtosolve);
        return;
    }

    var ticketSubmitForm = document.getElementById("ticketSubmitForm");
    var ticketJsonField = document.getElementById("ticketSubmitForm:ticketJSON");
    ticketJsonField.value = JSON.stringify(viewmodel.ticket);
    document.getElementById("ticketSubmitForm:submitTicketBtn").click();
}

function getEmbeddedTicketStructure(lotteryIdentifier) {
    if (lotteryIdentifier === 'plus5') {
        return plus5Model;
    }
}

function isEmbeddedTicketPresent(ticket, lotteryIdentifier) {
    var maxIndex = ticket.embeddedTickets.length;
    if (maxIndex > 0) {
        for (var i = 0; i < maxIndex; ++i) {
            if (ticket.embeddedTickets[i].hasOwnProperty(lotteryIdentifier)) {
                return true;
            }
        }
    }
    return false;
}

var init = function init() {
    $('.header').each(function () {
        updateHeaderText($(this), 0);
    });

    for (var i = 0; i < 5; ++i) {
        var field = {
            fieldNumber: i + 1,
            selectedNumbers: []
        };
        viewmodel.ticket.fields.push(field);
    }

    initTicketNumber();
    initNextDrawing();
    initCountDown(nextDrawing, '.jsHours', '.jsMinutes', '.jsSeconds');
    updatePrice();
};