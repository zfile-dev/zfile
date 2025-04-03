package im.zhaojun.zfile.core.config.mybatis;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.core.ResolvableType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 自定义 Set 类型处理器, 用于处理数据库 VARCHAR 类型字段和 Java Set 类型属性之间的转换.
 * 支持字符串格式为: "[a, b, c]".
 *
 * @author zhaojun
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public abstract class CollectionTypeHandler<T> extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        if (parameter instanceof Collection collection) {
            StringJoiner joiner = new StringJoiner(",");
            for (Object o : collection) {
                joiner.add(Convert.toStr(o));
            }
            ps.setString(i, joiner.toString());
        } else {
            ps.setString(i, Convert.toStr(parameter));
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String str = rs.getString(columnName);
        return convertToEntityAttribute(str);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String str = rs.getString(columnIndex);
        return convertToEntityAttribute(str);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String str = cs.getString(columnIndex);
        return convertToEntityAttribute(str);
    }

    private Class<?> collectionClazz;

    private Type innerType;

    /**
     * 构造方法
     */
    public CollectionTypeHandler() {
        ResolvableType resolvableType = ResolvableType.forClass(getClass());
        Type type = resolvableType.as(CollectionTypeHandler.class).getGeneric().getType();

        if (type instanceof ParameterizedType parameterizedType) {
            collectionClazz = (Class<?>) parameterizedType.getRawType();
            // 获取实际类型参数（泛型参数，例如 List<String> 中的 String）
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            // 使用这些信息做进一步操作
            for (Type actualTypeArgument : actualTypeArguments) {
                innerType = actualTypeArgument;
                break;
            }
        }
    }

    private Object convertToEntityAttribute(String dbData) {
        if (StrUtil.isEmpty(dbData)) {
            if (List.class.isAssignableFrom(collectionClazz)) {
                return Collections.emptyList();
            } else if (Set.class.isAssignableFrom(collectionClazz)) {
                return Collections.emptySet();
            } else {
                return null;
            }
        }

        Collection collection;

        if (List.class.isAssignableFrom(collectionClazz)) {
            collection = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(collectionClazz)) {
            collection = new HashSet<>();
        } else {
            return null;
        }

        String[] split = dbData.split(",");
        for (String s : split) {
            if (NumberUtil.isNumber(s)) {
                collection.add(Convert.convert(Integer.class, s));
            } else {
                collection.add(s);
            }
        }

        return collection;
    }

}