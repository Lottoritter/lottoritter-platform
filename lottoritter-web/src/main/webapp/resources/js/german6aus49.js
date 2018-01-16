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
        lotteryIdentifier: 'german6aus49',
        fields: [],
        drawingType: 'german6aus49wesa',
        embeddedTickets: [],
        permaTicket: false,
        durationOrBillingPeriod: 1,
        number: [1, 2, 3, 4, 5, 6, 7]
    },
    price: {
        lotto: 0,
        super6: 0,
        spiel77: 0,
        glucksspirale: 0,
        feeFirstDrawing: 0,
        feeSecondDrawing: 0,
        feeGlucksspirale: 0,
        total: 0
    }
};

var super6model = {
    lotteryIdentifier: 'super6',
    fields: [
        {
            fieldNumer: 1,
            selectedNumbers: []
        }
    ]
};

var spiel77model = {
    lotteryIdentifier: 'spiel77',
    fields: [
        {
            fieldNumer: 1,
            selectedNumbers: []
        }
    ]
};

var gluecksspiraleModel = {
    lotteryIdentifier: 'gluecksspirale',
    fields: [
        {
            fieldNumer: 1,
            selectedNumbers: []
        }
    ]
};

var lotto6aus49 = {
    selectNumber: function select(elem, number, fieldNumber) {
        var fields = getField(viewmodel.ticket.fields, fieldNumber);
        var header = $(elem).siblings('.header');

        if (fields[0].selectedNumbers.indexOf(number) === -1) {
            if (fields[0].selectedNumbers.length + 1 <= 6) {
                $(elem).addClass('marked');

                header.addClass('highlight');
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
            }
        }

        updatePrice();
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
        }

        updatePrice();
    },
    quickPick: function (elem, fieldNumber) {
        var fields = getField(viewmodel.ticket.fields, fieldNumber);
        var header = $(elem).siblings('.header');

        if (fields.length === 0) {
            var newField = {
                fieldNumber: fieldNumber,
                selectedNumbers: getRandomField()
            };

            $(elem).siblings('.tippbox').each(function () {
                var number = parseInt($(this).text());
                if ($.inArray(number, newField.selectedNumbers) > -1) {
                    $(this).addClass('marked');
                }
            });

            viewmodel.ticket.fields.push(newField);
        } else {
            fields[0].selectedNumbers = getRandomField();

            $(elem).siblings('.tippbox').each(function () {
                $(this).removeClass('marked');
            });

            $(elem).siblings('.tippbox').each(function () {
                var number = parseInt($(this).text());
                if ($.inArray(number, fields[0].selectedNumbers) > -1) {
                    $(this).addClass('marked');
                }
            });
        }

        updateHeaderText(header, 6);
        header.addClass('highlight');
        updatePrice();
    },
    togglePermaTicketOption: function (elem) {
        var message;

        if ($(elem).hasClass('jsCheckedPermaTicket')) {
            message = messages['lottery.german6aus49.options.duration.header.1'];
            $(elem).removeClass('jsCheckedPermaTicket');
            viewmodel.ticket.permaTicket = false;
        } else {
            message = messages['lottery.german6aus49.options.duration.header.2'];
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
        var quickpickElements = $('.quickpick');

        var run = sizeOfEmptyFields < cntFields ? sizeOfEmptyFields : cntFields;

        for (var i = 0; i < run; ++i) {
            quickpickElements.each(function (index) {
                if (index + 1 === emptyFields[i].fieldNumber) {
                    lotto6aus49.quickPick($(this), emptyFields[i].fieldNumber);
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
        var exists = viewmodel.ticket.embeddedTickets.filter(function(et) {
            return et.hasOwnProperty(lotteryId);
            // return et.super6 !== 'undefined'
        });
        var embeddedTicketModel = getEmbeddedTicketStructure(lotteryId);
        if (exists.length === 0) {
            embeddedTicketModel.fields[0].selectedNumbers = [];
            if (lotteryId === 'super6') {
                embeddedTicketModel.fields[0].selectedNumbers.push(viewmodel.ticket.number[1], viewmodel.ticket.number[2], viewmodel.ticket.number[3],
                    viewmodel.ticket.number[4], viewmodel.ticket.number[5], viewmodel.ticket.number[6]);
            } else if (lotteryId === 'spiel77') {
                embeddedTicketModel.fields[0].selectedNumbers.push(viewmodel.ticket.number[0], viewmodel.ticket.number[1],
                    viewmodel.ticket.number[2],viewmodel.ticket.number[3],
                    viewmodel.ticket.number[4], viewmodel.ticket.number[5], viewmodel.ticket.number[6]);
            } else if (lotteryId === 'gluecksspirale') {
                embeddedTicketModel.fields[0].selectedNumbers.push(viewmodel.ticket.number[0], viewmodel.ticket.number[1],
                    viewmodel.ticket.number[2],viewmodel.ticket.number[3],
                    viewmodel.ticket.number[4], viewmodel.ticket.number[5], viewmodel.ticket.number[6]);
            }
            var embeddedTicketToAdd = {};
            var embeddedTicketId = lotteryId;
            embeddedTicketToAdd[embeddedTicketId] = embeddedTicketModel;
            viewmodel.ticket.embeddedTickets.push(embeddedTicketToAdd);
        } else {
            var idx = 0;
            for (i=0; i<viewmodel.ticket.embeddedTickets.length; i++){
                if (viewmodel.ticket.embeddedTickets[i].hasOwnProperty(lotteryId)) {
                    idx = i;
                    break;
                }
            }
            viewmodel.ticket.embeddedTickets.splice(idx, 1);
        }
        updatePrice();
    },
    changeDrawing: function (drawing) {
        viewmodel.ticket.drawingType = drawing;

        if (drawing === 'german6aus49we') {
            $('.additionalLotteries .additionalLottery .not-selectable').css('visibility', 'visible');
            if (isEmbeddedTicketPresent(viewmodel.ticket, 'gluecksspirale')) {
                $('.jsGlucksspiraleCheckbox').click();
            }
        }

        if (drawing === 'german6aus49wesa' || drawing === 'german6aus49sa') {
            $('.additionalLotteries .additionalLottery .not-selectable').css('visibility', 'hidden');
        }

        updatePrice();
        updateNextDrawing();
    }
};

function updateHeaderText(header, selectedFieldsCnt) {
    var text;
    if (selectedFieldsCnt === 6) {
        text = messages['lottery.german6aus49.tipparea.header.normal'];
    }

    if (selectedFieldsCnt === 5) {
        text = messages['lottery.german6aus49.tipparea.header.chooseone'];
    }

    if (selectedFieldsCnt < 5) {
        var message = messages['lottery.german6aus49.tipparea.header.choose'];
        text = messageFormatter(message, 6 - selectedFieldsCnt);
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

function getRandomField() {
    var randomNumbers = [];
    for (var i = 0; i < 6; ++i) {
        var rnd = getRandomInRange(49, 1);
        while ($.inArray(rnd, randomNumbers) > -1) {
            rnd = getRandomInRange(49, 1);
        }
        randomNumbers.push(rnd);
    }
    return randomNumbers;
}

function updatePrice() {
    viewmodel.price.lotto = viewmodel.ticket.fields.filter(function (field) {
            return field.selectedNumbers.length === 6
        }).length * (prices.lotto / 100);

    if (viewmodel.price.lotto > 0) {
        viewmodel.price.super6 = isEmbeddedTicketPresent(viewmodel.ticket, 'super6') ? prices.super6 / 100 : 0;
        viewmodel.price.spiel77 = isEmbeddedTicketPresent(viewmodel.ticket, 'spiel77') ? prices.spiel77 / 100 : 0;
        viewmodel.price.glucksspirale = isEmbeddedTicketPresent(viewmodel.ticket, 'gluecksspirale') ? prices.glucksspirale / 100 : 0;
    }

    viewmodel.price.feeFirstDrawing = prices.feeFirstDrawing / 100;
    viewmodel.price.feeSecondDrawing = prices.feeSecondDrawing / 100;
    viewmodel.price.feeGlucksspirale = prices.feeGlucksspirale / 100;

    var totalLotto = viewmodel.price.lotto * (viewmodel.ticket.drawingType === 'german6aus49wesa' ? 2 : 1) * viewmodel.ticket.durationOrBillingPeriod;
    var totalSuper6 = viewmodel.price.super6 * (viewmodel.ticket.drawingType === 'german6aus49wesa' ? 2 : 1) * viewmodel.ticket.durationOrBillingPeriod;
    var totalSpiel77 = viewmodel.price.spiel77 * (viewmodel.ticket.drawingType === 'german6aus49wesa' ? 2 : 1) * viewmodel.ticket.durationOrBillingPeriod;
    var totalGlucksspirale = viewmodel.price.glucksspirale * viewmodel.ticket.durationOrBillingPeriod;
    var totalFee = 0;

    if (viewmodel.price.lotto > 0) {
        if (viewmodel.ticket.drawingType === 'german6aus49wesa') {
            totalFee = viewmodel.price.feeFirstDrawing + viewmodel.price.feeSecondDrawing ;
        } else {
            totalFee = viewmodel.price.feeFirstDrawing;
        }

        if (isEmbeddedTicketPresent(viewmodel.ticket, 'gluecksspirale')) {
            totalFee += viewmodel.price.feeGlucksspirale;
        }
    }

    var total = totalLotto + totalSuper6 + totalSpiel77 + totalGlucksspirale + totalFee;
    viewmodel.price.total = total;


    $('.jsLottoPrice').text(totalLotto.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsSuper6Price').text(totalSuper6.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsSpiel77Price').text(totalSpiel77.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsGlucksspiralePrice').text(totalGlucksspirale.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsFeePrice').text(totalFee.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
    $('.jsTotalPrice').text(total.toLocaleString('de', {minimumFractionDigits: 2}) + ' EUR');
}

function initTicketNumber() {
    for (var i = 0; i < 7; ++i) {
        viewmodel.ticket.number[i] = getRandomInRange(9, 0);
    }

    $('.jsFirstTicketNumber').text(viewmodel.ticket.number[0]);
    $('.jsSecondTicketNumber').text(viewmodel.ticket.number[1]);
    $('.jsThirdTicketNumber').text(viewmodel.ticket.number[2]);
    $('.jsFourthTicketNumber').text(viewmodel.ticket.number[3]);
    $('.jsFifthTicketNumber').text(viewmodel.ticket.number[4]);
    $('.jsSixthTicketNumber').text(viewmodel.ticket.number[5]);
    $('.jsSeventhTicketNumber').text(viewmodel.ticket.number[6]);
}

function updateTicketNumber() {
    viewmodel.ticket.number[0] = parseInt($('.jsFirstTicketNumber').text());
    viewmodel.ticket.number[1] = parseInt($('.jsSecondTicketNumber').text());
    viewmodel.ticket.number[2] = parseInt($('.jsThirdTicketNumber').text());
    viewmodel.ticket.number[3] = parseInt($('.jsFourthTicketNumber').text());
    viewmodel.ticket.number[4] = parseInt($('.jsFifthTicketNumber').text());
    viewmodel.ticket.number[5] = parseInt($('.jsSixthTicketNumber').text());
    viewmodel.ticket.number[6] = parseInt($('.jsSeventhTicketNumber').text());

    super6model.fields[0].selectedNumbers[0] = viewmodel.ticket.number[1];
    super6model.fields[0].selectedNumbers[1] = viewmodel.ticket.number[2];
    super6model.fields[0].selectedNumbers[2] = viewmodel.ticket.number[3];
    super6model.fields[0].selectedNumbers[3] = viewmodel.ticket.number[4];
    super6model.fields[0].selectedNumbers[4] = viewmodel.ticket.number[5];
    super6model.fields[0].selectedNumbers[5] = viewmodel.ticket.number[6];

    spiel77model.fields[0].selectedNumbers[0] = viewmodel.ticket.number[0];
    spiel77model.fields[0].selectedNumbers[1] = viewmodel.ticket.number[1];
    spiel77model.fields[0].selectedNumbers[2] = viewmodel.ticket.number[2];
    spiel77model.fields[0].selectedNumbers[3] = viewmodel.ticket.number[3];
    spiel77model.fields[0].selectedNumbers[4] = viewmodel.ticket.number[4];
    spiel77model.fields[0].selectedNumbers[5] = viewmodel.ticket.number[5];
    spiel77model.fields[0].selectedNumbers[6] = viewmodel.ticket.number[6];

    gluecksspiraleModel.fields[0].selectedNumbers[0] = viewmodel.ticket.number[0];
    gluecksspiraleModel.fields[0].selectedNumbers[1] = viewmodel.ticket.number[1];
    gluecksspiraleModel.fields[0].selectedNumbers[2] = viewmodel.ticket.number[2];
    gluecksspiraleModel.fields[0].selectedNumbers[3] = viewmodel.ticket.number[3];
    gluecksspiraleModel.fields[0].selectedNumbers[4] = viewmodel.ticket.number[4];
    gluecksspiraleModel.fields[0].selectedNumbers[5] = viewmodel.ticket.number[5];
    gluecksspiraleModel.fields[0].selectedNumbers[6] = viewmodel.ticket.number[6];
}

function updateNextDrawing() {
    var days = ['su', 'mo', 'tu', 'we', 'th', 'fr', 'sa'];
    var we = messages['lottery.german6aus49.options.nextdrawing.we'];
    var sa = messages['lottery.german6aus49.options.nextdrawing.sa'];

    var date = new Date(nextDrawing);
    var weFullText = we + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var saFullText = sa + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var drawingDay = days[date.getDay()];

    if (viewmodel.ticket.drawingType === 'german6aus49we') {
        if (drawingDay === 'we') {
            $('.jsNextDrawingText').text(weFullText);
        } else {
            var nextWeText = we + ' ' + (date.getDate() + 4) + "." + (date.getMonth() + 1) + "." + date.getFullYear();
            $('.jsNextDrawingText').text(nextWeText);
        }
    }

    if (viewmodel.ticket.drawingType === 'german6aus49sa') {
        if (drawingDay === 'we') {
            var nextSaText = sa + ' ' + (date.getDate() + 3) + "." + (date.getMonth() + 1) + "." + date.getFullYear();
            $('.jsNextDrawingText').text(nextSaText);
        } else {
            $('.jsNextDrawingText').text(saFullText);
        }
    }

    if (viewmodel.ticket.drawingType === 'german6aus49wesa') {
        initNextDrawing();
    }
}

function initNextDrawing() {
    var days = ['su', 'mo', 'tu', 'we', 'th', 'fr', 'sa'];
    var we = messages['lottery.german6aus49.options.nextdrawing.we'];
    var sa = messages['lottery.german6aus49.options.nextdrawing.sa'];


    var date = new Date(nextDrawing);
    var weFullText = we + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    var saFullText = sa + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();

    var currentDay = days[new Date().getDay()];

    if (currentDay === 'su' || currentDay === 'mo' || currentDay === 'tu') {
        $('.jsNextDrawingText').text(weFullText);
    }

    if (currentDay === 'th' || currentDay === 'fr') {
        $('.jsNextDrawingText').text(saFullText);
    }

    if (currentDay === 'we') {
        if (new Date().getHours() >= 17 && new Date().getMinutes() >= 45) {
            $('.jsNextDrawingText').text(saFullText);
        } else {
            $('.jsNextDrawingText').text(weFullText);
        }
    }

    if (currentDay === 'sa') {
        if (new Date().getHours() >= 18 && new Date().getMinutes() >= 45) {
            $('.jsNextDrawingText').text(weFullText);
        } else {
            $('.jsNextDrawingText').text(saFullText);
        }
    }
}

function userHasUncompletedFields() {
    return viewmodel.ticket.fields.filter(function (field) {
            return field.selectedNumbers.length > 0 && field.selectedNumbers.length < 6;
        }).length > 0;
}

function getAllUncompletedFields() {
    return viewmodel.ticket.fields.filter(function (field) {
        if (field.selectedNumbers.length > 0 && field.selectedNumbers.length < 6) {
            return field;
        }
    });
}

function noTippsWereMade() {
    return viewmodel.ticket.fields.filter(function (field) {
            return field.selectedNumbers.length === 0;
        }).length === 12;
}

function submitTicket() {
    if (noTippsWereMade()) {
        showError(messages['lottery.german6aus49.error.title'], messages['lottery.german6aus49.error.notipps.why'], messages['lottery.german6aus49.error.notipps.howtosolve']);
        return;
    }

    if (userHasUncompletedFields()) {
        var uncompletedFields = getAllUncompletedFields();
        var why = messages['lottery.german6aus49.error.missingtipp.why'];
        var howtosolve = messageFormatter(messages['lottery.german6aus49.error.missingtipp.howtosolve'], uncompletedFields[0].fieldNumber);
        showError(messages['lottery.german6aus49.error.title'], why, howtosolve);
        return;
    }

    var ticketSubmitForm = document.getElementById("ticketSubmitForm");
    var ticketJsonField = document.getElementById("ticketSubmitForm:ticketJSON");
    viewmodel.ticket.fields.forEach(function(element) {
        if (element.selectedNumbers.length > 0) {
            var additionalNumber = [];
            additionalNumber.push(viewmodel.ticket.number[6]);
            element.selectedAdditionalNumbers = additionalNumber;
        }
    });
    ticketJsonField.value = JSON.stringify(viewmodel.ticket);
    document.getElementById("ticketSubmitForm:submitTicketBtn").click();
}

function getEmbeddedTicketStructure(lotteryIdentifier) {
    if (lotteryIdentifier === 'super6') {
        return super6model;
    } else if (lotteryIdentifier === 'spiel77') {
        return spiel77model;
    } else if (lotteryIdentifier === 'gluecksspirale') {
        return gluecksspiraleModel;
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

    for (var i = 0; i < 12; ++i) {
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