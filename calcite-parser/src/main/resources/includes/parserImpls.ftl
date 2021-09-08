<#--
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to you under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
-->

SqlCreate SqlCreateFunction(Span s, boolean replace) :
{
SqlParserPos propertyPos;
SqlIdentifier functionName = null;
String className = null;
String methodName = null;
String comment = null;
SqlNodeList properties = null;
}
{
    <FUNCTION>
    functionName = CompoundIdentifier()
    <AS>
    { className = StringLiteralValue(); }
    [
        <METHOD>
        {
            methodName = StringLiteralValue();
        }
    ]
    [
        <PROPERTY>
         {
            propertyPos = getPos();
            SqlNode property;
            properties = new SqlNodeList(propertyPos);
         }
         <LPAREN>
         [
            property = PropertyValue()
            {
                properties.add(property);
            }
         (
            <COMMA>
            {
                property = PropertyValue();
                properties.add(property);
            }
         )*
         ]
         <RPAREN>
    ]
    [
        <COMMENT> {
            comment = StringLiteralValue();
        }
    ]
    {
        return new SqlCreateFunction(s.end(this), replace, functionName, className, methodName, comment, properties);
    }
}

JAVACODE String StringLiteralValue() {
    SqlNode sqlNode = StringLiteral();
    return ((NlsString) SqlLiteral.value(sqlNode)).getValue();
}

SqlNode PropertyValue() :
{
    SqlNode key;
    SqlNode value;
    SqlParserPos pos;
}
{
    key = StringLiteral()
    { pos = getPos(); }
    <EQ> value = StringLiteral()
    {
        return new SqlProperty(getPos(), key, value);
    }
}
