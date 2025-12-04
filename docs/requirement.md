# Inventra – Technical and Functional Requirements (v3)

This document defines the technical and functional requirements for **Inventra**,
an offline-first, open-source inventory and sales tracker built with **Kotlin Multiplatform (KMP)**.

---

## ⚙️ Technical Requirements

Inventra is a Kotlin Multiplatform (KMP) app using **Compose Multiplatform** for UI,
with offline-first architecture, **SQLDelight** for local storage, and **Ktor** for networking.
It supports both **admin and seller roles**, configurable APIs, and full **dark mode**.

### Highlights
- Kotlin Multiplatform (KMP) architecture
- Compose Multiplatform UI
- Offline-first with SQLDelight database
- Configurable API (user-defined host & endpoints)
- Dark mode and edge-to-edge layout
- Admin and seller roles with PIN-based access
- Full unit test coverage for shared logic
- MIT open-source license
- For testing, uses MockK, Koin Test, and Turbine libraries

---

## �� Functional Requirements

Inventra manages **Categories, Products, Offers, Sales, Payments, and Events** with an intuitive tile-based UI.  
It allows tracking stock, recording sales, and managing payments including **QR-based transactions**.

### Data Model Overview

Category → Product → Offer → Sale → Payment

Each category can have multiple products, each product can have multiple offers, and each sale can have multiple payments.

### Functional Highlights
- Categories have default tax % and group products.
- Products include SKU, barcode, and belong to one or more categories.
- Offers define price, stock, unit of measurement (UoM), and tax.
- Inventory automatically updates when sales occur.
- Sales can include multiple offers and split payments.
- QR code generation works offline using EPC format.
- Events group related sales for specific sessions (e.g., festivals).

### Checkout Flow
1. Seller selects offer → chooses quantity → adds to cart.
2. Checkout displays subtotal, tax per item, and total with tax.
3. Multiple payment methods can be added (cash, QR, etc.).
4. Upon confirmation, inventory decreases and the sale is logged.  
