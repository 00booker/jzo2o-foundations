package com.jzo2o.foundations.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: zjw16
 * @time: 2024/3/31 17:49
 */

@AllArgsConstructor
@Getter
public enum HotStatusEnum {
    ENABLE(1,"热门"),
    DISABLE(0, "非热门");
    private int status;
    private String description;

    public boolean equals(Integer status) {
        return this.status == status;
    }

    public boolean equals(HotStatusEnum hotStatusEnum) {
        return hotStatusEnum != null && hotStatusEnum.status == this.getStatus();
    }
}
