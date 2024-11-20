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

### 6. `QueriesMain`
Contains multiple queries to analyze purchase data, such as counting customers, grouping purchases, and calculating statistics.

---
