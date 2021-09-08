package com.siemens.calcite.sql.func;

import org.apache.calcite.sql.SqlCreate;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;

import java.util.List;

/**
 * @author zjjfly[https://github.com/zjjfly] on 2020/8/21
 */
public class SqlCreateFunction extends SqlCreate {

    private SqlIdentifier functionName;

    private String className;

    private SqlNodeList properties;

    private String methodName;

    private String comment;

    private static final SqlSpecialOperator OPERATOR =
            new SqlSpecialOperator("CREATE FUNCTION", SqlKind.CREATE_FUNCTION);


    public SqlCreateFunction(SqlParserPos pos, boolean replace, SqlIdentifier name,
            String className, String methodName,
            String comment, SqlNodeList properties) {

        super(OPERATOR, pos,replace,false);
        this.functionName = name;
        this.className = className;
        this.properties = properties;
        this.methodName = methodName;
        this.comment = comment;
    }

    public SqlNode getFunctionName() {
        return functionName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public SqlNodeList getProperties() {
        return properties;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public SqlOperator getOperator() {
        return null;
    }

    @Override
    public List<SqlNode> getOperandList() {
        return null;
    }

    @Override
    public SqlKind getKind() {
        return SqlKind.OTHER_DDL;
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        writer.keyword("CREATE");
        writer.keyword("FUNCTION");
        functionName.unparse(writer, leftPrec, rightPrec);
        writer.keyword("AS");
        writer.print("'" + className + "'");
        if (methodName != null) {
            writer.newlineAndIndent();
            writer.keyword("METHOD");
            writer.print("'" + methodName + "'");
        }
        if (properties != null) {
            writer.newlineAndIndent();
            writer.keyword("PROPERTY");
            SqlWriter.Frame propertyFrame = writer.startList("(", ")");
            for (SqlNode property : properties) {
                writer.sep(",", false);
                writer.newlineAndIndent();
                writer.print("  ");
                property.unparse(writer, leftPrec, rightPrec);
            }
            writer.newlineAndIndent();
            writer.endList(propertyFrame);
        }
        if (comment != null) {
            writer.newlineAndIndent();
            writer.keyword("COMMENT");
            writer.print("'" + comment + "'");
        }
    }
}
