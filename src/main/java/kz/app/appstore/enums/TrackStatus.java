package kz.app.appstore.enums;

public enum TrackStatus {
    PENDING,
    NEW,           // Новый — ждет действий менеджера
    ACCEPTED,      // Принят менеджером
    REJECTED,      // Отклонён
    CONFIRMED,     // Подтвержден к отправке
    SHIPPED,      // Отправлен

    IN_KZ_WAREHOUSE, //принят складчиком в Казахстане
    READY_FOR_PICKUP, //заказ готов к выдаче клиенту
    DELIVERED //заказ выдан клиенту
}
