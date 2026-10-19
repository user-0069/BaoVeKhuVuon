package com.team.gardendefense.model.entity.pet;

import com.team.gardendefense.controller.GameManager;

/**
 * Interface quy định khả năng tự động tìm kiếm và thu gom tiền tệ/mặt trời.
 */
public interface IAutoCollector {
    float getCollectionRadius();
    void scanAndCollect(GameManager gm);
    boolean isBusy();
}

