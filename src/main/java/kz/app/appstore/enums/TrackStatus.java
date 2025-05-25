package kz.app.appstore.enums;

public enum TrackStatus {
    PENDING,
    NEW,           // Новый — ждет действий менеджера
    ACCEPTED,      // Принят менеджером
    REJECTED,      // Отклонён
    CONFIRMED,     // Подтвержден к отправке
    SHIPPED        // Отправлен
}
