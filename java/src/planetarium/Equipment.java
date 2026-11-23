package planetarium;

/**
 * Оборудование сеанса. Реальный программно-аппаратный комплекс и имитация реализуют один интерфейс,
 * поэтому ProfileApplier не знает, с каким из них работает.
 */
public interface Equipment {

    /** Проверка связи. */
    boolean ping();

    /** Отправка команды; false означает, что оборудование её не приняло. */
    boolean send(DeviceCommand command);
}
