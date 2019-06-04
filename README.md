# Lottoritter
Lottoritter is a gambling platform which supports multiple lotteries. On Lottoritter you can play real lotteries WITHOUT
SPENDING ANY money. This project is written in Java and is using the JavaEE 8 standard technology stack (EE4J).

The running project can be found here: http://lottoritter.ulrichcech.de

The tech stack further includes [MongoDB](https://www.mongodb.com) as backend storage with
[MongoDB-Morphia](https://mongodb.github.io/morphia/) as Java Object Document Mapper. The frontend is based on JSF and
JavaScript. The project is completely independent from any application server.

## Contents

1. [Background](#background)
2. [Features](#features)
3. [License](#license)

## Background

Lottoritter was created by [Ulrich Cech](https://github.com/UlrichCech) &  [Christopher Schmidt](https://github.com/crzo). The main intention was to show that you can create
something really big with just a few tools and frameworks. So we decided to implement a fully featured gambling
platform. See some features below.

## Features

- supporting "6 aus 49", "Keno", "Glücksspirale", "EuroJackpot" lotteries
- real purchase feeling (shoppingcart) including e-Mail notifications
- registration/activation/forget password/login (OAuth and Remember-me)
- profile for each player where you can see your active games etc.
- sum of money you spent
- show the hit numbers in the ticket-history
- automatic drawing crawler
- multi language support (i18n)
- social authentication (G+, Facebook, Instagram)

## License

This project is licensed under the GPLv3 License - see the [LICENSE](https://github.com/Lottoritter/lottoritter-platform/blob/master/LICENSE) file
for details.
