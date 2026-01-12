# Mobile Project Standards (React Native + Expo)

> **Versiyon:** 2.0  
> **Son Güncelleme:** 11.01.2025  
> **Durum:** Aktif / Zorunlu

Bu doküman, React Native + Expo projeleri için mimari kararları, kodlama standartlarını ve operasyonel kuralları **Tek Doğruluk Kaynağı (SSOT)** olarak tanımlar.

Bu dokümana tabi projelerde yapılan **tüm geliştirmeler (insan veya AI)** bu kurallara uymak zorundadır.

---

## İçindekiler

1. [Teknoloji Yığını](#1-teknoloji-yığını)
2. [Mimari: Proje Yapısı](#2-mimari-proje-yapısı)
3. [Naming Conventions](#3-naming-conventions)
4. [TypeScript Kuralları](#4-typescript-kuralları)
5. [Navigation (Expo Router)](#5-navigation-expo-router)
6. [State Management](#6-state-management)
7. [API Layer](#7-api-layer)
8. [Authentication Stratejisi](#8-authentication-stratejisi)
9. [HTTP Error Handling](#9-http-error-handling)
10. [Form Handling](#10-form-handling)
11. [UI & Component Standartları](#11-ui--component-standartları)
12. [Performance](#12-performance)
13. [Offline & Cache](#13-offline--cache)
14. [Assets, Fonts, Icons](#14-assets-fonts-icons)
15. [Security](#15-security)
16. [Logging & Observability](#16-logging--observability)
17. [Testing](#17-testing)
18. [Anti-Pattern Referans Tabloları](#18-anti-pattern-referans-tabloları)
19. [Pull Request Kuralları](#19-pull-request-kuralları)
20. [AI / Codex / Cursor Kullanımı](#20-ai--codex--cursor-kullanımı)
21. [Değiştirilemez Kurallar](#21-değiştirilemez-kurallar)
22. [Environment & Config](#22-environment--config)

---

## 1. Teknoloji Yığını

| Alan | Tercih |
| --- | --- |
| Runtime | Expo SDK (managed workflow) |
| React Native | Expo ile gelen RN sürümü |
| Dil | TypeScript (strict) |
| Package Manager | pnpm |
| Navigation | Expo Router |
| Server State | TanStack Query v5 |
| Client State | Zustand (minimal) |
| Forms | React Hook Form + Zod |
| Styling | NativeWind (Tailwind) |
| UI Kit | React Native Paper / Tamagui / custom (proje bazlı) |
| Icons | @expo/vector-icons veya lucide-react-native |
| Images | expo-image |
| Animations | react-native-reanimated + react-native-gesture-handler |
| Secure Storage | expo-secure-store |
| HTTP Client | Axios |
| Analytics | Firebase Analytics / Segment (opsiyonel) |
| Crash/Logging | Sentry |
| Testing (Unit/UI) | Vitest/Jest + @testing-library/react-native |
| E2E | Maestro veya Detox (opsiyonel) |

### Dependency Versioning

- Expo SDK major upgrade'leri PR'da gerekçelendirilir.
- Lock file (`pnpm-lock.yaml`) her zaman commit edilir.
- Expo ile uyumsuz RN paketleri eklenmez (önce uyumluluk kontrol edilir).

---

## 2. Mimari: Proje Yapısı

### Amaç

- Feature izolasyonu
- Okunabilirlik
- Sürdürülebilirlik
- Test edilebilirlik

### Önerilen Yapı (Expo Router)

```
src/
├── app/                      # expo-router routes
│   ├── (auth)/
│   │   ├── login.tsx
│   │   └── register.tsx
│   ├── (tabs)/
│   │   ├── _layout.tsx
│   │   ├── home.tsx
│   │   └── settings.tsx
│   ├── _layout.tsx
│   └── +not-found.tsx
├── components/
│   ├── ui/                   # atomic UI primitives
│   ├── common/               # shared components
│   └── features/             # feature-specific components
├── hooks/                    # shared hooks
├── lib/
│   ├── api/                  # API client, endpoints, query keys
│   ├── auth/                 # Token management
│   ├── config/               # env, constants
│   ├── utils/                # helpers
│   └── telemetry/            # logger, analytics
├── stores/                   # Zustand stores (minimal)
├── types/                    # models, api types
└── styles/                   # tokens, theme (minimal)
```

### Temel Kurallar

- **Feature isolation:** Feature component/hook'ları kendi klasöründe kalır.
- **No circular imports:** Döngüsel bağımlılık yasaktır.
- **Barrel exports:** Feature klasörlerinde `index.ts` ile export.
- UI primitives (`components/ui`) iş kuralı içermez.

---

## 3. Naming Conventions

| Tip | Format | Örnek |
| --- | --- | --- |
| Component dosyası | kebab-case | `user-card.tsx` |
| Component adı | PascalCase | `UserCard` |
| Hook | camelCase + use | `useAuth` |
| Utility | camelCase | `formatCurrency` |
| Type/Interface | PascalCase | `User`, `ApiResponse` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_FILE_SIZE` |
| Store | camelCase + Store | `useAuthStore` |
| Test dosyası | `.test` | `user-card.test.tsx` |

---

## 4. TypeScript Kuralları

### Zorunlu Ayarlar

```json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "noUncheckedIndexedAccess": true
  }
}
```

### Kurallar

| Kural | Açıklama |
| --- | --- |
| `any` yasak ❌ | `unknown` + type guard kullan |
| `!` non-null assertion yasak ❌ | Optional chaining (`?.`) kullan |
| Explicit return types | Public fonksiyon/hook'larda zorunlu |

---

## 5. Navigation (Expo Router)

### Kurallar

- Route'lar `app/` altında tutulur, ekranlar route dosyalarıdır.
- Layout'lar: `_layout.tsx`
- Protected routes: `(auth)` / `(tabs)` gibi route groups ile ayrılır.

### Param & Deep Link

- Route paramları typed olmalı.
- Deep link kurgusu `app.json` / `expo-router` config ile netleştirilir.

### Typed Routes

```typescript
// types/navigation.ts
export type RootStackParamList = {
  "(tabs)": undefined;
  "(auth)/login": undefined;
  "(auth)/register": undefined;
  "journal/[id]": { id: string };
};

// Kullanım
import { useLocalSearchParams } from "expo-router";

export default function JournalDetail(): JSX.Element {
  const { id } = useLocalSearchParams<{ id: string }>();
  // ...
}
```

---

## 6. State Management

### State Karar Checklist

```
1. State backend'den mi geliyor?
   └─► EVET → TanStack Query
   └─► HAYIR ↓

2. Birden fazla unrelated ekran kullanıyor mu?
   └─► EVET → Zustand (minimal)
   └─► HAYIR ↓

3. Sadece tek ekran/modal içinde mi?
   └─► EVET → useState / useReducer
```

### TanStack Query Kullanımı

```typescript
// lib/api/query-keys.ts
export const journalKeys = {
  all: ["journals"] as const,
  lists: () => [...journalKeys.all, "list"] as const,
  list: (filters: JournalFilters) => [...journalKeys.lists(), filters] as const,
  details: () => [...journalKeys.all, "detail"] as const,
  detail: (id: string) => [...journalKeys.details(), id] as const,
};

// hooks/use-journals.ts
export function useJournals(filters: JournalFilters): UseQueryResult<Journal[]> {
  return useQuery({
    queryKey: journalKeys.list(filters),
    queryFn: () => journalApi.getAll(filters),
  });
}
```

### Zustand Kuralları

- Store minimal tutulur: auth/session, theme, global UI (örn: bottom sheet state).
- Async logic store içine gömülmez (TanStack Query ile).

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

---

## 7. API Layer

### API Client Yapısı (Axios + Interceptors)

```typescript
// lib/api/client.ts
import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import * as SecureStore from "expo-secure-store";
import { TOKEN_KEYS } from "@/lib/auth/constants";

const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL;

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request Interceptor - Token ekleme
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const accessToken = await SecureStore.getItemAsync(TOKEN_KEYS.ACCESS);
    
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    
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

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => apiClient(originalRequest));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = await SecureStore.getItemAsync(TOKEN_KEYS.REFRESH);
        
        if (!refreshToken) {
          throw new Error("No refresh token");
        }

        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken: newAccessToken } = response.data;
        await SecureStore.setItemAsync(TOKEN_KEYS.ACCESS, newAccessToken);

        processQueue(null);
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError);
        
        // Refresh başarısız - token'ları temizle, login'e yönlendir
        await SecureStore.deleteItemAsync(TOKEN_KEYS.ACCESS);
        await SecureStore.deleteItemAsync(TOKEN_KEYS.REFRESH);
        
        // Navigation - router kullanılabilir
        // router.replace("/login");
        
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
```

### Query Keys Standardı

```typescript
// lib/api/query-keys.ts
export const queryKeys = {
  auth: {
    me: ["auth", "me"] as const,
  },
  journals: {
    all: ["journals"] as const,
    list: (filters?: JournalFilters) => ["journals", "list", filters] as const,
    detail: (id: string) => ["journals", "detail", id] as const,
  },
  habits: {
    all: ["habits"] as const,
    list: () => ["habits", "list"] as const,
    detail: (id: string) => ["habits", "detail", id] as const,
  },
};
```

---

## 8. Authentication Stratejisi

### Genel Bakış

**Strateji:** Access Token + Refresh Token (Secure Storage)

| Token | Ömür | Storage | Gönderim |
| --- | --- | --- | --- |
| Access Token | 15 dakika | `expo-secure-store` | `Authorization: Bearer xxx` header |
| Refresh Token | 7 gün | `expo-secure-store` | Request body |

### Web vs Mobil Farkı

| Platform | Token Storage | Gönderim |
| --- | --- | --- |
| Web | httpOnly Cookie | Cookie (otomatik) |
| Mobil | expo-secure-store | Authorization header (manuel) |

**Neden farklı?** React Native'de browser yok, dolayısıyla cookie mekanizması çalışmaz.

### Token Akışı

```
1. LOGIN
   App                             Backend
     │ POST /auth/login              │
     │ {email, password}             │
     │─────────────────────────────►│
     │                               │ Validate credentials
     │                               │ Generate tokens
     │   { accessToken, refreshToken }
     │◄─────────────────────────────│
     │                               │
     │ SecureStore.setItemAsync(     │
     │   "accessToken", token)       │
     │ SecureStore.setItemAsync(     │
     │   "refreshToken", token)      │
     │                               │

2. API İSTEĞİ
     │ GET /api/journals             │
     │ Authorization: Bearer xxx     │
     │─────────────────────────────►│
     │        200 OK + data          │
     │◄─────────────────────────────│

3. TOKEN REFRESH (Access Token Expired)
     │ GET /api/journals             │
     │ Authorization: Bearer xxx     │
     │─────────────────────────────►│
     │        401 Unauthorized       │
     │◄─────────────────────────────│
     │                               │
     │ POST /auth/refresh            │
     │ { refreshToken: yyy }         │
     │─────────────────────────────►│
     │                               │ Validate refresh token
     │   { accessToken: newXxx }     │
     │◄─────────────────────────────│
     │                               │
     │ SecureStore.setItemAsync(     │
     │   "accessToken", newToken)    │
     │                               │
     │ GET /api/journals (retry)     │
     │─────────────────────────────►│
     │        200 OK + data          │
     │◄─────────────────────────────│

4. LOGOUT
     │ POST /auth/logout             │
     │─────────────────────────────►│
     │        200 OK                 │
     │◄─────────────────────────────│
     │                               │
     │ SecureStore.deleteItemAsync(  │
     │   "accessToken")              │
     │ SecureStore.deleteItemAsync(  │
     │   "refreshToken")             │
```

### Token Management

```typescript
// lib/auth/constants.ts
export const TOKEN_KEYS = {
  ACCESS: "accessToken",
  REFRESH: "refreshToken",
} as const;

// lib/auth/token-service.ts
import * as SecureStore from "expo-secure-store";
import { TOKEN_KEYS } from "./constants";

export const tokenService = {
  async getAccessToken(): Promise<string | null> {
    return SecureStore.getItemAsync(TOKEN_KEYS.ACCESS);
  },

  async getRefreshToken(): Promise<string | null> {
    return SecureStore.getItemAsync(TOKEN_KEYS.REFRESH);
  },

  async setTokens(accessToken: string, refreshToken: string): Promise<void> {
    await SecureStore.setItemAsync(TOKEN_KEYS.ACCESS, accessToken);
    await SecureStore.setItemAsync(TOKEN_KEYS.REFRESH, refreshToken);
  },

  async clearTokens(): Promise<void> {
    await SecureStore.deleteItemAsync(TOKEN_KEYS.ACCESS);
    await SecureStore.deleteItemAsync(TOKEN_KEYS.REFRESH);
  },

  async hasTokens(): Promise<boolean> {
    const access = await this.getAccessToken();
    const refresh = await this.getRefreshToken();
    return !!(access && refresh);
  },
};
```

### Auth Hook

```typescript
// hooks/use-auth.ts
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "expo-router";
import { authApi } from "@/lib/api/endpoints/auth";
import { tokenService } from "@/lib/auth/token-service";
import { useAuthStore } from "@/stores/auth-store";
import { queryKeys } from "@/lib/api/query-keys";

export function useAuth() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { setUser, logout: clearStore } = useAuthStore();

  const { data: user, isLoading } = useQuery({
    queryKey: queryKeys.auth.me,
    queryFn: authApi.getMe,
    retry: false,
  });

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: async (data) => {
      await tokenService.setTokens(data.accessToken, data.refreshToken);
      setUser(data.user);
      queryClient.invalidateQueries({ queryKey: queryKeys.auth.me });
      router.replace("/(tabs)/home");
    },
  });

  const logoutMutation = useMutation({
    mutationFn: authApi.logout,
    onSettled: async () => {
      await tokenService.clearTokens();
      clearStore();
      queryClient.clear();
      router.replace("/(auth)/login");
    },
  });

  return {
    user,
    isLoading,
    isAuthenticated: !!user,
    login: loginMutation.mutate,
    logout: logoutMutation.mutate,
    isLoggingIn: loginMutation.isPending,
    isLoggingOut: logoutMutation.isPending,
  };
}
```

### Kurallar

| Kural | Açıklama |
| --- | --- |
| `expo-secure-store` zorunlu | AsyncStorage yasak (şifresiz) |
| Token JS'de erişilebilir | Mobilde kaçınılmaz, SecureStore ile güvenli |
| Interceptor ile refresh | 401'de otomatik token yenileme |
| Logout'ta token temizleme | Hem local hem server-side |

---

## 9. HTTP Error Handling

### Error Response Yapısı (RFC 7807)

Backend'den gelen hata formatı:

```typescript
// types/api.ts
interface ProblemDetails {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  errors?: Record<string, string[]>;
}
```

### HTTP Status Code Davranışları

| Status | Durum | Mobil Davranış |
| --- | --- | --- |
| 400 | Validation Error | Form alanlarına hata mesajlarını map et |
| 401 | Unauthorized | Token refresh dene, başarısızsa login'e yönlendir |
| 403 | Forbidden | Alert: "Yetkiniz yok" |
| 404 | Not Found | Alert veya boş state göster |
| 409 | Conflict | Alert: İş kuralı hatası mesajı |
| 422 | Unprocessable Entity | Validation hatalarını forma göster |
| 429 | Rate Limited | Toast + backoff (retry-after header) |
| 500 | Server Error | Toast: "Bir hata oluştu" + retry seçeneği |
| 503 | Service Unavailable | Maintenance mode ekranı |

### Global Error Handler

```typescript
// lib/api/error-handler.ts
import { AxiosError } from "axios";
import { Alert } from "react-native";
import Toast from "react-native-toast-message";
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
      Alert.alert("Yetkisiz İşlem", "Bu işlem için yetkiniz bulunmuyor.");
      break;

    case 404:
      Toast.show({
        type: "error",
        text1: "Bulunamadı",
        text2: problemDetails?.detail ?? "Kayıt bulunamadı",
      });
      break;

    case 409:
      Alert.alert("İşlem Hatası", problemDetails?.detail ?? "İşlem çakışması");
      break;

    case 429:
      const retryAfter = error.response?.headers["retry-after"];
      Toast.show({
        type: "error",
        text1: "Çok Fazla İstek",
        text2: `${retryAfter ? `${retryAfter} saniye sonra` : "Biraz sonra"} tekrar deneyin.`,
      });
      break;

    case 500:
    case 502:
    case 503:
      Toast.show({
        type: "error",
        text1: "Sunucu Hatası",
        text2: "Lütfen daha sonra tekrar deneyin.",
      });
      break;

    default:
      Toast.show({
        type: "error",
        text1: "Hata",
        text2: "Beklenmeyen bir hata oluştu",
      });
  }
}
```

---

## 10. Form Handling

### Standart Form Yapısı

```typescript
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { TextInput, Button, Text, View } from "react-native";

const loginSchema = z.object({
  email: z.string().email("Geçerli email giriniz"),
  password: z.string().min(8, "Şifre en az 8 karakter olmalı"),
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginForm(): JSX.Element {
  const { control, handleSubmit, formState: { errors } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: LoginForm): Promise<void> => {
    // API call
  };

  return (
    <View>
      <Controller
        control={control}
        name="email"
        render={({ field: { onChange, onBlur, value } }) => (
          <TextInput
            onBlur={onBlur}
            onChangeText={onChange}
            value={value}
            placeholder="Email"
            keyboardType="email-address"
            autoCapitalize="none"
          />
        )}
      />
      {errors.email && <Text>{errors.email.message}</Text>}

      <Controller
        control={control}
        name="password"
        render={({ field: { onChange, onBlur, value } }) => (
          <TextInput
            onBlur={onBlur}
            onChangeText={onChange}
            value={value}
            placeholder="Şifre"
            secureTextEntry
          />
        )}
      />
      {errors.password && <Text>{errors.password.message}</Text>}

      <Button title="Giriş Yap" onPress={handleSubmit(onSubmit)} />
    </View>
  );
}
```

### Kurallar

- Her form için Zod schema zorunlu
- RHF + Zod resolver standart
- Backend validation error'ları forma map edilir

---

## 11. UI & Component Standartları

### Kurallar

| Kural | Açıklama |
| --- | --- |
| Named exports | Default export yasak (barrel export hariç) |
| Props interface | Her component için zorunlu |
| Single responsibility | Bir component bir iş yapar |
| Max 200 satır | Büyük bileşenler parçalanır |

### Styling

- Inline style minimumda tutulur; öncelik NativeWind className.
- Theme/tokens yaklaşımı: spacing, radius, colors tek yerden yönetilir.

```tsx
// ✅ NativeWind kullan
<View className="p-4 rounded-lg bg-white dark:bg-gray-800">
  <Text className="text-lg font-semibold text-gray-900 dark:text-white">
    Başlık
  </Text>
</View>

// ❌ Inline style
<View style={{ padding: 16, borderRadius: 8, backgroundColor: "white" }}>
```

### Component Yapısı

```tsx
// components/features/journal/journal-card.tsx
import { View, Text, Pressable } from "react-native";
import type { Journal } from "@/types";

interface JournalCardProps {
  journal: Journal;
  onPress?: (id: string) => void;
}

export function JournalCard({ journal, onPress }: JournalCardProps): JSX.Element {
  const handlePress = (): void => {
    onPress?.(journal.id);
  };

  return (
    <Pressable onPress={handlePress} className="p-4 bg-white rounded-lg mb-2">
      <Text className="text-lg font-semibold">{journal.title}</Text>
      <Text className="text-gray-600 mt-1">{journal.preview}</Text>
    </Pressable>
  );
}
```

---

## 12. Performance

### Liste Optimizasyonu

```tsx
// ✅ FlashList kullan (büyük listeler için)
import { FlashList } from "@shopify/flash-list";

<FlashList
  data={journals}
  renderItem={({ item }) => <JournalCard journal={item} />}
  estimatedItemSize={100}
  keyExtractor={(item) => item.id}
/>

// ✅ FlatList ile optimizasyon
<FlatList
  data={items}
  renderItem={renderItem}
  keyExtractor={(item) => item.id}
  getItemLayout={(data, index) => ({
    length: ITEM_HEIGHT,
    offset: ITEM_HEIGHT * index,
    index,
  })}
  removeClippedSubviews={true}
  maxToRenderPerBatch={10}
  windowSize={5}
/>
```

### Kurallar

| Kural | Açıklama |
| --- | --- |
| `keyExtractor` | Zorunlu, unique stable key |
| `getItemLayout` | Mümkünse ekle (sabit yükseklik) |
| FlashList | 100+ item için tercih et |
| Görsel optimizasyonu | `expo-image` + caching |
| Animasyon | Reanimated (JS thread'i yormaz) |

### Re-render Kontrolü

```tsx
// ✅ Stable callback
const handlePress = useCallback((id: string) => {
  // ...
}, []);

// ✅ Selector ile store
const userName = useAuthStore((state) => state.user?.name);

// ✅ Memoized component
const MemoizedCard = memo(JournalCard);
```

---

## 13. Offline & Cache

### Server Cache

```typescript
// TanStack Query config
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 dakika
      gcTime: 1000 * 60 * 30,   // 30 dakika (eski adı: cacheTime)
      retry: 3,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    },
  },
});
```

### Offline Persistence (Opsiyonel)

```typescript
// Kritik offline senaryolar için
import { createAsyncStoragePersister } from "@tanstack/query-async-storage-persister";
import AsyncStorage from "@react-native-async-storage/async-storage";

const asyncStoragePersister = createAsyncStoragePersister({
  storage: AsyncStorage,
});

// PersistQueryClientProvider ile kullanılır
```

---

## 14. Assets, Fonts, Icons

### Fonts

```typescript
// app/_layout.tsx
import { useFonts } from "expo-font";
import { SplashScreen } from "expo-router";

SplashScreen.preventAutoHideAsync();

export default function RootLayout(): JSX.Element | null {
  const [fontsLoaded] = useFonts({
    "Inter-Regular": require("@/assets/fonts/Inter-Regular.ttf"),
    "Inter-Bold": require("@/assets/fonts/Inter-Bold.ttf"),
  });

  useEffect(() => {
    if (fontsLoaded) {
      SplashScreen.hideAsync();
    }
  }, [fontsLoaded]);

  if (!fontsLoaded) {
    return null;
  }

  return <Stack />;
}
```

### Icons

- Tek kütüphane seçilir, projede karıştırılmaz
- Önerilen: `lucide-react-native` veya `@expo/vector-icons`

### Images

```typescript
// expo-image kullan
import { Image } from "expo-image";

<Image
  source={{ uri: imageUrl }}
  style={{ width: 200, height: 200 }}
  contentFit="cover"
  placeholder={blurhash}
  transition={200}
/>
```

---

## 15. Security

### Token Storage

| ❌ Yasak | ✅ Zorunlu |
| --- | --- |
| AsyncStorage | expo-secure-store |
| MMKV (token için) | expo-secure-store |

```typescript
// ✅ Secure storage
import * as SecureStore from "expo-secure-store";

await SecureStore.setItemAsync("accessToken", token);
const token = await SecureStore.getItemAsync("accessToken");

// ❌ AsyncStorage - ŞİFRESİZ!
import AsyncStorage from "@react-native-async-storage/async-storage";
await AsyncStorage.setItem("accessToken", token); // YASAK
```

### Sensitive Data

| Kural | Açıklama |
| --- | --- |
| `console.log` prod'da yasak | Logger kullan, prod'da kapalı |
| PII masking | Email/phone maskelenir |
| Token loglama yasak | Hassas veri hiç loglanmaz |

### Network Security

- HTTPS zorunlu
- Certificate pinning (opsiyonel, yüksek güvenlik için)

---

## 16. Logging & Observability

### Central Logger

```typescript
// lib/telemetry/logger.ts
import * as Sentry from "@sentry/react-native";

type LogLevel = "debug" | "info" | "warn" | "error";

const isDev = __DEV__;

export const logger = {
  debug(message: string, data?: Record<string, unknown>): void {
    if (isDev) {
      console.log(`[DEBUG] ${message}`, data);
    }
  },

  info(message: string, data?: Record<string, unknown>): void {
    if (isDev) {
      console.info(`[INFO] ${message}`, data);
    }
    Sentry.addBreadcrumb({ message, data, level: "info" });
  },

  warn(message: string, data?: Record<string, unknown>): void {
    if (isDev) {
      console.warn(`[WARN] ${message}`, data);
    }
    Sentry.addBreadcrumb({ message, data, level: "warning" });
  },

  error(message: string, error?: Error, data?: Record<string, unknown>): void {
    if (isDev) {
      console.error(`[ERROR] ${message}`, error, data);
    }
    Sentry.captureException(error ?? new Error(message), { extra: data });
  },
};
```

### Error Boundary

```tsx
// components/common/error-boundary.tsx
import { ErrorBoundary as SentryErrorBoundary } from "@sentry/react-native";
import { View, Text, Button } from "react-native";

function FallbackComponent({ resetError }: { resetError: () => void }): JSX.Element {
  return (
    <View className="flex-1 items-center justify-center p-4">
      <Text className="text-lg font-semibold mb-2">Bir şeyler yanlış gitti</Text>
      <Text className="text-gray-600 mb-4">Uygulama beklenmeyen bir hata ile karşılaştı.</Text>
      <Button title="Tekrar Dene" onPress={resetError} />
    </View>
  );
}

export function AppErrorBoundary({ children }: { children: React.ReactNode }): JSX.Element {
  return (
    <SentryErrorBoundary fallback={FallbackComponent}>
      {children}
    </SentryErrorBoundary>
  );
}
```

---

## 17. Testing

### Beklentiler

| Senaryo | Gereksinim |
| --- | --- |
| Yeni feature | En az 1 happy path test |
| Bug fix | Regression test zorunlu |
| Custom hook | Unit test |
| Auth / ödeme / kritik akışlar | %80 coverage hedefi |

### Test Yapısı

```typescript
// components/features/journal/journal-card.test.tsx
import { render, screen, fireEvent } from "@testing-library/react-native";
import { JournalCard } from "./journal-card";

const mockJournal = {
  id: "1",
  title: "Test Journal",
  preview: "Test preview",
};

describe("JournalCard", () => {
  it("should render journal title", () => {
    render(<JournalCard journal={mockJournal} />);
    expect(screen.getByText("Test Journal")).toBeTruthy();
  });

  it("should call onPress with journal id", () => {
    const onPress = jest.fn();
    render(<JournalCard journal={mockJournal} onPress={onPress} />);
    
    fireEvent.press(screen.getByText("Test Journal"));
    
    expect(onPress).toHaveBeenCalledWith("1");
  });
});
```

---

## 18. Anti-Pattern Referans Tabloları

### 18.1 State Management Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `useEffect` + `fetch` + `useState` | TanStack Query | Cache, retry otomatik |
| Server state'i Zustand'da | TanStack Query | Zustand sadece client state |
| Her şey global state | Local state öncelikli | Gereksiz complexity |
| Async logic store içinde | TanStack Query mutations | Separation of concerns |
| Store'da türetilmiş veri | Selector ile hesapla | Tek kaynak |

### 18.2 Component Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `export default function` | `export function` | Named export tutarlılığı |
| 300+ satır component | Max 200 satır | Single responsibility |
| Inline style her yerde | NativeWind className | Tutarlılık, tema |
| Props'ta `any` | Explicit interface | Type safety |
| Component içinde component | Ayrı dosyaya çıkar | Re-render sorunu |

### 18.3 Performance Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `FlatList` 1000+ item | `FlashList` | Performans |
| `key={index}` dinamik liste | Unique stable key | Reconciliation bug |
| Inline object/array prop | useMemo veya dışarıda | Gereksiz re-render |
| JS thread'de ağır animasyon | Reanimated (UI thread) | Performans |
| `<Image>` (RN core) | `expo-image` | Caching, placeholder |
| Her render'da yeni callback | `useCallback` | Child re-render |

### 18.4 Security Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| Token AsyncStorage'da | expo-secure-store | Şifreli storage |
| `console.log(token)` | Logger (prod kapalı) | Token sızıntısı |
| `console.log(user)` | PII maskeleme | KVKK/GDPR |
| HTTP kullanımı | HTTPS zorunlu | MitM koruması |
| Hardcoded API URL | Environment variable | Ortam bağımsızlığı |

### 18.5 Navigation Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| Untyped params | Typed route params | Type safety |
| Navigation logic component'te | Custom hook | Separation of concerns |
| Deep link test yok | Deep link test | Kritik akış |
| Auth check her ekranda | Layout'ta guard | DRY |

### 18.6 API Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| Component içinde raw fetch | Merkezi API client | Interceptor, error handling |
| Manuel loading state | TanStack Query | Boilerplate azalt |
| Her yerde try-catch | Global error handler | Tutarlılık |
| Response type `any` | Typed response + Zod | Runtime safety |

### 18.7 TypeScript Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
| --- | --- | --- |
| `any` type | `unknown` + guard | Type safety |
| `as` assertion | Type narrowing | Runtime hata önleme |
| `!` non-null assertion | Optional chaining | Null safety |
| `// @ts-ignore` | Sorunu çöz | Teknik borç |
| Implicit return type | Explicit return type | API kontratı |

---

## 19. Pull Request Kuralları

| Kural | Açıklama |
| --- | --- |
| PR başlığı | `feat/fix/chore(scope): açıklama` |
| Max diff | 400 LOC (gerekirse böl) |
| UI değişikliği | Ekran görüntüsü / video zorunlu |
| Logic değişikliği | Test zorunlu |
| Expo config değişikliği | Build/test notu zorunlu |

---

## 20. AI / Codex / Cursor Kullanımı

AI tarafından üretilen **her kod** bu dokümana uymak zorundadır.

### AI kullanırken

- Bu doküman mutlaka referans verilir
- Küçük ve review edilebilir diff istenir
- Şu komut verilir: *"Bu değişikliği MOBILE-STANDARDS.md v2.0 kurallarına %100 uyarak yap"*

### Kritik Alanlar (Manuel Review Zorunlu)

- Auth (login, register, token management)
- Ödeme / finans
- PII içeren formlar
- Güvenlik ile ilgili kod
- API client / interceptor değişiklikleri

---

## 21. Değiştirilemez Kurallar

Aşağıdaki kuralların ihlali Code Review aşamasında **reddedilme** sebebidir:

| Kural | Durum |
| --- | --- |
| TypeScript strict mode | ❌ Kapatılamaz |
| `any` type kullanımı | ❌ Yasak |
| Default exports (Expo Router hariç) | ❌ Yasak |
| Token AsyncStorage'da | ❌ Yasak |
| `console.log` (production) | ❌ Yasak |
| `!` non-null assertion | ❌ Yasak |
| `// @ts-ignore` | ❌ Yasak |
| `useEffect` + `fetch` + `setState` | ❌ Yasak |
| `key={index}` dinamik listede | ❌ Yasak |
| Inline style (NativeWind varken) | ❌ Yasak |
| HTTP (HTTPS yerine) | ❌ Yasak |
| PII loglama | ❌ Yasak |
| Auth/Payment test coverage < %80 | ❌ Yasak |
| Expo uyumsuz paket ekleme | ❌ Yasak |

---

## 22. Environment & Config

### Gereksinimler

- Node.js 20+
- pnpm 8+
- Expo CLI

### Çalıştırma

```bash
pnpm install
pnpm start
pnpm ios
pnpm android
```

### Environment Variables

- `app.config.ts` üzerinden yönetilir
- Secrets repo'da tutulmaz
- Production secret'lar EAS / CI üzerinden sağlanır

```typescript
// app.config.ts
export default {
  expo: {
    // ...
    extra: {
      apiUrl: process.env.EXPO_PUBLIC_API_URL,
    },
  },
};
```

---

## Genel Prensipler

Bu standart şu prensiplere dayanır:

- **Type-safety first** — TypeScript strict mode, no any
- **Performance odaklı** — Liste optimizasyonu, lazy loading
- **Security-first** — Secure storage, no AsyncStorage tokens
- **Erişilebilirlik ve kullanılabilirlik** — a11y best practices
- **Test edilebilirlik** — Critical paths covered
- **Sürdürülebilir mimari** — Clear structure, feature isolation

---

> **Doküman Sonu**  
> Sorular veya öneriler için: Teknik Lider ile iletişime geçin.
