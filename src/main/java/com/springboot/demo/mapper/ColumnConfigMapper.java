package com.springboot.demo.mapper;

import com.springboot.demo.entity.ColumnConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ColumnConfigMapper {

    @Select("SELECT * FROM api_column_config WHERE table_node_id = #{tableNodeId}")
    List<ColumnConfig> selectByTableNodeId(Long tableNodeId);

    @Select("SELECT * FROM api_column_config WHERE table_node_id = #{tableNodeId} AND exposed = 1")
    List<ColumnConfig> selectExposedByTableNodeId(Long tableNodeId);

    @Insert("INSERT INTO api_column_config(table_node_id, column_name, exposed) " +
            "VALUES(#{tableNodeId}, #{columnName}, #{exposed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ColumnConfig config);

    @Update("UPDATE api_column_config SET exposed = #{exposed} WHERE id = #{id}")
    int updateExposed(@Param("id") Long id, @Param("exposed") Boolean exposed);

    @Update("UPDATE api_column_config SET exposed = #{exposed} " +
            "WHERE table_node_id = #{tableNodeId} AND column_name = #{columnName}")
    int updateExposedByNodeAndColumn(@Param("tableNodeId") Long tableNodeId,
                                     @Param("columnName") String columnName,
                                     @Param("exposed") Boolean exposed);

    @Delete("DELETE FROM api_column_config WHERE table_node_id = #{tableNodeId}")
    int deleteByTableNodeId(Long tableNodeId);

    @Delete("DELETE FROM api_column_config WHERE id = #{id}")
    int deleteById(Long id);
}
