package com.github.zjjfly.sql.parser.impl;

import com.google.common.collect.Lists;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.type.SqlTypeTransforms;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zjjfly[https://github.com/zjjfly] on 2020/8/20
 */
public class Functions {

    public static final SqlFunction FUNC1 = new SqlFunction(
            new SqlIdentifier("FUNC1", SqlParserPos.ZERO),
            ReturnTypes.cascade(ReturnTypes.explicit(SqlTypeName.INTEGER), SqlTypeTransforms.TO_NULLABLE),
            InferTypes.VARCHAR_1024,
            OperandTypes.family(SqlTypeFamily.STRING),
            Lists.newArrayList(TypeUtils.getRelType(SqlTypeName.VARCHAR)),
            SqlFunctionCategory.USER_DEFINED_FUNCTION);

    public static final SqlFunction FUNC2 = new SqlFunction(
            new SqlIdentifier("FUNC2", SqlParserPos.ZERO),
            ReturnTypes.cascade(ReturnTypes.explicit(SqlTypeName.VARCHAR), SqlTypeTransforms.TO_NULLABLE),
            InferTypes.FIRST_KNOWN,
            OperandTypes.family(SqlTypeFamily.INTEGER),
            Lists.newArrayList(TypeUtils.getRelType(SqlTypeName.INTEGER)),
            SqlFunctionCategory.USER_DEFINED_FUNCTION);

    public final static Map<String, SqlFunction> FUNCS = new HashMap<>();

    static {
        List<Field> fields = Arrays.stream(Functions.class.getDeclaredFields())
                                   .filter(field -> field.getType() == SqlFunction.class).collect(
                        Collectors.toList());
        for (Field field : fields) {
            try {
                FUNCS.put(field.getName(), (SqlFunction) field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

}
