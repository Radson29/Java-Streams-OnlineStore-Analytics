# Java-Streams-OnlineStore-Analytics

## Overview
This project demonstrates how to analyze purchase data in an online store using Java Streams. The system processes data about clients, products, and purchases, extracting insights such as popular product categories, customer spending, and order statistics. It showcases advanced usage of the Streams API for functional programming and efficient data manipulation.

---

## Classes

### 1. `Client`
Represents a customer in the system.
- **Attributes:**
  - `id`: Unique identifier of the customer.
  - `name`: First name of the customer.
  - `surname`: Last name of the customer.
  - `pesel`: Polish national ID, used to calculate age or year of birth.
  - `city`: City where the customer resides.

### 2. `Money`
Represents monetary values with currency.
- **Attributes:**
  - `value`: The amount in `BigDecimal` format.
  - `currency`: Enum representing the currency (`PLN`, `EUR`).

### 3. `Product`
Represents a product available for purchase.
- **Attributes:**
  - `id`: Unique identifier of the product.
  - `name`: Name of the product.
  - `category`: Enum representing the category (`HOBBY`, `CLOTHES`, `GARDEN`, `AUTOMOTIVE`).
  - `price`: Price of the product (represented by `Money`).

### 4. `Purchase`
Represents a transaction made by a customer.
- **Attributes:**
  - `buyer`: The customer who made the purchase.
  - `product`: The product purchased.
  - `quantity`: Number of units purchased.
  - `delivery`: Enum representing the delivery method (`IN_POST`, `UPS`, `DHL`).
  - `payment`: Enum representing the payment method (`CASH`, `BLIK`, `CREDIT_CARD`).
  - `status`: Enum representing the status of the order (`PAID`, `SENT`, `DONE`).

### 5. `OrderService`
Provides utility methods for updating the status of purchases based on predefined rules.

### 6. `DataFactory`
Class is responsible for generating and providing sample data for clients, products, and purchases. It simulates the creation of a set of predefined transactions to be used in queries for analysis. The `produce()` method in this class creates a list of `Purchase` objects, each of which contains a client, a product, the quantity of items purchased, the delivery method, the payment method, and the purchase date.

#### **Attributes:**
- **Clients:** A list of sample clients, each with attributes such as `id`, `name`, `surname`, `pesel`, and `city`.
- **Products:** A list of sample products belonging to different categories such as `HOBBY`, `CLOTHES`, `GARDEN`, and `AUTOMOTIVE`. Each product has an `id`, `name`, `category`, and `price` with a specified currency (either `PLN` or `EUR`).
- **Purchases:** A collection of `Purchase` objects that represent individual transactions. These include the client who made the purchase, the product, the quantity, the payment method (e.g., `BLIK`, `CREDIT_CARD`, `CASH`), and the delivery method (e.g., `UPS`, `DHL`).


### 7. `QueriesMain`
Contains multiple queries to analyze purchase data, such as counting customers, grouping purchases, and calculating statistics.

# Queries in `QueriesMain`

