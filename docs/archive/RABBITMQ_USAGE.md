# RabbitMQ trong CMS: Có cần không? Đang dùng để làm gì?

## 1) RabbitMQ dùng để làm gì (mục tiêu chung)

RabbitMQ (AMQP broker) thường được dùng để:
- Tách (decouple) xử lý nền khỏi request HTTP: API trả nhanh, phần nặng xử lý async.
- Chống “spike” tải: hệ thống xử lý theo queue, dễ scale consumer.
- Tăng độ bền: message có thể durable/persist, consumer retry, DLQ.
- Làm nền tảng event-driven: publish sự kiện (user registered, enrollment created, …) cho các dịch vụ khác.

Trong CMS, RabbitMQ hiện tại chỉ đóng vai trò **queue nền cho gửi email**.

## 2) Hiện trạng trong codebase (điểm tích hợp)

### 2.1 Dependency + cấu hình kết nối

- Maven dependency: `spring-boot-starter-amqp` trong [pom.xml](file:///c:/Users/Admin/Downloads/Repo/cms/pom.xml)
- Broker properties trong [application.properties](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/application.properties)
  - `spring.rabbitmq.host`
  - `spring.rabbitmq.port`
  - `spring.rabbitmq.username`
  - `spring.rabbitmq.password`

### 2.2 Topology (exchange/queue/binding)

Khai báo trong [RabbitMqConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/RabbitMqConfig.java):
- Exchange: `cms_email_exchange` (topic)
- Queue: `cms_email_queue` (durable)
- Routing key: `cms_email_routing_key`
- JSON converter: `Jackson2JsonMessageConverter`

### 2.3 Producer/Consumer

- Producer: [EmailProducer.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/infrastructure/messaging/EmailProducer.java)
  - Gửi message bằng `RabbitTemplate.convertAndSend(exchange, routingKey, message)`
- Consumer: [EmailConsumer.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/infrastructure/messaging/EmailConsumer.java)
  - `@RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)`
  - Hiện tại chỉ log + mô phỏng xử lý (chưa tích hợp SMTP/provider)

## 3) Use case đang dùng RabbitMQ

### 3.1 Quên mật khẩu (forgot password)

Luồng trong [PasswordService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/PasswordService.java):
- Sinh `resetToken`, lưu Redis TTL.
- Enqueue “email message” qua `EmailProducer`.

Ghi chú quan trọng:
- Service đang `try/catch` khi enqueue và chỉ `log.warn` nếu RabbitMQ down.
  - Điều này làm API có thể trả “đã gửi email” trong khi thực tế không có email nào được gửi.

### 3.2 Ngoài ra có dùng không?

Không. Trong code hiện tại:
- Không có queue/exchange khác cho học vụ (courses/enrollments/…)
- Không có publisher confirms/returns
- Không có retry/backoff/DLQ
- Không có consumer khác ngoài email

## 4) Vậy dự án có “thực sự cần” RabbitMQ không?

Tùy theo mục tiêu sản phẩm và môi trường:

### 4.1 Nếu mục tiêu là “dev/demo chạy nhanh” (không email thật)

Bạn **không bắt buộc cần** RabbitMQ.
- Vì consumer email chưa gửi email thật.
- Trong dev, có thể debug trả token reset trực tiếp (property `cms.debug.return-reset-token=true` trong `application-dev.properties`).

### 4.2 Nếu mục tiêu là “production có reset password qua email”

Bạn **cần một cơ chế gửi email**. RabbitMQ là một lựa chọn tốt để async hóa email.
Nhưng hiện trạng vẫn thiếu các mảnh quan trọng để đạt mức production-grade:
- Cơ chế gửi mail thật (SMTP/provider)
- Retry/backoff và DLQ (tránh mất mail khi provider lỗi)
- Quan sát/metrics + dashboard queue depth
- Quy ước “enqueue thất bại” thì API phản hồi thế nào (không nên nói “đã gửi” nếu enqueue fail)

### 4.3 Nếu không dùng RabbitMQ, thay thế tối thiểu là gì?

- Gửi email synchronous ngay trong request (đơn giản nhưng dễ chậm/timeout).
- Hoặc dùng một scheduler/outbox table (DB-driven queue) thay vì broker ngoài.
- Hoặc dùng provider async (ví dụ HTTP API của email service) nhưng vẫn cần retry.

## 5) Đề xuất hướng tiếp theo

Nếu bạn muốn giữ RabbitMQ:
- Thêm DLQ + retry/backoff cho `cms_email_queue`.
- Consumer gọi email provider thật.
- Đổi cách xử lý lỗi enqueue: trả message rõ ràng hoặc fallback.

Nếu bạn muốn bỏ RabbitMQ:
- Loại dependency AMQP và config RabbitMqConfig, EmailProducer/Consumer.
- Chuyển PasswordService sang cơ chế gửi mail khác (sync hoặc outbox).

