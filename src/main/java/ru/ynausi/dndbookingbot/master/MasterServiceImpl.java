package ru.ynausi.dndbookingbot.master;

import java.util.List;
import java.util.Optional;

public class MasterServiceImpl implements MasterService{
    @Override
    public List<Master> getActiveMasters() {
        return List.of();
    }

    @Override
    public Optional<Master> getById(String id) {
        return getActiveMasters().stream()
                .filter(master -> master.id().equals(id))
                .findFirst();
    }
}
