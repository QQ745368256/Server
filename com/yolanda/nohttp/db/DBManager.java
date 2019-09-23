/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yolanda.nohttp.db;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.yolanda.nohttp.Logger;

import java.util.List;

/**
 * <p>Database management generic class, has realized the basic functions, inheritance of the subclass only need to implement {@link #replace(DBId)}, {@link #get(String)} and
 * 数据库管理泛型类,实现了基本功能,只需要实现继承的子类
 * {@link #getTableName()}.</p>
 * Created in Jan 10, 2016 8:18:28 PM.
 *
 * @author Yan Zhenjie.
 */
public abstract class DBManager<T extends DBId> {

    private static final boolean DEBUG = false;
    /**
     * A helper class to manage database creation and version management.
     * 一个辅助类来管理数据库创建和版本管理。
     */
    private SQLiteOpenHelper disk;

    public DBManager(SQLiteOpenHelper disk) {
        this.disk = disk;
    }

    /**
     * Open the database when the read data.
     *打开数据库时,读取数据。
     * @return {@link SQLiteDatabase}.
     */
    protected final SQLiteDatabase getReader() {
        return disk.getReadableDatabase();
    }

    /**
     * Open the database when the write data.
     *打开数据库时,写入数据。
     * @return {@link SQLiteDatabase}.
     */
    protected final SQLiteDatabase getWriter() {
    	try{
    		return disk.getWritableDatabase();
    	}catch(SQLiteCantOpenDatabaseException e){
    		Logger.e(e.toString());
    	}
    	return null;
    }

    /**
     * Close the database when reading data.
     *关闭数据库时,读取数据。
     * @param execute {@link SQLiteDatabase}.
     * @param cursor  {@link Cursor}.
     */
    protected final void closeReader(SQLiteDatabase execute, Cursor cursor) {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        closeWriter(execute);
    }

    /**
     * Close the database when writing data.
     *关闭数据库时,写入数据。
     * @param execute {@link SQLiteDatabase}.
     */
    protected final void closeWriter(SQLiteDatabase execute) {
        if (execute != null && execute.isOpen()) {
            execute.close();
        }
    }

    /**
     * The query id number.
     *查询id number。
     * @return int format.
     */
    public final int count() {
        return countColumn(Field.ID);
    }

    /**
     * According to the "column" query "column" number.
     *根据“列”查询“列”数字。
     * @param columnName ColumnName.
     * @return column count.
     */
    public final int countColumn(String columnName) {
        StringBuilder sqlBuild = new StringBuilder("SELECT COUNT(").append(columnName).append(") FROM ").append(getTableName());
        return count(sqlBuild.toString());
    }

    /**
     * According to the "column" query number.
     *根据“列”查询号码。
     * @param sql sql.
     * @return count
     */
    public final int count(String sql) {
        SQLiteDatabase execute = getReader();
        print(sql);
        Cursor cursor = execute.rawQuery(sql, null);
        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        closeReader(execute, cursor);
        return count;
    }

    /**
     * Delete all data.
     *删除所有数据。
     * @return a boolean value, whether deleted successfully.
     */
    public final boolean deleteAll() {
        return delete("1=1");
    }

    /**
     * Must have the id.
     *
     * @param ts delete the queue list.
     * @return a boolean value, whether deleted successfully.
     */
    public final boolean delete(List<T> ts) {
        StringBuilder where = new StringBuilder(Field.ID).append(" IN(");
        for (T t : ts) {
            long id = t.getId();
            if (id > 0) {
                where.append(',');
                where.append(id);
            }
        }
        where.append(')');
        if (',' == where.charAt(6))
            where.deleteCharAt(6);
        return delete(where.toString());
    }

    /**
     * According to the where to delete data.
     *根据where删除数据。
     * @param where performs conditional.执行条件的地方。
     * @return a boolean value, whether deleted successfully.
     */
    public final boolean delete(String where) {
        if (TextUtils.isEmpty(where))
            return true;
        SQLiteDatabase execute = getWriter();
        if(null != execute){
		    StringBuilder sqlBuild = new StringBuilder("DELETE FROM ").append(getTableName()).append(" WHERE ").append(where);
		    boolean result = true;
		    try {
		        String sql = sqlBuild.toString();
		        print(sql);
		        execute.execSQL(sql);
		    } catch (SQLException e) {
		        Logger.e(e);
		        result = false;
		    }
		    closeWriter(execute);
		    return result;
        }
        return false;
    }

    /**
     * Query all data.
     *查询所有的数据。
     * @return list data.
     */
    public final List<T> getAll() {
        return getAll(Field.ALL);
    }

    /**
     * All the data query a column.
     *所有的数据查询一个列。
     * @param columnName columnName.
     * @return list data.
     */
    public final List<T> getAll(String columnName) {
        return get(columnName, null, null, null, null);
    }

    /**
     * All the data query a column.
     *所有的数据查询一个列。
     * @param columnName such as: {@code "*"}.
     * @param where      such as: {@code age > 20}.
     * @param orderBy    such as: {@code "age"}.
     * @param limit      such as. {@code '20'}.
     * @param offset     offset.
     * @return list data.
     */
    public final List<T> get(String columnName, String where, String orderBy, String limit, String offset) {
        return get(getSelectSql(columnName, where, orderBy, limit, offset));
    }

    /**
     * Create query sql
     *创建查询sql
     * @param columnName columnName.
     * @param where      where.
     * @param orderBy    orderBy.
     * @param limit      limit.
     * @param offset     offset.
     * @return {@link String}.
     */
    private String getSelectSql(String columnName, String where, String orderBy, String limit, String offset) {
        StringBuilder sqlBuild = new StringBuilder("SELECT ").append(columnName).append(" FROM ").append(getTableName());
        if (!TextUtils.isEmpty(where)) {
            sqlBuild.append(" WHERE ");
            sqlBuild.append(where);
        }
        if (!TextUtils.isEmpty(orderBy)) {
            sqlBuild.append(" ORDER BY ");
            sqlBuild.append(orderBy);
        }
        if (!TextUtils.isEmpty(limit)) {
            sqlBuild.append(" LIMIT ");
            sqlBuild.append(limit);
        }
        if (!TextUtils.isEmpty(limit) && !TextUtils.isEmpty(offset)) {
            sqlBuild.append(" OFFSET ");
            sqlBuild.append(offset);
        }
        String sql = sqlBuild.toString();
        print(sql);
        return sqlBuild.toString();
    }

    /**
     * According to the SQL query data list.
     *根据SQL查询的数据列表。
     * @param querySql sql.
     * @return list data.
     */
    public abstract List<T> get(String querySql);

    /**
     * According to the unique index adds or updates a row data.
     *根据惟一索引添加或更新一行数据。
     * @param t {@link T}.
     * @return long.
     */
    public abstract long replace(T t);

    /**
     * Table name should be.
     *表名。
     * @return table name.
     */
    protected abstract String getTableName();

    /**
     * Print the test data.
     *打印测试数据。
     * @param print string.
     */
    protected void print(String print) {
        if (DEBUG)
            Log.d("NoHttp", print);
    }

}
