package com.server.mapper;

import com.server.pojo.entity.App;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;
import java.util.Map;

/**
* 
* @author chenhu
* @date 2023-11-20 14:47:02
*/
@Mapper
public interface AppMapper {

    /**
     * 主键查询
     * @param primaryKey 主键
     * @return
    */
    App selectByPrimaryKey(@Param(value = "primaryKey") Long primaryKey);


    /**
     * 多条件查询
     * @param param
     * @return
    */
    List<App> select(Map<String,Object> param);

    /**
     * 多条件查询
     * @param appName
     * @return
     */
    List<App> selectByAppName(String appName);

    /**
     * 流式查询，可以设置 fetchSize 属性设置一次流查询多少条数据，直至取完数据
     * 注意：使用流式查询，需要在service层将使用流式查询的方法上添加@Transactional注解，不然会报错
     * 错误原因为 @Autowired注入的mapper查询一次就会将连接关闭，不会保持链接
     * @param param
     * @return
    */
    Cursor<App> flowSelect(Map<String,Object> param);


    /**
     * 添加
     * @param app
     * @return
    */
    Integer save(App app);


    /**
     * 批量添加
     * @param list
     * @return
    */
    Integer batchSave(@Param("list") List<App> list);


    /**
     * 添加或者修改
     * 此方法 会根据主键来进行判断，如果主键存在则修改，如果主键不存在，会检查是否有唯一索引，在根据唯一索引判断是新增还是修改
     * @param app
     * @return
    */
    Integer saveOrUpdate(App app);


    /**
     * 修改
     * @param param
     * @return
    */
    Integer update(Map<String,Object> param);

    Integer del(@Param("appId") Long appId);


}
