package im.zhaojun.zfile.module.share.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.share.model.entity.ShareLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ShareLinkMapper extends BaseMapper<ShareLink> {

    /**
     * 根据分享 key 获取分享链接信息
     */
    ShareLink getByShareKey(@Param("shareKey") String shareKey);

    /**
     * 根据用户 ID 获取分享链接列表
     */
    List<ShareLink> getByUserId(@Param("userId") Integer userId);

    /**
     * 更新访问次数
     */
    int incrementAccessCount(@Param("shareKey") String shareKey, @Param("increment") int increment);

    /**
     * 更新下载次数
     */
    int incrementDownloadCount(@Param("shareKey") String shareKey, @Param("increment") int increment);

    /**
     * 删除过期的分享链接
     */
    int deleteExpiredLinks(@Param("currentTime") Date currentTime);

    /**
     * 删除指定用户的过期分享链接
     */
    int deleteExpiredLinksByUserId(@Param("userId") Integer userId, @Param("currentTime") Date currentTime);

}
