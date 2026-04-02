# Auction App (Java, OOP, MVC, Observer, Concurrent Bidding)

## Chức năng đã có
- Quản lý user
- Quản lý product
- Tạo phiên đấu giá
- Đặt bid thường
- Tự động auto-bid
- Chống bid giây cuối (anti-sniping)
- Observer để phát sự kiện realtime
- Concurrent bidding bằng `ReentrantLock`
- Bid history + ASCII price curve
- Cấu trúc tách lớp theo `model / repository / service / controller / view / network / util`
- Có sẵn `AuctionServer` và `AuctionClient` để mở rộng networking

## Chạy demo
```bash
mvn clean package
java -jar target/auction-app-1.0-SNAPSHOT.jar
```

## Cấu trúc thư mục
```text
src/main/java/com/auction
├── controller
├── model
├── network
├── observer
├── repository
│   └── impl
├── service
├── util
├── view
└── Main.java
```

## Gợi ý để nâng cấp bài nộp
- Thay repository in-memory bằng MySQL/PostgreSQL
- Thêm JavaFX/Swing GUI
- Thêm authentication/login
- Dùng WebSocket nếu muốn realtime chuẩn hơn
- Tách riêng module server và client nếu thầy yêu cầu networking rõ hơn
