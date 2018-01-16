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
        lotteryIdentifier: 'euroJackpot',
        fields: [],
        drawingType: 'eurojackpotfr',
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

var eurojackpot = {
    selectNumber: function select(elem, number, fieldNumber) {
        var fields = getField(viewmodel.ticket.fields, fieldNumber);
        var header = $(elem).siblings('.header');

        if (fields[0].selectedNumbers.indexOf(number) === -1) {
            if (fields[0].selectedNumbers.length + 1 <= 5) {
                $(elem).addClass('marked');

                header.addClass('highlight');
                updateHeaderText(header, fields[0].selectedNumbers.length + 1, fields[0].selectedAdditionalNumbers.length);

                fields[0].selectedNumbers.push(number);
            }
        } else {
            var index = fields[0].selectedNumbers.indexOf(number);
            fields[0].selectedNumbers.splice(index, 1);
            $(elem).removeClass('marked');

            updateHeaderText(header, fields[0].selectedNumbers.length, fields[0].selectedAdditionalNumbers.length);

            if (fields[0].selectedNumbers.length === 0  && fields[0].selectedNumbers.length === 0) {
                header.removeClass('highlight');
            }
        }

        updatePrice();
    },
    selectAdditionalNumber: function select(elem, number, fieldNumber) {
        var fields = getField(viewmodel.ticket.fields, fieldNumber);
        var header = $(elem).siblings('.header');

        if (fields[0].selectedAdditionalNumbers.indexOf(number) === -1) {
            if (fields[0].selectedAdditionalNumbers.length + 1 <= 2) {
                $(elem).addClass('marked');

                header.addClass('highlight');
                updateHeaderText(header, fields[0].selectedNumbers.length, fields[0].selectedAdditionalNumbers.length + 1);

                fields[0].selectedAdditionalNumbers.push(number);
            }
        } else {
            var index = fields[0].selectedAdditionalNumbers.indexOf(number);
            fields[0].selectedAdditionalNumbers.splice(index, 1);
            $(elem).removeClass('marked');

            updateHeaderText(header, fields[0].selectedNumbers.length, fields[0].selectedAdditionalNumbers.length);

            if (fields[0].selectedAdditionalNumbers.length === 0 && fields[0].selectedNumbers.length === 0) {
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
            fields[0].selectedAdditionalNumbers = [];
            updateHeaderText(header, 0, 0);
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
                selectedNumbers: getRandomField(),
                selectedAdditionalNumbers: getRandomAdditionalNumbers()
            };

            $(elem).siblings('.jsNormalNumber').each(function () {
                var number = parseInt($(this).text());
                if ($.inArray(number, newField.selectedNumbers) > -1) {
                    $(this).addClass('marked');
                }
            });

            $(elem).siblings('.jsAdditionalNumber').each(function () {
                var number = parseInt($(this).text());
                if ($.inArray(number, newField.selectedAdditionalNumbers) > -1) {
                    $(this).addClass('marked');
                }
            });

            viewmodel.ticket.fields.push(newField);
        } else {
            fields[0].selectedNumbers = getRandomField();
            fields[0].selectedAdditionalNumbers = getRandomAdditionalNumbers();

            $(elem).siblings('.jsNormalNumber').each(function () {
                $(this).removeClass('marked');
            });

            $(elem).siblings('.jsNormalNumber').each(function () {
                var number = parseInt($(this).text());
                if ($.inArray(number, fields[0].selectedNumbers) > -1) {
                    $(this).addClass('marked');
                }
            });

            $(elem).siblings('.jsAdditionalNumber').each(function () {
                $(this).removeClass('marked');
            });

            $(elem).siblings('.jsAdditionalNumber').each(function () {
                var number = parseInt($(this).text());
                if ($.inArray(number, fields[0].selectedAdditionalNumbers) > -1) {
                    $(this).addClass('marked');
                }
            });
        }

        updateHeaderText(header, 5, 2);
        header.addClass('highlight');
        updatePrice();
    },
    togglePermaTicketOption: function (elem) {
        var message;

        if ($(elem).hasClass('jsCheckedPermaTicket')) {
            message = messages['lottery.eurojackpot.options.duration.header.1'];
            $(elem).removeClass('jsCheckedPermaTicket');
            viewmodel.ticket.permaTicket = false;
        } else {
            message = messages['lottery.eurojackpot.options.duration.header.2'];
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
                    eurojackpot.quickPick($(this), emptyFields[i].fieldNumber);
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
        if ($.inArray(lotteryId, viewmodel.ticket.additionalLotteries) > -1) {
            viewmodel.ticket.additionalLotteries.splice($.inArray(lotteryId, viewmodel.ticket.additionalLotteries), 1);
        } else {
            viewmodel.ticket.additionalLotteries.push(lotteryId);
        }
        updatePrice();
    }
};

function updateHeaderText(header, selectedFieldsCnt, additionalNumbersCnt) {
    var text;

    if (additionalNumbersCnt === 1) {
        text = messages['lottery.eurojackpot.tipparea.header.chooseone.additional'];
    }

    if (additionalNumbersCnt === 0) {
        var temp = messages['lottery.eurojackpot.tipparea.header.choose.additional'];
        text = messageFormatter(temp, 2 - additionalNumbersCnt);
    }

    if (selectedFieldsCnt === 5 && additionalNumbersCnt === 2) {
        text = messages['lottery.eurojackpot.tipparea.header.normal'];
    }

    if (selectedFieldsCnt === 4) {
        text = messages['lottery.eurojackpot.tipparea.header.chooseone'];
    }

    if (selectedFieldsCnt < 4) {
        var message = messages['lottery.eurojackpot.tipparea.header.choose'];
        text = messageFormatter(message, 5 - selectedFieldsCnt);
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
    for (var i = 0; i < 5; ++i) {
        var rnd = getRandomInRange(50, 1);
        while ($.inArray(rnd, randomNumbers) > -1) {
            rnd = getRandomInRange(50, 1);
        }
        randomNumbers.push(rnd);
    }
    return randomNumbers;
}

function getRandomAdditionalNumbers() {
    var randomNumbers = [];
    for (var i = 0; i < 2; ++i) {
        var rnd = getRandomInRange(10, 1);
        while ($.inArray(rnd, randomNumbers) > -1) {
            rnd = getRandomInRange(10, 1);
        }
        randomNumbers.push(rnd);
    }
    return randomNumbers;
}

function updatePrice() {
    viewmodel.price.lotto = viewmodel.ticket.fields.filter(function (field) {
            return field.selectedNumbers.length === 5 && field.selectedAdditionalNumbers.length === 2
        }).length * (prices.lotto / 100);

    viewmodel.price.feeFirstDrawing = prices.feeFirstDrawing / 100;

    var totalLotto = viewmodel.price.lotto * viewmodel.ticket.durationOrBillingPeriod;
    var totalFee = viewmodel.price.lotto > 0 ? viewmodel.price.feeFirstDrawing : 0;

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
}

function initNextDrawing() {
    var fr = messages['lottery.eurojackpot.options.nextdrawing.fr'];
    var date = new Date(nextDrawing);
    var frFullText = fr + ' ' + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
    $('.jsNextDrawingText').text(frFullText);
}

function userHasUncompletedFields() {
    return viewmodel.ticket.fields.filter(function (field) {
            return (field.selectedNumbers.length > 0 && field.selectedNumbers.length < 5) || (field.selectedAdditionalNumbers.length > 0 && field.selectedAdditionalNumbers.length < 2);
        }).length > 0;
}

function getAllUncompletedFields() {
    return viewmodel.ticket.fields.filter(function (field) {
        if ((field.selectedNumbers.length > 0 && field.selectedNumbers.length < 5) || (field.selectedAdditionalNumbers.length > 0 && field.selectedAdditionalNumbers.length < 2)) {
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
        showError(messages['lottery.eurojackpot.error.title'], messages['lottery.eurojackpot.error.notipps.why'], messages['lottery.eurojackpot.error.notipps.howtosolve']);
        return;
    }

    if (userHasUncompletedFields()) {
        var uncompletedFields = getAllUncompletedFields();
        var why = messages['lottery.eurojackpot.error.missingtipp.why'];
        var howtosolve = messageFormatter(messages['lottery.eurojackpot.error.missingtipp.howtosolve'], uncompletedFields[0].fieldNumber);
        showError(messages['lottery.eurojackpot.error.title'], why, howtosolve);
        return;
    }

    var ticketSubmitForm = document.getElementById("ticketSubmitForm");
    var ticketJsonField = document.getElementById("ticketSubmitForm:ticketJSON");
    ticketJsonField.value = JSON.stringify(viewmodel.ticket);
    document.getElementById("ticketSubmitForm:submitTicketBtn").click();
}


var init = function init() {
    $('.header').each(function () {
        updateHeaderText($(this), 0, 0);
    });

    for (var i = 0; i < 12; ++i) {
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