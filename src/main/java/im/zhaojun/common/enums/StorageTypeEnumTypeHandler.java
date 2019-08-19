package im.zhaojun.common.enums;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StorageTypeEnumTypeHandler extends BaseTypeHandler<StorageTypeEnum>{

    public StorageTypeEnumTypeHandler(Class<StorageTypeEnum> type) {
        if (type == null)
            throw new IllegalArgumentException("Type argument cannot be null");
        StorageTypeEnum[] enums = type.getEnumConstants();
        if (enums == null)
            throw new IllegalArgumentException(type.getSimpleName()
                    + " does not represent an enum type.");
    }
 
    @Override
    public StorageTypeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String i = rs.getString(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return StorageTypeEnum.getEnum(i);
        }
    }
 
    @Override
    public StorageTypeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    	 String i = rs.getString(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return StorageTypeEnum.getEnum(i);
        }
    }
 
    @Override
    public StorageTypeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
   	 String i = cs.getString(columnIndex);
       if (cs.wasNull()) {
           return null;
       } else {
           return StorageTypeEnum.getEnum(i);
       }
    }
 
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, StorageTypeEnum parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getKey());
    }
	
}