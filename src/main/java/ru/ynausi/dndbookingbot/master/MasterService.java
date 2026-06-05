package ru.ynausi.dndbookingbot.master;

import java.util.List;
import java.util.Optional;

public interface MasterService {

    List<Master> getActiveMasters();

    Optional<Master> getById(String id);
}
