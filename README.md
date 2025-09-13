# Shopify Assistant (Spring Boot 3, Java 21)

Funkcje:
- Import CSV -> Postgres (`/admin/import/csv`)
- Wyszukiwanie GET `/search` z filtrami (make/model/chassis/body/lci/material/finish/category/vendor/price/tags)
- Indeks pełnotekstowy (tsvector), Liquibase

## Szybki start
1. Postgres:
```sql
create database shopify;
create user shopify with encrypted password 'shopify';
grant all privileges on database shopify to shopify;
```
2. ENV:
```
DB_URL=jdbc:postgresql://localhost:5432/shopify
DB_USER=shopify
DB_PASS=shopify
BASE_URL=https://twoj-sklep.myshopify.com
```
3. Run:
```
./gradlew bootRun
```
4. Import CSV (dry run):
```
curl -F file=@products.csv -F dryRun=true http://localhost:8080/admin/import/csv
```
5. Import CSV (commit):
```
curl -F file=@products.csv -F baseUrl=https://twoj-sklep.myshopify.com http://localhost:8080/admin/import/csv
```
6. Szukaj:
```
GET http://localhost:8080/search?q=dyfuzor%20M3%20G80%20karbon%20po%C5%82ysk&limit=5
GET http://localhost:8080/search?make=BMW&model=M3&chassis=G80&lci=true&material=carbon&finish=gloss&category=diffuser&priceMax=2500
```
# AiAssist
