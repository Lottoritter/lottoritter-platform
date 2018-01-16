/*
 * Copyright 2017 Ulrich Cech & Christopher Schmidt
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
package de.lottoritter.business.shoppingcart.control;

import com.mongodb.ReadPreference;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import de.lottoritter.business.shoppingcart.entity.ShoppingCartState;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;

import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Stateless
@DependsOn("DBConnection")
public class ShoppingCartRepository {

    @Inject
    Datastore datastore;

    public ShoppingCartRepository() {
    }


    public ShoppingCart getShoppingCartForPlayer(final Player player) {
        ShoppingCart currentOpenShoppingCartForPlayer = null;
        if (player != null) {
            currentOpenShoppingCartForPlayer =
                    datastore.createQuery(ShoppingCart.class).field("playerId").equal(player.getId())
                            .field("state").equal(ShoppingCartState.OPEN).get();
        }
        if ((currentOpenShoppingCartForPlayer == null) && (player != null)) {
            currentOpenShoppingCartForPlayer = new ShoppingCart(player);
            datastore.save(currentOpenShoppingCartForPlayer); // initial persist
        }
        return currentOpenShoppingCartForPlayer;
    }

    public List<ShoppingCart> getAllShoppingsCartWithTickets() {
        Query<ShoppingCart> query = datastore.find(ShoppingCart.class);
        query.disableValidation();
        query.field("state").equal(ShoppingCartState.OPEN);
        query.where("this.ticketList.length > 0");
        FindOptions findOptions = new FindOptions().readPreference(ReadPreference.secondary());
        return query.asList(findOptions);
    }
}
