# Система бронирований отелей

## Порты
- `api-gateway`: 8080
- `booking-service`: 8083
- `eureka-server`: 8761
- `hotel-service`: 8082

## Требования
- Java 17+ 
- Maven 3.9+

## Запуск проекта
- Желательно заранее установить плагин для Lombok, если работаете в IntelliJ IDEA
- Скачать репозиторий `git@github.com:OlegPrizov/booking.git` и перейти в него
- Запустить eureka-server командой `mvn -pl eureka-server spring-boot:run`
- Запустить api-gateway командой `mvn -pl api-gateway spring-boot:run`
- Запустить hotel-service командой`mvn -pl hotel-service spring-boot:run`
- Запустить booking-service командой `mvn -pl booking-service spring-boot:run`

## Эндпоинты
- Для тестирования легче и удобнее всего использовать Postman
- тут допишу эндпоинты
---
Автор: Призов О.О.