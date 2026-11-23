package planetarium;

/**
 * Имитация оборудования, как в apply_profile.py: связь всегда есть, громкость выше 90 не принимается.
 * Реального устройства в проекте нет, поэтому это единственная реализация Equipment.
 */
public final class SimulatedEquipment implements Equipment {

    static final int MAX_VOLUME = 90;

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public boolean send(DeviceCommand command) {
        boolean volumeTooHigh = command.parameter().equals("volume")
                && Integer.parseInt(command.value()) > MAX_VOLUME;
        return !volumeTooHigh;
    }
}
