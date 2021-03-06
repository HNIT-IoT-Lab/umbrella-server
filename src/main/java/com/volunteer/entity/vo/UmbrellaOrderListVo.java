package com.volunteer.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: 梁峰源
 * @date: 2022/2/18 19:50
 * Description:
 */
@Data
@Accessors(chain = true)//开启链式编程
public class UmbrellaOrderListVo {
    /**
     * 第几页
     */
    private Integer pageNo;
    /**
     * 显示多少条数据
     */
    private Integer pageSize;
    /**
     * 共多少条数据
     */
    private Integer Total;
    /**
     * 返回的数据集合
     */
    private List<UmbrellaOrderVo> records;
}
