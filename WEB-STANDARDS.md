# Frontend Project Standards

> **Versiyon:** 2.0  
> **Son Güncelleme:** 11.01.2025  
> **Durum:** Aktif / Zorunlu

Bu doküman, frontend projeleri için mimari kararları, kodlama standartlarını ve operasyonel kuralları **Tek Doğruluk Kaynağı (SSOT)** olarak tanımlar.

Bu dokümana tabi projelerde yapılan **tüm geliştirmeler (insan veya AI)** bu kurallara uymak zorundadır.

---

## İçindekiler

1. [Teknoloji Yığını](#1-teknoloji-yığını)
2. [Mimari: Proje Yapısı](#2-mimari-proje-yapısı)
3. [Naming Conventions](#3-naming-conventions)
4. [TypeScript Kuralları](#4-typescript-kuralları)
5. [Component Standartları](#5-component-standartları)
6. [State Management](#6-state-management)
7. [API Layer](#7-api-layer)
8. [Authentication Stratejisi](#8-authentication-stratejisi)
9. [HTTP Error Handling](#9-http-error-handling)
10. [Form Handling](#10-form-handling)
11. [Error Boundaries & User Feedback](#11-error-boundaries--user-feedback)
12. [Styling Kuralları](#12-styling-kuralları)
13. [Performance](#13-performance)
14. [Accessibility (a11y)](#14-accessibility-a11y)
15. [Testing](#15-testing)
16. [Security](#16-security)
17. [Linting & Formatting](#17-linting--formatting)
18. [Anti-Pattern Referans Tabloları](#18-anti-pattern-referans-tabloları)
19. [AI / Codex / Cursor Kullanımı](#19-ai--codex--cursor-kullanımı)
20. [Değiştirilemez Kurallar](#20-değiştirilemez-kurallar)
21. [Environment Setup](#21-environment-setup)

---

## 1. Teknoloji Yığını

| Teknoloji | Tercih |
| --- | --- |
| Framework | Next.js 14+ (App Router) |
| Language | TypeScript (strict mode) |
| Package Manager | pnpm |
| State (Server) | TanStack Query v5 |
| State (Client) | Zustand |
| Forms | React Hook Form + Zod |
| Styling | Tailwind CSS |
| Components | shadcn/ui |
| Icons | Lucide React |
| HTTP Client | Axios |
| Testing | Vitest + React Testing Library |
| E2E | Playwright |
| Linting | ESLint + Prettier |

### Dependency Versioning

- Major version upgrade'leri PR'da gerekçelendirilir.
- shadcn/ui component'leri proje içine kopyalanır (`npx shadcn-ui add`).
- Lock file (`pnpm-lock.yaml`) her zaman commit edilir.

---

## 2. Mimari: Proje Yapısı

### Amaç

- Net modül sınırları
- Okunabilirlik
- Uzun vadeli sürdürülebilirlik
- Test edilebilirlik

### Proje Yapısı

```
src/
├── app/                      # Next.js App Router
│   ├── (auth)/               # Auth route group (public)
│   │   ├── login/
│   │   └── register/
│   ├── (dashboard)/          # Protected route group
│   │   ├── layout.tsx
│   │   └── page.tsx
│   ├── api/                  # API Routes (minimal kullanım)
│   ├── layout.tsx
│   ├── page.tsx
│   └── globals.css
├── components/
│   ├── ui/                   # shadcn/ui atomic components
│   ├── common/               # Shared components (Header, Footer)
│   └── features/             # Feature-specific components
│       ├── auth/
│       ├── journal/
│       └── habits/
├── hooks/                    # Custom hooks
│   ├── use-auth.ts
│   └── use-media-query.ts
├── lib/                      # Utilities & configurations
│   ├── api/                  # API client, endpoints
│   │   ├── client.ts
│   │   └── endpoints/
│   ├── validations/          # Zod schemas
│   ├── utils.ts              # Helper functions
│   └── constants.ts
├── stores/                   # Zustand stores
│   ├── auth-store.ts
│   └── ui-store.ts
├── types/                    # TypeScript type definitions
│   ├── api.ts
│   └── models.ts
└── styles/                   # Global styles (minimal)
```

### Temel Kurallar

- **Feature isolation:** Feature component'leri kendi klasöründe kalır.
- **No circular imports:** Modüller arası döngüsel bağımlılık yasaktır.
- **Barrel exports:** Her feature klasöründe `index.ts` ile export.
- `components/ui/` sadece shadcn/ui component'leri içerir.
- `components/ui/` altındaki shadcn/ui component'leri tek dosyada kalır; bu klasörde kebab-case klasör yapısı, `types.ts`/`constants.ts` ayrımı veya `index.ts` kullanılmaz.

### Server vs Client Components

| Component Tipi | Kullanım Yeri |
| --- | --- |
| Server Component | Data fetching, SEO-critical pages, static content |
| Client Component | İnteraktif UI, hooks kullanan component'ler, browser API'leri |

**Varsayılan:** Server Component. Client Component sadece gerektiğinde `"use client"` ile.

### Server vs Client Component Karar Ağacı

```
Component'in görevi ne?
│
├─► Statik içerik gösterimi, SEO önemli
│   └─► SERVER COMPONENT
│
├─► Backend'den veri çekme (fetch)
│   ├─► İlk yükleme / SEO gerekli → SERVER COMPONENT
│   └─► Kullanıcı etkileşimi sonrası → CLIENT + TanStack Query
│
├─► useState, useEffect, custom hook kullanımı
│   └─► CLIENT COMPONENT ("use client")
│
├─► Event handler (onClick, onChange, onSubmit)
│   └─► CLIENT COMPONENT
│
├─► Browser API (localStorage, window, navigator)
│   └─► CLIENT COMPONENT
│
└─► Üçüncü parti kütüphane (chart, map, editor)
    └─► CLIENT COMPONENT + dynamic import
```

---

## 3. Naming Conventions

| Tip | Format | Örnek |
| --- | --- | --- |
| Component dosyası | kebab-case | `user-profile.tsx` |
| Component adı | PascalCase | `UserProfile` |
| Hook | camelCase, use prefix | `useAuth`, `useMediaQuery` |
| Utility function | camelCase | `formatDate`, `cn` |
| Type/Interface | PascalCase | `User`, `ApiResponse` |
| Constant | SCREAMING_SNAKE_CASE | `API_BASE_URL`, `MAX_FILE_SIZE` |
| Zustand store hook | `use` + PascalCase + Store | `useAuthStore`, `useUiStore` |
| API endpoint dosyası | kebab-case | `auth-endpoints.ts` |
| Test dosyası | Component adı + .test | `user-profile.test.tsx` |

**Not:** Next.js App Router özel dosyaları (`page.tsx`, `layout.tsx`, `loading.tsx`, `error.tsx`, `not-found.tsx`, `template.tsx`, `route.ts` vb.) framework isimlendirmesini kullanır.

### Types ve Constants Dosyalama

- Component, hook, context ve page dosyalarındaki `type`/`interface` tanımları `types.ts` dosyasına taşınır ve oradan import edilir.
- Component, hook, context ve page dosyalarındaki sabit değerler `constants.ts` dosyasına taşınır ve oradan import edilir.
- `components/ui/` altındaki shadcn/ui component'leri bu kuralın dışındadır.

### Component Dosya Yapısı

```
features/
└── journal/
    ├── index.ts                    # Barrel export
    ├── journal-entry.tsx           # Main component
    ├── journal-entry.test.tsx      # Tests
    ├── journal-entry-form.tsx      # Sub-component
    ├── types.ts                    # Feature types
    ├── constants.ts                # Feature constants
    └── use-journal-entries.ts      # Feature-specific hook
```

### components/ Klasör Yapısı

- `components/` altında (`components/ui` hariç) yeni component oluştururken component adı ile kebab-case klasör açılır.
- Component dosyası ve ilgili type/constant dosyaları aynı klasörde tutulur.
- Klasör içinde `index.ts` bulunur ve export buradan yapılır.
- shadcn/ui component'leri `components/ui/<component>.tsx` olarak tek dosya halinde kalır.

Örnek:

```
components/
└── user-card/
    ├── user-card.tsx
    ├── types.ts
    ├── constants.ts
    └── index.ts
```

`index.ts` örnek export:

```ts
export { UserCard } from "./user-card";
export type { UserCardProps } from "./types";
```

---

## 4. TypeScript Kuralları

### Zorunlu tsconfig Ayarları

```json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "noUncheckedIndexedAccess": true,
    "forceConsistentCasingInFileNames": true
  }
}
```

### Kurallar

| Kural | Açıklama |
| --- | --- |
| `any` yasak ❌ | `unknown` kullan, sonra type guard ile daralt |
| Type vs Interface | Object shapes için `interface`, unions/primitives için `type` |
| Explicit return types | Public functions ve hooks için zorunlu |
| Non-null assertion (`!`) | Yasak, optional chaining (`?.`) kullan |
| Type assertions | Minimum kullanım, gerekçe yorum olarak yazılır |

### Örnek

```typescript
// ❌ Yasak
function getUser(id: any): any {
  return data!.users[id];
}

// ✅ Doğru
function getUser(id: string): User | undefined {
  return data?.users?.[id];
}
```

---

## 5. Component Standartları

### Temel Yapı

```tsx
// 1. Imports (external → internal → types → styles)
import { useState } from "react";
import { Button } from "@/components/ui/button";
import type { User } from "@/types";

// 2. Types/Interfaces (veya types.ts'den import)
interface UserCardProps {
  user: User;
  onEdit?: (id: string) => void;
}

// 3. Component
export function UserCard({ user, onEdit }: UserCardProps): JSX.Element {
  // Hooks en üstte
  const [isLoading, setIsLoading] = useState(false);

  // Event handlers
  const handleEdit = (): void => {
    onEdit?.(user.id);
  };

  // Early returns
  if (!user) {
    return null;
  }

  // Render
  return (
    <div className="rounded-lg border p-4">
      <h3>{user.name}</h3>
      <Button onClick={handleEdit} disabled={isLoading}>
        Edit
      </Button>
    </div>
  );
}
```

### Kurallar

| Kural | Açıklama |
| --- | --- |
| Named exports | Default export yasak (barrel exports ve Next.js özel dosyaları hariç) |
| Props interface | Her component için explicit props tanımı |
| Destructure props | Component signature'da destructure |
| Single responsibility | Bir component bir iş yapar |
| Max 200 satır | Büyük component'ler parçalanır |

### Composition Patterns

```tsx
// ✅ Compound Component Pattern (kompleks UI için)
<Card>
  <Card.Header>Title</Card.Header>
  <Card.Body>Content</Card.Body>
  <Card.Footer>Actions</Card.Footer>
</Card>

// ✅ Render Props (flexible rendering için)
<DataList
  data={users}
  renderItem={(user) => <UserCard user={user} />}
/>
```

---

## 6. State Management

### Karar Matrisi

| State Tipi | Çözüm | Örnek |
| --- | --- | --- |
| Server state | TanStack Query | API data, cache |
| Global UI state | Zustand | Theme, sidebar open |
| Local UI state | useState | Form inputs, modals |
| Form state | React Hook Form | Complex forms |
| URL state | nuqs / searchParams | Filters, pagination |

### State Karar Checklist

```
1. State backend'den mi geliyor?
   └─► EVET → TanStack Query
   └─► HAYIR ↓

2. Birden fazla unrelated component kullanıyor mu?
   └─► EVET → Zustand
   └─► HAYIR ↓

3. URL'de tutulması mantıklı mı? (filtre, sayfa, tab)
   └─► EVET → URL state (nuqs / searchParams)
   └─► HAYIR ↓

4. Form state mi?
   └─► EVET → React Hook Form
   └─► HAYIR → useState / useReducer
```

### TanStack Query Kullanımı

```typescript
// lib/api/endpoints/users.ts
export const userKeys = {
  all: ["users"] as const,
  lists: () => [...userKeys.all, "list"] as const,
  list: (filters: UserFilters) => [...userKeys.lists(), filters] as const,
  details: () => [...userKeys.all, "detail"] as const,
  detail: (id: string) => [...userKeys.details(), id] as const,
};

// hooks/use-users.ts
export function useUsers(filters: UserFilters): UseQueryResult<User[]> {
  return useQuery({
    queryKey: userKeys.list(filters),
    queryFn: () => fetchUsers(filters),
  });
}
```

### Zustand Store Yapısı

```typescript
// stores/auth-store.ts
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  setUser: (user: User | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: !!user }),
  logout: () => set({ user: null, isAuthenticated: false }),
}));
```

**Kurallar:**

- Store'lar minimal tutulur, sadece gerçekten global state.
- Derived state için selector kullan.
- Async logic store dışında (TanStack Query ile).

---

## 7. API Layer

### API Client Yapısı (Axios + Interceptors)

```typescript
// lib/api/client.ts
import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Cookie'leri her istekte gönder
  headers: {
    "Content-Type": "application/json",
  },
});

// Request Interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // İstek öncesi işlemler (logging vb.)
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response Interceptor - Token Refresh
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: AxiosError | null): void => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve();
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // 401 aldık ve henüz retry yapmadık
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Başka bir refresh işlemi devam ediyorsa kuyruğa ekle
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => apiClient(originalRequest));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Refresh endpoint'i çağır
        await axios.post(
          `${API_BASE_URL}/auth/refresh`,
          {},
          { withCredentials: true }
        );

        processQueue(null);
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError);
        
        // Refresh başarısız - login'e yönlendir
        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }
        
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
```

### API Endpoint Yapısı

```typescript
// lib/api/endpoints/auth.ts
import { apiClient } from "../client";
import type { LoginRequest, AuthResponse, User } from "@/types";

export const authApi = {
  login: (data: LoginRequest): Promise<AuthResponse> =>
    apiClient.post("/auth/login", data).then((res) => res.data),

  logout: (): Promise<void> =>
    apiClient.post("/auth/logout").then((res) => res.data),

  getMe: (): Promise<User> =>
    apiClient.get("/auth/me").then((res) => res.data),

  refresh: (): Promise<void> =>
    apiClient.post("/auth/refresh").then((res) => res.data),
};
```

---

## 8. Authentication Stratejisi

### Genel Bakış

**Strateji:** Access Token + Refresh Token

| Token | Ömür | Saklama | Amaç |
| --- | --- | --- | --- |
| Access Token | 15 dakika | httpOnly Secure Cookie | API istekleri |
| Refresh Token | 7 gün | httpOnly Secure Cookie | Access token yenileme |

### Token Akışı

```
1. LOGIN
   Client                          Backend
     │ POST /auth/login              │
     │ {email, password}             │
     │─────────────────────────────►│
     │                               │ Validate credentials
     │                               │ Generate tokens
     │   Set-Cookie:                 │
     │   accessToken=xxx; HttpOnly;  │
     │   Secure; SameSite=Strict;    │
     │   Path=/; Max-Age=900         │
     │                               │
     │   Set-Cookie:                 │
     │   refreshToken=yyy; HttpOnly; │
     │   Secure; SameSite=Strict;    │
     │   Path=/auth; Max-Age=604800  │
     │◄─────────────────────────────│
     │                               │

2. API İSTEĞİ (Token Geçerli)
     │ GET /api/journals             │
     │ Cookie: accessToken=xxx       │ (browser otomatik)
     │─────────────────────────────►│
     │        200 OK + data          │
     │◄─────────────────────────────│

3. TOKEN REFRESH (Access Token Expired)
     │ GET /api/journals             │
     │ Cookie: accessToken=xxx       │
     │─────────────────────────────►│
     │        401 Unauthorized       │
     │◄─────────────────────────────│
     │                               │
     │ POST /auth/refresh            │
     │ Cookie: refreshToken=yyy      │
     │─────────────────────────────►│
     │                               │ Validate refresh token
     │                               │ Generate new access token
     │   Set-Cookie:                 │
     │   accessToken=newXxx;         │
     │◄─────────────────────────────│
     │                               │
     │ GET /api/journals (retry)     │
     │─────────────────────────────►│
     │        200 OK + data          │
     │◄─────────────────────────────│

4. LOGOUT
     │ POST /auth/logout             │
     │─────────────────────────────►│
     │                               │ (opsiyonel: token blacklist)
     │   Set-Cookie:                 │
     │   accessToken=; Max-Age=0     │ (clear cookies)
     │   refreshToken=; Max-Age=0    │
     │◄─────────────────────────────│
```

### İleride: Refresh Token Rotation

Güvenlik gereksinimleri artarsa (finans, sağlık, enterprise) rotation eklenebilir:
- Her refresh'te yeni refresh token verilir, eskisi invalidate edilir
- Token çalınması tespit edilebilir hale gelir

### Frontend Kuralları

| Kural | Açıklama |
| --- | --- |
| `withCredentials: true` | Tüm API isteklerinde zorunlu |
| Token'a JS'den erişim yok | httpOnly sayesinde XSS koruması |
| Manuel token yönetimi yok | Browser cookie'yi otomatik yönetir |
| Interceptor ile refresh | 401'de otomatik token yenileme |

### Cookie Ayarları (Backend Sorumluluğu)

```
Set-Cookie: accessToken=<token>;
  HttpOnly;           # JS erişemez (XSS koruması)
  Secure;             # Sadece HTTPS
  SameSite=Strict;    # CSRF koruması
  Path=/;             # Tüm endpoint'ler için
  Max-Age=900;        # 15 dakika

Set-Cookie: refreshToken=<token>;
  HttpOnly;
  Secure;
  SameSite=Strict;
  Path=/auth;         # Sadece auth endpoint'leri için
  Max-Age=604800;     # 7 gün
```

---

## 9. HTTP Error Handling

### Error Response Yapısı (RFC 7807)

Backend'den gelen hata formatı:

```typescript
// types/api.ts
interface ProblemDetails {
  type: string;      // Hata tipi URI
  title: string;     // Kısa açıklama
  status: number;    // HTTP status code
  detail: string;    // Detaylı açıklama
  instance?: string; // İstek URI
  errors?: Record<string, string[]>; // Validation hataları
}
```

### HTTP Status Code Davranışları

| Status | Durum | Frontend Davranışı |
| --- | --- | --- |
| 400 | Validation Error | Form alanlarına hata mesajlarını map et |
| 401 | Unauthorized | Token refresh dene, başarısızsa login'e yönlendir |
| 403 | Forbidden | "Yetkiniz yok" mesajı veya 403 sayfası |
| 404 | Not Found | "Bulunamadı" mesajı veya 404 sayfası |
| 409 | Conflict | İş kuralı hatası mesajı (örn: "Email zaten kayıtlı") |
| 422 | Unprocessable Entity | Validation hatalarını forma göster |
| 429 | Rate Limited | "Çok fazla istek" toast + retry-after header'a göre bekle |
| 500 | Server Error | "Bir hata oluştu" toast + retry seçeneği |
| 503 | Service Unavailable | "Bakım modu" sayfası |

### Global Error Handler

```typescript
// lib/api/error-handler.ts
import { AxiosError } from "axios";
import { toast } from "sonner";
import type { ProblemDetails } from "@/types";

export function handleApiError(error: AxiosError<ProblemDetails>): void {
  const status = error.response?.status;
  const problemDetails = error.response?.data;

  switch (status) {
    case 400:
    case 422:
      // Form validation - component'te handle edilecek
      break;

    case 401:
      // Interceptor'da handle ediliyor
      break;

    case 403:
      toast.error("Bu işlem için yetkiniz bulunmuyor");
      break;

    case 404:
      toast.error(problemDetails?.detail ?? "Kayıt bulunamadı");
      break;

    case 409:
      toast.error(problemDetails?.detail ?? "İşlem çakışması");
      break;

    case 429:
      const retryAfter = error.response?.headers["retry-after"];
      toast.error(
        `Çok fazla istek. ${retryAfter ? `${retryAfter} saniye sonra` : "Biraz sonra"} tekrar deneyin.`
      );
      break;

    case 500:
    case 502:
    case 503:
      toast.error("Sunucu hatası. Lütfen daha sonra tekrar deneyin.");
      break;

    default:
      toast.error("Beklenmeyen bir hata oluştu");
  }
}
```

### Form Validation Error Mapping

```typescript
// hooks/use-form-error.ts
import type { UseFormSetError, FieldValues, Path } from "react-hook-form";
import type { ProblemDetails } from "@/types";

export function mapApiErrorsToForm<T extends FieldValues>(
  problemDetails: ProblemDetails,
  setError: UseFormSetError<T>
): void {
  if (problemDetails.errors) {
    Object.entries(problemDetails.errors).forEach(([field, messages]) => {
      setError(field as Path<T>, {
        type: "server",
        message: messages[0],
      });
    });
  }
}

// Kullanım
const onSubmit = async (data: FormData): Promise<void> => {
  try {
    await api.createUser(data);
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 400) {
      mapApiErrorsToForm(error.response.data, form.setError);
    }
  }
};
```

---

## 10. Form Handling

### Standart Form Yapısı

```tsx
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

// 1. Schema tanımı
const createUserSchema = z.object({
  email: z.string().email("Geçerli email giriniz"),
  name: z.string().min(2, "İsim en az 2 karakter olmalı"),
  password: z.string().min(8, "Şifre en az 8 karakter olmalı"),
});

type CreateUserForm = z.infer<typeof createUserSchema>;

// 2. Component
export function CreateUserForm(): JSX.Element {
  const form = useForm<CreateUserForm>({
    resolver: zodResolver(createUserSchema),
    defaultValues: {
      email: "",
      name: "",
      password: "",
    },
  });

  const onSubmit = async (data: CreateUserForm): Promise<void> => {
    try {
      await userApi.create(data);
      toast.success("Kullanıcı oluşturuldu");
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 400) {
        mapApiErrorsToForm(error.response.data, form.setError);
      }
    }
  };

  return (
    <form onSubmit={form.handleSubmit(onSubmit)}>
      {/* Form fields with error display */}
    </form>
  );
}
```

### Kurallar

- Her form için Zod schema zorunlu.
- Schema'lar `lib/validations/` altında tutulabilir (reuse için).
- Backend validation error'ları form'a map edilir.

---

## 11. Error Boundaries & User Feedback

### Error Boundary Yapısı

```tsx
// components/common/error-boundary.tsx
"use client";

import { Component, type ReactNode } from "react";
import { Button } from "@/components/ui/button";

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    // Hata loglama servisi (Sentry vb.)
    console.error("Error caught by boundary:", error, errorInfo);
  }

  handleRetry = (): void => {
    this.setState({ hasError: false, error: undefined });
  };

  render(): ReactNode {
    if (this.state.hasError) {
      return (
        this.props.fallback ?? (
          <div className="flex flex-col items-center justify-center gap-4 p-8">
            <h2 className="text-lg font-semibold">Bir şeyler yanlış gitti</h2>
            <p className="text-muted-foreground">
              Sayfa yüklenirken bir hata oluştu.
            </p>
            <Button onClick={this.handleRetry}>Tekrar Dene</Button>
          </div>
        )
      );
    }
    return this.props.children;
  }
}
```

### Next.js Error Handling

```tsx
// app/error.tsx (Global error page)
"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";

interface ErrorPageProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function ErrorPage({ error, reset }: ErrorPageProps): JSX.Element {
  useEffect(() => {
    // Hata loglama
    console.error(error);
  }, [error]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      <h1 className="text-2xl font-bold">Bir hata oluştu</h1>
      <Button onClick={reset}>Tekrar Dene</Button>
    </div>
  );
}

// app/not-found.tsx (404 page)
export default function NotFoundPage(): JSX.Element {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center">
      <h1 className="text-4xl font-bold">404</h1>
      <p className="text-muted-foreground">Sayfa bulunamadı</p>
    </div>
  );
}
```

### Toast/Notification

- **Library:** sonner (önerilen) veya react-hot-toast
- Success, error, warning, info türleri
- Auto-dismiss: 5 saniye (error için 8 saniye)

```tsx
import { toast } from "sonner";

// Kullanım
toast.success("Kaydedildi");
toast.error("Bir hata oluştu");
toast.warning("Dikkat!");
toast.info("Bilgi");

// Promise ile
toast.promise(saveData(), {
  loading: "Kaydediliyor...",
  success: "Kaydedildi!",
  error: "Kayıt başarısız",
});
```

### Loading States

```tsx
// ✅ Skeleton kullan, spinner değil
{isLoading ? <UserCardSkeleton /> : <UserCard user={user} />}

// ✅ TanStack Query ile
const { data, isLoading, isError, error } = useUsers();

if (isLoading) return <UserListSkeleton />;
if (isError) return <ErrorMessage error={error} />;
return <UserList users={data} />;
```

---

## 12. Styling Kuralları

### Tailwind Kullanımı

```tsx
// ✅ cn() utility ile conditional classes
import { cn } from "@/lib/utils";

<button
  className={cn(
    "rounded-md px-4 py-2 font-medium",
    "bg-primary text-primary-foreground",
    "hover:bg-primary/90",
    disabled && "cursor-not-allowed opacity-50"
  )}
>
```

### Kurallar

| Kural | Açıklama |
| --- | --- |
| Inline styles yasak ❌ | Tailwind class kullan |
| !important yasak ❌ | Specificity sorunlarını düzgün çöz |
| Magic numbers yasak | Design tokens / Tailwind config kullan |
| Dark mode | `dark:` prefix ile, sistem tercihi varsayılan |
| Responsive | Mobile-first (`sm:`, `md:`, `lg:`) |

### Design Tokens

```typescript
// tailwind.config.ts
const config = {
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#f0f9ff",
          // ...
          900: "#0c4a6e",
        },
      },
      spacing: {
        // Custom spacing if needed
      },
    },
  },
};
```

---

## 13. Performance

### Code Splitting

```tsx
// ✅ Dynamic import for heavy components
import dynamic from "next/dynamic";

const HeavyChart = dynamic(() => import("@/components/features/chart"), {
  loading: () => <ChartSkeleton />,
  ssr: false, // Client-only component
});
```

### Memoization Kuralları

| Kullan | Kullanma |
| --- | --- |
| Expensive calculations | Primitive props |
| Reference-stable callbacks for children | Simple components |
| Large lists with React.memo | Premature optimization |

```tsx
// ✅ useMemo sadece expensive computation için
const sortedItems = useMemo(
  () => items.sort((a, b) => a.name.localeCompare(b.name)),
  [items]
);

// ❌ Gereksiz memoization
const greeting = useMemo(() => `Hello ${name}`, [name]);
```

### Image Optimization

```tsx
// ✅ next/image kullan
import Image from "next/image";

<Image
  src="/hero.jpg"
  alt="Hero"
  width={1200}
  height={600}
  priority // Above-the-fold images
  placeholder="blur"
/>
```

---

## 14. Accessibility (a11y)

### Zorunlu Kurallar

| Kural | Açıklama |
| --- | --- |
| Semantic HTML | `<button>`, `<nav>`, `<main>`, `<article>` kullan |
| Alt text | Tüm `<img>` için zorunlu |
| Keyboard navigation | Tüm interactive elementler tab ile erişilebilir |
| Focus states | Visible focus ring zorunlu |
| Color contrast | WCAG AA minimum (4.5:1 text, 3:1 UI) |
| ARIA labels | Icon-only buttons için zorunlu |

### Örnek

```tsx
// ✅ Accessible icon button
<Button variant="ghost" size="icon" aria-label="Menüyü aç">
  <Menu className="h-5 w-5" />
</Button>

// ✅ Form accessibility
<label htmlFor="email">Email</label>
<input
  id="email"
  type="email"
  aria-describedby="email-error"
  aria-invalid={!!errors.email}
/>
{errors.email && (
  <span id="email-error" role="alert">
    {errors.email.message}
  </span>
)}
```

---

## 15. Testing

### Test Piramidi

| Tip | Coverage | Araç |
| --- | --- | --- |
| Unit tests | Hooks, utilities | Vitest |
| Component tests | UI components | Vitest + RTL |
| Integration tests | Feature flows | Vitest + RTL |
| E2E tests | Critical paths | Playwright |

### Kritik Test Coverage

**Auth ve Payment UI için test coverage zorunludur (%80 minimum).**

```tsx
// components/features/auth/login-form.test.tsx
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LoginForm } from "./login-form";

describe("LoginForm", () => {
  it("should show validation errors for empty fields", async () => {
    render(<LoginForm />);

    await userEvent.click(screen.getByRole("button", { name: /giriş/i }));

    expect(screen.getByText(/email zorunlu/i)).toBeInTheDocument();
  });

  it("should call onSubmit with form data", async () => {
    const onSubmit = vi.fn();
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.type(screen.getByLabelText(/email/i), "test@test.com");
    await userEvent.type(screen.getByLabelText(/şifre/i), "password123");
    await userEvent.click(screen.getByRole("button", { name: /giriş/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      email: "test@test.com",
      password: "password123",
    });
  });
});
```

### Test Dosya Yapısı

- Test dosyaları component yanında: `component.test.tsx`
- Test utilities: `src/test/utils.tsx`
- Mocks: `src/test/mocks/`

---

## 16. Security

### Zorunlu Kurallar

| Kural | Açıklama |
| --- | --- |
| Token storage | httpOnly Secure Cookie (localStorage yasak) |
| XSS Prevention | `dangerouslySetInnerHTML` yasak (DOMPurify ile sanitize şart) |
| Environment variables | Client-side için `NEXT_PUBLIC_` prefix |
| Form validation | Client + Server side zorunlu |
| CSRF | SameSite=Strict cookie + Next.js Server Actions |
| Sensitive data | Console.log yasak, PII maskeleme zorunlu |

### Sensitive Data

```typescript
// ❌ Yasak
console.log("User password:", password);
console.log("User data:", user);
localStorage.setItem("token", accessToken);

// ✅ Doğru
// Token backend tarafından httpOnly cookie ile set edilir
// Hassas data loglanmaz
// Development'ta bile dikkatli olunur
```

### Content Security Policy

```typescript
// next.config.js
const securityHeaders = [
  {
    key: "Content-Security-Policy",
    value: "default-src 'self'; script-src 'self' 'unsafe-eval' 'unsafe-inline';",
  },
  {
    key: "X-Frame-Options",
    value: "DENY",
  },
  {
    key: "X-Content-Type-Options",
    value: "nosniff",
  },
];
```

---

## 17. Linting & Formatting

### ESLint Config

```javascript
// eslint.config.mjs (Flat config)
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

export default [
  ...nextVitals,
  ...nextTs,
  {
    ignores: [".next/**", "out/**", "build/**"],
  },
  {
    rules: {
      "@typescript-eslint/no-explicit-any": "error",
      "@typescript-eslint/explicit-function-return-type": "warn",
      "no-console": ["warn", { allow: ["warn", "error"] }],
      "react/jsx-no-leaked-render": "error",
    },
  },
];
```

### Prettier Config

```json
{
  "semi": true,
  "singleQuote": false,
  "tabWidth": 2,
  "trailingComma": "es5",
  "plugins": ["prettier-plugin-tailwindcss"]
}
```

### Pre-commit Hooks

```json
// package.json
{
  "lint-staged": {
    "*.{ts,tsx}": ["eslint --fix", "prettier --write"],
    "*.{json,md}": ["prettier --write"]
  }
}
```

---

## 18. Anti-Pattern Referans Tabloları

Bu bölüm, sık yapılan hataları ve doğru yöntemleri hızlı referans için özetler.

### 18.1 State Management Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `useEffect` + `fetch` + `useState` | TanStack Query `useQuery` | Cache, retry, stale yönetimi otomatik |
| Server state'i Zustand'da tutmak | TanStack Query | Zustand sadece client state için |
| Prop drilling (3+ seviye) | Zustand veya Context | Component ağacını kirletme |
| Global state overuse | Local state öncelikli | Her şey global olmak zorunda değil |
| `useState` ile form yönetimi | React Hook Form | Performans ve validation |
| Store'da async logic | TanStack Query mutations | Separation of concerns |

### 18.2 Component Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `export default function` | `export function` (named) | Import tutarlılığı, refactor kolaylığı |
| 300+ satır component | Max 200 satır, parçala | Single responsibility |
| Business logic component'te | Custom hook'a çıkar | Separation of concerns |
| Inline handler `onClick={() => complexFn()}` | Ayrı `handleClick` fonksiyonu | Okunabilirlik |
| Props'ta `any` type | Explicit interface | Type safety |
| `{condition && <Component />}` | `{condition ? <Component /> : null}` | `0` ve `""` render bug'ı |
| Component içinde component tanımı | Ayrı dosyaya çıkar | Re-render sorunu |

### 18.3 API & Data Fetching Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| Component içinde raw `fetch` | Merkezi API client | Interceptor, error handling |
| Hardcoded API URL | Environment variable | Ortam bağımsızlığı |
| Her component'te try-catch | Global error handler | Tutarlılık |
| `localStorage.setItem('token')` | httpOnly cookie | XSS koruması |
| Manuel loading/error state | TanStack Query states | Boilerplate azalt |
| Response type `any` | Zod schema validation | Runtime type safety |
| Waterfall requests | Parallel fetch / prefetch | Performans |

### 18.4 Security Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| Token localStorage'da | httpOnly Secure Cookie | XSS koruması |
| `dangerouslySetInnerHTML` direkt | DOMPurify ile sanitize | XSS önleme |
| `console.log(user)` | Logger + maskeleme / hiç loglama | PII sızıntısı |
| Client-side only validation | Client + Server validation | Bypass önleme |
| Secret'lar `.env.local`'de (prod) | Platform env variables | Git güvenliği |
| CORS `*` | Spesifik origin | Cross-origin güvenlik |
| `eval()` veya `new Function()` | Alternatif yöntemler | Code injection |

### 18.5 Performance Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `useMemo` her yerde | Sadece expensive computation | Premature optimization |
| `<img src="">` | `next/image` | Lazy load, optimization |
| Bundle'da büyük kütüphane | Dynamic import | Code splitting |
| Re-render kontrolsüz | React.memo + selector | Performans |
| `key={index}` dinamik listede | Unique stable key | Reconciliation bug |
| Inline object/array prop | useMemo veya dışarıda tanımla | Gereksiz re-render |
| Her component'te `"use client"` | Server Component default | Bundle size |

### 18.6 TypeScript Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `any` type | `unknown` + type guard | Type safety |
| `as` type assertion | Type narrowing | Runtime hatalarını önle |
| `!` non-null assertion | Optional chaining `?.` | Null safety |
| Implicit return type | Explicit return type | API kontratı |
| `// @ts-ignore` | Sorunu çöz | Teknik borç |
| `Object`, `Function` type | Spesifik type tanımı | Type safety |
| Type'ları component içinde | `types.ts` dosyasında | Organizasyon |

### 18.7 Next.js Specific Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| Her yerde `"use client"` | Server Component default | Bundle size, SEO |
| Client Component'te initial fetch | Server Component + fetch | Waterfall önleme |
| `router.push` ile data passing | URL params veya state | Bookmark, share |
| API route'ta heavy logic | Service layer | Separation of concerns |
| Layout'ta client state | URL veya context | Layout re-render |
| `getServerSideProps` (App Router) | Server Component + fetch | Modern pattern |

### 18.8 Form Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `useState` per field | React Hook Form | Performans, validation |
| Manuel validation | Zod schema | Type safety, reuse |
| Validation sadece submit'te | Real-time validation (onBlur) | UX |
| Backend error'ları toast ile | Form field'lara map et | UX |
| Form state'i prop drilling | Form context (RHF) | Clean code |

---

## 19. AI / Codex / Cursor Kullanımı

AI tarafından üretilen **her kod** bu dokümana uymak zorundadır.

### AI kullanırken

- Bu doküman mutlaka referans verilir.
- Küçük ve review edilebilir diff'ler istenir.
- Şu komut verilir: *"Bu değişikliği WEB-STANDARDS.md v2.0 kurallarına %100 uyarak yap"*

### Kritik Alanlar (Manuel Review Zorunlu)

- Auth components (login, register, password reset)
- Payment UI
- User data forms (PII içeren)
- Security-related code
- API client / interceptor değişiklikleri

Bu alanlarda AI-generated code **mutlaka human review** geçmelidir.

---

## 20. Değiştirilemez Kurallar

Aşağıdaki kuralların ihlali Code Review aşamasında **reddedilme** sebebidir:

| Kural | Durum |
| --- | --- |
| TypeScript strict mode | ❌ Kapatılamaz |
| `any` type kullanımı | ❌ Yasak |
| Default exports (Next.js özel dosyaları hariç) | ❌ Yasak |
| Token localStorage'da | ❌ Yasak |
| `dangerouslySetInnerHTML` (sanitize olmadan) | ❌ Yasak |
| Inline styles | ❌ Yasak |
| `console.log` (production) | ❌ Yasak |
| `!` non-null assertion | ❌ Yasak |
| `// @ts-ignore` | ❌ Yasak |
| `useEffect` + `fetch` + `setState` (TanStack Query varken) | ❌ Yasak |
| Component içinde component tanımı | ❌ Yasak |
| `key={index}` dinamik listede | ❌ Yasak |
| Auth/Payment UI test coverage < %80 | ❌ Yasak |
| Client-side only validation | ❌ Yasak |
| Hardcoded API URL | ❌ Yasak |
| PII loglama | ❌ Yasak |

---

## 21. Environment Setup

### Gereksinimler

- Node.js 20+ (LTS)
- pnpm 8+

### Çalıştırma

```bash
# Install dependencies
pnpm install

# Development
pnpm dev

# Build
pnpm build

# Test
pnpm test

# Lint
pnpm lint

# Type check
pnpm type-check
```

### Environment Variables

```bash
# .env.local (git-ignored)
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_APP_URL=http://localhost:3000

# Secrets (never in .env.local for production)
# Use Vercel/platform environment variables
```

---

## Genel Yaklaşım

Bu standart şu prensiplere dayanır:

- **Type-safety first** — TypeScript strict mode, no any
- **Server-first** — Server Components varsayılan
- **Security-first** — httpOnly cookies, no localStorage tokens
- **Performance-aware** — Code splitting, lazy loading, proper memoization
- **Accessible** — WCAG AA compliance
- **Testable** — Critical paths covered
- **Maintainable** — Clear structure, naming conventions, documentation

---

> **Doküman Sonu**  
> Sorular veya öneriler için: Teknik Lider ile iletişime geçin.
