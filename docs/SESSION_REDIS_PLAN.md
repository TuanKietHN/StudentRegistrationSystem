# Plan: Redis Session Repository (Optimistic Tx → Lua-ready)

## Mục tiêu

- Chuẩn hoá lưu session/refresh token trong Redis theo dạng string (UUID/JSON) để dễ debug và tránh serializer ngầm.
- Tách tầng truy cập Redis (SessionRepository) khỏi business rules (AuthSessionService) để thay đổi implementation dễ dàng.
- Giảm race condition cho refresh token rotation bằng optimistic transaction (WATCH/MULTI/EXEC).
- Thiết kế API/keyspace đủ ổn định để có thể thay rotate sang Lua atomic khi traffic cao mà không đổi controller/service.

## Kiến trúc đề xuất

- `SessionRepository` (interface)
  - Hợp đồng thao tác session/refresh token/device mapping/list/revoke.
  - Không chứa business decisions (ví dụ max sessions), chỉ cung cấp primitive operations (kèm helper).
- `RedisSessionRepository` (implementation hiện tại)
  - Dùng `StringRedisTemplate` (key/value đều string).
  - Serialize `SessionData` → JSON string bằng `ObjectMapper` nội bộ (manual) để không phụ thuộc bean auto-config.
  - Rotate dùng optimistic transaction.
- `AuthSessionService` (business rules)
  - Quyết định: max sessions/user, 1 device ↔ 1 session, revoke policies khi nghi reuse.
  - Gọi `SessionRepository` để thao tác Redis.

## Redis keyspace (không đổi khi nâng cấp Lua)

- `auth:rt:{refreshToken}` → `sessionId`
- `auth:sess:{sessionId}` → `SessionData` (JSON string)
- `auth:sess:rt:{sessionId}` → `refreshToken` (current/active)
- `auth:u:dev:{username}:{deviceId}` → `sessionId` (active session for device)
- `auth:u:sess:{username}` (ZSET) → member `sessionId`, score `createdAtMs` (list + evict)

TTL:
- Dùng TTL refresh (ms) và set/expire đồng nhất cho toàn bộ key liên quan tới session.

## Optimistic transaction cho rotate (hiện tại)

### Vấn đề cần tránh

- Double-submit refresh cùng một refresh token: 2 request cùng rotate được → sinh 2 refresh token hợp lệ hoặc trạng thái key lệch.
- Refresh + logout/revoke chồng nhau → orphan keys (session list còn nhưng data mất…).

### Cách xử lý

- Rotate thực hiện theo vòng lặp retry ngắn (1–2 lần):
  - `WATCH auth:rt:{oldRt}`
  - Đọc `sessionId` từ `auth:rt:{oldRt}`; nếu null → invalid/expired.
  - `WATCH auth:sess:rt:{sessionId}`
  - Đọc `currentRt` và `sessionJson`; validate:
    - `currentRt == oldRt`
    - `deviceId` trùng với `SessionData.deviceId`
    - `auth:u:dev:{username}:{deviceId} == sessionId`
  - `MULTI`
    - Ghi `auth:rt:{newRt} = sessionId` (PX ttl)
    - Update `auth:sess:rt:{sessionId} = newRt` (PX ttl)
    - Update `auth:sess:{sessionId} = updatedSessionJson` (PX ttl)
    - Xoá `auth:rt:{oldRt}`
  - `EXEC`
    - Nếu fail (null) → key đã thay đổi bởi request khác → retry hoặc trả invalid/expired.

Lưu ý: hành vi “reuse detected” (mismatch/device mismatch) vẫn do service quyết định (ví dụ revoke all + lock user).

## Khi traffic cao: nâng cấp rotate sang Lua (tương lai)

- Giữ nguyên `SessionRepository.rotate(...)` signature.
- Thay implementation `rotate` từ WATCH/MULTI sang 1 Lua script atomic:
  - Input: `oldRt`, `deviceId`, `newRt`, `ttlMs`, `nowMs`, `ip`, `ua`
  - Output: `{ok, sessionId, username}` hoặc `{err, code}` (INVALID/REUSE/DEVICE_MISMATCH)
- Ưu điểm:
  - Atomic tuyệt đối, không cần retry.
  - Mọi key update/delete trong 1 script → không orphan.

## Tính khả thi & tác động

- Thay `RedisTemplate<String,Object>` sang `StringRedisTemplate` trong auth/session giúp:
  - Loại rủi ro serializer JSON/JDK không nhất quán.
  - Dữ liệu Redis inspect/debug dễ.
- Tách repository không làm thay đổi API bên ngoài (controller vẫn như cũ).
- Việc giữ `ObjectMapper manual` giải quyết vấn đề thiếu bean `ObjectMapper` mà không cần cấu hình global.

## Checklist triển khai

- Tạo `SessionRepository` + `RedisSessionRepository` (StringRedisTemplate + ObjectMapper manual).
- Refactor `AuthSessionService` dùng repository.
- Rotate dùng optimistic transaction + retry nhỏ.
- Bỏ phụ thuộc bean `ObjectMapper` (xoá config bean nếu không còn dùng).
- Chạy compile/build để đảm bảo không lỗi.

