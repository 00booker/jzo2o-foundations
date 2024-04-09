package com.jzo2o.foundations.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 服务类型表
 * </p>
 *
 * @author hasity
 * @since 2024-03-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("serve_type")
public class ServeType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 服务类型编码
     */
    private String code;

    /**
     * 服务类型名称
     */
    private String name;

    /**
     * 服务类型图标
     */
    private String serveTypeIcon;

    /**
     * 服务类型图片
     */
    private String img;

    /**
     * 排序字段
     */
    private Integer sortNum;

    /**
     * 是否启用，0草稿,1禁用，2启用
     */
    private Integer activeStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 更新者
     */
    private Long updateBy;


}