## lv1, query 1: Count customers who made a purchase
```java
long count = DataFactory.produce().stream()
    .map(Purchase::getBuyer)
    .distinct()
    .count();
System.out.println("Result: " + count);
```
## lv1, query 2: Count customers who paid with BLIK
```java
long count1 = DataFactory.produce().stream()
    .filter(purchase -> purchase.getPayment().equals(Purchase.Payment.BLIK))
    .map(Purchase::getBuyer)
    .distinct()
    .count();
System.out.println("Result: " + count1);
```
## lv1, query 3: Count customers who paid with a credit card
```java
long count2 = DataFactory.produce().stream()
    .filter(purchase -> purchase.getPayment().equals(Purchase.Payment.CREDIT_CARD))
    .map(Purchase::getBuyer)
    .distinct()
    .count();
System.out.println("Result: " + count2);
```
## lv1, query 4: Count purchases made in EUR
```java
long count3 = DataFactory.produce().stream()
    .filter(purchase -> purchase.getProduct().getPrice().getCurrency().equals(Money.Currency.EUR))
    .count();
System.out.println("Result: " + count3);
```
## lv1, query 5: Count unique products purchased in EUR
```java
long count4 = DataFactory.produce().stream()
    .filter(purchase -> purchase.getProduct().getPrice().getCurrency().equals(Money.Currency.EUR))
    .map(Purchase::getProduct)
    .distinct()
    .count();
System.out.println("Result: " + count4);
```
## lv2, query 1: Total PLN spent by each customer
```java
Map<Client, BigDecimal> collect1 = DataFactory.produce().stream()
    .filter(purchase -> purchase.getProduct().getPrice().getCurrency().equals(Money.Currency.PLN))
    .collect(Collectors.groupingBy(
        Purchase::getBuyer,
        Collectors.reducing(BigDecimal.ZERO,
            purchase -> purchase.getProduct().getPrice().getValue().multiply(BigDecimal.valueOf(purchase.getQuantity())),
            BigDecimal::add)
    ));
collect1.forEach((k, v) -> System.out.println(k.getName() + " " + k.getSurname() + ": " + v + " PLN"));
```
## lv2, query 2: Products per client in a specific category(for example HOBBY)
```java
Map<String, Long> result = productsPerClientByCategory(Product.Category.HOBBY);
result.forEach((key, value) -> System.out.println("Client ID: " + key + ", Quantity: " + value));
```
## lv2, query 3: Update of order statuses and count of DONE orders
```java
long processedOrdersCount = DataFactory.produce().stream()
    .peek(purchase -> purchase.setStatus(OrderService.checkOrderStatus(purchase)))
    .filter(purchase -> purchase.getStatus() == Purchase.Status.DONE)
    .count();
System.out.println("Result: " + processedOrdersCount);
```
## lv2, query 4: Unique EUR clients and purchases in EUR
```java
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
```
## lv2, query 5: Products per year
```java
Map<Integer, List<Product>> productsPerYear = DataFactory.produce().stream()
    .collect(Collectors.groupingBy(
        purchase -> Integer.parseInt(purchase.getBuyer().getPesel().toString().substring(0, 2)),
        Collectors.mapping(Purchase::getProduct, Collectors.toList())
    ));
productsPerYear.forEach((key, value) -> {
    System.out.println("Year: " + key);
    value.forEach(product -> System.out.println("  Product ID: " + product.getId() + ", Name: " + product.getName()));
});
```
## lv3, query 1: Least popular category for the 50+ age group
```java
Map<String, List<Map.Entry<Product.Category, Long>>> yearWithMinimumCategories = yearWithCategoriesWithZeros.entrySet().stream()
    .collect(Collectors.toMap(
        entry -> entry.getKey(),
        entry -> {
            long minCount = entry.getValue().values().stream().min(Long::compare).orElse(0L);
            return entry.getValue().entrySet().stream()
                .filter(e -> e.getValue() == minCount)
                .collect(Collectors.toList());
        }
    ));
yearWithMinimumCategories.forEach((year, categories) -> {
    System.out.print("Year: " + year + " ");
    categories.forEach(categoryEntry ->
        System.out.print(categoryEntry.getKey() + " - " + categoryEntry.getValue() + " ")
    );
    System.out.println();
});
```
## lv3, query 2: Age group that bought the most products
```java
Map.Entry<Integer, Long> yearWithMostProducts = DataFactory.produce().stream()
    .collect(Collectors.groupingBy(
        purchase -> Integer.parseInt(purchase.getBuyer().getPesel().toString().substring(0, 2)),
        Collectors.summingLong(Purchase::getQuantity)
    ))
    .entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .orElse(Map.entry(-1, 0L));
System.out.println("Result: Year " + yearWithMostProducts.getKey() + ", Quantity: " + yearWithMostProducts.getValue());
```

---
