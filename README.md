# Shopify Assistant (Spring Boot 3, Java 21)

## Requirements
- JDK 21
- Docker with Docker Compose

## Quick start
1. Copy `.env.example` to `.env` and adjust values if needed.
2. Run the stack:
   ```bash
   docker compose up --build
   ```
   App will start on port 8080 and Liquibase will run migrations.
3. Import CSV (dry run):
   ```bash
   curl -F "file=@products.csv" -F "dryRun=true" http://localhost:8080/admin/import/csv
   ```
4. Import CSV (commit):
   ```bash
   curl -u admin:admin123 -F "file=@products.csv" -F "baseUrl=https://twoj-sklep.myshopify.com" http://localhost:8080/admin/import/csv
   ```
5. Search:
   ```bash
   curl "http://localhost:8080/search?q=dyfuzor%20M3%20G80%20karbon%20po%C5%82ysk&limit=5"
   ```
6. Chat:
   ```bash
   curl -X POST "http://localhost:8080/chat" -H "Content-Type: application/json" -d '{"message":"dyfuzor M3 G80 karbon gloss do 2500","limit":3}'
   ```

## Security
- `/admin/**` endpoints require HTTP Basic auth with credentials from `ADMIN_USER` / `ADMIN_PASS`.
- Public endpoints: `/search`, `/chat`, `/actuator/health`.

## ENV
Environment variables (see `.env.example`):
```
DB_URL=jdbc:postgresql://db:5432/shopify
DB_USER=shopify
DB_PASS=shopify
BASE_URL=https://twoj-sklep.myshopify.com
SHOPIFY_SHOP=yourshop
SHOPIFY_ADMIN_TOKEN=changeme
SHOPIFY_WEBHOOK_SECRET=changeme
SHOPIFY_API_VERSION=2024-07
ADMIN_USER=admin
ADMIN_PASS=admin123
CORS_ORIGINS=https://twoja-domena.pl,https://localhost:3000
```

## Development
Run locally:
```bash
./gradlew bootRun
```
Example Postgres extension setup:
```sql
psql -U shopify -d shopify -c 'create extension if not exists "uuid-ossp";'
```
