package org.apache.calcite;

import com.siemens.calcite.sql.parser.impl.Functions;
import com.siemens.calcite.sql.parser.impl.SquareFunction;
import com.siemens.calcite.sql.parser.impl.TypeUtils;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.OracleSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.ChainedSqlOperatorTable;
import org.apache.calcite.sql.util.ListSqlOperatorTable;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.junit.Test;

import java.util.Properties;

import static org.apache.calcite.sql.SqlExplainLevel.ALL_ATTRIBUTES;

/**
 * tests for user defined functions
 *
 * @author zjjfly[https://github.com/zjjfly] on 2020/8/20
 */
public class UdfTest {

    @Test
    public void sqlOptTableUdf1() {
        CalciteSchema rootSchema = CalciteSchema
                .createRootSchema(false, false);

        //添加表Test
        rootSchema.add("test", new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = new RelDataTypeFactory
                        .Builder(TypeUtils.getFactory());
                //列id, 类型int
                builder.add("id", TypeUtils.getRelType(SqlTypeName.INTEGER));
                //列name, 类型为varchar
                builder.add("name", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                builder.add("time_str", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                return builder.build();
            }
        });

        SqlParser.ConfigBuilder builder = SqlParser.configBuilder();
        //使用calcite的默认的sql parser
        builder.setParserFactory(SqlParserImpl::new);
        //以下需要设置成大写并且忽略大小写
        builder.setQuotedCasing(Casing.TO_UPPER);
        builder.setUnquotedCasing(Casing.TO_UPPER);
        builder.setCaseSensitive(false);

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                                                 .defaultSchema(rootSchema.plus())
                                                 .parserConfig(builder.build())
                                                 //注意用你自已的SqlOperatorTable实现
                                                 .operatorTable(MySqlOperatorTable.newInstance())
                                                 .build();
        Planner planner = Frameworks.getPlanner(config);
        //now start to parser
        try {
            SqlNode originSqlNode = planner.parse("select name, timestr2long(time_str) from test where id < 5");
            SqlNode sqlNode = planner.validate(originSqlNode);
            RelRoot root = planner.rel(sqlNode);
            System.out.println(RelOptUtil.toString(root.rel, ALL_ATTRIBUTES));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sqlOptTableUdf2() {
        CalciteSchema rootSchema = CalciteSchema
                .createRootSchema(false, false);

        ListSqlOperatorTable listSqlOperatorTable = new ListSqlOperatorTable();
        listSqlOperatorTable.add(Functions.FUNC1);
        listSqlOperatorTable.add(Functions.FUNC2);

        //添加表Test
        rootSchema.add("test", new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = new RelDataTypeFactory
                        .Builder(TypeUtils.getFactory());
                //列id, 类型int
                builder.add("id", TypeUtils.getRelType(SqlTypeName.INTEGER));
                //列name, 类型为varchar
                builder.add("name", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                builder.add("time_str", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                return builder.build();
            }
        });

        SqlParser.ConfigBuilder builder = SqlParser.configBuilder();
        //使用calcite的默认的sql parser
        builder.setParserFactory(SqlParserImpl::new);
        //以下需要设置成大写并且忽略大小写
        builder.setQuotedCasing(Casing.TO_UPPER);
        builder.setUnquotedCasing(Casing.TO_UPPER);
        builder.setCaseSensitive(false);

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                                                 .defaultSchema(rootSchema.plus())
                                                 .parserConfig(builder.build())
                                                 //使用ChainedSqlOperatorTable把默认的SqlStdOperatorTable和上面的listSqlOperatorTable串起来
                                                 .operatorTable(ChainedSqlOperatorTable.of(
                                                         listSqlOperatorTable,
                                                         SqlStdOperatorTable
                                                                 .instance()))
                                                 .build();
        Planner planner = Frameworks.getPlanner(config);
        //now start to parser
        try {
            SqlNode originSqlNode = planner.parse("select func2(id) ,name, func1(time_str) from test where id < 5");
            SqlNode sqlNode = planner.validate(originSqlNode);
            RelRoot root = planner.rel(sqlNode);
            System.out.println(RelOptUtil.toString(root.rel, ALL_ATTRIBUTES));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sqlOptTableUdf3() {
        CalciteSchema rootSchema = CalciteSchema
                .createRootSchema(false, false);
        SqlStdOperatorTable operatorTable = SqlStdOperatorTable
                .instance();
        operatorTable.register(Functions.FUNC1);
        operatorTable.register(Functions.FUNC2);

        //添加表Test
        rootSchema.add("test", new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = new RelDataTypeFactory
                        .Builder(TypeUtils.getFactory());
                //列id, 类型int
                builder.add("id", TypeUtils.getRelType(SqlTypeName.INTEGER));
                //列name, 类型为varchar
                builder.add("name", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                builder.add("time_str", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                return builder.build();
            }
        });

        SqlParser.ConfigBuilder builder = SqlParser.configBuilder();
        //使用calcite的默认的sql parser
        builder.setParserFactory(SqlParserImpl::new);
        //以下需要设置成大写并且忽略大小写
        builder.setQuotedCasing(Casing.TO_UPPER);
        builder.setUnquotedCasing(Casing.TO_UPPER);
        builder.setCaseSensitive(false);

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                                                 .defaultSchema(rootSchema.plus())
                                                 .parserConfig(builder.build())
                                                 .operatorTable(operatorTable)
                                                 .build();
        Planner planner = Frameworks.getPlanner(config);
        //now start to parser
        try {
            SqlNode originSqlNode = planner.parse("select func2(id) ,name, func1(time_str) from test where id < 5");
            SqlNode sqlNode = planner.validate(originSqlNode);
            RelRoot root = planner.rel(sqlNode);
            System.out.println(RelOptUtil.toString(root.rel, ALL_ATTRIBUTES));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void schemaUdf() {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(false, false);
        SchemaPlus schema = rootSchema.plus();
        schema.add("SQUARE_FUNC", ScalarFunctionImpl.create(SquareFunction.class, "eval"));

        SqlParser.ConfigBuilder builder = SqlParser.configBuilder();
        //使用calcite的默认的sql parser
        builder.setParserFactory(SqlParserImpl::new);
        //以下需要设置成大写并且忽略大小写
        builder.setQuotedCasing(Casing.TO_UPPER);
        builder.setUnquotedCasing(Casing.TO_UPPER);
        builder.setCaseSensitive(false);

        SqlParser.Config parserConfig = builder.build();

        Properties properties = new Properties();
        properties.setProperty(
                CalciteConnectionProperty.CASE_SENSITIVE.camelName(),
                String.valueOf(parserConfig.caseSensitive()));
        CalciteConnectionConfigImpl calciteConnectionConfig = new CalciteConnectionConfigImpl(properties);
        CalciteCatalogReader reader = new CalciteCatalogReader(
                rootSchema,
                rootSchema.path(null),
                new JavaTypeFactoryImpl(),
                calciteConnectionConfig
        );
        //添加表Test
        schema.add("test", new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = new RelDataTypeFactory
                        .Builder(TypeUtils.getFactory());
                //列id, 类型int
                builder.add("id", TypeUtils.getRelType(SqlTypeName.INTEGER));
                //列name, 类型为varchar
                builder.add("name", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                builder.add("time_str", TypeUtils.getRelType(SqlTypeName.VARCHAR));
                return builder.build();
            }
        });

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                                                 .defaultSchema(schema)
                                                 .parserConfig(parserConfig)
                                                 .operatorTable(ChainedSqlOperatorTable.of(
                                                         reader,
                                                         SqlStdOperatorTable
                                                                 .instance()))
                                                 .build();
        Planner planner = Frameworks.getPlanner(config);
        //now start to parser
        try {
            SqlNode originSqlNode = planner.parse("select SQUARE_FUNC(2) ,name, time_str from test where id > 5");
            SqlNode sqlNode = planner.validate(originSqlNode);
            RelRoot root = planner.rel(sqlNode);
            System.out.println(RelOptUtil.toString(root.rel, ALL_ATTRIBUTES));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void parseSql() throws SqlParseException {
        SqlParser.Config mysqlConfig = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        String exp = "select * from test limit 3 offset 0";
        SqlParser expressionParser = SqlParser.create(exp, mysqlConfig);
        // 解析sql
        SqlNode sqlNode = expressionParser.parseQuery();
        // 还原某个方言的SQL
        System.out.println(sqlNode.toSqlString(OracleSqlDialect.DEFAULT));
    }

    @Test
    public void createFunc() throws SqlParseException {
        SqlParser.Config mysqlConfig = SqlParser.configBuilder()
                                                // 使用自定义的parser的工厂
                                                .setParserFactory(MySqlParserImpl.FACTORY)
                                                .setLex(Lex.MYSQL)
                                                .build();
        SqlParser parser = SqlParser.create("", mysqlConfig);
        String sql = "create function " +
                "hr.custom_function as 'com.siemens.calcite.func.CustomFunction' " +
                "method 'eval'  " +
                "property ('a'='b','c'='1') comment 'my custom function'";
        SqlNode sqlNode = parser.parseQuery(sql);
        System.out.println(sqlNode.toSqlString(OracleSqlDialect.DEFAULT));
    }

}
