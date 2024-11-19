import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class QueriesMain {
    static final Integer CURRENT_YEAR = 2024;

    public static void main(String[] args) {
        // lv1, query 1: Count customers who made a purchase
        System.out.println("lv1, query 1: Count customers who made a purchase");
        long count = DataFactory.produce().stream()
                .map(Purchase::getBuyer)
                .distinct()
                .count();
        System.out.println("Result: " + count);

        // lv1, query 2: Count customers who paid with BLIK
        System.out.println("lv1, query 2: Count customers who paid with BLIK");
        long count1 = DataFactory.produce().stream()
                .filter(purchase -> purchase.getPayment().equals(Purchase.Payment.BLIK))
                .map(Purchase::getBuyer)
                .distinct()
                .count();
        System.out.println("Result: " + count1);

        // lv1, query 3: Count customers who paid with a credit card
        System.out.println("lv1, query 3: Count customers who paid with a credit card");
        long count2 = DataFactory.produce().stream()
                .filter(purchase -> purchase.getPayment().equals(Purchase.Payment.CREDIT_CARD))
                .map(Purchase::getBuyer)
                .distinct()
                .count();
        System.out.println("Result: " + count2);

        // lv1, query 4: Count purchases made in EUR
        System.out.println("lv1, query 4: Count purchases made in EUR");
        long count3 = DataFactory.produce().stream()
                .filter(purchase -> purchase.getProduct().getPrice().getCurrency().equals(Money.Currency.EUR))
                .count();
        System.out.println("Result: " + count3);

        // lv1, query 5: Count unique products purchased in EUR
        System.out.println("lv1, query 5: Count unique products purchased in EUR");
        long count4 = DataFactory.produce().stream()
                .filter(purchase -> purchase.getProduct().getPrice().getCurrency().equals(Money.Currency.EUR))
                .map(Purchase::getProduct)
                .distinct()
                .count();
        System.out.println("Result: " + count4);

        // lv2, query 1: Total PLN spent by each customer
        System.out.println("lv2, query 1: Total PLN spent by each customer");
        Map<Client, BigDecimal> collect1 = DataFactory.produce().stream()
                .filter(purchase -> purchase.getProduct().getPrice().getCurrency().equals(Money.Currency.PLN))
                .collect(Collectors.groupingBy(
                        Purchase::getBuyer,
                        Collectors.reducing(BigDecimal.ZERO,
                                purchase -> purchase.getProduct().getPrice().getValue().multiply(BigDecimal.valueOf(purchase.getQuantity())),
                                BigDecimal::add)
                ));
        collect1.forEach((k, v) -> System.out.println(k.getName() + " " + k.getSurname() + ": " + v + " PLN"));

        // lv2, query 2: Products per client in a specific category
        System.out.println("lv2, query 2: Products per client in category HOBBY");
        Map<String, Long> result = productsPerClientByCategory(Product.Category.HOBBY);
        result.forEach((key, value) -> System.out.println("Client ID: " + key + ", Quantity: " + value));

        // lv2, query 3: Update of order statuses and calculation of processed orders
        System.out.println("lv2, query 3: Update order statuses and count DONE orders");
        long processedOrdersCount = DataFactory.produce().stream()
                .peek(purchase -> purchase.setStatus(OrderService.checkOrderStatus(purchase)))
                .filter(purchase -> purchase.getStatus() == Purchase.Status.DONE)
                .count();
        System.out.println("Result: " + processedOrdersCount);

        // lv2, query 4: Number of unique customers in EUR + map of purchases in EUR
        System.out.println("lv2, query 4: Unique EUR clients and purchases in EUR");
        Map<String, List<Purchase>> eurPurchasesPerClient = DataFactory.produce().stream()
                .filter(purchase -> purchase.getProduct().getPrice().getCurrency() == Money.Currency.EUR)
                .collect(Collectors.groupingBy(purchase -> purchase.getBuyer().getId()));
        System.out.println("Total unique clients with EUR purchases: " + eurPurchasesPerClient.size());
        eurPurchasesPerClient.forEach((key, value) -> {
            System.out.println("Client ID: " + key);
            value.forEach(purchase -> {
                System.out.println("  Product: " + purchase.getProduct().getName() +
                        ", Quantity: " + purchase.getQuantity() +
                        ", Price: " + purchase.getProduct().getPrice().getValue() + " EUR");
            });
        });

        // lv2, query 5: Map of year -> list of customer products
        System.out.println("lv2, query 5: Products per year");
        Map<Integer, List<Product>> productsPerYear = DataFactory.produce().stream()
                .collect(Collectors.groupingBy(
                        purchase -> Integer.parseInt(purchase.getBuyer().getPesel().toString().substring(0, 2)),
                        Collectors.mapping(Purchase::getProduct, Collectors.toList())
                ));
        productsPerYear.forEach((key, value) -> {
            System.out.println("Year: " + key);
            value.forEach(product -> System.out.println("  Product ID: " + product.getId() + ", Name: " + product.getName()));
        });

        // lv2, query 6: Map of year -> unique product categories
        System.out.println("lv2, query 6: Unique product categories per year");
        Map<Integer, Set<Product.Category>> categoriesPerYear = DataFactory.produce().stream()
                .collect(Collectors.groupingBy(
                        purchase -> Integer.parseInt(purchase.getBuyer().getPesel().toString().substring(0, 2)),
                        Collectors.mapping(purchase -> purchase.getProduct().getCategory(), Collectors.toSet())
                ));
        categoriesPerYear.forEach((key, value) -> {
            System.out.println("Year: " + key);
            value.forEach(category -> System.out.println("  Category: " + category));
        });

        // lv2, query 7: Second most frequently purchased product
        System.out.println("lv2, query 7: Second most frequently purchased product");
        Product secondMostBought = DataFactory.produce().stream()
                .collect(Collectors.groupingBy(Purchase::getProduct, Collectors.summingLong(Purchase::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(entry -> entry.getKey().getId()))
                .skip(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (secondMostBought != null) {
            System.out.println("Result: " + secondMostBought.getName());
        }

        // lv3, query 1: Least popular category for 50+ age group
        System.out.println("lv3, query 1: Least popular category for 50+ age group");

// Step 1: Group by year and category with counts
        Map<String, Map<Product.Category, Long>> yearWithCategoriesWithoutZeros = DataFactory.produce().stream()
                .filter(purchase -> {
                    int birthYearPrefix = Integer.parseInt(purchase.getBuyer().getPesel().toString().substring(0, 2));
                    return CURRENT_YEAR - (1900 + birthYearPrefix) > 50;
                })
                .collect(Collectors.groupingBy(
                        purchase -> purchase.getBuyer().getPesel().toString().substring(0, 2),
                        Collectors.groupingBy(
                                purchase -> purchase.getProduct().getCategory(),
                                Collectors.counting()
                        )
                ));

// Step 2: Fill missing categories with zeros
        Map<String, Map<Product.Category, Long>> yearWithCategoriesWithZeros = yearWithCategoriesWithoutZeros.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> Arrays.stream(Product.Category.values())
                                .collect(Collectors.toMap(
                                        category -> category,
                                        category -> entry.getValue().getOrDefault(category, 0L),
                                        (v1, v2) -> v1,
                                        TreeMap::new
                                )),
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

// Step 3: Find the least popular categories for each year
        Map<String, List<Map.Entry<Product.Category, Long>>> yearWithMinimumCategories = new TreeMap<>();
        for (Map.Entry<String, Map<Product.Category, Long>> stringMapEntry : yearWithCategoriesWithZeros.entrySet()) {
            long minCount = stringMapEntry.getValue().values().stream().min(Long::compare).orElse(0L);

            yearWithMinimumCategories.putIfAbsent(stringMapEntry.getKey(), stringMapEntry.getValue().entrySet().stream()
                    .filter(e -> e.getValue() == minCount)
                    .collect(Collectors.toList()));
        }

// Print the result
        yearWithMinimumCategories.forEach((year, categories) -> {
            System.out.print("Year: " + year + " ");
            categories.forEach(categoryEntry ->
                    System.out.print(categoryEntry.getKey() + " - " + categoryEntry.getValue() + " ")
            );
            System.out.println();
        });


        // lv3, query 2: Age group that bought the most products
        System.out.println("lv3, query 2: Age group that bought the most products");
        Map.Entry<Integer, Long> yearWithMostProducts = DataFactory.produce().stream()
                .collect(Collectors.groupingBy(
                        purchase -> Integer.parseInt(purchase.getBuyer().getPesel().toString().substring(0, 2)),
                        Collectors.summingLong(Purchase::getQuantity)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry(-1, 0L));


        System.out.println("Result: Year " + yearWithMostProducts.getKey() + ", Quantity: " + yearWithMostProducts.getValue());
    }

    public static Map<String, Long> productsPerClientByCategory(Product.Category category) {
        return DataFactory.produce().stream()
                .filter(purchase -> purchase.getProduct().getCategory() == category && purchase.getQuantity() > 1)
                .collect(Collectors.groupingBy(
                        purchase -> purchase.getBuyer().getId(),
                        Collectors.summingLong(Purchase::getQuantity)
                ));
    }
}
