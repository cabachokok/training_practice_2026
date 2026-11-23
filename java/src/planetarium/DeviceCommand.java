package planetarium;

/** Команда оборудованию: установить параметр в значение. */
public record DeviceCommand(String parameter, String value) {}
