package com.foodlab.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.form.entity.DataMigrationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataMigrationLogMapper extends BaseMapper<DataMigrationLog> {

    @Select("SELECT * FROM data_migration_log WHERE template_id = #{templateId} AND from_version = #{fromVersion} AND to_version = #{toVersion} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    DataMigrationLog selectLatestMigration(@Param("templateId") Long templateId, @Param("fromVersion") Integer fromVersion, @Param("toVersion") Integer toVersion);
}
