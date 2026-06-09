package com.orderup;

import com.orderup.entity.MenuItem;
import com.orderup.repository.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;

    public DataSeeder(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if table is empty
        if (menuItemRepository.count() > 0) return;

        List<MenuItem> items = List.of(
            item("Espresso",          "Drinks",   "5.50",  "Rich and bold single shot"),
            item("Latte",             "Drinks",   "7.00",  "Espresso with steamed milk"),
            item("Cappuccino",        "Drinks",   "7.00",  "Equal parts espresso, milk, foam"),
            item("Iced Americano",    "Drinks",   "6.50",  "Espresso over ice with water"),
            item("Matcha Latte",      "Drinks",   "8.00",  "Ceremonial grade matcha with milk"),
            item("Croissant",         "Food",     "5.00",  "Buttery, flaky pastry"),
            item("Chicken Sandwich",  "Food",     "12.00", "Grilled chicken on sourdough"),
            item("Avocado Toast",     "Food",     "11.00", "Sourdough, smashed avo, chili flakes"),
            item("Caesar Salad",      "Food",     "13.00", "Romaine, croutons, parmesan"),
            item("Cheesecake",        "Desserts", "9.00",  "New York style, berry compote"),
            item("Tiramisu",          "Desserts", "9.50",  "Classic Italian, cocoa dusted"),
            item("Chocolate Brownie", "Desserts", "6.00",  "Warm, served with vanilla ice cream")
        );

        menuItemRepository.saveAll(items);
        System.out.println("✅ Menu seeded with " + items.size() + " items.");
    }

    private MenuItem item(String name, String category, String price, String desc) {
        MenuItem m = new MenuItem();
        m.setName(name);
        m.setCategory(category);
        m.setPrice(new BigDecimal(price));
        m.setDescription(desc);
        m.setAvailable(true);
        return m;
    }
}
