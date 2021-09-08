package com.siemens.calcite.sql.parser.impl;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zjjfly[https://github.com/zjjfly] on 2020/8/20
 */
public class TypeUtils {

    private static final SqlTypeFactoryImpl FACTORY = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);

    public static final Map<String, RelDataType> TYPE_CACHE = new HashMap<>();

    public static SqlTypeFactoryImpl getFactory(){
        return FACTORY;
    }

    public static RelDataType getRelType(SqlTypeName name) {
        RelDataType relDataType = TYPE_CACHE.get(name.getName());
        if (relDataType == null) {
            relDataType = FACTORY.createSqlType(name);
            TYPE_CACHE.put(name.getName(), relDataType);
        }
        return relDataType;
    }
}
