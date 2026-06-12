package com.foodlab.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.report.entity.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {

    @Select("SELECT * FROM report_template WHERE template_type = #{templateType} AND is_default = '1' AND status = '0' AND deleted = 0 LIMIT 1")
    ReportTemplate selectDefaultByType(String templateType);
}
