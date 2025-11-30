package com.ifsp.Leel.Service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private static final Map<String, Map<String, Integer>> DB = new ConcurrentHashMap<>();

    public Map<String, Integer> getItems(String cartId) {
        return DB.getOrDefault(cartId, new HashMap<>());
    }

    public void addItem(String cartId, String sku, int quantity) {
        DB.computeIfAbsent(cartId, k -> new ConcurrentHashMap<>())
                .merge(sku, quantity, Integer::sum);
    }

    public void removeItem(String cartId, String sku) {
        Map<String, Integer> items = DB.get(cartId);
        if (items != null && items.containsKey(sku)) {
            int qty = items.get(sku) - 1;
            if (qty <= 0)
                items.remove(sku);
            else
                items.put(sku, qty);
        }
    }

    public void clearCart(String cartId) {
        if (cartId != null) {
            DB.remove(cartId);
        }
    }

    public boolean hasItems(String cartId) {
        return DB.containsKey(cartId) && !DB.get(cartId).isEmpty();
    }
}