package com.foodlab.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.sample.entity.Sample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SampleMapper extends BaseMapper<Sample> {

    IPage<Sample> selectSamplePage(Page<Sample> page,
                                   @Param("sampleName") String sampleName,
                                   @Param("sampleCode") String sampleCode,
                                   @Param("sampleStatus") String sampleStatus,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);
}
