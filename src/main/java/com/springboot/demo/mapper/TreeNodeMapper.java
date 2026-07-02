package com.springboot.demo.mapper;

import com.springboot.demo.entity.TreeNode;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreeNodeMapper {

    @Select("SELECT * FROM api_tree_node ORDER BY sort_order, id")
    List<TreeNode> selectAll();

    @Select("SELECT * FROM api_tree_node WHERE id = #{id}")
    TreeNode selectById(Long id);

    @Select("SELECT * FROM api_tree_node WHERE parent_id IS NULL ORDER BY sort_order, id")
    List<TreeNode> selectRootNodes();

    @Select("SELECT * FROM api_tree_node WHERE parent_id = #{parentId} ORDER BY sort_order, id")
    List<TreeNode> selectByParentId(Long parentId);

    @Select("SELECT * FROM api_tree_node WHERE node_type = 'TABLE' AND exposed = 1")
    List<TreeNode> selectExposedTables();

    @Insert("INSERT INTO api_tree_node(parent_id, label, node_type, table_name, sort_order, exposed) " +
            "VALUES(#{parentId}, #{label}, #{nodeType}, #{tableName}, #{sortOrder}, #{exposed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TreeNode node);

    @Update("UPDATE api_tree_node SET parent_id=#{parentId}, label=#{label}, node_type=#{nodeType}, " +
            "table_name=#{tableName}, sort_order=#{sortOrder}, exposed=#{exposed} WHERE id=#{id}")
    int update(TreeNode node);

    @Update("UPDATE api_tree_node SET exposed = #{exposed} WHERE id = #{id}")
    int updateExposed(@Param("id") Long id, @Param("exposed") Boolean exposed);

    @Delete("DELETE FROM api_tree_node WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM api_tree_node")
    int count();
}
