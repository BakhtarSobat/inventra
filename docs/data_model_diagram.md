# �� Data Model Diagram

This document visualizes the main data relationships for the **Inventra** app.

---

## �� Entity Relationship Diagram

CATEGORY ||--o{ CATEGORY_PRODUCT : contains
CATEGORY {
    string categoryId PK
    string title
    string description
    string image
    double taxPercentage
}

PRODUCT ||--o{ CATEGORY_PRODUCT : belongs_to
PRODUCT ||--o{ OFFER : has
PRODUCT {
    string id PK
    string title
    string description
    string image
    string skuCode
    string barcode
}

OFFER ||--o{ INVENTORYTRANSACTION : tracked_by
OFFER ||--o{ SALEITEM : sold_in
OFFER {
    string offerId PK
    string title
    string image
    int amountInInventory
    string type
    double price
    string uom
    double taxPercentage
}

INVENTORYTRANSACTION {
    string transactionId PK
    string offerId FK
    int changeAmount
    string reason
    datetime timestamp
    string eventId FK
}

SALE ||--o{ SALEITEM : includes
SALE ||--o{ PAYMENTPART : paid_by
SALE {
    string saleId PK
    datetime timestamp
    string eventId FK
}

SALEITEM {
    string offerId FK
    int quantity
    double unitPrice
    double totalPrice
    double taxPercentage
    bool inventoryAdjusted
}

PAYMENTPART {
    string method
    double amount
    string qrCodeData
    string status
    string note
}

EVENT ||--o{ SALE : contains
EVENT ||--o{ INVENTORYTRANSACTION : groups
EVENT {
    string eventId PK
    string title
    string description
    datetime startDate
    datetime endDate
}

CATEGORY_PRODUCT {
    string categoryId FK
    string productId FK
}


---

## �� Entity Descriptions

| Entity | Purpose |
|--------|----------|
| **Category** | Groups products (e.g., "Sweet", "Savory"). Holds default tax %. |
| **Product** | Conceptual item (e.g., "Candy", "Espresso") linked to one or more categories. |
| **Offer** | Concrete selling option (e.g., "Candy per box", "Espresso double shot"). Tracks price, stock, and UoM. |
| **InventoryTransaction** | History of stock changes per offer (sales, restock, sync). |
| **Sale** | Records a transaction including items and payments. |
| **SaleItem** | Represents one sold offer in a sale. |
| **PaymentPart** | Records each portion of payment (cash, card, QR, etc.). |
| **Event** | Session context for sales and inventory (e.g., "Festival Utrecht 2025"). |
| **CategoryProduct** | Join table for many-to-many Category ↔ Product relation. |

---

**End of `/docs/data_model_diagram.md`**