# OrderUp - Small Cafe POS

OrderUp is a simple full-stack POS system for a small cafe. It handles real cafe workflows: taking orders, editing unpaid orders, sending orders to the kitchen, updating kitchen status, taking payments, splitting bills, and showing practical AI add-on suggestions.

## Tech Stack

- Angular 17
- Spring Boot 3.2
- Java 17+
- MySQL 8
- Optional OpenAI API key for nicer AI suggestion wording

## What It Can Do

- Take dine-in or takeaway orders
- Add notes for the kitchen
- Edit unpaid orders
- Track order status: `PENDING -> PREPARING -> READY -> COMPLETED`
- Accept cash, card, or e-wallet payments
- Split bills 2 or 3 ways
- Auto-seed cafe menu items
- Suggest cart-based add-ons using real menu data

## Quick Start

You need:

- Java 17 or newer
- Maven
- Node.js + npm
- MySQL running locally

Docker is not required for this project. The app runs directly using local MySQL.

## 1. Prepare MySQL

Open MySQL and create the database:

```sql
CREATE DATABASE IF NOT EXISTS orderup_db;
```

Default app settings:

```text
database: orderup_db
username: root
password: root
```

If your MySQL password is not `root`, set it before starting Spring Boot:

```powershell
$env:DB_PASSWORD="your_mysql_password"
```

If your MySQL username is not `root`, set both:

```powershell
$env:DB_USERNAME="your_mysql_username"
$env:DB_PASSWORD="your_mysql_password"
```

## 2. Run Backend

From the project root:

```powershell
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

The menu is seeded automatically the first time the app runs.

## 3. Run Frontend

Open a second terminal:

```powershell
cd frontend
npm install
npm start
```

Frontend URL:

```text
http://localhost:4200
```

## Common Problems

### `Public Key Retrieval is not allowed`

This project already includes `allowPublicKeyRetrieval=true` in the MySQL connection URL. If you still see this, restart the backend.

### `Access denied for user root`

Your MySQL password is different. Run:

```powershell
$env:DB_PASSWORD="your_mysql_password"
mvn spring-boot:run
```

### `npm cannot find package.json`

You are probably in the project root. Go into the frontend folder first:

```powershell
cd frontend
npm install
npm start
```

## AI Feature

The AI add-on suggestion works even without an API key. It uses local rules to look at the current cart and suggest a useful missing item from the menu.

If you want OpenAI to rewrite the suggestion in a nicer sentence, set:

```powershell
$env:OPENAI_API_KEY="your_openai_key"
```

Then restart Spring Boot.

## API Summary

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/menu` | List menu items |
| `POST` | `/api/orders` | Create an order |
| `GET` | `/api/orders` | List orders |
| `GET` | `/api/orders/{id}` | Get one order |
| `PUT` | `/api/orders/{id}` | Edit unpaid order |
| `PUT` | `/api/orders/{id}/status` | Update kitchen status |
| `POST` | `/api/orders/{id}/payments` | Pay or split bill |
| `POST` | `/api/ai/recommend` | Get add-on suggestion |

## Example Payment Split

```json
{
  "paymentMethod": "CARD",
  "splits": [
    { "payerName": "Guest 1", "amount": 12.50 },
    { "payerName": "Guest 2", "amount": 12.50 }
  ]
}
```

## Project Structure

```text
src/main/java/com/orderup
  controller/    API endpoints
  dto/           Request and response objects
  entity/        Menu, order, item, and payment tables
  repository/    Database access
  service/       Business logic

frontend/src/app
  api.service.ts       Calls the backend API
  app.component.*      Main POS screen
  models.ts            Frontend data types

docs/
  pitch.pdf
  screenshots.pdf
```

## Build Checks

Backend:

```powershell
mvn test
```

Frontend:

```powershell
cd frontend
npm install
npm run build
```

## Pitch

OrderUp is intentionally small and easy to explain: one Spring Boot backend, one Angular POS screen, MySQL storage, and one useful AI feature. It focuses on real cafe operations instead of fake dashboard mockups.
