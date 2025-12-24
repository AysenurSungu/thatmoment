# That Moment – Backend Project Guide

Bu doküman, **That Moment backend projesi** için:
- mimari kararları
- kodlama standartlarını
- API prensiplerini
- AI (Codex) kullanım kurallarını

**tek bir referans kaynağı** olarak tanımlar.

Bu repo içinde yapılan **tüm geliştirmeler (insan veya AI)** bu dokümana uymak zorundadır.

---

## 1. Proje Özeti

That Moment backend:
- Java 21 + Spring Boot ile geliştirilir
- Modüler Monolith mimariyi hedefler
- Klasik monolith’in spagetti riskini azaltır
- Erken microservice karmaşasından bilinçli olarak kaçınır
- İleride bölünebilir olacak şekilde tasarlanır

---

## 2. Teknoloji Yığını

- Java: 21
- Framework: Spring Boot
- Build Tool: Maven
- Database: PostgreSQL
- Migration: Flyway (zorunlu)
- Mapping: MapStruct (varsayılan)
- Lombok: sınırlı kullanım

---

## 3. Mimari: Modüler Monolith

### Amaç
- Net sınırlar
- Okunabilirlik
- Uzun vadeli sürdürülebilirlik
- Test edilebilirlik

### Temel Kurallar
- Her iş alanı ayrı bir modül olmalıdır
- Modüller arası entity paylaşımı yasaktır
- Domain logic dışarı sızmaz
- Modüller arası iletişim:
    - çoğunlukla doğrudan çağrı
    - sadece kritik yerlerde event

### Örnek Paket Yapısı

```text
com.thatmoment
├── common
│   ├── exception
│   ├── response
│   ├── event
│   └── config
├── modules
│   ├── auth
│   │   ├── api
│   │   ├── app
│   │   ├── domain
│   │   └── infra
│   ├── billing
│   ├── media
│   └── routine
```

## 4. Lombok Kullanım Kuralları

### ❌ Yasak Olan Yerler
- **Domain katmanı**
- **Auth domain**
- **Billing domain**
- `@Data` anotasyonu (her yerde)

### ✅ İzin Verilen Yerler
- DTO’lar:
  - Tercih: **Java `record`**
  - Alternatif: `@Getter` + `@Builder`
- Test kodları (okunabilirlik için)

### Amaç
- Domain kodunun:
  - açık
  - immutable
  - sihirden uzak
  - okunabilir olması

---

## 5. BaseEntity Prensipleri

### Genel İlke
- BaseEntity **minimal tutulur**
- Her ihtiyacın base’e eklenmesi **yasaktır**

### İçerebilecek Alanlar
- `id`
- `createdAt`
- `updatedAt`
- Opsiyonel: `@Version` (optimistic locking)

### İçermemelidir
- Business logic
- Status / lifecycle alanları
- User / tenant / audit detayları  
  (gerekiyorsa ayrı yapı kurulur)

---

## 6. Mapping Stratejisi

### Varsayılan Mapping
- **MapStruct**
- `componentModel = "spring"`

### İstisnalar
- **Auth & Billing domain**
  - Explicit mapping
  - Immutable nesneler
  - Builder pattern
  - MapStruct **kullanılmaz**

### Gerekçe
Auth ve Billing:
- kritik
- hataya kapalı
- açık kontrol gerektiren alanlardır

---

## 7. Flyway (Zorunlu)

- Flyway **kesinlikle kullanılır**
- Hibernate auto-ddl **yasaktır**
- Tüm schema değişiklikleri migration ile yapılır

### Migration Yapısı

```text
src/main/resources/db/migration
V1__init.sql
V2__add_user_table.sql
```


### Kurallar
- Geriye dönük migration yazılmaz
- Rename yerine yeni migration
- Modül bazlı tablolar net ayrılır

---

## 8. Event Kullanımı

### Prensip
- **Az ve öz**
- Sadece gerçekten sınır aşan durumlarda

### Event Kullanılabilecek Alanlar
- Media işlemleri
- Billing / ödeme
- Notification tetikleme

### Kurallar
- Event consumer’lar **idempotent** olmalıdır
- Şimdilik: Spring in-memory event
- Büyüdüğünde: **Outbox pattern + broker**

---

## 9. API Response Standartları

### Error Response (Zorunlu)
- **RFC 7807 – Problem Details**

Örnek:
```json
{
  "type": "validation-error",
  "title": "Invalid request",
  "status": 400,
  "detail": "email must not be blank"
}
```
### Success Response

- Success response’lar case-by-case değerlendirilir; standartlaştırma amaç değil, araçtır.
- Varsayılan: **yalın response**
- Generic envelope (`ApiResponse<T>`)
    - sadece gerçekten değer katıyorsa kullanılır

❌ Gereksiz response wrapper **yasaktır**

---

## 10. Exception Handling

- Tüm exception’lar `@ControllerAdvice` ile yönetilir
- Business exception ≠ technical exception
- Stacktrace client’a **asla dönmez**

---

## 11. Test Prensipleri

- Unit test önceliklidir
- Domain ve Service katmanları test edilir
- Auth ve Billing için test coverage **kritiktir**

### Testler:

- kısa
- net
- tek sorumluluklu

---

## 12. AI / Codex Kullanımı

AI tarafından üretilen **her kod** bu dokümana uymak zorundadır.

### Codex kullanırken:

- Bu doküman mutlaka referans verilir
- Küçük ve review edilebilir diff’ler istenir
- Domain ve security kodları **explicit** tutulur

### Örnek ifade:

> “Bu değişiklikleri STANDARDS.md kurallarına %100 uyarak yap.”

---

## 13. Değiştirilemez Kurallar

- Flyway kaldırılamaz
- Domain katmanına Lombok eklenemez
- Auth ve Billing domain’de MapStruct kullanılamaz
- Error response Problem Details dışına çıkamaz

---

## 14. Local Çalıştırma

### Gereksinimler

- Java 21
- Maven
- PostgreSQL

### Çalıştırma

```bash
./mvnw spring-boot:run
```

---

## 15. Genel Yaklaşım
Bu proje:
- architecture-first,
- explicit code,
- minimal magic,
- uzun vadeli bakım
- öncelikleriyle geliştirilir.

